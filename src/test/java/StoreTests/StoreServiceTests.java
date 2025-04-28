package StoreTests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

import Application.MarketService;
import Application.Response;
import Application.StoreDTO;
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
    public void GivenNewStore_WhenCreateStore_ThenReturnStore() {
        String sessionToken = "validSessionToken";
        String storeName = "NewStore";
        Response<StoreDTO> result = storeService.addStore(sessionToken, storeName, "A new store");
        assertTrue(result.getValue() != null);
    }

    @Test
    public void GivenExistingStoreName_WhenOpenStore_ThenReturnNull() {
        String sessionToken = "validSessionToken";
        String storeName = "ExistingStore";
        storeService.addStore(sessionToken, storeName, "A new store");
        Response<StoreDTO> result = storeService.addStore(sessionToken, storeName,"I should not exist");
        assertFalse(result.getValue() == null);
    }

    @Test
    public void GivenClosedStore_WhenOpenStore_ThenReturnTrue() {
        String sessionToken = "validSessionToken";
        String storeName = "ExistingStore";
        Integer storeId = 1234;
        storeService.addStore(sessionToken, storeName, "A new store");
        marketService.closeStore(sessionToken, storeId.toString());
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
    @Test
    public void GivenOpenStore_WhenCloseStore_ThenReturnTrue() {
        String sessionToken = "validSessionToken";
        String storeName = "StoreToClose";
        Integer storeId = 1235;
        storeService.addStore(sessionToken, storeName, "Temporary store");
        Response<Boolean> result = storeService.closeStore(sessionToken, storeId.toString());
        assertTrue("Expected store to close successfully", result.getValue());
    }
    @Test
    public void GivenClosedStore_WhenCloseStore_ThenReturnFalse() {
        String sessionToken = "validSessionToken";
        String storeName = "AlreadyClosedStore";
        Integer storeId = 1236;
        storeService.addStore(sessionToken, storeName, "Store to test double close");
        storeService.closeStore(sessionToken, storeId.toString());
        Response<Boolean> result = storeService.closeStore(sessionToken, storeId.toString());
        assertFalse("Expected store to not be closed again", result.getValue());
    }

    
}