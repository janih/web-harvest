package org.webharvest.events;

import static org.easymock.EasyMock.*;
import static org.testng.AssertJUnit.*;

import org.easymock.IAnswer;
import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.ScrapingAware;
import org.webharvest.ioc.ScrapingAwareTypeListener;
import org.webharvest.ioc.ScrapingInterceptor.ScrapingAwareHelper;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;

public class ScrapingAwareTypeListenerTest extends UnitilsTestNG {

    @RegularMock
    private TypeLiteral<Object> mockType;

    @RegularMock
    private TypeEncounter<Object> mockEncounter;

    @RegularMock
    private Provider<ScrapingAwareHelper> mockProvider;

    @RegularMock
    private ScrapingAware mockAwareObject;


    @RegularMock
    private ScrapingAwareHelper mockAwareHelper;

    private ScrapingAwareTypeListener listener;

    @BeforeMethod
    public void setUp() {
        listener = new ScrapingAwareTypeListener();
    }

    @AfterMethod
    public void tearDown() {
        listener = null;
    }

    @Test
    public void testApplyInjection() {
        final Capture capture = new Capture();
        expect(mockEncounter.getProvider(ScrapingAwareHelper.class)).
            andReturn(mockProvider);
        mockEncounter.register(isA(InjectionListener.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                capture.setCaptured(getCurrentArguments()[0]);
                return null;
            }
        });
        expect(mockProvider.get()).andReturn(mockAwareHelper);
        mockAwareHelper.addListener(mockAwareObject);
        expectLastCall();
        EasyMockUnitils.replay();
        listener.hear(mockType, mockEncounter);
        final InjectionListener<Object> listener = capture.getCaptured();
        assertNotNull(listener);
        listener.afterInjection(mockAwareObject);
        listener.afterInjection(this);
    }

    @Test
    public void test() {
        EasyMockUnitils.replay();
    }

    private final class Capture {

        private Object captured;

        @SuppressWarnings("unchecked")
        public <T> T getCaptured() {
            return (T) captured;
        }

        private void setCaptured(final Object captured) {
            this.captured = captured;
        }

    }

}