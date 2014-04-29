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
package com.github.autermann.wps.streaming.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.n52.wps.server.ExceptionReport;

import com.github.autermann.wps.streaming.message.ErrorMessage;
import com.github.autermann.wps.streaming.message.Message;
import com.github.autermann.wps.streaming.message.RelationshipType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class StreamingError extends ExceptionReport {
    public static final String UNRESOLVABLE_INPUT = "UnresolvableInput";
    private static final long serialVersionUID = -7876931918796740783L;
    private final List<RemoteException> additionalExceptions = new LinkedList<>();

    public StreamingError(String message, String errorKey) {
        super(message, errorKey);
    }

    public StreamingError(String message, String errorKey, Throwable e) {
        super(message, errorKey, e);
    }

    public StreamingError(String message, String errorKey, String locator) {
        super(message, errorKey, locator);
    }

    public StreamingError(String message, String errorKey, String locator,
                          Throwable e) {
        super(message, errorKey, locator, e);
    }

    public String getErrorKey() {
        return super.errorKey;
    }

    public String getLocator() {
        return super.locator;
    }

    public ErrorMessage toMessage(Message cause) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setPayload(this);
        errorMessage.setProcessID(cause.getProcessID());
        errorMessage.addRelatedMessage(RelationshipType.Reply, cause);
        return errorMessage;
    }

    public StreamingError addRemoteException(RemoteException e) {
        this.additionalExceptions.add(e);
        return this;
    }

    public List<RemoteException> getRemoteExceptions() {
        return Collections.unmodifiableList(additionalExceptions);
    }

    public static class RemoteException {
        private final String[] message;
        private final String locator;
        private final String code;

        public RemoteException(String[] message, String code, String locator) {
            this.message = message;
            this.locator = locator;
            this.code = code;
        }

        public String[] getMessage() {
            return message;
        }

        public String getLocator() {
            return locator;
        }

        public String getCode() {
            return code;
        }
    }
}
