package com.github.autermann.wps.streaming;


import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.util.dependency.Repository;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public interface MessageRepository extends
        Repository<MessageID, InputMessage, OutputMessage> {
}
