package com.github.autermann.wps.streaming;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(identifier = "com.github.autermann.wps.streaming.TestAlgorithm",
           version = "1.0.0")
public class TestAlgorithm extends AbstractAnnotatedAlgorithm {
    private int a, b, result;

    @Execute
    public void execute() {
        result = a + b;
    }

    @LiteralDataInput(identifier = "a")
    public void setA(int a) {
        this.a = a;
    }

    @LiteralDataInput(identifier = "b")
    public void setB(int b) {
        this.b = b;
    }

    @LiteralDataOutput(identifier = "result")
    public int getResult() {
        return this.result;
    }
}
