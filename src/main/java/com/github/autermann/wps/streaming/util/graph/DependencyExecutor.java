/*
 * Copyright (C) 2014 Christian Autermann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.github.autermann.wps.streaming.util.graph;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

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
            = new DirectedAcyclicGraph<K, DefaultEdge>(DefaultEdge.class);
    private final Map<K, Job> jobs = Maps.newHashMap();
    private final ExecutorService executorService;
    private final JobExecutor<I, O> executor;
    private int waiting = 0;
    private int running = 0;
    private int size = 0;
    private int success = 0;
    private int failure = 0;
    private int needsInput = 0;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();
    private final Condition empty = lock.newCondition();
    private volatile boolean shuttingDown = false;
    private volatile boolean complete = true;

    public DependencyExecutor(JobExecutor<I, O> jobExecutor,
                              ExecutorService executorService) {
        this.executorService = executorService;
        this.executor = jobExecutor;
    }

    public void shutdown(boolean complete)
            throws InterruptedException, MissingInputException {
        this.lock.lock();
        try {
            if (this.shuttingDown) {
                throw new IllegalStateException();
            }
            this.shuttingDown = true;
            this.complete = complete;

            if (this.complete) {
                if (this.needsInput != 0) {
                    throw new MissingInputException();
                }
                while (!isEmpty()) {
                    empty.await();
                }
            } else {
                while (!isDone()) {
                    done.await();
                }
            }
            this.executorService.shutdown();
        } finally {
            this.lock.unlock();
        }
    }

    private boolean isEmpty() {
        return this.success + this.failure == this.size;
    }

    private boolean isDone() {
        return this.success + this.failure + this.running == this.size;
    }

    public int getSuccess() {
        this.lock.lock();
        try {
            return this.success;
        } finally {
            this.lock.unlock();
        }
    }

    public int getFailure() {
        this.lock.lock();
        try {
            return this.failure;
        } finally {
            this.lock.unlock();
        }
    }

    public int getSize() {
        this.lock.lock();
        try {
            return this.size;
        } finally {
            this.lock.unlock();
        }
    }

    public int getWaiting() {
        this.lock.lock();
        try {
            return this.waiting;
        } finally {
            this.lock.unlock();
        }
    }

    public int getExecuting() {
        this.lock.lock();
        try {
            return this.waiting;
        } finally {
            this.lock.unlock();
        }
    }

    public void addJob(K key, K... dependencies)
            throws CyclicDependencyException {
        addJob(key, Arrays.asList(dependencies));
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
                job = new Job(key);
                this.graph.addVertex(key);
                addedNodes.add(job);
                this.jobs.put(key, job);
            }
            try {
                for (K dependency : dependencies) {
                    checkNotNull(dependency);
                    Job dependencyJob = this.jobs.get(dependency);
                    if (dependencyJob == null) {
                        dependencyJob = new Job(dependency);
                        this.graph.addVertex(dependency);
                        addedNodes.add(job);
                    }
                    deps.add(dependencyJob);
                    this.jobs.put(dependencyJob.getKey(), dependencyJob);
                    addedEdges.add(this.graph.addDagEdge(key, dependency));
                }
            } catch (CycleFoundException ex) {
                rollback(addedEdges, addedNodes);
                throw new CyclicDependencyException(ex);
            } catch (NullPointerException ex) {
                rollback(addedEdges, addedNodes);
                throw ex;
            }
            this.size += addedNodes.size();
            this.needsInput += addedNodes.size();
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

    public void setInput(K key, I input) throws NoSuchElementException {
        this.lock.lock();
        try {
            Job job = this.jobs.get(key);
            if (job != null) {
                job.setInput(input);
                --this.needsInput;
            } else {
                throw new NoSuchElementException("No such job: " + key);
            }
        } finally {
            this.lock.unlock();
        }
    }

    private enum State {
        WAITING,
        RUNNING,
        SUCCESS,
        FAILURE;
    }

    private void changeState(State from, State to) {
        this.lock.lock();
        try {
            if (from != null) {
                switch (from) {
                    case SUCCESS:
                        --this.success;
                        break;
                    case FAILURE:
                        --this.failure;
                        break;
                    case RUNNING:
                        --this.running;
                        break;
                    case WAITING:
                        --this.waiting;
                        break;
                }
            }
            switch (to) {
                case SUCCESS:
                    ++this.success;
                    break;
                case FAILURE:
                    ++this.failure;
                    break;
                case RUNNING:
                    ++this.running;
                    break;
                case WAITING:
                    ++this.waiting;
                    break;
            }
            if (isDone()) {
                this.done.signalAll();
            }
            if (isEmpty()) {
                this.empty.signalAll();
            }
        } finally {
            this.lock.unlock();
        }
    }

    private class Job {
        private final K key;
        private final SettableFuture<I> input = SettableFuture.create();
        private final SettableFuture<O> output = SettableFuture.create();
        private ListenableFuture<List<O>> dependency;
        private final Lock lock = new ReentrantLock();
        private State state;

        Job(K key) {
            this.key = checkNotNull(key);
            changeState(State.WAITING);
        }

        public K getKey() {
            return this.key;
        }

        private void changeState(State state) {
            lock.lock();
            try {
                log.debug("[{}] Switching state: {} -> {}",
                          this.key, this.state, state);
                DependencyExecutor.this.changeState(this.state, state);
                this.state = state;
            } finally {
                lock.unlock();
            }
        }

        private void checkState(State state) {
            this.lock.lock();
            try {
                if (this.state != state) {
                    throw new IllegalStateException();
                }
            } finally {
                this.lock.unlock();
            }
        }

        public State getState() {
            this.lock.lock();
            try {
                return this.state;
            } finally {
                this.lock.unlock();
            }
        }

        public ListenableFuture<I> getInput() {
            return this.input;
        }

        public ListenableFuture<O> getOutput() {
            return this.output;
        }

        public boolean hasInput() {
            return this.input.isDone();
        }

        public boolean hasOutput() {
            return this.output.isDone();
        }

        public void setInput(I input) {
            this.lock.lock();
            try {
                log.debug("[{}] Received input: {}", this.key, input);
                checkState(State.WAITING);
                this.input.set(checkNotNull(input));
                checkForExecution();
            } finally {
                this.lock.unlock();
            }
        }

        private void setOutput(O output) {
            this.lock.lock();
            try {
                log.debug("[{}] Received output: {}", this.key, output);
                this.output.set(checkNotNull(output));
                changeState(State.SUCCESS);
            } finally {
                this.lock.unlock();
            }
        }

        private void setOutputFailure(Throwable t) {
            this.lock.lock();
            try {
                log.debug("[" + this.key + "] Output failed", t);
                this.output.setException(t);
                changeState(State.FAILURE);
            } finally {
                this.lock.unlock();
            }
        }

        public void setDependencies(Iterable<Job> dependencies) {
            this.lock.lock();
            try {
                checkState(State.WAITING);
                List<ListenableFuture<O>> outputs = Lists.newLinkedList();
                for (Job job : dependencies) {
                    log.debug("[{}] Dependency added: {}",
                              this.key, job.getKey());
                    outputs.add(job.getOutput());
                }
                this.dependency = Futures.allAsList(outputs);
                Futures
                        .addCallback(this.dependency, new FutureCallback<List<O>>() {

                            @Override
                            public void onSuccess(List<O> result) {
                                log.debug("[{}] Received dependencies: {}",
                                          key, result);
                                checkForExecution();
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                log.debug("[" + key + "] Failed dependencies",
                                          throwable);
                                setOutputFailure(throwable);
                            }
                        });
            } finally {
                this.lock.unlock();
            }
        }

        private void checkForExecution() {
            lock.lock();
            try {
                if (this.input.isDone() &&
                    (this.dependency == null || this.dependency.isDone()) &&
                    this.state == State.WAITING) {
                    changeState(State.RUNNING);
                    log.debug("[{}] Scheduling for execution", key);
                    final I in;
                    final List<O> out;
                    try {
                        in = this.input.get();
                        if (this.dependency != null) {
                            out = this.dependency.get();
                        } else {
                            out = Collections.emptyList();
                        }
                    } catch (InterruptedException ex) {
                        setOutputFailure(ex);
                        return;
                    } catch (ExecutionException ex) {
                        setOutputFailure(ex);
                        return;
                    }

                    if (in == null || out == null) {
                        log.debug("[{}] No input/output", key);
                        setOutputFailure(new NullPointerException());
                    } else {
                        if (shuttingDown && !complete) {
                            log
                                    .debug("[{}] Shutting done and should not complete", key);
                            setOutputFailure(new IllegalStateException());
                        } else {
                            executorService.submit(new Execution(in, out));
                        }
                    }

                }
            } finally {
                this.lock.unlock();
            }
        }

        private class Execution implements Runnable {
            private final I in;
            private final List<O> out;

            Execution(I in, List<O> out) {
                this.in = in;
                this.out = out;
            }

            @Override
            public void run() {
                try {
                    setOutput(executor.execute(this.in, this.out));
                } catch (Exception ex) {
                    setOutputFailure(ex);
                }
            }
        }
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            return Objects.toStringHelper(this)
                    .add("size", this.size)
                    .add("success", this.success)
                    .add("failure", this.failure)
                    .add("waiting", this.waiting)
                    .add("running", this.running)
                    .toString();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws CyclicDependencyException,
                                                  InterruptedException,
                                                  MissingInputException {
        final DependencyExecutor<String, Input, Output> exec
                = new DependencyExecutor<String, Input, Output>(new Exec(), Executors
                        .newFixedThreadPool(5));
        Thread t
                = new Thread() {

                    @Override
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            log.debug("{}", exec);
                        }
                    }

                };
        t.setDaemon(true);
        t.start();
        exec.addJob("1", "3", "4");
        exec.addJob("2", "4", "5");
        exec.addJob("3", "5", "6");
        exec.addJob("4", "6", "7");
        exec.addJob("5", "7", "8");
        exec.addJob("6", "8", "9");
        exec.addJob("7", "9", "10");
        exec.addJob("8", "10", "11");
        exec.addJob("9", "11", "12");
        exec.addJob("11");
        exec.addJob("12");
        for (int i = 1; i <= 12; ++i) {
            exec.setInput(String.valueOf(i), new Input(i));
        }
        exec.shutdown(true);
        log.info("{}", exec);
    }

    private static class Input {
        private final int value;

        public Input(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + this.value;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Input other = (Input) obj;
            if (this.value != other.value) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Input{" + "value=" + value + '}';
        }
    }

    private static class Output {
        private final int value;

        public Output(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.value;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Output other = (Output) obj;
            if (this.value != other.value) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Output{" + "value=" + value + '}';
        }
    }

    private static class Exec implements JobExecutor<Input, Output> {

        @Override
        public Output execute(Input value, Iterable<Output> dependencies) {
            try {
                Thread.sleep(500);
                int val = value.getValue();
                for (Output out : dependencies) {
                    val += out.getValue();
                }
                return new Output(val);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
