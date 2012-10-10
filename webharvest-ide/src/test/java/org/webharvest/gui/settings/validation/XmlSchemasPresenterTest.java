package org.webharvest.gui.settings.validation;

import static org.easymock.EasyMock.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.tools.ant.filters.StringInputStream;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.unitils.inject.annotation.InjectInto;
import org.unitils.inject.annotation.TestedObject;
import org.webharvest.Transformer;
import org.webharvest.definition.validation.SchemaResolver;
import org.webharvest.definition.validation.SchemaSource;

public class XmlSchemasPresenterTest extends UnitilsTestNG {

    private static final String SAMPLE_LOCATION = "/aaa.xsd";
    private static final String SAMPLE_OTHER_LOCATION = "/bbb.xsd";
    private static final String SAMPLE_ANOTHER_LOCATION = "/ccc.xsd";

    @RegularMock
    @InjectInto(property = "schemaResolver")
    private SchemaResolver mockSchemaResolver;

    @RegularMock
    @InjectInto(property = "transformer")
    private Transformer<String, SchemaSource> mockTransformer;

    @RegularMock
    private XmlSchemasView mockView;

    @TestedObject
    private XmlSchemasPresenter presenter;

    @BeforeMethod
    public void setUp() throws Exception {
        presenter = new XmlSchemasPresenter(mockView);

        final Set<XmlSchemaDTO> dtoSet = new HashSet<XmlSchemaDTO>(
                Arrays.asList(new XmlSchemaDTO(SAMPLE_LOCATION)));
        final Field field = presenter.getClass().getDeclaredField("schemaDTOs");
        field.setAccessible(true);
        field.set(presenter, dtoSet);
    }

    @AfterMethod
    public void tearDown() {
        presenter = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testRegisterSchemaIfNullSchema() {
        presenter.registerSchema(null);
    }

    @Test
    public void testRegisterExistingSchema() {
        EasyMockUnitils.replay();

        presenter.registerSchema(new XmlSchemaDTO(SAMPLE_LOCATION));
    }

    @Test
    public void testRegisterSchema() {
        final XmlSchemaDTO dto = new XmlSchemaDTO(SAMPLE_OTHER_LOCATION);

        mockSchemaResolver.refresh();
        expectLastCall();
        mockView.addToList(same(dto));
        expectLastCall();

        EasyMockUnitils.replay();

        presenter.registerSchema(dto);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testRegisterSchemasIfNullSet() {
        presenter.registerSchemas(null);
    }

    @Test
    public void testRegisterExistingSchemas() {
        EasyMockUnitils.replay();

        presenter.registerSchemas(new HashSet<XmlSchemaDTO>(
                Arrays.asList(new XmlSchemaDTO(SAMPLE_LOCATION))));
    }

    @Test
    public void testRegisterSchemas() {
        final XmlSchemaDTO dto1 = new XmlSchemaDTO(SAMPLE_OTHER_LOCATION);
        final XmlSchemaDTO dto2 = new XmlSchemaDTO(SAMPLE_ANOTHER_LOCATION);
        final Set<XmlSchemaDTO> dtos = new LinkedHashSet<XmlSchemaDTO>();
        dtos.add(dto1);
        dtos.add(dto2);

        mockSchemaResolver.refresh();
        expectLastCall();
        mockView.addToList(same(dto1));
        expectLastCall();
        mockView.addToList(same(dto2));
        expectLastCall();

        EasyMockUnitils.replay();

        presenter.registerSchemas(dtos);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testUnregisterSchemaIfNullSchema() {
        presenter.unregisterSchema(null);
    }

    @Test
    public void testUnregisterUnexistingSchema() {
        EasyMockUnitils.replay();

        presenter.unregisterSchema(new XmlSchemaDTO(SAMPLE_OTHER_LOCATION));
    }

    @Test
    public void testUnregisterSchema() {
        final XmlSchemaDTO dto = new XmlSchemaDTO(SAMPLE_LOCATION);

        mockSchemaResolver.refresh();
        expectLastCall();
        mockView.removeFromList(same(dto));
        expectLastCall();

        EasyMockUnitils.replay();

        presenter.unregisterSchema(dto);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testPostProcessIfNullResolver() {
        presenter.postProcess(null);
    }

    @Test
    public void testPostProcess() throws Exception {
        final SchemaSource schemaSource = new SchemaSource(
                new StringInputStream("Dummy"), "dummyId");

        expect(mockTransformer.transform(eq(SAMPLE_LOCATION)))
            .andReturn(schemaSource);
        mockSchemaResolver.registerSchemaSource(same(schemaSource));
        expectLastCall();

        EasyMockUnitils.replay();

        presenter.postProcess(mockSchemaResolver);
    }

}
