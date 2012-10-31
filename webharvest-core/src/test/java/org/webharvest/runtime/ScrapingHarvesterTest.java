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
import org.webharvest.ioc.ScraperFactory;
import org.xml.sax.InputSource;

import com.google.inject.Injector;
import com.google.inject.Module;

public class ScrapingHarvesterTest extends UnitilsTestNG {

    @RegularMock
    private Injector mockInjector;

    @RegularMock
    private Injector mockChildInjector;

    @RegularMock
    private ScraperFactory mockFactory;

    @RegularMock
    private HarvestLoadCallback mockLoadCallback;

    @RegularMock
    private ScraperConfiguration mockConfiguration;

    @RegularMock
    private List<IElementDef> mockElementDef;

    @RegularMock
    private InputSource mockInputSource;

    @RegularMock
    private WebScraper mockScraper;

    @RegularMock
    private Harvester.ContextInitCallback mockInitCallback;

    @RegularMock
    private DynamicScopeContext mockContext;

    @Test
    public void testConstructorWithFilePathConfiguration()
            throws IOException {
        expect(mockInjector.createChildInjector(isA(Module.class))).
            andReturn(mockChildInjector);
        expect(mockChildInjector.getInstance(ScraperConfiguration.class)).
            andReturn(mockConfiguration);
        expect(mockConfiguration.getOperations()).andReturn(mockElementDef);
        mockLoadCallback.onSuccess(mockElementDef);
        expectLastCall();
        final File tmp = File.createTempFile("foo", "tmp");
        try {
            EasyMockUnitils.replay();
            new ScrapingHarvester(mockInjector, mockFactory,
                    tmp.getAbsolutePath(), mockLoadCallback);
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
        expect(mockConfiguration.getOperations()).andReturn(mockElementDef);
        mockLoadCallback.onSuccess(mockElementDef);
        expectLastCall();
        EasyMockUnitils.replay();
        new ScrapingHarvester(mockInjector, mockFactory,
                new URL("http://www.hurra.com"), mockLoadCallback);
    }

    @Test
    public void testConstructorWithInputSourceConfiguration()
            throws IOException {
        expect(mockInjector.createChildInjector(isA(Module.class))).
            andReturn(mockChildInjector);
        expect(mockChildInjector.getInstance(ScraperConfiguration.class)).
            andReturn(mockConfiguration);
        expect(mockConfiguration.getOperations()).andReturn(mockElementDef);
        mockLoadCallback.onSuccess(mockElementDef);
        expectLastCall();
        EasyMockUnitils.replay();
        new ScrapingHarvester(mockInjector, mockFactory,
                mockInputSource, mockLoadCallback);
    }

    @Test
    public void testExecute() {
        expect(mockInjector.createChildInjector(isA(Module.class))).
        andReturn(mockChildInjector);
        expect(mockChildInjector.getInstance(ScraperConfiguration.class)).
            andReturn(mockConfiguration);
        expect(mockConfiguration.getOperations()).andReturn(mockElementDef);
        mockLoadCallback.onSuccess(mockElementDef);
        expectLastCall();
        expect(mockFactory.create(mockConfiguration)).andReturn(mockScraper);
        expect(mockScraper.getContext()).andReturn(mockContext);
        mockInitCallback.onSuccess(mockContext);
        expectLastCall();
        mockScraper.execute();
        expectLastCall();
        expect(mockScraper.getContext()).andReturn(mockContext);
        EasyMockUnitils.replay();
        final Harvester harvester = new ScrapingHarvester(mockInjector,
                mockFactory, mockInputSource, mockLoadCallback);
        final DynamicScopeContext context = harvester.execute(mockInitCallback);
        assertNotNull(context);
        assertSame(mockContext, context);
        final WebScraper scraper = harvester.getScraper();
        assertNotNull(scraper);
        assertSame(mockScraper, scraper);
    }


}
