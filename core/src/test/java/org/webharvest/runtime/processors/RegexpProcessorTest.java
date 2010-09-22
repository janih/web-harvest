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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.mock.Mock;
import org.unitils.mock.annotation.Dummy;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.web.HttpClientManager;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 22, 2010
 * Time: 10:53:40 PM
 */

@RunWith(UnitilsJUnit4TestClassRunner.class)

public class RegexpProcessorTest {

    ScraperContext context;
    Mock<Scraper> scraperMock;

    @Dummy
    Logger logger;

    @Before
    public void before() {
        scraperMock.returns(logger).getLogger();
        scraperMock.returns(new HttpClientManager()).getHttpClientManager();

        context = new ScraperContext();
    }

    @Test
    public void testExecute_full() throws Exception {
        Assert.assertEquals(
                "[111][222][333]",
                ProcessorTestUtils.<RegexpProcessor>processor("" +
                        "<regexp>" +
                        "  <regexp-pattern>^(?:a(\\d+))?(?:b(\\d+))?(?:c(\\d+))?$</regexp-pattern>" +
                        "  <regexp-source>a111b222c333</regexp-source>" +
                        "  <regexp-result>" +
                        "    [<var name='_1'/>]" +
                        "    [<var name='_2'/>]" +
                        "    [<var name='_3'/>]" +
                        "  </regexp-result>" +
                        "</regexp>").
                        execute(scraperMock.getMock(), context).toString().replaceAll("\\s", ""));
    }

    @Test
    public void testExecute_empty() throws Exception {
        Assert.assertEquals(
                "[][][]",
                ProcessorTestUtils.<RegexpProcessor>processor("" +
                        "<regexp>" +
                        "  <regexp-pattern>^(?:a(\\d+))?(?:b(\\d+))?(?:c(\\d+))?$</regexp-pattern>" +
                        "  <regexp-source></regexp-source>" +
                        "  <regexp-result>" +
                        "    [<var name='_1'/>]" +
                        "    [<var name='_2'/>]" +
                        "    [<var name='_3'/>]" +
                        "  </regexp-result>" +
                        "</regexp>").
                        execute(scraperMock.getMock(), context).toString().replaceAll("\\s", ""));
    }

    @Test
    public void testExecute_1st_part_absent() throws Exception {
        Assert.assertEquals(
                "[][222][333]",
                ProcessorTestUtils.<RegexpProcessor>processor("" +
                        "<regexp>" +
                        "  <regexp-pattern>^(?:a(\\d+))?(?:b(\\d+))?(?:c(\\d+))?$</regexp-pattern>" +
                        "  <regexp-source>b222c333</regexp-source>" +
                        "  <regexp-result>" +
                        "    [<var name='_1'/>]" +
                        "    [<var name='_2'/>]" +
                        "    [<var name='_3'/>]" +
                        "  </regexp-result>" +
                        "</regexp>").
                        execute(scraperMock.getMock(), context).toString().replaceAll("\\s", ""));
    }

    @Test
    public void testExecute_2nd_part_absent() throws Exception {
        Assert.assertEquals(
                "[111][][333]",
                ProcessorTestUtils.<RegexpProcessor>processor("" +
                        "<regexp>" +
                        "  <regexp-pattern>^(?:a(\\d+))?(?:b(\\d+))?(?:c(\\d+))?$</regexp-pattern>" +
                        "  <regexp-source>a111c333</regexp-source>" +
                        "  <regexp-result>" +
                        "    [<var name='_1'/>]" +
                        "    [<var name='_2'/>]" +
                        "    [<var name='_3'/>]" +
                        "  </regexp-result>" +
                        "</regexp>").
                        execute(scraperMock.getMock(), context).toString().replaceAll("\\s", ""));
    }

    @Test
    public void testExecute_3rd_part_absent() throws Exception {
        Assert.assertEquals(
                "[111][222][]",
                ProcessorTestUtils.<RegexpProcessor>processor("" +
                        "<regexp>" +
                        "  <regexp-pattern>^(?:a(\\d+))?(?:b(\\d+))?(?:c(\\d+))?$</regexp-pattern>" +
                        "  <regexp-source>a111b222</regexp-source>" +
                        "  <regexp-result>" +
                        "    [<var name='_1'/>]" +
                        "    [<var name='_2'/>]" +
                        "    [<var name='_3'/>]" +
                        "  </regexp-result>" +
                        "</regexp>").
                        execute(scraperMock.getMock(), context).toString().replaceAll("\\s", ""));
    }
}
