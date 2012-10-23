package org.webharvest;

import static org.testng.AssertJUnit.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;

public class LockedRegistryTest  extends UnitilsTestNG  {

    private static final String KEY = "key";

    private static final String VALUE = "value";

    private Registry<String, String> delegate;

    private LockedRegistry<String, String> decorator;

    @BeforeClass
    public void setUp() {
        delegate = new RegistryMock();
        decorator = new LockedRegistry<String, String>(delegate);
    }

    @AfterClass
    public void tearDown() {
        decorator = null;
        delegate = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testConstructorWithNullDelegate() {
         new LockedRegistry<String, String>(null);
    }

    @Test(threadPoolSize = 100, invocationCount = 100)
    public void test() throws Exception {
        decorator.bind(KEY, VALUE);
        final String value = decorator.lookup(KEY);
        assertNotNull("Null value", value);
        assertSame("Unexpected value", VALUE, value);
        decorator.unbind(KEY);
        final Set<String> entries = decorator.listBound();
        assertNotNull("Null entries", entries);
        assertTrue("Found entries", entries.isEmpty());
    }

    private final class RegistryMock implements Registry<String, String> {

        private final Map<String, String> storage =
            new HashMap<String, String>();

        @Override
        public String lookup(final String name) {
            return storage.get(name);
        }

        @Override
        public void bind(final String name, final String value)
                throws AlreadyBoundException {
            storage.put(name, value);
        }

        @Override
        public void unbind(final String name) {
            storage.remove(name);
        }

        @Override
        public Set<String> listBound() {
            return new HashSet<String>(storage.values());
        }

    }

}
