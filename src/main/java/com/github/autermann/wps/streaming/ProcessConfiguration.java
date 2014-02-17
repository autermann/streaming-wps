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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.github.autermann.wps.streaming.util.dependency.InMemoryRepository;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class ProcessConfiguration {
    protected static final int THREADS = 10;
    private final StreamingProcessID processId = StreamingProcessID.create();

    public StreamingProcessID getProcessID() {
        return processId;
    }

    public abstract CallbackJobExecutor createExecutor(MessageReceiver callback);

    public ExecutorService createThreadPool() {
        String nameFormat = "streaming-process-" + getProcessID() + "-%d";
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(nameFormat).build();
        return Executors.newFixedThreadPool(THREADS, threadFactory);
    }

    public MessageRepository createMessageRepository() {
        return new DefaultMessageRepository();
    }

    private static class DefaultMessageRepository
            extends InMemoryRepository<MessageID, InputMessage, OutputMessage>
            implements MessageRepository {
    }
}
