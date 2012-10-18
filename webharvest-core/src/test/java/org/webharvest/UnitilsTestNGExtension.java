package org.webharvest;

import java.lang.reflect.Field;

import org.testng.annotations.BeforeMethod;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;
import org.webharvest.ioc.InjectorHelper;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.templaters.BaseTemplater;

import com.google.inject.Injector;

/**
 * Extension of {@link UnitilsTestNG} which before each test method inject
 * appropriate mocks such as {@link Injector} mock and
 * {@link ScriptEngineFactory} mock into static fields in {@link InjectorHelper}
 * and {@link BaseTemplater} classes.
 *
 * Each test class verifying WebHarvest's processor or plugin should extend this
 * class.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public abstract class UnitilsTestNGExtension extends UnitilsTestNG {

    protected Mock<ScriptEngineFactory> scriptEngineFactoryMock;
    protected Mock<Injector> injectorMock;

    /**
     * Put mocks into static fields of InjectorHelper and BaseTemplater classes.
     * It must be done in this way, because unit tests work out of Guice IoC
     * which injects to these classes appropriate objects.
     */
    @BeforeMethod
    protected void injectStaticFields() throws Exception {

        // Puts Injector's mock into InjectorHelper class
        final Field injectorField = InjectorHelper.class
                .getDeclaredField("injector");
        injectorField.setAccessible(true);
        injectorField.set(null, injectorMock.getMock());

        // Puts ScriptEngineFactory's mock into BaseTemplater class
        final Field templaterField = BaseTemplater.class
                .getDeclaredField("scriptEngineFactory");
        templaterField.setAccessible(true);
        templaterField.set(null, getScriptEngineFactory());

    }

    /**
     * Helper method returning {@link ScriptEngineFactory} instance which by
     * default is a mock of this interface. This method could be overridden.
     *
     * @return {@link ScriptEngineFactory} instance
     */
    protected ScriptEngineFactory getScriptEngineFactory() {
        return scriptEngineFactoryMock.getMock();
    }

}
