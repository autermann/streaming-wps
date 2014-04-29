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
package com.github.autermann.wps.streaming.message.receiver;

import java.util.Set;

import com.github.autermann.wps.streaming.message.Message;
import com.google.common.collect.Sets;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DelegatingMessageReceiver implements MessageReceiver {
    private final Set<MessageReceiver> callbacks = Sets.newHashSet();

    public boolean contains(MessageReceiver output) {
        if (output != null) {
            synchronized (callbacks) {
                return callbacks.contains(output);
            }
        } else {
            return false;
        }
    }

    public void bind(MessageReceiver output) {
        if (output != null) {
            synchronized (callbacks) {
                this.callbacks.add(output);
            }
        }

    }

    public void unbind(MessageReceiver output) {
        if (output != null) {
            synchronized (callbacks) {
                this.callbacks.remove(output);
            }
        }
    }

    @Override
    public void receive(Message message) {
        synchronized (callbacks) {
            for (MessageReceiver callback : callbacks) {
                callback.receive(message);
            }
        }
    }
}
