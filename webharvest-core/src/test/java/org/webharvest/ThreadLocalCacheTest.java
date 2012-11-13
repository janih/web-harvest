package org.webharvest;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;

public class ThreadLocalCacheTest extends UnitilsTestNG {

    private ThreadLocalCache<String, Object> cache;

    @BeforeMethod
    public void setUp() {
        this.cache = new ThreadLocalCache<String, Object>();
    }

    @Test
    public void doNotContainNotCachedObject() {
        assertFalse("Contains not cached object", cache.contains("NOT_CACHED"));
    }

    @Test
    public void doNotLookupNotCachedObject() {
        assertNull("Lookup returns not cached object",
                cache.lookup("NOT_CACHED"));
    }

    @Test
    public void containsCachedObject() {
        final Object cachedObject = new Object();
        cache.put("mykey", cachedObject);
        assertTrue("Cache should contain previously stored object",
                cache.contains("mykey"));
    }

    @Test
    public void lookupsCachedObject() {
        final Object cachedObject = new Object();
        cache.put("mykey", cachedObject);
        assertSame("Cache should be able to lookup previously stored object",
                cachedObject, cache.lookup("mykey"));
    }

    @Test
    public void objectIsNotAvailableAfterInvalidation() {
        final Object cachedObject = new Object();
        cache.put("mykey", cachedObject);
        cache.invalidate("mykey");
        assertNull("Object should not be available after its invalidation",
                cache.lookup("mykey"));
    }

    /**
     * Multithreaded test is declared inside inner class to overcome
     * unitils's 'before method' interceptor problems.
     * (We have to use 'before class' interceptor for multithreaded tests,
     * otherwise we can get unexpected results, since the cache reference may
     * change between test method invocations)
     */
    public static class MultithreadedTest {
        private ThreadLocalCache<String, Object> cache;

        @BeforeClass
        public void setUp() {
            this.cache = new ThreadLocalCache<String, Object>();
        }

        @Test(invocationCount = 100, threadPoolSize = 10)
        public void cacheWorksLocallyPerThread() throws Exception {
            final Long cachedObject = new Long(Thread.currentThread().getId());
            cache.put("mykey", cachedObject);
            assertTrue("Cache should contain previously stored object",
                    cache.contains("mykey"));
            assertSame("Cache should be able to lookup previously stored object",
                    cachedObject, cache.lookup("mykey"));
        }
    }
}
