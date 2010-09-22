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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.mock.Mock;
import org.unitils.mock.annotation.Dummy;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.web.HttpClientManager;

import java.util.Arrays;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.webharvest.runtime.processors.ProcessorTestUtils.processor;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 17, 2010
 * Time: 10:05:46 AM
 */

@RunWith(UnitilsJUnit4TestClassRunner.class)

public class VarDefProcessorTest {

    VarDefProcessor processor;

    Mock<Scraper> scraperMock;

    @Dummy
    Logger logger;
    ScraperContext context;

    @Before
    public void before() {
        scraperMock.returns(logger).getLogger();
        scraperMock.returns(new HttpClientManager()).getHttpClientManager();

        context = new ScraperContext();
    }

    @Test
    public void testExecute_newVar_overwriteDefault() throws Exception {
        processor = processor("<var-def name='x'>123</var-def>");
        assertReflectionEquals(Arrays.asList(new NodeVariable("123")), processor.execute(scraperMock.getMock(), context).getWrappedObject());
        assertReflectionEquals(Arrays.asList(new NodeVariable("123")), context.getVar("x").getWrappedObject());
    }

    @Test
    public void testExecute_newVar_overwriteTrue() throws Exception {
        processor = processor("<var-def name='x' overwrite='true'>123</var-def>");
        assertReflectionEquals(Arrays.asList(new NodeVariable("123")), processor.execute(scraperMock.getMock(), context).getWrappedObject());
        assertReflectionEquals(Arrays.asList(new NodeVariable("123")), context.getVar("x").getWrappedObject());
    }

    @Test
    public void testExecute_newVar_overwriteFalse() throws Exception {
        processor = processor("<var-def name='x' overwrite='false'>123</var-def>");
        assertReflectionEquals(Arrays.asList(new NodeVariable("123")), processor.execute(scraperMock.getMock(), context).getWrappedObject());
        assertReflectionEquals(Arrays.asList(new NodeVariable("123")), context.getVar("x").getWrappedObject());
    }

    @Test
    public void testExecute_reassigning_overwriteDefault() throws Exception {
        context.setVar("x", new NodeVariable("existing"));
        processor = processor("<var-def name='x'>123</var-def>");
        assertReflectionEquals(Arrays.asList(new NodeVariable("123")), processor.execute(scraperMock.getMock(), context).getWrappedObject());
        assertReflectionEquals(Arrays.asList(new NodeVariable("123")), context.getVar("x").getWrappedObject());
    }

    @Test
    public void testExecute_reassigning_overwriteTrue() throws Exception {
        context.setVar("x", new NodeVariable("existing"));
        processor = processor("<var-def name='x' overwrite='true'>123</var-def>");
        assertReflectionEquals(Arrays.asList(new NodeVariable("123")), processor.execute(scraperMock.getMock(), context).getWrappedObject());
        assertReflectionEquals(Arrays.asList(new NodeVariable("123")), context.getVar("x").getWrappedObject());
    }

    @Test
    public void testExecute_reassigning_overwriteFalse() throws Exception {
        context.setVar("x", new NodeVariable("existing"));
        processor = processor("<var-def name='x' overwrite='false'>123</var-def>");

        // todo: inconsistency in returning result when variable is NOT reassigned! NodeVariable versus ListVariable
        assertReflectionEquals("existing", processor.execute(scraperMock.getMock(), context).getWrappedObject());
        assertReflectionEquals("existing", context.getVar("x").getWrappedObject());
    }
}
