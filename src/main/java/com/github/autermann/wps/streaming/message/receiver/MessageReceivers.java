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
package com.github.autermann.wps.streaming.message.receiver;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.wps.streaming.message.DescribeMessage;
import com.github.autermann.wps.streaming.message.DescriptionMessage;
import com.github.autermann.wps.streaming.message.ErrorMessage;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.Message;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.OutputRequestMessage;
import com.github.autermann.wps.streaming.message.StopMessage;
import com.google.common.base.Preconditions;

public final class MessageReceivers {

    private static final MessageReceiver NULL = new MessageReceiver() {
        @Override public void receive(Message message) { }
    };

    public static MessageReceiver nullReceiver() {
        return NULL;
    }

    public static MessageReceiver onlyIncomingMessages(MessageReceiver delegate) {
        return new IncomingMessageFilter(delegate);
    }

    public static MessageReceiver onlyOutgoingMessages(MessageReceiver delegate) {
        return new OutgoingMessageFilter(delegate);
    }

    public static MessageReceiver split(MessageReceiver in, MessageReceiver out) {
        return new SplittingMessageReceiver(in, out);
    }

    private static class IncomingMessageFilter extends IncomingMessageReceiver {

        private static final Logger log = LoggerFactory
                .getLogger(IncomingMessageFilter.class);
        private final MessageReceiver delegate;

        IncomingMessageFilter(MessageReceiver delegate) {
            this.delegate = delegate;
        }

        @Override
        protected void receiveStop(StopMessage message) {
            log.debug("Receiving stop message {}", message);
            this.delegate.receive(message);
        }

        @Override
        protected void receiveInput(InputMessage message) {
            log.debug("Receiving input message {}", message);
            this.delegate.receive(message);
        }

        @Override
        protected void receiveOutputRequest(OutputRequestMessage message) {
            log.debug("Receiving output request message {}", message);
            this.delegate.receive(message);
        }

        @Override
        protected void receiveDescribe(DescribeMessage message) {
            log.debug("Receiving describe message {}", message);
            this.delegate.receive(message);
        }

    }

    private static class OutgoingMessageFilter extends OutgoingMessageReceiver {

        private static final Logger log = LoggerFactory
                .getLogger(OutgoingMessageFilter.class);
        private final MessageReceiver delegate;

        OutgoingMessageFilter(MessageReceiver delegate) {
            this.delegate = checkNotNull(delegate);
        }

        @Override
        protected void receiveError(ErrorMessage message) {
            log.debug("Receiving error message {}", message);
            this.delegate.receive(message);
        }

        @Override
        protected void receiveOutput(OutputMessage message) {
            log.debug("Receiving output message {}", message);
            this.delegate.receive(message);
        }

        @Override
        protected void receiveDescription(DescriptionMessage message) {
            log.debug("Receiving description message {}", message);
            this.delegate.receive(message);
        }

    }

    private static class SplittingMessageReceiver extends AbstractMessageReceiver {
        private final MessageReceiver outgoing;
        private final MessageReceiver incoming;

        SplittingMessageReceiver(MessageReceiver incoming,
                                 MessageReceiver outgoing) {
            this.outgoing = Preconditions.checkNotNull(outgoing);
            this.incoming = Preconditions.checkNotNull(incoming);
        }

        @Override
        protected void receiveError(ErrorMessage message) {
            this.outgoing.receive(message);
        }

        @Override
        protected void receiveOutputRequest(OutputRequestMessage message) {
            this.incoming.receive(message);
        }

        @Override
        protected void receiveStop(StopMessage message) {
            this.incoming.receive(message);
        }

        @Override
        protected void receiveInput(InputMessage message) {
            this.incoming.receive(message);
        }

        @Override
        protected void receiveOutput(OutputMessage message) {
            this.outgoing.receive(message);
        }

        @Override
        protected void receiveDescribe(DescribeMessage message) {
            this.incoming.receive(message);
        }

        @Override
        protected void receiveDescription(DescriptionMessage message) {
            this.outgoing.receive(message);
        }
    }

}
