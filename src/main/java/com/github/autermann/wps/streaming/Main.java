package com.github.autermann.wps.streaming;

import com.github.autermann.wps.streaming.delegate.DelegatingStreamingAlgorithm;
import com.github.autermann.wps.streaming.delegate.ProcessDescriptionParser;
import com.github.autermann.wps.streaming.delegate.StaticInputParser;


/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class Main {

    public static void main(String[] args) throws Exception {
        StreamingWPS wps = new StreamingWPS("localhost", 12121);
        wps.addAlgorithm(DelegatingStreamingAlgorithm.class);
        wps.addParser(ProcessDescriptionParser.class);
        wps.addParser(StaticInputParser.class);
        wps.start();
    }
}
