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

import com.github.autermann.wps.streaming.message.DescribeMessage;
import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.OutputRequestMessage;
import com.github.autermann.wps.streaming.message.StopMessage;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class OutgoingMessageReceiver extends AbstractMessageReceiver {

    @Override
    protected void receiveOutputRequest(OutputRequestMessage message)
            throws UnsupportedMessageTypeException {
        throw new UnsupportedMessageTypeException(message);
    }

    @Override
    protected void receiveStop(StopMessage message)
            throws UnsupportedMessageTypeException {
        throw new UnsupportedMessageTypeException(message);
    }

    @Override
    protected void receiveInput(InputMessage message)
            throws UnsupportedMessageTypeException {
        throw new UnsupportedMessageTypeException(message);
    }

    @Override
    protected void receiveDescribe(DescribeMessage message)
            throws UnsupportedMessageTypeException {
        throw new UnsupportedMessageTypeException(message);
    }
}
