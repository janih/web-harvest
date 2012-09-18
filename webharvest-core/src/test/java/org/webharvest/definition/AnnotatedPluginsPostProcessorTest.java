package org.webharvest.definition;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.Variable;

public class AnnotatedPluginsPostProcessorTest extends UnitilsTestNG {

    private static final String TARGET_NAMESPACE =
        "http://web-harvest.sourceforge.net/schema/dummy";

    private Mock<ConfigurableResolver> mockConfigurbleResolver;

    private AnnotatedPluginsPostProcessor postProcessor;

    @BeforeMethod
    public void setUp() {
        this.postProcessor = new AnnotatedPluginsPostProcessor(
                this.getClass().getPackage().getName());
    }

    @AfterMethod
    public void tearDown() {
        this.postProcessor = null;
    }

    @Test
    public void testPostProcess() {
        postProcessor.postProcess(mockConfigurbleResolver.getMock());

        mockConfigurbleResolver.assertInvoked().registerPlugin(
                ValidPlugin.class, TARGET_NAMESPACE);
        mockConfigurbleResolver.assertNotInvoked().registerPlugin(null, null);
    }

    @Autoscanned
    @TargetNamespace(TARGET_NAMESPACE)
    static class ValidPlugin extends MockAbstractPlugin {
    }

    @Autoscanned
    static class MissingNamespacePlugin extends MockAbstractPlugin {
    }

    @Autoscanned
    @TargetNamespace(TARGET_NAMESPACE)
    class NotWebHarvestPlugin {
    }

    abstract static class MockAbstractPlugin extends WebHarvestPlugin {

        @Override
        public String getName() {
            throw new UnsupportedOperationException("not supported by mock");
        }

        @Override
        public Variable executePlugin(final Scraper scraper,
                final DynamicScopeContext context) throws InterruptedException {
            throw new UnsupportedOperationException("not supported by mock");
        }
    }
}
