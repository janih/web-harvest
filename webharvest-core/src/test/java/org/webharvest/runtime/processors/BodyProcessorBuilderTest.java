package org.webharvest.runtime.processors;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.webharvest.definition.ElementDefProxy;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.XmlNodeTestUtils;
import org.webharvest.runtime.processors.BodyProcessor.Builder;

public class BodyProcessorBuilderTest extends UnitilsTestNG {

        @Test
        public void testBuilder() {
            final IElementDef def = new MockElementDef();
            final Builder builder = new Builder(def);
            final BodyProcessor processor = builder.build();

            Assert.assertNotNull(processor, "Processor is null.");
            Assert.assertNotNull(processor.getElementDef(),
                    "Elemenet definition is null.");
            Assert.assertSame(processor.getElementDef(), def,
                    "Unexpected element definition.");
        }

        private class MockElementDef extends ElementDefProxy {

            protected MockElementDef() {
                super(XmlNodeTestUtils.createXmlNode("<empty/>",
                        XmlNodeTestUtils.NAMESPACE_21));
            }

        }
}
