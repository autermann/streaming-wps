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
package com.github.autermann.wps.streaming;

import com.github.autermann.wps.streaming.delegate.DelegatingStreamingAlgorithm;
import com.github.autermann.wps.streaming.delegate.ProcessDescriptionParser;
import com.github.autermann.wps.streaming.delegate.StaticInputParser;
import com.github.autermann.wps.streaming.example.AddAlgorithm;
import com.github.autermann.wps.streaming.example.PreconfiguredDelegateProcess;
import com.github.autermann.wps.streaming.example.StatefulSummingProcess;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class Main {

    public static void main(String[] args) throws Exception {
        new StreamingWPS("localhost", 12121)
                .addAlgorithm(DelegatingStreamingAlgorithm.class)
                .addAlgorithm(AddAlgorithm.class)
                .addAlgorithm(StatefulSummingProcess.class)
                .addAlgorithm(PreconfiguredDelegateProcess.class)
                .addParser(ProcessDescriptionParser.class)
                .addParser(StaticInputParser.class)
                .start();
    }
}
