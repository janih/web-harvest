/*
 Copyright (c) 2006-2007, Vladimir Nikic
 All rights reserved.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
   copyright notice, this list of conditions and the
   following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
   products derived from this software without specific prior
   written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.

 You can contact Vladimir Nikic by sending e-mail to
 nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
 subject line.
 */

package org.webharvest.runtime;

import org.apache.commons.collections.IteratorUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.mock.annotation.Dummy;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.KeyValuePair;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;


/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 22, 2010
 * Time: 12:43:00 AM
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class ScraperContextTest {

    ScraperContext context = new ScraperContext();

    @Dummy
    private Variable dummyVar;

    @Test
    public void testSetVar() throws Exception {
        context.setVar("x", dummyVar);
        context.setVar("y", dummyVar);
        assertSame(dummyVar, context.getVar("x"));
        assertSame(dummyVar, context.getVar("y"));

        context.setVar("x", "test");
        context.setVar("y", Arrays.asList(1, 2, 3));
        assertReflectionEquals(new NodeVariable("test"), context.getVar("x"));
        assertReflectionEquals(new ListVariable(Arrays.asList(1, 2, 3)), context.getVar("y"));

        assertNull(context.getVar("non-existing"));
    }

    @Test
    public void testRemoveVar() throws Exception {
        assertNull(context.removeVar("non-existing"));
        context.setVar("x", dummyVar);
        assertSame(dummyVar, context.removeVar("x"));
        assertNull(context.getVar("x"));
    }

    @Test
    public void testIterator() throws Exception {
        assertFalse(context.iterator().hasNext());

        context.setVar("z", "zzz");
        context.setVar("x", "will be overwritten");
        context.setVar("x", dummyVar);
        context.setVar("y", dummyVar);

        assertReflectionEquals(Arrays.asList(
                new KeyValuePair<Variable>("x", dummyVar),
                new KeyValuePair<Variable>("y", dummyVar),
                new KeyValuePair<Variable>("z", new NodeVariable("zzz"))),
                IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
    }

    @Test
    public void testExecuteWithinNewContext() throws Exception {
        context.setVar("x", "a");
        context.setVar("y", 1);

        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                context.setVar("y", 2);

                context.executeWithinNewContext(new Runnable() {
                    @Override
                    public void run() {
                        context.setVar("z", "zzz");

                        context.executeWithinNewContext(new Runnable() {
                            @Override
                            public void run() {
                                context.setVar("x", "b");
                                context.setVar("zzz", dummyVar);
                                context.setVar("local", dummyVar);

                                assertReflectionEquals(dummyVar, context.removeVar("local"));
                                assertReflectionEquals(dummyVar, context.removeVar("zzz"));
                                assertNull(context.removeVar("zzz")); // upper level var
                                assertNull(context.removeVar("y")); // upper level var

                                assertReflectionEquals(new NodeVariable("b"), context.getVar("x"));
                                assertReflectionEquals(new NodeVariable(2), context.getVar("y"));
                                assertReflectionEquals(new NodeVariable("zzz"), context.getVar("z"));
                                assertReflectionEquals(Arrays.asList(
                                        new KeyValuePair<Variable>("x", new NodeVariable("b")),
                                        new KeyValuePair<Variable>("y", new NodeVariable(2)),
                                        new KeyValuePair<Variable>("z", new NodeVariable("zzz"))
                                ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);

                            }
                        });

                        assertReflectionEquals(new NodeVariable("a"), context.getVar("x"));
                        assertReflectionEquals(new NodeVariable(2), context.getVar("y"));
                        assertReflectionEquals(new NodeVariable("zzz"), context.getVar("z"));
                        assertReflectionEquals(Arrays.asList(
                                new KeyValuePair<Variable>("x", new NodeVariable("a")),
                                new KeyValuePair<Variable>("y", new NodeVariable(2)),
                                new KeyValuePair<Variable>("z", new NodeVariable("zzz"))
                        ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
                    }
                });

                assertReflectionEquals(new NodeVariable("a"), context.getVar("x"));
                assertReflectionEquals(new NodeVariable(2), context.getVar("y"));
                assertNull(context.getVar("z"));
                assertReflectionEquals(Arrays.asList(
                        new KeyValuePair<Variable>("x", new NodeVariable("a")),
                        new KeyValuePair<Variable>("y", new NodeVariable(2))
                ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
            }
        });

        assertReflectionEquals(new NodeVariable("a"), context.getVar("x"));
        assertReflectionEquals(new NodeVariable(1), context.getVar("y"));
        assertNull(context.getVar("z"));
        assertReflectionEquals(Arrays.asList(
                new KeyValuePair<Variable>("x", new NodeVariable("a")),
                new KeyValuePair<Variable>("y", new NodeVariable(1))
        ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
    }
}
