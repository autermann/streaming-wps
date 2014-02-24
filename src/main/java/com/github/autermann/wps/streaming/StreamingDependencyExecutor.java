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
package com.github.autermann.wps.streaming;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.util.dependency.DependencyExecutor;

public class StreamingDependencyExecutor extends DependencyExecutor<MessageID, InputMessage, OutputMessage>
        implements Closeable {

    public StreamingDependencyExecutor(StreamingExecutor jobExecutor,
                                       ExecutorService executorService,
                                       MessageRepository repository) {
        super(jobExecutor, executorService, repository);
    }

    @Override
    public void close() throws IOException {
        ((StreamingExecutor) getExecutor()).close();
    }

}
