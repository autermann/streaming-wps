package com.github.autermann.wps.streaming.util.dependency;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;


import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public abstract class LoadableFuture<T> implements ListenableFuture<T> {

    private final ExecutionList listeners = new ExecutionList();
    private final Sync sync = new Sync();

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!this.sync.cancel(mayInterruptIfRunning)) {
            return false;
        }
        listeners.execute();
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

    public boolean setAvailable() {
        boolean done = this.sync.setDone();
        if (done) {
            this.listeners.execute();
        }
        return done;
    }

    public boolean setFailure(Throwable t) {
        boolean done = this.sync.setException(t);
        if (done) {
            this.listeners.execute();
        }
        return done;
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        this.listeners.add(listener, executor);
    }

    protected abstract T load();

    private class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 2593493634180807289L;
        private static final int RUNNING = 0;
        private static final int COMPLETING = 1;
        private static final int COMPLETED = 2;
        private static final int CANCELLED = 4;
        private static final int INTERRUPTED = 8;
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

        private void check()
                throws CancellationException, ExecutionException {
            int state = getState();
            switch (state) {
                case COMPLETED:
                    if (exception != null) {
                        throw new ExecutionException(exception);
                    } else {
                        return;
                    }
                case CANCELLED:
                case INTERRUPTED:
                    CancellationException ee
                            = new CancellationException("Task was cancelled.");
                    ee.initCause(ee);
                    throw ee;
                default:
                    throw new IllegalStateException("Error, synchronizer in invalid state: " +
                                                    state);
            }
        }

        boolean isDone() {
            return (getState() & (COMPLETED | CANCELLED | INTERRUPTED)) != 0;
        }

        boolean isCancelled() {
            return (getState() & (CANCELLED | INTERRUPTED)) != 0;
        }

        boolean wasInterrupted() {
            return getState() == INTERRUPTED;
        }

        boolean setDone() {
            return complete(null, COMPLETED);
        }

        boolean setException(Throwable t) {
            return complete(t, COMPLETED);
        }

        boolean cancel(boolean interrupt) {
            return complete(null, interrupt ? INTERRUPTED : CANCELLED);
        }

        private boolean complete(Throwable t, int finalState) {
            boolean doCompletion = compareAndSetState(RUNNING, COMPLETING);
            if (doCompletion) {
                if ((finalState & (CANCELLED | INTERRUPTED)) != 0) {
                    this.exception
                            = new CancellationException("Future.cancel() was called.");
                } else {
                    this.exception = t;
                }
                releaseShared(finalState);
            } else if (getState() == COMPLETING) {
                acquireShared(-1);
            }
            return doCompletion;
        }
    }

}