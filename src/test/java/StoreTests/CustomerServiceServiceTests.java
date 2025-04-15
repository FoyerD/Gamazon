package StoreTests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import Application.CustomerServiceService;
import Application.Response;
import Application.StoreService;

public class CustomerServiceServiceTests {
    private CustomerServiceService customerServiceService;

    @Before
    public void setUp() {
        customerServiceService = new CustomerServiceService();

    }

    @Test
    public void GivenNonemptyFeedback_WhenSendingFeedback_ThenReturnTrue() {
        String sessionToken = "validSessionToken";
        String storeId = "NewStore";
        String feedback = "Great service!";
        String productId = "12345";
        Response<Boolean> result = customerServiceService.addFeedback(sessionToken, storeId, productId, feedback);
        assertTrue(result.getValue());
    }

    @Test
    public void GivenEmptyFeedback_WhenSendingFeedback_ThenReturnFalse() {
        String sessionToken = "validSessionToken";
        String storeId = "NewStore";
        String feedback = "";
        String productId = "12345";
        Response<Boolean> result = customerServiceService.addFeedback(sessionToken, storeId, productId, feedback);
        assertFalse(result.getValue());
    }
}
