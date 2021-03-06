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
package com.github.autermann.wps.streaming.example;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import com.google.common.math.LongMath;

@Algorithm(identifier = "com.github.autermann.wps.streaming.example.AddAlgorithm",
           version = "1.0.0")
public class AddAlgorithm extends AbstractAnnotatedAlgorithm {
    private long a, b, result;

    @Execute
    public void execute() {
        //WPS does not support BigInteger/BigDecimal...
        result = LongMath.checkedAdd(a, b);
    }

    @LiteralDataInput(identifier = "a")
    public void setA(long a) {
        this.a = a;
    }

    @LiteralDataInput(identifier = "b")
    public void setB(long b) {
        this.b = b;
    }

    @LiteralDataOutput(identifier = "result")
    public long getResult() {
        return this.result;
    }
}
