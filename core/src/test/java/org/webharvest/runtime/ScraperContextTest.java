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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.annotation.Dummy;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.KeyValuePair;

import java.util.Arrays;

import static org.testng.Assert.*;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;


/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 22, 2010
 * Time: 12:43:00 AM
 */
public class ScraperContextTest extends UnitilsTestNG {

    ScraperContext context;

    @Dummy
    private Variable dummyVar;

    @BeforeMethod
    public void before() {
        context = new ScraperContext();
    }

    @Test
    public void testSetVar() throws Exception {
        context.setLocalVar("x", dummyVar);
        context.setLocalVar("y", dummyVar);
        assertSame(context.getVar("x"), dummyVar);
        assertSame(context.getVar("y"), dummyVar);

        context.setLocalVar("x", "test");
        context.setLocalVar("y", Arrays.asList(1, 2, 3));
        assertReflectionEquals(new NodeVariable("test"), context.getVar("x"));
        assertReflectionEquals(new ListVariable(Arrays.asList(1, 2, 3)), context.getVar("y"));
    }

    @Test
    public void testContainsVar() throws Exception {
        context.setLocalVar("x", dummyVar);
        context.setLocalVar("null", null);
        assertTrue(context.containsVar("x"));
        assertFalse(context.containsVar("not existing"));
        assertSame(context.getVar("null"), EmptyVariable.INSTANCE);
    }

    @Test
    public void testReplaceExistingVar() throws Exception {
        context.setLocalVar("x", new NodeVariable("old val"));
        context.replaceExistingVar("x", dummyVar);
        assertSame(context.getVar("x"), dummyVar);
    }

