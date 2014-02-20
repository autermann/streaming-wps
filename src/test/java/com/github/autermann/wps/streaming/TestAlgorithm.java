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
