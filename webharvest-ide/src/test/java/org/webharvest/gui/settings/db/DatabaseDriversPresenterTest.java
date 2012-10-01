package org.webharvest.gui.settings.db;

import static org.unitils.mock.ArgumentMatchers.same;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;

public class DatabaseDriversPresenterTest extends UnitilsTestNG {

    private Mock<DatabaseDriversView> mockView;

    private DatabaseDriverDTO dto;

    /**
     * class under the test
     */
    private DatabaseDriversPresenter presenter;

    @BeforeMethod
    public void setUp() {
        this.dto = new DatabaseDriverDTO("/tmp/driver.jar");
        this.presenter = new DatabaseDriversPresenter(mockView.getMock());
    }

    @AfterMethod
    public void tearDown() {
        this.dto = null;
        this.presenter = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullViewNotAllowed() {
        new DatabaseDriversPresenter(null);
    }

    @Test
    public void isRegisteredDriverAddedToList() {
        presenter.registerDriver(dto);
        mockView.assertInvoked().addToList(same(dto));
        mockView.assertNotInvoked().addToList(null); // only one invocation
    }

    @Test
    public void isUnregisteredDriverRemovedFromList() {
        presenter.unregisterDriver(dto);
        mockView.assertInvoked().removeFromList(same(dto));
        mockView.assertNotInvoked().removeFromList(null); // only one invocation
    }
}
