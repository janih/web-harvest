package org.webharvest.definition;

import static org.testng.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;

public class BufferConfigSourceTest extends UnitilsTestNG {

    private static final String CONTENT = "blah, blah";

    private BufferConfigSource source;

    @BeforeMethod
    public void setUp() throws Exception {
        source = new BufferConfigSource(CONTENT);
    }

    @AfterMethod
    public void testTearDown() {
        source = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCreateWithoutFile() {
        new BufferConfigSource(null);
    }

    @Test
    public void testGetLocation() {
        assertNull(source.getLocation());
    }

    @Test
    public void testGetReader() throws IOException {
        final Reader reader = source.getReader();
        assertNotNull(reader);
        assertTrue((reader instanceof StringReader));
    }

}
