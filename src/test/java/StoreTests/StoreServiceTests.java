package StoreTests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

import Application.Response;
import Application.StoreService;



public class StoreServiceTests {

    private StoreService storeService;

    @Before
    public void setUp() {
        storeService = new StoreService();

    }

    @Test
    public void GivenNewStore_WhenCreateStore_ThenReturnTrue() {
        String sessionToken = "validSessionToken";
        String storeName = "NewStore";
        Response<Boolean> result = storeService.addStore(sessionToken, storeName, "A new store");
        assertTrue(result.getValue());
    }

    @Test
    public void GivenExistingStoreName_WhenOpenStore_ThenReturnFalse() {
        String sessionToken = "validSessionToken";
        String storeName = "ExistingStore";
        storeService.addStore(sessionToken, storeName, "A new store");
        Response<Boolean> result = storeService.addStore(sessionToken, storeName,"I should not exist");
        assertFalse(result.getValue());
    }
}