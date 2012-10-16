package org.webharvest.ioc;

import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.easymock.EasyMockUnitils;

import com.google.inject.OutOfScopeException;

public class AttributeHolderScopeTest {

    private AttributeHolderScope<AttributeHolder> scope;

    private AttributeHolder mockHolder;

    @BeforeMethod
    public void setUp() {
        this.scope = new AttributeHolderScope<AttributeHolder>();
        this.mockHolder = EasyMockUnitils.createRegularMock(
                AttributeHolder.class);
    }

    @AfterMethod
    public void tearDown() {
        this.scope = null;
        this.mockHolder = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotEnterWithNullHolder() {
        scope.enter(null);
    }

    @Test(expectedExceptions = OutOfScopeException.class)
    public void cannotExitWhenNotInScope() {
        scope.exit();
    }

    @Test(expectedExceptions = OutOfScopeException.class)
    public void cannotGetHolderWhenNotInScope() {
        scope.get();
    }

    @Test
    public void enterScopeOnce() {
        scope.enter(mockHolder);
        assertSame("Unexpected scope's attribute holder",
                mockHolder, scope.get());
        scope.exit();
    }

    @Test
    public void supportsNestedScopes() {
        final AttributeHolder mockHolder2 = EasyMockUnitils.createRegularMock(
                AttributeHolder.class);

        EasyMockUnitils.replay();

        scope.enter(mockHolder);
        assertSame("Unexpected scope's attribute holder",
                mockHolder, scope.get());
        scope.enter(mockHolder2);
        assertSame("Unexpected scope's attribute holder",
                mockHolder2, scope.get());
        scope.exit();
        assertSame("Unexpected scope's attribute holder",
                mockHolder, scope.get());
        scope.exit();
    }
}
