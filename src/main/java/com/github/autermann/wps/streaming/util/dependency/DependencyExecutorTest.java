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

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DependencyExecutorTest {
    private static final Logger log = LoggerFactory
            .getLogger(DependencyExecutorTest.class);

    public static void main(String[] args)
            throws Exception {
        final DependencyExecutor<String, Input, Output> exec
                = new DependencyExecutor<>(new Exec(), Executors
                        .newFixedThreadPool(5), new InMemoryRepository<String, Input, Output>());
        Thread t = new WatchThread(exec);
        t.setDaemon(true);
        t.start();
        exec.addJob("1", Lists.newArrayList("3", "4"));
        exec.addJob("2", Lists.newArrayList("4", "5"));
        exec.addJob("3", Lists.newArrayList("5", "6"));
        exec.addJob("4", Lists.newArrayList("6", "7"));
        exec.addJob("5", Lists.newArrayList("7", "8"));
        exec.addJob("6", Lists.newArrayList("8", "9"));
        exec.addJob("7", Lists.newArrayList("9", "10"));
        exec.addJob("8", Lists.newArrayList("10", "11"));
        exec.addJob("9", Lists.newArrayList("11", "12"));
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
            return this.value == other.value;
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
            return this.value == other.value;
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

    private static class WatchThread extends Thread {
        private final DependencyExecutor<String, Input, Output> exec;

        WatchThread(DependencyExecutor<String, Input, Output> exec) {
            this.exec = exec;
        }

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
    }
}
