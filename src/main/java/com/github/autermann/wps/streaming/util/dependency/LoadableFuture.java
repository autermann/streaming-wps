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
package com.github.autermann.wps.streaming.util.dependency;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public abstract class LoadableFuture<T> implements ListenableFuture<T> {
    private static final Logger log = LoggerFactory.getLogger(LoadableFuture.class);
    private final ExecutionList listeners = new ExecutionList();
    private final Sync sync = new Sync();

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!sync.cancel()) {
            return false;
        }
        informListeners();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return this.sync.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.sync.isDone();
    }

    @Override
    public T get()
            throws InterruptedException, ExecutionException {
        this.sync.await();
        return load();
    }

    @Override
    public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        this.sync.await(unit.toNanos(timeout));
        return load();
    }

    protected boolean setAvailable() {
        boolean done = this.sync.setDone();
        if (done) {
            informListeners();
        }
        return done;
    }

    protected boolean setFailure(Throwable t) {
        boolean done = this.sync.setException(t);
        if (done) {
            informListeners();
        }
        return done;
    }

    private void informListeners() {
        log.debug("Informing Listeners");
        this.listeners.execute();
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        this.listeners.add(listener, executor);
    }

    protected abstract T load();

    private static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 2593493634180807289L;
        private static final int RUNNING = 0;
        private static final int COMPLETING = 1;
        private static final int COMPLETED = 2;
        private static final int CANCELLED = 4;
        private Throwable exception;

        @Override
        protected int tryAcquireShared(int ignored) {
            if (isDone()) {
                return 1;
            }
            return -1;
        }

        @Override
        protected boolean tryReleaseShared(int finalState) {
            setState(finalState);
            return true;
        }

        void await(long nanos)
                throws TimeoutException, CancellationException,
                       ExecutionException, InterruptedException {
            if (!tryAcquireSharedNanos(-1, nanos)) {
                throw new TimeoutException("Timeout waiting for task.");
            }
        }

        void await()
                throws CancellationException, ExecutionException,
                       InterruptedException {
            acquireSharedInterruptibly(-1);
            check();
        }

        void check()
                throws CancellationException, ExecutionException {
            int state = getState();
            switch (state) {
                case COMPLETED:
                    if (exception != null) {
                        throw new ExecutionException(exception);
                    }
                    break;
                case CANCELLED:
                    throw new CancellationException("Task was cancelled.");
                default:
                    throw new IllegalStateException("Error, synchronizer in invalid state: " +
                                                    state);
            }
        }

        boolean isDone() {
            return (getState() & (COMPLETED | CANCELLED)) != 0;
        }

        boolean isCancelled() {
            return getState() == CANCELLED;
        }

        boolean setDone() {
            return complete(null, COMPLETED);
        }

        boolean setException(Throwable t) {
            return complete(t, COMPLETED);
        }

        boolean cancel() {
            return complete(null, CANCELLED);
        }

        private boolean complete(Throwable t, int finalState) {
            boolean doCompletion = compareAndSetState(RUNNING, COMPLETING);
            if (doCompletion) {
                this.exception = t;
                releaseShared(finalState);
            } else if (getState() == COMPLETING) {
                acquireShared(-1);
            }
            return doCompletion;
        }
    }

}
