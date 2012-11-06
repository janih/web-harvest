package org.webharvest.runtime;

import static org.easymock.EasyMock.*;
import static org.testng.AssertJUnit.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.definition.ConfigSource;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.ioc.ConfigFactory;
import org.webharvest.ioc.ContextFactory;

import com.google.inject.Provider;

public class ScrapingHarvesterTest extends UnitilsTestNG {

    private static final String NAMESPACE = "http://foo.com";

    @RegularMock
    private ConfigFactory mockConfigFactory;

    @RegularMock
    private Provider<WebScraper> mockScraperProvider;

    @RegularMock
    private HarvestLoadCallback mockLoadCallback;

    @RegularMock
    private ScraperConfiguration mockConfiguration;

    @RegularMock
    private List<IElementDef> mockElementDefs;

    @RegularMock
    private IElementDef mockElementDef;

    @RegularMock
    private WebScraper mockScraper;

    @RegularMock
    private Harvester.ContextInitCallback mockInitCallback;

    @RegularMock
    private DynamicScopeContext mockContext;

    @RegularMock
    private ContextFactory mockContextFactory;

    @RegularMock
    private ConfigSource mockConfigSource;

    @Test
    public void testConstructor()
            throws IOException {
        final URL expectedURL = new URL("http://www.hurra.com");
        expect(mockConfigFactory.create(mockConfigSource)).
            andReturn(mockConfiguration);
        expect(mockConfiguration.getOperations()).andReturn(mockElementDefs);
        mockLoadCallback.onSuccess(mockElementDefs);
        expectLastCall();
        EasyMockUnitils.replay();
        new ScrapingHarvester(mockConfigFactory, mockScraperProvider,
                mockContextFactory, mockConfigSource,
                mockLoadCallback);
    }

    @Test
    public void testExecute() throws IOException {
        expect(mockConfigFactory.create(mockConfigSource)).
            andReturn(mockConfiguration);
        expect(mockConfiguration.getOperations()).andReturn(mockElementDefs);
        mockLoadCallback.onSuccess(mockElementDefs);
        expectLastCall();
        expect(mockScraperProvider.get()).andReturn(mockScraper);
        expect(mockConfiguration.getNamespaceURI()).andReturn(NAMESPACE);
        expect(mockContextFactory.create(NAMESPACE)).andReturn(mockContext);
        expect(mockConfiguration.getRootElementDef()).andReturn(mockElementDef);
        mockContext.setRootDef(mockElementDef);
        expectLastCall();
        mockInitCallback.onSuccess(mockContext);
        expectLastCall();
        mockScraper.execute(mockContext);
        expectLastCall();
        EasyMockUnitils.replay();
        final Harvester harvester = new ScrapingHarvester(mockConfigFactory,
                mockScraperProvider, mockContextFactory, mockConfigSource,
                mockLoadCallback);
        final DynamicScopeContext context = harvester.execute(mockInitCallback);
        assertNotNull(context);
        assertSame(mockContext, context);
        final WebScraper scraper = harvester.getScraper();
        assertNotNull(scraper);
        assertSame(mockScraper, scraper);
    }

}
