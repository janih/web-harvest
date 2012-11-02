package org.webharvest.runtime;

import static org.easymock.EasyMock.*;
import static org.testng.AssertJUnit.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.ioc.ContextFactory;
import org.xml.sax.InputSource;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

public class ScrapingHarvesterTest extends UnitilsTestNG {

    private static final String NAMESPACE = "http://foo.com";

    @RegularMock
    private Injector mockInjector;

    @RegularMock
    private Injector mockChildInjector;

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
    private InputSource mockInputSource;

    @RegularMock
    private WebScraper mockScraper;

    @RegularMock
    private Harvester.ContextInitCallback mockInitCallback;

    @RegularMock
    private DynamicScopeContext mockContext;

    @RegularMock
    private ContextFactory mockContextFactory;

    @Test
    public void testConstructorWithFilePathConfiguration()
            throws IOException {
        expect(mockInjector.createChildInjector(isA(Module.class))).
            andReturn(mockChildInjector);
        expect(mockChildInjector.getInstance(ScraperConfiguration.class)).
            andReturn(mockConfiguration);
        expect(mockConfiguration.getOperations()).andReturn(mockElementDefs);
        mockLoadCallback.onSuccess(mockElementDefs);
        expectLastCall();
        EasyMockUnitils.replay();
        final File tmp = File.createTempFile("foo", "tmp");
        try {
            new ScrapingHarvester(mockInjector, mockScraperProvider,
                    mockContextFactory, tmp.getAbsolutePath(),
                    mockLoadCallback);
        } finally {
            tmp.delete();
        }
    }

    @Test
    public void testConstructorWithConfigurationFromURL()
            throws IOException {
        expect(mockInjector.createChildInjector(isA(Module.class))).
            andReturn(mockChildInjector);
        expect(mockChildInjector.getInstance(ScraperConfiguration.class)).
            andReturn(mockConfiguration);
        expect(mockConfiguration.getOperations()).andReturn(mockElementDefs);
        mockLoadCallback.onSuccess(mockElementDefs);
        expectLastCall();
        EasyMockUnitils.replay();
        new ScrapingHarvester(mockInjector, mockScraperProvider,
                mockContextFactory, new URL("http://www.hurra.com"),
                mockLoadCallback);
    }

    @Test
    public void testConstructorWithInputSourceConfiguration()
            throws IOException {
        expect(mockInjector.createChildInjector(isA(Module.class))).
            andReturn(mockChildInjector);
        expect(mockChildInjector.getInstance(ScraperConfiguration.class)).
            andReturn(mockConfiguration);
        expect(mockConfiguration.getOperations()).andReturn(mockElementDefs);
        mockLoadCallback.onSuccess(mockElementDefs);
        expectLastCall();
        EasyMockUnitils.replay();
        new ScrapingHarvester(mockInjector, mockScraperProvider,
                mockContextFactory, mockInputSource, mockLoadCallback);
    }

    @Test
    public void testExecute() {
        expect(mockInjector.createChildInjector(isA(Module.class))).
        andReturn(mockChildInjector);
        expect(mockChildInjector.getInstance(ScraperConfiguration.class)).
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
        final Harvester harvester = new ScrapingHarvester(mockInjector,
                mockScraperProvider, mockContextFactory, mockInputSource,
                mockLoadCallback);
        final DynamicScopeContext context = harvester.execute(mockInitCallback);
        assertNotNull(context);
        assertSame(mockContext, context);
        final WebScraper scraper = harvester.getScraper();
        assertNotNull(scraper);
        assertSame(mockScraper, scraper);
    }


}
