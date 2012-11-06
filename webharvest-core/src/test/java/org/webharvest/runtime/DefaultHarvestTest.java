package org.webharvest.runtime;

import static org.easymock.EasyMock.*;
import static org.testng.AssertJUnit.*;

import java.io.IOException;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.definition.ConfigSource;
import org.webharvest.events.EventHandler;
import org.webharvest.events.EventSink;
import org.webharvest.events.HandlerHolder;
import org.webharvest.events.HarvesterEvent;
import org.webharvest.ioc.HarvesterFactory;

public class DefaultHarvestTest extends UnitilsTestNG {

    @RegularMock
    private HarvesterFactory mockFactory;

    @RegularMock
    private HandlerHolder mockHandlerHolder;

    @RegularMock
    private HarvestLoadCallback mockLoadCallback;

    @RegularMock
    private EventSink mockEventSink;

    @RegularMock
    private HarvesterEvent mockEvent;

    @RegularMock
    private Harvester mockHarvester;

    @RegularMock
    private ConfigSource mockConfigSource;

    private DefaultHarvest harvest;

    @BeforeMethod
    public void setUp() {
        harvest = new DefaultHarvest(mockFactory, mockHandlerHolder,
                mockEventSink);
    }

    @AfterMethod
    public void tearDown() {
        harvest = null;
    }

    @Test
    public void testGetHarvester() throws IOException {
        expect(mockFactory.create(mockConfigSource, mockLoadCallback)).
            andReturn(mockHarvester);
        EasyMockUnitils.replay();
        final Harvester harvester = harvest.getHarvester(mockConfigSource,
                mockLoadCallback);
        assertNotNull(harvester);
        assertSame(mockHarvester, harvester);
    }

    @Test
    public void testAddEventHandler() {
        final EventHandler<HarvesterEvent> handler =
                new EventHandler<HarvesterEvent>() {

                    @Override
                    public void handle(final HarvesterEvent event) {
                        // Do nothing
                    }

                };
        mockHandlerHolder.register(handler);
        expectLastCall();
        EasyMockUnitils.replay();
        harvest.addEventHandler(handler);
    }

    @Test
    public void testPostEvent() {
        mockEventSink.publish(mockEvent);
        EasyMockUnitils.replay();
        harvest.postEvent(mockEvent);
    }

}
