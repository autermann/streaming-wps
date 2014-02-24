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
package com.github.autermann.wps.streaming.message;

import com.github.autermann.wps.streaming.StreamingProcessID;
import com.github.autermann.wps.streaming.message.receiver.MessageReceiver;
import com.github.autermann.wps.streaming.message.receiver.MessageReceivers;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class Message extends SoapMessage {
    private StreamingProcessID processId;
    private MessageReceiver receiver = MessageReceivers.nullReceiver();

    public StreamingProcessID getProcessID() {
        return this.processId;
    }

    public void setProcessID(StreamingProcessID id) {
        this.processId = Preconditions.checkNotNull(id);
    }

    public MessageReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(MessageReceiver receiver) {
        this.receiver = Preconditions.checkNotNull(receiver);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", getID())
                .add("process", getProcessID())
                .toString();

    }


}
