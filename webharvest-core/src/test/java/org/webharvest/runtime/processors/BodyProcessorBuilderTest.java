package org.webharvest.runtime.processors;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.webharvest.definition.AbstractElementDef;
import org.webharvest.definition.XmlNodeTestUtils;
import org.webharvest.runtime.processors.BodyProcessor.Builder;

public class BodyProcessorBuilderTest extends UnitilsTestNG {

        @Test
        public void testBuilder() {
            final AbstractElementDef def = new MockElementDef();
            final Builder builder = new Builder(def);
            final BodyProcessor processor = builder.build();

            Assert.assertNotNull(processor, "Processor is null.");
            Assert.assertNotNull(processor.getElementDef(),
                    "Elemenet definition is null.");
            Assert.assertSame(processor.getElementDef(), def,
                    "Unexpected element definition.");
        }

        private class MockElementDef extends AbstractElementDef {

            protected MockElementDef() {
                super(XmlNodeTestUtils.createXmlNode("<empty/>",
                        XmlNodeTestUtils.NAMESPACE_21), false);
            }

        }
}
