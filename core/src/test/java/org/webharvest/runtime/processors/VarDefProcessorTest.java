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

package org.webharvest.runtime.processors;

import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;
import org.unitils.mock.annotation.Dummy;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.deprecated.VarDefProcessor;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.web.HttpClientManager;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 17, 2010
 * Time: 10:05:46 AM
 */

public class VarDefProcessorTest extends UnitilsTestNG {

    Mock<Scraper> scraperMock;

    @Dummy
    Logger logger;
    ScraperContext context;

    @BeforeMethod
    public void before() {
        scraperMock.returns(logger).getLogger();
        scraperMock.returns(new HttpClientManager()).getHttpClientManager();

        context = new ScraperContext(scraperMock.getMock());
    }

    private Variable invoke(String varDefXml) throws InterruptedException {
        return ProcessorTestUtils.<VarDefProcessor>processor(varDefXml).
                execute(scraperMock.getMock(), context);
    }

    @Test
    public void testExecute_newVar_overwriteDefault() throws Exception {
        assertReflectionEquals(new NodeVariable("123"), invoke("<var-def name='x'>123</var-def>"));
        assertReflectionEquals(new NodeVariable("123"), context.getVar("x"));
    }

    @Test
    public void testExecute_newVar_overwriteTrue() throws Exception {
        assertReflectionEquals(new NodeVariable("123"), invoke("<var-def name='x' overwrite='true'>123</var-def>"));
        assertReflectionEquals(new NodeVariable("123"), context.getVar("x"));
    }

    @Test
    public void testExecute_newVar_overwriteFalse() throws Exception {
        assertReflectionEquals(new NodeVariable("123"), invoke("<var-def name='x' overwrite='false'>123</var-def>"));
        assertReflectionEquals(new NodeVariable("123"), context.getVar("x"));
    }

    @Test
    public void testExecute_reassigning_overwriteDefault() throws Exception {
        context.setLocalVar("x", new NodeVariable("existing"));
        assertReflectionEquals(new NodeVariable("123"), invoke("<var-def name='x'>123</var-def>"));
        assertReflectionEquals(new NodeVariable("123"), context.getVar("x"));
    }

    @Test
    public void testExecute_reassigning_overwriteTrue() throws Exception {
        context.setLocalVar("x", new NodeVariable("existing"));
        assertReflectionEquals(new NodeVariable("123"), invoke("<var-def name='x' overwrite='true'>123</var-def>"));
        assertReflectionEquals(new NodeVariable("123"), context.getVar("x"));
    }

    @Test
    public void testExecute_reassigning_overwriteFalse() throws Exception {
        context.setLocalVar("x", new NodeVariable("existing"));
        assertReflectionEquals(new NodeVariable("existing"), invoke("<var-def name='x' overwrite='false'>123</var-def>"));
        assertReflectionEquals(new NodeVariable("existing"), context.getVar("x"));
    }
}
