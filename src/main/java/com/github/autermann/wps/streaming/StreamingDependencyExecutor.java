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
package com.github.autermann.wps.streaming;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.wps.streaming.data.StreamingError;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.util.dependency.DependencyExecutor;

public class StreamingDependencyExecutor extends DependencyExecutor<MessageID, InputMessage, OutputMessage>
        implements Closeable {
    private static final Logger log = LoggerFactory
            .getLogger(StreamingDependencyExecutor.class);

    public StreamingDependencyExecutor(StreamingExecutor jobExecutor,
                                       ExecutorService executorService,
                                       MessageRepository repository) {
        super(jobExecutor, executorService, repository);
    }

    @Override
    public StreamingExecutor getExecutor() {
        return (StreamingExecutor) super.getExecutor();
    }

    public void finish() throws StreamingError {
        getExecutor().finish();
    }

    @Override
    public void close() throws IOException {
        getExecutor().close();
    }

}
