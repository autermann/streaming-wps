/*
 * Copyright (C) 2014 Christian Autermann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.autermann.wps.streaming.util.dependency;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 * @param <K> the key type
 * @param <I> the input type
 * @param <O> the output type
 */
public class DependencyExecutor<K, I, O> {
    private static final Logger log = LoggerFactory
            .getLogger(DependencyExecutor.class);
    private final DirectedAcyclicGraph<K, DefaultEdge> graph
            = new DirectedAcyclicGraph<>(DefaultEdge.class);
    private final Map<K, Job> jobs = Maps.newHashMap();
    private final ExecutorService executorService;
    private final JobExecutor<I, O> executor;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();
    private final Condition empty = lock.newCondition();
    private volatile boolean shuttingDown = false;
    private volatile boolean shouldComplete = true;
    private final Repository<K, I, O> repository;
    private final Stats stats = new Stats();

    public DependencyExecutor(JobExecutor<I, O> jobExecutor,
                              ExecutorService executorService,
                              Repository<K, I, O> repository) {
        this.executorService = Preconditions.checkNotNull(executorService);
        this.executor = Preconditions.checkNotNull(jobExecutor);
        this.repository = Preconditions.checkNotNull(repository);
    }

    public void shutdown(boolean shouldComplete)
            throws InterruptedException, MissingInputException {
        this.lock.lock();
        try {
            if (this.shuttingDown) {
                throw new IllegalStateException();
            }
            this.shuttingDown = true;
            this.shouldComplete = shouldComplete;

            if (this.shouldComplete) {
                if (this.stats.jobsWaitingForInput() != 0) {
                    throw new MissingInputException();
                }
                while (!this.stats.isEmpty()) {
                    empty.await();
                }
            } else {
                while (!this.stats.isDone()) {
                    done.await();
                }
            }
            this.executorService.shutdown();
        } finally {
            this.lock.unlock();
        }
    }

    public void addJob(K key) throws CyclicDependencyException {
        addJob(key, Collections.<K>emptyList());
    }

    public void addJob(K key, Iterable<K> dependencies)
            throws CyclicDependencyException {
        checkNotNull(key);
        checkNotNull(dependencies);
        this.lock.lock();
        try {
            checkState(!shuttingDown);
            List<DefaultEdge> addedEdges = Lists.newLinkedList();
            List<Job> addedNodes = Lists.newLinkedList();
            List<Job> deps = Lists.newLinkedList();
            Job job = this.jobs.get(key);
            if (job == null) {
                job = new Job(this.repository, key);
                log.debug("Adding job {}", job);
                this.graph.addVertex(key);
                addedNodes.add(job);
                this.jobs.put(key, job);
            } else {
                log.debug("Job {} was already present", job);
            }
            try {
                for (K dependency : dependencies) {
                    checkNotNull(dependency);
                    Job dependencyJob = this.jobs.get(dependency);
                    if (dependencyJob == null) {
                        log.debug("Adding Dependency Job {}", dependencyJob);
                        dependencyJob = new Job(this.repository, dependency);
                        this.graph.addVertex(dependency);
                        addedNodes.add(job);
                        this.jobs.put(dependency, dependencyJob);
                    } else {
                        log.debug("Dependency Job {} was already present", job);
                    }
                    deps.add(dependencyJob);
                    addedEdges.add(this.graph.addDagEdge(key, dependency));
                }
            } catch (CycleFoundException ex) {
                rollback(addedEdges, addedNodes);
                throw new CyclicDependencyException(ex);
            } catch (NullPointerException ex) {
                rollback(addedEdges, addedNodes);
                throw ex;
            }
            this.stats.onNewJobs(addedNodes.size());
            job.setDependencies(deps);
        } finally {
            this.lock.unlock();
        }
    }
    private void rollback(List<DefaultEdge> addedEdges,
                          List<Job> addedNodes) {
        this.graph.removeAllEdges(addedEdges);
        for (Job addedJob : addedNodes) {
            this.jobs.remove(addedJob.getKey());
            this.graph.removeVertex(addedJob.getKey());
        }
    }

