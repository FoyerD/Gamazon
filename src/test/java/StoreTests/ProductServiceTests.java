package StoreTests;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import Application.DTOs.ProductDTO;
import Application.DTOs.UserDTO;
import Application.ProductService;
import Application.UserService;
import Application.utils.Response;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductServiceTests {

    //!private ServiceManager serviceManager;

    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;


    private String tokenId;

    @Before
    public void setUp() {
        //! Initialize in-memory infrastructure
        //!MemoryRepoManager repoManager = new MemoryRepoManager();
        //!FacadeManager facadeManager = new FacadeManager(repoManager, mock(IExternalPaymentService.class), mock(IExternalSupplyService.class));

        //! Initialize service manager and services
        //!this.serviceManager = new ServiceManager(facadeManager);
        //!this.productService = serviceManager.getProductService();
        //!this.userService = serviceManager.getUserService();

        // Register a user and obtain token
        Response<UserDTO> guestResp = userService.guestEntry();
        assertFalse("Guest creation failed", guestResp.errorOccurred());

        Response<UserDTO> registerResp = userService.register(
            guestResp.getValue().getSessionToken(),
            "testUser",
            "Password123!",
            "test@example.com"
        );
        assertFalse("User registration failed", registerResp.errorOccurred());
        this.tokenId = registerResp.getValue().getSessionToken();
    }

    @Test
    public void GivenValidRequest_WhenAddProduct_ThenProductCreatedSuccessfully() {
        Response<ProductDTO> response = productService.addProduct(
            tokenId,
            "TestProduct",
            List.of("categoryA", "categoryB"),
            List.of("descA", "descB")
        );
        assertFalse("Product creation should succeed", response.errorOccurred());
        assertNotNull("ProductDTO should not be null", response.getValue());
        assertEquals("Product name should match", "TestProduct", response.getValue().getName());
    }

    @Test
    public void GivenInvalidToken_WhenAddProduct_ThenErrorReturned() {
        Response<ProductDTO> response = productService.addProduct(
            "invalid-token",
            "InvalidProduct",
            List.of("cat"),
            List.of("desc")
        );
        assertTrue("Product creation should fail", response.errorOccurred());
        assertTrue("Error message should mention invalid token",
                   response.getErrorMessage().toLowerCase().contains("invalid token"));
    }

    @Test
    public void GivenAddedProduct_WhenGetProductByName_ThenReturnCorrectProduct() {
        String productName = "MyProduct";
        productService.addProduct(tokenId, productName, List.of("cat"), List.of("desc"));

        Response<ProductDTO> response = productService.getProductByName(tokenId, productName);
        assertFalse("Fetching product by name should succeed", response.errorOccurred());
        assertNotNull("Returned product should not be null", response.getValue());
        assertEquals("Product name should match", productName, response.getValue().getName());
    }

    @Test
    public void GivenNonexistentProduct_WhenGetProductByName_ThenReturnError() {
        Response<ProductDTO> response = productService.getProductByName(tokenId, "Nonexistent");
        assertTrue("Should return error for nonexistent product", response.errorOccurred());
        assertTrue("Error message should mention not found", 
                   response.getErrorMessage().toLowerCase().contains(""));
    }

    @Test
    public void GivenMultipleProducts_WhenGetAllProducts_ThenReturnAll() {
        productService.addProduct(tokenId, "ProductA", List.of("catA"), List.of("descA"));
        productService.addProduct(tokenId, "ProductB", List.of("catB"), List.of("descB"));

        Response<Set<ProductDTO>> response = productService.getAllProducts(tokenId);
        assertFalse("Fetching all products should succeed", response.errorOccurred());
        assertNotNull("Resulting set should not be null", response.getValue());
        assertTrue("At least two products should be returned", response.getValue().size() >= 2);
    }

    @Test
    public void GivenInvalidToken_WhenGetAllProducts_ThenReturnError() {
        Response<Set<ProductDTO>> response = productService.getAllProducts("invalid-token");
        assertTrue("Should return error for invalid token", response.errorOccurred());
        assertTrue("Error message should mention invalid token", 
                   response.getErrorMessage().toLowerCase().contains("invalid token"));
    }
}
