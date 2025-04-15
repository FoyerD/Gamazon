package StoreTests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

import Application.MarketService;
import Application.Response;
import Application.StoreService;


public class StoreServiceTests {

    private StoreService storeService;
    private MarketService marketService;

    @Before
    public void setUp() {
        storeService = new StoreService();
        marketService = null;

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

    @Test
    public void GivenClosedStore_WhenOpenStore_ThenReturnTrue() {
        String sessionToken = "validSessionToken";
        String storeName = "ExistingStore";
        Integer storeId = 1234;
        storeService.addStore(sessionToken, storeName, "A new store");
        marketService.closeStore(storeId);
        Response<Boolean> result = storeService.openStore(sessionToken, storeId.toString());
        assertTrue(result.getValue());
    }

    @Test
    public void GivenOpenStore_WhenOpenStore_ThenReturnFalse() {
        String sessionToken = "validSessionToken";
        String storeName = "ExistingStore";
        Integer storeId = 1234;
        storeService.addStore(sessionToken, storeName, "A new store");
        Response<Boolean> result = storeService.openStore(sessionToken, storeId.toString());
        assertFalse(result.getValue());
    }
}