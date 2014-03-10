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

import com.github.autermann.wps.streaming.message.DescribeMessage;
import com.github.autermann.wps.streaming.message.DescriptionMessage;
import com.github.autermann.wps.streaming.message.ErrorMessage;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.Message;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.message.OutputRequestMessage;
import com.github.autermann.wps.streaming.message.StopMessage;

/**
 *
 * @author Christian Autermann
 */
public abstract class AbstractMessageReceiver implements MessageReceiver {

    @Override
    public void receive(Message message)
            throws UnsupportedMessageTypeException {
        if (message instanceof InputMessage) {
            receiveInput((InputMessage) message);
        } else if (message instanceof StopMessage) {
            receiveStop((StopMessage) message);
        } else if (message instanceof OutputRequestMessage) {
            receiveOutputRequest((OutputRequestMessage) message);
        } else if (message instanceof OutputMessage) {
            receiveOutput((OutputMessage) message);
        } else if (message instanceof ErrorMessage) {
            receiveError((ErrorMessage) message);
        } else if (message instanceof DescribeMessage) {
            receiveDescribe((DescribeMessage) message);
        } else if (message instanceof DescriptionMessage) {
            receiveDescription((DescriptionMessage) message);
        } else {
            throw new UnsupportedMessageTypeException(message);
        }
    }

    protected abstract void receiveError(ErrorMessage message)
            throws UnsupportedMessageTypeException;

    protected abstract void receiveOutputRequest(OutputRequestMessage message)
            throws UnsupportedMessageTypeException;

    protected abstract void receiveStop(StopMessage message)
            throws UnsupportedMessageTypeException;

    protected abstract void receiveInput(InputMessage message)
            throws UnsupportedMessageTypeException;

    protected abstract void receiveOutput(OutputMessage message)
            throws UnsupportedMessageTypeException;

    protected abstract void receiveDescribe(DescribeMessage message)
            throws UnsupportedMessageTypeException;

    protected abstract void receiveDescription(DescriptionMessage message)
            throws UnsupportedMessageTypeException;

}