    @Test
    public void testReplaceExistingVar_setNull() throws Exception {
        context.setLocalVar("x", new NodeVariable("old val"));
        context.replaceExistingVar("x", null);
        assertSame(context.getVar("x"), EmptyVariable.INSTANCE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testReplaceExistingVar_noSuchVariable() throws Exception {
        context.replaceExistingVar("not existing", dummyVar);
    }

    @Test
    public void testReplaceExistingVar_inSubContext() throws Exception {
        context.setLocalVar("x", new NodeVariable("old val"));
        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                context.replaceExistingVar("x", dummyVar);
                assertSame(context.getVar("x"), dummyVar);
            }
        }, false);
        assertSame(context.getVar("x"), dummyVar);
    }

    @Test
    public void testIterator() throws Exception {
        assertFalse(context.iterator().hasNext());

        context.setLocalVar("z", "zzz");
        context.setLocalVar("x", "will be overwritten");
        context.setLocalVar("x", dummyVar);
        context.setLocalVar("y", dummyVar);

        assertReflectionEquals(Arrays.asList(
                new KeyValuePair<Variable>("x", dummyVar),
                new KeyValuePair<Variable>("y", dummyVar),
                new KeyValuePair<Variable>("z", new NodeVariable("zzz"))),
                IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
    }

    @Test
    public void testExecuteWithinNewContext() throws Exception {
        context.setLocalVar("x", "a");
        context.setLocalVar("y", 1);

        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                context.setLocalVar("y", 2);

                context.executeWithinNewContext(new Runnable() {
                    @Override
                    public void run() {
                        context.setLocalVar("z", "zzz");

                        context.executeWithinNewContext(new Runnable() {
                            @Override
                            public void run() {
                                context.setLocalVar("x", "b");

                                assertReflectionEquals(new NodeVariable("b"), context.getVar("x"));
                                assertReflectionEquals(new NodeVariable(2), context.getVar("y"));
                                assertReflectionEquals(new NodeVariable("zzz"), context.getVar("z"));

                                assertReflectionEquals(Arrays.asList(
                                        new KeyValuePair<Variable>("x", new NodeVariable("b")),
                                        new KeyValuePair<Variable>("y", new NodeVariable(2)),
                                        new KeyValuePair<Variable>("z", new NodeVariable("zzz"))
                                ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);

                            }
                        }, false);

                        assertReflectionEquals(new NodeVariable("a"), context.getVar("x"));
                        assertReflectionEquals(new NodeVariable(2), context.getVar("y"));
                        assertReflectionEquals(new NodeVariable("zzz"), context.getVar("z"));
                        assertReflectionEquals(Arrays.asList(
                                new KeyValuePair<Variable>("x", new NodeVariable("a")),
                                new KeyValuePair<Variable>("y", new NodeVariable(2)),
                                new KeyValuePair<Variable>("z", new NodeVariable("zzz"))
                        ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
                    }
                }, false);

                assertReflectionEquals(new NodeVariable("a"), context.getVar("x"));
                assertReflectionEquals(new NodeVariable(2), context.getVar("y"));
                assertNull(context.getVar("z"));
                assertReflectionEquals(Arrays.asList(
                        new KeyValuePair<Variable>("x", new NodeVariable("a")),
                        new KeyValuePair<Variable>("y", new NodeVariable(2))
                ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
            }
        }, false);

        assertReflectionEquals(new NodeVariable("a"), context.getVar("x"));
        assertReflectionEquals(new NodeVariable(1), context.getVar("y"));
        assertNull(context.getVar("z"));
        assertReflectionEquals(Arrays.asList(
                new KeyValuePair<Variable>("x", new NodeVariable("a")),
                new KeyValuePair<Variable>("y", new NodeVariable(1))
        ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
    }

    @Test
    public void testSetVar_compat2b1_outsideLoop() throws Exception {
        context.setVar_compat2b1("x", new NodeVariable("a"));
        context.setVar_compat2b1("y", new NodeVariable(1));

        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                context.setVar_compat2b1("y", new NodeVariable(2));

                context.executeWithinNewContext(new Runnable() {
                    @Override
                    public void run() {
                        context.setVar_compat2b1("z", new NodeVariable("zzz"));

                        context.executeWithinNewContext(new Runnable() {
                            @Override
                            public void run() {
                                context.setVar_compat2b1("x", new NodeVariable("b"));

                                assertReflectionEquals(new NodeVariable("b"), context.getVar("x"));
                                assertReflectionEquals(new NodeVariable(2), context.getVar("y"));
                                assertReflectionEquals(new NodeVariable("zzz"), context.getVar("z"));
                                assertReflectionEquals(Arrays.asList(
                                        new KeyValuePair<Variable>("x", new NodeVariable("b")),
                                        new KeyValuePair<Variable>("y", new NodeVariable(2)),
                                        new KeyValuePair<Variable>("z", new NodeVariable("zzz"))
                                ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);

                            }
                        }, false);

                        assertReflectionEquals(new NodeVariable("a"), context.getVar("x"));
                        assertReflectionEquals(new NodeVariable(2), context.getVar("y"));
                        assertReflectionEquals(new NodeVariable("zzz"), context.getVar("z"));
                        assertReflectionEquals(Arrays.asList(
                                new KeyValuePair<Variable>("x", new NodeVariable("a")),
                                new KeyValuePair<Variable>("y", new NodeVariable(2)),
                                new KeyValuePair<Variable>("z", new NodeVariable("zzz"))
                        ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
                    }
                }, false);

                assertReflectionEquals(new NodeVariable("a"), context.getVar("x"));
                assertReflectionEquals(new NodeVariable(2), context.getVar("y"));
                assertNull(context.getVar("z"));
                assertReflectionEquals(Arrays.asList(
                        new KeyValuePair<Variable>("x", new NodeVariable("a")),
                        new KeyValuePair<Variable>("y", new NodeVariable(2))
                ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
            }
        }, false);

        assertReflectionEquals(new NodeVariable("a"), context.getVar("x"));
        assertReflectionEquals(new NodeVariable(1), context.getVar("y"));
        assertNull(context.getVar("z"));
        assertReflectionEquals(Arrays.asList(
                new KeyValuePair<Variable>("x", new NodeVariable("a")),
                new KeyValuePair<Variable>("y", new NodeVariable(1))
        ), IteratorUtils.toList(context.iterator()), ReflectionComparatorMode.LENIENT_ORDER);
    }

    @Test
    public void testSetVar_compat2b1_insideLoop_caseA() throws Exception {
        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                context.setVar_compat2b1("a", new NodeVariable(2));
                assertReflectionEquals(new NodeVariable(2), context.getVar("a"));
            }
        }, true);

        assertReflectionEquals(new NodeVariable(2), context.getVar("a"));
    }

    @Test
    public void testSetVar_compat2b1_insideLoop_caseB() throws Exception {
        context.setLocalVar("a", new NodeVariable(1));

        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                context.setLocalVar("a", new NodeVariable(2));
                context.setVar_compat2b1("a", new NodeVariable(3));
                assertReflectionEquals(new NodeVariable(2), context.getVar("a")); //mixing <def> and <var-def> leads to confusing results !!!
            }
        }, true);

        assertReflectionEquals(new NodeVariable(3), context.getVar("a"));
    }

    @Test
    public void testSetVar_compat2b1_insideLoop_caseC() throws Exception {
        context.setLocalVar("a", new NodeVariable(1));

        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                context.setVar_compat2b1("a", new NodeVariable(2));
                assertReflectionEquals(new NodeVariable(2), context.getVar("a"));
            }
        }, true);

        assertReflectionEquals(new NodeVariable(2), context.getVar("a"));
    }

    @Test
    public void testSetVar_compat2b1_insideLoop_caseD() throws Exception {
        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                context.setLocalVar("a", new NodeVariable(1));
                context.setVar_compat2b1("a", new NodeVariable(2));
                assertReflectionEquals(new NodeVariable(1), context.getVar("a")); //mixing <def> and <var-def> leads to confusing results !!!
            }
        }, true);

        assertReflectionEquals(new NodeVariable(2), context.getVar("a"));
    }

    @Test
    public void testSetVar_compat2b1_insideLoop_caseE() throws Exception {
        context.setLocalVar("a", new NodeVariable(1));

        context.executeWithinNewContext(new Runnable() {
            @Override
            public void run() {
                context.executeWithinNewContext(new Runnable() {
                    @Override
                    public void run() {
                        context.setVar_compat2b1("a", new NodeVariable(2));
                        assertReflectionEquals(new NodeVariable(2), context.getVar("a"));
                    }
                }, true);
                assertReflectionEquals(new NodeVariable(2), context.getVar("a"));
            }
        }, false);

        assertReflectionEquals(new NodeVariable(1), context.getVar("a"));
    }
}
