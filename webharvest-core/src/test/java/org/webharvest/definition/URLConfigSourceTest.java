package org.webharvest.definition;

import static org.testng.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.webharvest.definition.ConfigSource.Location;

public class URLConfigSourceTest extends UnitilsTestNG {

     private URL url;

     private URLConfigSource source;

     @BeforeMethod
     public void setUp() throws Exception {
         url = new URL("http://sourceforge.net/");
         source = new URLConfigSource(url);
     }

     @AfterMethod
     public void testTearDown() {
         source = null;
         url = null;
     }

     @Test(expectedExceptions=IllegalArgumentException.class)
     public void testCreateWithoutFile() {
         new URLConfigSource(null);
     }

     @Test
     public void testGetLocation() {
         final Location location = source.getLocation();
         assertNotNull(location);
         assertEquals(location.toString(), url.toString());
     }

     @Test
     public void testGetReader() throws IOException {
         final Reader reader = source.getReader();
         assertNotNull(reader);
         assertTrue((reader instanceof InputStreamReader));
     }

}
