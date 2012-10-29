package org.webharvest.events;

import org.unitils.UnitilsTestNG;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.Harvester;
import org.webharvest.Registry;

import com.google.common.eventbus.EventBus;

public class DefaultHandlerHolderTest extends UnitilsTestNG {

    @RegularMock
    private Registry<Harvester, EventBus> mockRegistry;
    
    

}