    public void setInput(K key, I input)
            throws NoSuchElementException {
        this.lock.lock();
        try {
            Job job = this.jobs.get(key);
            if (job == null) {
                throw new NoSuchElementException("No such job: " + key);
            }
            job.setInput(input);
            this.stats.onInput();
        } finally {
            this.lock.unlock();
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public JobExecutor<I, O> getExecutor() {
        return executor;
    }

    private void changeState(State from, State to) {
        this.lock.lock();
        try {
            this.stats.onJobStateChange(from, to);
            if (this.stats.isDone()) { this.done.signalAll(); }
            if (this.stats.isEmpty()) { this.empty.signalAll(); }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(this.stats).toString();
    }

    private static class Stats {
        private int waiting = 0;
        private int running = 0;
        private int size = 0;
        private int success = 0;
        private int failure = 0;
        private int needsInput = 0;

        int waiting() {
            return waiting;
        }

        int running() {
            return running;
        }

        int size() {
            return size;
        }

        int succeeded() {
            return success;
        }

        int failed() {
            return failure;
        }

        int jobsWaitingForInput() {
            return needsInput;
        }

        boolean isEmpty() {
            return this.success + this.failure == this.size;
        }

        boolean isDone() {
            return this.success + this.failure + this.running == this.size;
        }

        void onJobStateChange(State from, State to) {
            if (from != null) {
                increase(from, -1);
            }
            increase(to, 1);
        }

         private void increase(State to, int amount) {
            switch (to) {
                case SUCCESS: this.success += amount; break;
                case FAILURE: this.failure += amount; break;
                case RUNNING: this.running += amount; break;
                case WAITING: this.waiting += amount; break;
            }
        }

        void onInput() {
            --this.needsInput;
        }

        void onNewJobs(int jobs) {
            this.size += jobs;
            this.needsInput += jobs;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("size", this.size)
                    .add("success", this.success)
                    .add("failure", this.failure)
                    .add("waiting", this.waiting)
                    .add("running", this.running)
                    .toString();
        }
    }

    private enum State {
        WAITING,
        RUNNING,
        SUCCESS,
        FAILURE;
    }

    private class Job {
        private final K key;
        private final RepositoryInput<K,I,O> input;
        private final RepositoryOutput<K,I,O> output;
        private ListenableFuture<List<O>> dependencies;
        private final Lock lock = new ReentrantLock();
        private State state;

        Job(Repository<K,I,O> repository, K key) {
            this.key = checkNotNull(key);
            this.output = new RepositoryOutput<>(repository, key);
            this.input = new RepositoryInput<>(repository, key);
            this.dependencies = Futures.immediateFuture(Collections.<O>emptyList());
            checkAndSetState(null, State.WAITING);
        }

        public K getKey() {
            return this.key;
        }

        public ListenableFuture<I> getInput() {
            return this.input;
        }

        public ListenableFuture<O> getOutput() {
            return this.output;
        }

        public ListenableFuture<List<O>> getDependencies() {
            return this.dependencies;
        }

        public boolean hasInput() {
            return this.input.isDone();
        }

        public boolean hasOutput() {
            return this.output.isDone();
        }

        public boolean hasDependencies() {
            return this.dependencies.isDone();
        }

        private void checkState(State state) {
            this.lock.lock();
            try {
                if (this.state != state) {
                    log.warn("[{}] State check failed: expected {} but was {}", this, state, this.state);
                    throw new IllegalStateException("Expected state " + state);
                }
            } finally {
                this.lock.unlock();
            }
        }
        private void checkAndSetState(State expected, State next) {
            this.lock.lock();
            try {
                checkState(expected);
                log.debug("[{}] Switching state: {} -> {}", this, state, next);
                DependencyExecutor.this.changeState(state, next);
                this.state = next;
            } finally {
                lock.unlock();
            }
        }

        public void setInput(I input) {
            this.lock.lock();
            try {
                log.debug("[{}] Received input: {}", this, input);
                checkState(State.WAITING);
                this.input.setAvailable(input);
                this.checkForExecution();
            } finally {
                this.lock.unlock();
            }
        }

        private void succeed(O output) {
            this.lock.lock();
            try {
                log.debug("[{}] Received output: {}", this, output);
                checkAndSetState(State.RUNNING, State.SUCCESS);
                this.output.setAvailable(output);
            } finally {
                this.lock.unlock();
            }
        }

        private void fail(Throwable t) {
            this.lock.lock();
            try {
                log.debug("[" + this + "] Output failed", t);
                checkAndSetState(State.RUNNING, State.FAILURE);
                this.output.setFailure(t);
            } finally {
                this.lock.unlock();
            }
        }

        public void setDependencies(Iterable<Job> dependencies) {
            this.lock.lock();
            try {
                checkState(State.WAITING);
                this.dependencies = asFuture(dependencies);
                log.debug("[{}] Adding dependency listener", this);
                this.dependencies.addListener(new Runnable() {
                    @Override public void run() {
                        log.debug("[{}] Dependency listener executed", Job.this);
                        checkForExecution();
                    }
                }, executorService);
            } finally {
                this.lock.unlock();
            }
        }

        private ListenableFuture<List<O>> asFuture(Iterable<Job> dependencies) {
            List<ListenableFuture<O>> outputs = Lists.newLinkedList();
            for (Job job : dependencies) {
                log.debug("[{}] Dependency added: {}", this, job);
                outputs.add(job.getOutput());
            }
            return Futures.allAsList(outputs);
        }

        private void checkForExecution() {
            this.lock.lock();
            try {
                if (this.state != State.WAITING){
                    return;
                }
                if (!hasInput()) {
                    log.debug("[{}] No input is available yet", this);
                    return;
                }
                if (!hasDependencies()) {
                    log.debug("[{}] No dependencies are available yet", this);
                    return;
                }
                checkAndSetState(State.WAITING, State.RUNNING);
                log.debug("[{}] Scheduling for execution", this);
                executorService.submit(new Execution(this));
            } finally {
                this.lock.unlock();
            }
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).addValue(this.key).toString();
        }

    }

    private class Execution implements Runnable {
        private final Job job;

        Execution(Job job) {
            this.job = job;
        }

        @Override
        public void run() {
            log.debug("[{}] Executing", job);
            try {
                if (shuttingDown && !shouldComplete) {
                    log.debug("[{}] Shutting done and should not complete", job);
                    job.fail(new IllegalStateException("Shutting done and should not complete"));
                    return;
                }
                final I in = job.getInput().get();
                final Iterable<O> deps = job.getDependencies().get();
                job.succeed(executor.execute(in, deps));
            } catch (Exception ex) {
                job.fail(ex);
            }
        }
    }

}
