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

package org.webharvest.deprecated.runtime;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.inject.annotation.TestedObject;
import org.unitils.mock.annotation.Dummy;
import org.webharvest.runtime.Scraper;
import org.webharvest.utils.CommonUtil;

import java.util.concurrent.Callable;

public class ScraperContext10Test extends UnitilsTestNG {

    @TestedObject
    ScraperContext10 context;

    @Dummy
    Scraper scraper;

    @BeforeMethod
    public void before() {
        context = new ScraperContext10(scraper);
    }

    @Test
    public void testGetVar() throws Exception {
        // not existing var
        Assert.assertNull(context.getVar("x"));

        // local var
        context.setLocalVar("x", CommonUtil.createVariable(123));
        Assert.assertEquals(context.getVar("x").toInt(), 123);

        // sub-context. 1st level
        context.executeFunctionCall(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Assert.assertNull(context.getVar("x"));
                Assert.assertEquals(context.getVar("caller.x").toInt(), 123);

                // sub-context. 2st level
                return context.executeFunctionCall(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Assert.assertNull(context.getVar("x"));
                        Assert.assertNull(context.getVar("caller.x"));
                        Assert.assertEquals(context.getVar("caller.caller.x").toInt(), 123);
                        return null;
                    }
                });
            }
        });
    }
}
