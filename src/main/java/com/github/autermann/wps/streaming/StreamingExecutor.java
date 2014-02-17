package com.github.autermann.wps.streaming;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.github.autermann.wps.streaming.message.InputMessage;
import com.github.autermann.wps.streaming.message.MessageID;
import com.github.autermann.wps.streaming.message.OutputMessage;
import com.github.autermann.wps.streaming.util.dependency.DependencyExecutor;

public class StreamingExecutor extends DependencyExecutor<MessageID, InputMessage, OutputMessage>
        implements Closeable {

    public StreamingExecutor(CallbackJobExecutor jobExecutor,
                             ExecutorService executorService,
                             MessageRepository repository) {
        super(jobExecutor, executorService, repository);
    }

    @Override
    public void close()
            throws IOException {
        ((CallbackJobExecutor) getExecutor()).close();
    }

}
