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

import com.github.autermann.wps.streaming.message.DescriptionMessage;
import com.github.autermann.wps.streaming.message.ErrorMessage;
import com.github.autermann.wps.streaming.message.OutputMessage;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class IncomingMessageReceiver extends AbstractMessageReceiver {

    @Override
    protected void receiveError(ErrorMessage message)
            throws UnsupportedMessageTypeException {
        throw new UnsupportedMessageTypeException(message);
    }

    @Override
    protected void receiveOutput(OutputMessage message)
            throws UnsupportedMessageTypeException {
        throw new UnsupportedMessageTypeException(message);
    }

    @Override
    protected void receiveDescription(DescriptionMessage message)
            throws UnsupportedMessageTypeException {
        throw new UnsupportedMessageTypeException(message);
    }
}
