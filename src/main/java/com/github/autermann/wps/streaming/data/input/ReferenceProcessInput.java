package com.github.autermann.wps.streaming.data.input;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.autermann.wps.commons.description.OwsCodeType;
import com.github.autermann.wps.streaming.message.MessageID;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class ReferenceProcessInput extends ProcessInput {

    private final OwsCodeType output;
    private final MessageID iteration;

    public ReferenceProcessInput(OwsCodeType id,
                                 MessageID iteration,
                                 OwsCodeType output) {
        super(id);
        this.iteration = checkNotNull(iteration);
        this.output = checkNotNull(output);
    }

    public OwsCodeType getReferencedOutput() {
        return this.output;
    }

    public MessageID getReferencedMessage() {
        return this.iteration;
    }

}
