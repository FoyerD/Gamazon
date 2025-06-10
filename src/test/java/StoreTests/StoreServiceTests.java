package StoreTests;

import java.security.Permission;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;




import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import Application.ItemService;
import Application.MarketService;
import Application.ProductService;
import Application.ServiceManager;
import Application.ShoppingService;
import Application.StoreService;
import Application.UserService;
import Application.DTOs.AuctionDTO;
import Application.DTOs.ConditionDTO;
import Application.DTOs.DiscountDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.OfferDTO;
import Application.DTOs.PaymentDetailsDTO;
import Application.DTOs.ProductDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.management.PermissionType;

import Domain.FacadeManager;
import Domain.ExternalServices.IExternalSupplyService;
import Infrastructure.MemoryRepoManager;


public class StoreServiceTests {
    // Existing fields
    private StoreService storeService;
    
    // Add ServiceManager as a field
    private ServiceManager serviceManager;

    private String tokenId = null;

    @Before
    public void setUp() {
        // Initialize repository manager
        MemoryRepoManager repositoryManager = new MemoryRepoManager();
            // Create mock payment service
        IExternalPaymentService mockPaymentService = mock(IExternalPaymentService.class);

        // Define mock behavior
        when(mockPaymentService.processPayment(
                anyString(), anyString(), any(Date.class), anyString(), anyString(), anyDouble()
        )).thenReturn(new Response<>(1234)); // fake transaction ID

        when(mockPaymentService.cancelPayment(anyInt())).thenReturn(new Response<>(true));
        when(mockPaymentService.handshake()).thenReturn(new Response<>(true));
        when(mockPaymentService.updatePaymentServiceURL(anyString())).thenReturn(new Response<>());
        
        // Initialize facade manager
        FacadeManager facadeManager = new FacadeManager(repositoryManager, mock(IExternalPaymentService.class), mock(IExternalSupplyService.class));

        // Initialize service manager and store as a field for use across tests
        this.serviceManager = new ServiceManager(facadeManager);
        
        // Get needed services directly from the service manager
        this.storeService = serviceManager.getStoreService();
        
        // Create a guest user
        Response<UserDTO> guestResponse = serviceManager.getUserService().guestEntry();
        assertFalse("Guest creation should succeed", guestResponse.errorOccurred());
        
        // Register a test user
        Response<UserDTO> userResponse = serviceManager.getUserService().register(
            guestResponse.getValue().getSessionToken(),
            "Member1",
            "WhyWontWork1!",
            "email@email.com"
        );
        assertFalse("User registration should succeed", userResponse.errorOccurred());
        
        // Get the token for the registered user
        this.tokenId = userResponse.getValue().getSessionToken();
    }

    @Test
    public void GivenExistingMemberAndNewStore_WhenAddStore_ThenReturnStore() {
        String storeName = "NewStore";
        Response<StoreDTO> result = storeService.addStore(this.tokenId, storeName, "A new store");
        assertTrue(result.getValue() != null);

    }

    @Test
    public void GivenExistingMemberExistingStoreName_WhenAddStore_ThenReturnError() {
        String storeName = "ExistingStore";
        storeService.addStore(this.tokenId, storeName, "A new store");
        Response<StoreDTO> result = storeService.addStore(this.tokenId, storeName,"I should not exist");
        assertTrue(result.errorOccurred());
    }

    @Test
    public void GivenExistingMemberClosedStore_WhenOpenStore_ThenReturnTrue() {
        String storeName = "ExistingStore";
        Response<StoreDTO> response = storeService.addStore(this.tokenId, storeName, "A new store");
        String storeId = response.getValue().getId();
        this.storeService.closeStore(this.tokenId, storeId);
        Response<Boolean> resultOpen = storeService.openStore(this.tokenId, storeId);
        assertTrue(resultOpen.getValue());
    }

    @Test
    public void GivenExistingMemberOpenStore_WhenOpenStore_ThenReturnError() {
        String storeName = "ExistingStore";
        Response<StoreDTO> storeRes = storeService.addStore(this.tokenId, storeName, "A new store");
        String storeId = storeRes.getValue().getId();
        Response<Boolean> result = storeService.openStore(this.tokenId, storeId);
        assertTrue(result.errorOccurred());
    }
    @Test
    public void GivenExistingMemberOpenStore_WhenCloseStore_ThenReturnTrue() {
        String storeName = "StoreToClose";
        Response<StoreDTO> storeRes = storeService.addStore(this.tokenId, storeName, "Temporary store");
        String storeId = storeRes.getValue().getId();
        Response<Boolean> result = storeService.closeStore(this.tokenId, storeId.toString());
        assertTrue(result.getValue());
    }
    @Test
    public void GivenExistingMemberClosedStore_WhenCloseStore_ThenReturnFalse() {
        String storeName = "ExistingStore";
        Response<StoreDTO> response = storeService.addStore(this.tokenId, storeName, "A new store");
        String storeId = response.getValue().getId();
        this.storeService.closeStore(this.tokenId, storeId);
        Response<Boolean> result = storeService.closeStore(this.tokenId, storeId);
        assertTrue(result.errorOccurred());
    }

    
    @Test
    public void GivenExistingMemberAndNewStore_WhenGetStoreByNameNewStore_ThenReturnStore() {
        String storeName = "NewStore";
        storeService.addStore(this.tokenId, storeName, "A new store");
        Response<StoreDTO> getResult = storeService.getStoreByName(this.tokenId, storeName);
        assertTrue(getResult.getValue().getName().equals(storeName));
    }

    @Test
    public void GivenExistingMemberAndNewStore_WhenGetStoreByNameNoneExist_ThenReturnError() {
        String storeName = "NewStore";
        storeService.addStore(this.tokenId, storeName, "A new store");
        Response<StoreDTO> getResult = storeService.getStoreByName(this.tokenId, "NoneExist");
        assertTrue(getResult.errorOccurred());
    }

    @Test
    public void GivenExistingMemberAndNewStoreAndNewProduct_WhenAddAuction_ThenReturnAuction() {
        // Create a store
        String storeName = "NewStore";
        Response<StoreDTO> addResult = storeService.addStore(this.tokenId, storeName, "A new store");
        assertFalse("Store creation should succeed", addResult.errorOccurred());
        String storeId = addResult.getValue().getId();
        
        // Create a product using ProductService from the shared service manager
        ProductService productService = serviceManager.getProductService();
        Response<ProductDTO> productResponse = productService.addProduct(
            tokenId,
            "Test Product",
            List.of("category1"),
            List.of("A test product description")
        );
        assertFalse("Product creation should succeed", productResponse.errorOccurred());
        String productId = productResponse.getValue().getId();
        
        // Add the product to the store using ItemService from the shared service manager
        ItemService itemService = serviceManager.getItemService();
        Response<ItemDTO> itemResponse = itemService.add(
            tokenId,
            storeId,
            productId,
            10.0f,
            10,
            "A new product"
        );
        assertFalse("Item addition should succeed", itemResponse.errorOccurred());

        // Create an auction for the product
        String endDate = "2077-01-01 00:00";
        Response<AuctionDTO> auctionResult = storeService.addAuction(this.tokenId, storeId, productId, endDate, 5.0);
        
        // Verify the auction was created successfully with correct details
        assertFalse("Auction creation should succeed", auctionResult.errorOccurred());
        assertEquals("Auction should be for the correct store", storeId, auctionResult.getValue().getStoreId());
        assertEquals("Auction should be for the correct product", productId, auctionResult.getValue().getProductId());
    }

    @Test
    public void GivenExistingMemberAndAndNewProduct_WhenAddAuctionForNoneexistingStore_ThenReturnError() {
        // Get services from the shared serviceManager
        ProductService productService = serviceManager.getProductService();
        
        // Create a product using ProductService
        Response<ProductDTO> productResponse = productService.addProduct(
            tokenId,
            "Test Product",
            List.of("category1"),
            List.of("A test product description")
        );
        assertFalse("Product creation should succeed", productResponse.errorOccurred());
        String productId = productResponse.getValue().getId();
        
        // Use a non-existent store ID
        String nonExistentStoreId = "whatwhat";
        
        // Try to create an auction for the item in the non-existent store
        Date endDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
        Response<AuctionDTO> auctionResult = storeService.addAuction(
            this.tokenId, 
            nonExistentStoreId, 
            productId, 
            endDate.toString(), 
            5.0
        );
        
        // Verify the auction creation fails
        assertTrue("Auction creation for non-existent store should fail", auctionResult.errorOccurred());
    }

    @Test
    public void GivenExistingMemberAndNewStore_WhenAddAuctionForNonExistingItem_ThenReturnError() {
        // Create a store
        String storeName = "NewStore";
        Response<StoreDTO> addResult = storeService.addStore(this.tokenId, storeName, "A new store");
        assertFalse("Store creation should succeed", addResult.errorOccurred());
        String storeId = addResult.getValue().getId();
        
        // Use a non-existent product ID
        String nonExistingProductId = UUID.randomUUID().toString();
        
        // Try to create an auction for a non-existent product
        Date endDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        Response<AuctionDTO> auctionResult = storeService.addAuction(
            this.tokenId, 
            storeId, 
            nonExistingProductId, 
            endDate.toString(), 
            5.0
        );
        
        // Verify the auction creation fails
        assertTrue("Auction creation for non-existent product should fail", auctionResult.errorOccurred());
    }

    @Test
    public void GivenExistingStoreWithAuctions_WhenGetAllAuctions_ThenReturnAllAuctions() {
        // Create a store through the service
        String storeName = "AuctionStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store with auctions");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        // Get services from the shared ServiceManager
        ProductService productService = serviceManager.getProductService();
        ItemService itemService = serviceManager.getItemService();
        
        // Create 3 products
        Response<ProductDTO> product1Response = productService.addProduct(
            tokenId, 
            "Product 1", 
            List.of("category1"), 
            List.of("Description for product 1")
        );
        Response<ProductDTO> product2Response = productService.addProduct(
            tokenId, 
            "Product 2", 
            List.of("category2"), 
            List.of("Description for product 2")
        );
        Response<ProductDTO> product3Response = productService.addProduct(
            tokenId, 
            "Product 3", 
            List.of("category3"), 
            List.of("Description for product 3")
        );
        
        assertFalse("Product 1 creation should succeed", product1Response.errorOccurred());
        assertFalse("Product 2 creation should succeed", product2Response.errorOccurred());
        assertFalse("Product 3 creation should succeed", product3Response.errorOccurred());
        
        String productId1 = product1Response.getValue().getId();
        String productId2 = product2Response.getValue().getId();
        String productId3 = product3Response.getValue().getId();
        
        // Add items to the store using ItemService
        Response<ItemDTO> item1Response = itemService.add(
            tokenId, 
            storeId, 
            productId1, 
            10.0f, 
            10, 
            "Product 1 in store"
        );
        Response<ItemDTO> item2Response = itemService.add(
            tokenId, 
            storeId, 
            productId2, 
            20.0f, 
            5, 
            "Product 2 in store"
        );
        Response<ItemDTO> item3Response = itemService.add(
            tokenId, 
            storeId, 
            productId3, 
            30.0f, 
            15, 
            "Product 3 in store"
        );
        
        assertFalse("Item 1 addition should succeed", item1Response.errorOccurred());
        assertFalse("Item 2 addition should succeed", item2Response.errorOccurred());
        assertFalse("Item 3 addition should succeed", item3Response.errorOccurred());

        // Add auctions for the products using StoreService
        String endDate = "2077-01-01 00:00";
        Response<AuctionDTO> auction1Response = storeService.addAuction(
            tokenId, 
            storeId, 
            productId1, 
            endDate, 
            5.0
        );
        Response<AuctionDTO> auction2Response = storeService.addAuction(
            tokenId, 
            storeId, 
            productId2, 
            endDate, 
            10.0
        );
        Response<AuctionDTO> auction3Response = storeService.addAuction(
            tokenId, 
            storeId, 
            productId3, 
            endDate, 
            15.0
        );
        
        assertFalse("Auction 1 creation should succeed", auction1Response.errorOccurred());
        assertFalse("Auction 2 creation should succeed", auction2Response.errorOccurred());
        assertFalse("Auction 3 creation should succeed", auction3Response.errorOccurred());

        // Get all auctions for the store using StoreService
        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllStoreAuctions(tokenId, storeId);
        
        // Verify results
        assertFalse("Getting auctions should succeed", auctionsResponse.errorOccurred());
        assertNotNull("Auctions list should not be null", auctionsResponse.getValue());
        assertEquals("Should have 3 auctions", 3, auctionsResponse.getValue().size());
        
        // Verify auction details if needed
        boolean foundAuction1 = false, foundAuction2 = false, foundAuction3 = false;
        for (AuctionDTO auction : auctionsResponse.getValue()) {
            if (auction.getProductId().equals(productId1) && auction.getStoreId().equals(storeId)) {
                foundAuction1 = true;
            } else if (auction.getProductId().equals(productId2) && auction.getStoreId().equals(storeId)) {
                foundAuction2 = true;
            } else if (auction.getProductId().equals(productId3) && auction.getStoreId().equals(storeId)) {
                foundAuction3 = true;
            }
        }
        
        assertTrue("Should find auction for product 1", foundAuction1);
        assertTrue("Should find auction for product 2", foundAuction2);
        assertTrue("Should find auction for product 3", foundAuction3);
    }

    @Test
    public void GivenStoreWithNoAuctions_WhenGetAllAuctions_ThenReturnEmptyList() {
        String storeName = "EmptyAuctionStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store with no auctions");
        String storeId = storeResponse.getValue().getId();

        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllStoreAuctions(this.tokenId, storeId);
        assertTrue(auctionsResponse.getValue() != null);
        assertEquals(0, auctionsResponse.getValue().size());
    }

    @Test
    public void GivenNonExistingStore_WhenGetAllAuctions_ThenReturnError() {
        String nonExistingStoreId = UUID.randomUUID().toString();

        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllStoreAuctions(this.tokenId, nonExistingStoreId);
        assertTrue(auctionsResponse.errorOccurred());
    }

    @Test
    public void GivenStoreWithAuctions_WhenGetAllProductAuctions_ThenReturnAllAuctions() {
        // Get services from the shared ServiceManager
        ProductService productService = serviceManager.getProductService();
        ItemService itemService = serviceManager.getItemService();
        
        // Create a product
        Response<ProductDTO> productResponse = productService.addProduct(
            tokenId, 
            "Shared Product", 
            List.of("category1"), 
            List.of("A product sold in multiple stores")
        );
        assertFalse("Product creation should succeed", productResponse.errorOccurred());
        String productId = productResponse.getValue().getId();
        
        // Create two stores through the service
        Response<StoreDTO> storeRes1 = storeService.addStore(
            tokenId, 
            "storestore", 
            "Store with auctions"
        );
        Response<StoreDTO> storeRes2 = storeService.addStore(
            tokenId, 
            "storestore2", 
            "Store with auctions"
        );
        
        assertFalse("First store creation should succeed", storeRes1.errorOccurred());
        assertFalse("Second store creation should succeed", storeRes2.errorOccurred());
        
        String storeId1 = storeRes1.getValue().getId();
        String storeId2 = storeRes2.getValue().getId();
        
        // Add the product to both stores using ItemService
        Response<ItemDTO> item1Response = itemService.add(
            tokenId, 
            storeId1, 
            productId, 
            10.0f, 
            10, 
            "Product in first store"
        );
        Response<ItemDTO> item2Response = itemService.add(
            tokenId, 
            storeId2, 
            productId, 
            20.0f, 
            5, 
            "Product in second store"
        );
        
        assertFalse("Adding item to first store should succeed", item1Response.errorOccurred());
        assertFalse("Adding item to second store should succeed", item2Response.errorOccurred());

        // Add auctions for the product in both stores using StoreService
        String endDate = "2077-01-01 00:00";
        Response<AuctionDTO> aucRes1 = storeService.addAuction(
            tokenId, 
            storeId1, 
            productId, 
            endDate, 
            5.0
        );
        Response<AuctionDTO> aucRes2 = storeService.addAuction(
            tokenId, 
            storeId2, 
            productId, 
            endDate, 
            10.0
        );
        
        assertFalse("First auction creation should succeed", aucRes1.errorOccurred());
        assertFalse("Second auction creation should succeed", aucRes2.errorOccurred());

        // Get all auctions for the product using StoreService
        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllProductAuctions(
            tokenId, 
            productId
        );
        
        // Verify results
        assertFalse("Getting product auctions should succeed", auctionsResponse.errorOccurred());
        assertNotNull("Auctions list should not be null", auctionsResponse.getValue());
        assertEquals("Should have 2 auctions for the product", 2, auctionsResponse.getValue().size());
        
        // Verify auction details if needed
        boolean foundAuction1 = false, foundAuction2 = false;
        for (AuctionDTO auction : auctionsResponse.getValue()) {
            if (auction.getStoreId().equals(storeId1) && auction.getProductId().equals(productId)) {
                foundAuction1 = true;
            } else if (auction.getStoreId().equals(storeId2) && auction.getProductId().equals(productId)) {
                foundAuction2 = true;
            }
        }
        
        assertTrue("Should find auction in first store", foundAuction1);
        assertTrue("Should find auction in second store", foundAuction2);
    }

    @Test
    public void GivenStoreWithNoAuctions_WhenGetAllProductAuctions_ThenReturnEmptyList() {
        String storeName = "EmptyAuctionStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store with no auctions");
        String storeId = storeResponse.getValue().getId();

        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllStoreAuctions(this.tokenId, storeId);
        assertTrue(auctionsResponse.getValue() != null);
        assertEquals(0, auctionsResponse.getValue().size());
    }

    @Test
    public void GivenNonExistingStore_WhenGetAllProductAuctions_ThenReturnError() {
        String nonExistingStoreId = UUID.randomUUID().toString();

        Response<List<AuctionDTO>> auctionsResponse = storeService.getAllStoreAuctions(this.tokenId, nonExistingStoreId);
        assertTrue(auctionsResponse.errorOccurred());
    }


    // ==================== DISCOUNT ACCEPTANCE TESTS ====================

    // ==================== DISCOUNT ACCEPTANCE TESTS ====================

    /**
     * Helper method to create a valid simple discount DTO for testing
     */
    private DiscountDTO createValidSimpleDiscountDTO() {
        DiscountDTO discountDTO = new DiscountDTO();
        discountDTO.setType(DiscountDTO.DiscountType.SIMPLE);
        discountDTO.setDiscountPercentage(0.10); // 10% discount (must be between 0 and 1)
        discountDTO.setQualifierType(DiscountDTO.QualifierType.STORE);
        discountDTO.setQualifierValue("store-qualifier");
        discountDTO.setDescription("Test discount");
        
        // Create a valid condition DTO - assuming a simple condition exists
        ConditionDTO conditionDTO = new ConditionDTO();
        conditionDTO.setType(ConditionDTO.ConditionType.TRUE); // Use a simple condition type for testing
        // Set condition properties based on available condition types
        discountDTO.setCondition(conditionDTO);
        
        return discountDTO;
    }

    /**
     * Helper method to create a valid simple discount DTO with custom percentage
     */
    private DiscountDTO createValidSimpleDiscountDTO(double percentage, String description) {
        DiscountDTO discountDTO = new DiscountDTO();
        discountDTO.setType(DiscountDTO.DiscountType.SIMPLE);
        discountDTO.setDiscountPercentage(percentage);
        discountDTO.setQualifierType(DiscountDTO.QualifierType.STORE);
        discountDTO.setQualifierValue("store-qualifier");
        discountDTO.setDescription(description);
        
        // Create a valid condition DTO
        ConditionDTO conditionDTO = new ConditionDTO();
        conditionDTO.setType(ConditionDTO.ConditionType.TRUE); // Use a simple condition type for testing

        discountDTO.setCondition(conditionDTO);
        
        return discountDTO;
    }

    @Test
    public void GivenExistingStoreWithNoDiscounts_WhenGetStoreDiscounts_ThenReturnEmptyList() {
        // Create a store
        String storeName = "NoDiscountStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store with no discounts");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        // Get discounts for the store
        Response<List<DiscountDTO>> discountsResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        
        // Verify results
        assertFalse("Getting discounts should succeed", discountsResponse.errorOccurred());
        assertNotNull("Discounts list should not be null", discountsResponse.getValue());
        assertEquals("Should have 0 discounts", 0, discountsResponse.getValue().size());
        
        // Invariant: Empty store should have empty discount list
        assertTrue("Store with no discounts should return empty list", discountsResponse.getValue().isEmpty());
    }

    @Test
    public void GivenNonExistentStore_WhenGetStoreDiscounts_ThenReturnError() {
        String nonExistentStoreId = "non-existent-store-id";
        
        Response<List<DiscountDTO>> discountsResponse = storeService.getStoreDiscounts(this.tokenId, nonExistentStoreId);
        
        // Verify error occurs
        assertTrue("Getting discounts for non-existent store should fail", discountsResponse.errorOccurred());
        
        // Invariant: Non-existent store operations should fail
        assertNotNull("Error message should be present", discountsResponse.getErrorMessage());
    }

    @Test
    public void GivenInvalidToken_WhenGetStoreDiscounts_ThenReturnError() {
        // Create a store with valid token first
        String storeName = "ValidStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Valid store");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        // Try to get discounts with invalid token
        String invalidToken = "invalid-token";
        Response<List<DiscountDTO>> discountsResponse = storeService.getStoreDiscounts(invalidToken, storeId);
        
        // Verify error occurs
        assertTrue("Getting discounts with invalid token should fail", discountsResponse.errorOccurred());
        
        // Invariant: Invalid authentication should always fail
        assertNotNull("Error message should be present", discountsResponse.getErrorMessage());
    }

    @Test
    public void GivenExistingStoreAndValidDiscount_WhenAddDiscount_ThenReturnDiscountAndStoreHasDiscount() {
        // Create a store
        String storeName = "DiscountStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store for discount testing");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        // Create a valid discount DTO
        DiscountDTO discountDTO = createValidSimpleDiscountDTO();

        // Add the discount
        Response<DiscountDTO> addDiscountResponse = storeService.addDiscount(this.tokenId, storeId, discountDTO);
        
        // Debug: Print error if it occurs
        if (addDiscountResponse.errorOccurred()) {
            assertEquals("some string", addDiscountResponse.getErrorMessage());
        }
        
        // Verify discount was added successfully
        assertFalse("Adding discount should succeed", addDiscountResponse.errorOccurred());
        assertNotNull("Returned discount should not be null", addDiscountResponse.getValue());
        assertNotNull("Discount should have an ID", addDiscountResponse.getValue().getId());

        // Verify the discount exists in the store
        Response<List<DiscountDTO>> discountsResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertFalse("Getting discounts should succeed", discountsResponse.errorOccurred());
        assertEquals("Store should have 1 discount", 1, discountsResponse.getValue().size());
        
        DiscountDTO retrievedDiscount = discountsResponse.getValue().get(0);
        assertEquals("Retrieved discount ID should match added discount", 
                    addDiscountResponse.getValue().getId(), retrievedDiscount.getId());
        
        // Invariant: Adding a discount should increase the store's discount count by 1
        // Invariant: Added discount should be retrievable from the store
        boolean discountFound = discountsResponse.getValue().stream()
                .anyMatch(d -> d.getId().equals(addDiscountResponse.getValue().getId()));
        assertTrue("Added discount should be found in store discounts", discountFound);
    }

    @Test
    public void GivenNonExistentStore_WhenAddDiscount_ThenReturnError() {
        String nonExistentStoreId = "non-existent-store-id";
        
        DiscountDTO discountDTO = createValidSimpleDiscountDTO();

        Response<DiscountDTO> addDiscountResponse = storeService.addDiscount(this.tokenId, nonExistentStoreId, discountDTO);
        
        // Verify error occurs
        assertTrue("Adding discount to non-existent store should fail", addDiscountResponse.errorOccurred());
        
        // Invariant: Operations on non-existent stores should fail
        assertNotNull("Error message should be present", addDiscountResponse.getErrorMessage());
    }

    @Test
    public void GivenInvalidToken_WhenAddDiscount_ThenReturnError() {
        // Create a store with valid token first
        String storeName = "ValidStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Valid store");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        DiscountDTO discountDTO = createValidSimpleDiscountDTO();

        // Try to add discount with invalid token
        String invalidToken = "invalid-token";
        Response<DiscountDTO> addDiscountResponse = storeService.addDiscount(invalidToken, storeId, discountDTO);
        
        // Verify error occurs
        assertTrue("Adding discount with invalid token should fail", addDiscountResponse.errorOccurred());
        
        // Verify store still has no discounts
        Response<List<DiscountDTO>> discountsResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertFalse("Getting discounts should succeed", discountsResponse.errorOccurred());
        assertEquals("Store should have 0 discounts", 0, discountsResponse.getValue().size());
        
        // Invariant: Failed operations should not modify store state
        // Invariant: Invalid authentication should not allow modifications
    }

    @Test
    public void GivenInvalidDiscountData_WhenAddDiscount_ThenReturnError() {
        // Create a store
        String storeName = "ValidationStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store for validation testing");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        // Test with invalid discount percentage (> 1.0)
        DiscountDTO invalidDiscountDTO = new DiscountDTO();
        invalidDiscountDTO.setType(DiscountDTO.DiscountType.SIMPLE);
        invalidDiscountDTO.setDiscountPercentage(1.5); // Invalid: > 1.0
        invalidDiscountDTO.setQualifierType(DiscountDTO.QualifierType.STORE);
        invalidDiscountDTO.setQualifierValue("store-qualifier");
        
        Response<DiscountDTO> addDiscountResponse = storeService.addDiscount(this.tokenId, storeId, invalidDiscountDTO);
        
        // Verify error occurs
        assertTrue("Adding invalid discount should fail", addDiscountResponse.errorOccurred());
        
        // Verify store still has no discounts
        Response<List<DiscountDTO>> discountsResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertFalse("Getting discounts should succeed", discountsResponse.errorOccurred());
        assertEquals("Store should have 0 discounts", 0, discountsResponse.getValue().size());
        
        // Invariant: Invalid data should be rejected
        // Invariant: Failed operations should not modify store state
    }

    @Test
    public void GivenStoreWithMultipleDiscounts_WhenAddDiscount_ThenReturnDiscountAndIncreaseCount() {
        // Create a store
        String storeName = "MultiDiscountStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store for multiple discounts");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        // Add first discount
        DiscountDTO discount1 = createValidSimpleDiscountDTO(0.10, "First discount");
        
        Response<DiscountDTO> addDiscount1Response = storeService.addDiscount(this.tokenId, storeId, discount1);
        assertFalse("Adding first discount should succeed", addDiscount1Response.errorOccurred());

        // Add second discount
        DiscountDTO discount2 = createValidSimpleDiscountDTO(0.15, "Second discount");
        
        Response<DiscountDTO> addDiscount2Response = storeService.addDiscount(this.tokenId, storeId, discount2);
        assertFalse("Adding second discount should succeed", addDiscount2Response.errorOccurred());

        // Verify both discounts exist in the store
        Response<List<DiscountDTO>> discountsResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertFalse("Getting discounts should succeed", discountsResponse.errorOccurred());
        assertEquals("Store should have 2 discounts", 2, discountsResponse.getValue().size());
        
        // Verify both discounts are present
        boolean discount1Found = discountsResponse.getValue().stream()
                .anyMatch(d -> d.getId().equals(addDiscount1Response.getValue().getId()));
        boolean discount2Found = discountsResponse.getValue().stream()
                .anyMatch(d -> d.getId().equals(addDiscount2Response.getValue().getId()));
        
        assertTrue("First discount should be found", discount1Found);
        assertTrue("Second discount should be found", discount2Found);
        
        // Invariant: Each discount addition should increase total count by 1
        // Invariant: All added discounts should be retrievable
    }

    @Test
    public void GivenStoreWithExistingDiscount_WhenRemoveDiscount_ThenReturnTrueAndDiscountRemoved() {
        // Create a store
        String storeName = "RemoveDiscountStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store for discount removal");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        // Add a discount
        DiscountDTO discountDTO = createValidSimpleDiscountDTO(0.25, "Discount to be removed");
        
        Response<DiscountDTO> addDiscountResponse = storeService.addDiscount(this.tokenId, storeId, discountDTO);
        assertFalse("Adding discount should succeed", addDiscountResponse.errorOccurred());
        String discountId = addDiscountResponse.getValue().getId();

        // Verify discount exists before removal
        Response<List<DiscountDTO>> beforeRemovalResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertFalse("Getting discounts should succeed", beforeRemovalResponse.errorOccurred());
        assertEquals("Store should have 1 discount before removal", 1, beforeRemovalResponse.getValue().size());

        // Remove the discount
        Response<Boolean> removeDiscountResponse = storeService.removeDiscount(this.tokenId, storeId, discountId);
        
        // Verify removal was successful
        assertFalse("Removing discount should succeed", removeDiscountResponse.errorOccurred());
        assertTrue("Remove operation should return true", removeDiscountResponse.getValue());

        // Verify discount no longer exists in the store
        Response<List<DiscountDTO>> afterRemovalResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertFalse("Getting discounts should succeed", afterRemovalResponse.errorOccurred());
        assertEquals("Store should have 0 discounts after removal", 0, afterRemovalResponse.getValue().size());
        
        // Verify the specific discount is not found
        boolean discountFound = afterRemovalResponse.getValue().stream()
                .anyMatch(d -> d.getId().equals(discountId));
        assertFalse("Removed discount should not be found", discountFound);
        
        // Invariant: Successful removal should decrease discount count by 1
        // Invariant: Removed discount should not be retrievable
    }

    @Test
    public void GivenStoreWithMultipleDiscounts_WhenRemoveOneDiscount_ThenReturnTrueAndOnlySpecificDiscountRemoved() {
        // Create a store
        String storeName = "MultiRemoveStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store for selective removal");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        // Add multiple discounts
        DiscountDTO discount1 = createValidSimpleDiscountDTO(0.10, "First discount");
        DiscountDTO discount2 = createValidSimpleDiscountDTO(0.15, "Second discount");
        DiscountDTO discount3 = createValidSimpleDiscountDTO(0.20, "Third discount");
        
        Response<DiscountDTO> add1Response = storeService.addDiscount(this.tokenId, storeId, discount1);
        Response<DiscountDTO> add2Response = storeService.addDiscount(this.tokenId, storeId, discount2);
        Response<DiscountDTO> add3Response = storeService.addDiscount(this.tokenId, storeId, discount3);
        
        assertFalse("Adding first discount should succeed", add1Response.errorOccurred());
        assertFalse("Adding second discount should succeed", add2Response.errorOccurred());
        assertFalse("Adding third discount should succeed", add3Response.errorOccurred());

        String discountToRemoveId = add2Response.getValue().getId();

        // Verify initial state
        Response<List<DiscountDTO>> beforeRemovalResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertEquals("Store should have 3 discounts before removal", 3, beforeRemovalResponse.getValue().size());

        // Remove the middle discount
        Response<Boolean> removeResponse = storeService.removeDiscount(this.tokenId, storeId, discountToRemoveId);
        assertFalse("Removing discount should succeed", removeResponse.errorOccurred());
        assertTrue("Remove operation should return true", removeResponse.getValue());

        // Verify final state
        Response<List<DiscountDTO>> afterRemovalResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertFalse("Getting discounts should succeed", afterRemovalResponse.errorOccurred());
        assertEquals("Store should have 2 discounts after removal", 2, afterRemovalResponse.getValue().size());
        
        // Verify only the specified discount was removed
        boolean discount1Found = afterRemovalResponse.getValue().stream()
                .anyMatch(d -> d.getId().equals(add1Response.getValue().getId()));
        boolean discount2Found = afterRemovalResponse.getValue().stream()
                .anyMatch(d -> d.getId().equals(add2Response.getValue().getId()));
        boolean discount3Found = afterRemovalResponse.getValue().stream()
                .anyMatch(d -> d.getId().equals(add3Response.getValue().getId()));
        
        assertTrue("First discount should still exist", discount1Found);
        assertFalse("Second discount should be removed", discount2Found);
        assertTrue("Third discount should still exist", discount3Found);
        
        // Invariant: Only the specified discount should be removed
        // Invariant: Other discounts should remain unchanged
    }

    @Test
    public void GivenNonExistentDiscount_WhenRemoveDiscount_ThenReturnErrorOrFalse() {
        // Create a store
        String storeName = "ErrorRemoveStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store for error testing");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        String nonExistentDiscountId = "non-existent-discount-id";
        
        Response<Boolean> removeResponse = storeService.removeDiscount(this.tokenId, storeId, nonExistentDiscountId);
        
        // Verify error occurs or returns false
        assertTrue("Removing non-existent discount should fail or return false", 
                removeResponse.errorOccurred() || (removeResponse.getValue() != null && !removeResponse.getValue()));
        
        // Invariant: Operations on non-existent resources should fail or return false
    }

    @Test
    public void GivenNonExistentStore_WhenRemoveDiscount_ThenReturnError() {
        String nonExistentStoreId = "non-existent-store-id";
        String discountId = "some-discount-id";
        
        Response<Boolean> removeResponse = storeService.removeDiscount(this.tokenId, nonExistentStoreId, discountId);
        
        // Verify error occurs
        assertTrue("Removing discount from non-existent store should fail", removeResponse.errorOccurred());
        
        // Invariant: Operations on non-existent stores should fail
        assertNotNull("Error message should be present", removeResponse.getErrorMessage());
    }

    @Test
    public void GivenInvalidToken_WhenRemoveDiscount_ThenReturnError() {
        // Create a store and add a discount with valid token
        String storeName = "ValidTokenStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store for token testing");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        DiscountDTO discountDTO = createValidSimpleDiscountDTO(0.30, "Protected discount");
        
        Response<DiscountDTO> addResponse = storeService.addDiscount(this.tokenId, storeId, discountDTO);
        if (addResponse.errorOccurred()) {
            assertEquals("some string", addResponse.getErrorMessage());
        }

        assertFalse("Adding discount should succeed", addResponse.errorOccurred());
        String discountId = addResponse.getValue().getId();

        // Try to remove with invalid token
        String invalidToken = "invalid-token";
        Response<Boolean> removeResponse = storeService.removeDiscount(invalidToken, storeId, discountId);
        
        // Verify error occurs
        assertTrue("Removing discount with invalid token should fail", removeResponse.errorOccurred());

        // Verify discount still exists
        Response<List<DiscountDTO>> discountsResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertFalse("Getting discounts should succeed", discountsResponse.errorOccurred());
        assertEquals("Store should still have 1 discount", 1, discountsResponse.getValue().size());
        
        boolean discountStillExists = discountsResponse.getValue().stream()
                .anyMatch(d -> d.getId().equals(discountId));
        assertTrue("Discount should still exist after failed removal", discountStillExists);
        
        // Invariant: Failed operations should not modify store state
        // Invariant: Invalid authentication should not allow modifications
    }

    @Test
    public void GivenStoreWithDiscounts_WhenAddAndRemoveDiscountsSequentially_ThenMaintainConsistentState() {
        // Integration test to verify consistent state management
        String storeName = "ConsistencyStore";
        Response<StoreDTO> storeResponse = storeService.addStore(this.tokenId, storeName, "Store for consistency testing");
        assertFalse("Store creation should succeed", storeResponse.errorOccurred());
        String storeId = storeResponse.getValue().getId();

        // Initial state: 0 discounts
        Response<List<DiscountDTO>> initialResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertEquals("Initial discount count should be 0", 0, initialResponse.getValue().size());

        // Add 3 discounts
        DiscountDTO[] discounts = new DiscountDTO[3];
        String[] discountIds = new String[3];
        
        for (int i = 0; i < 3; i++) {
            discounts[i] = createValidSimpleDiscountDTO(0.05 * (i + 1), "Discount " + (i + 1));
            
            Response<DiscountDTO> addResponse = storeService.addDiscount(this.tokenId, storeId, discounts[i]);
            assertFalse("Adding discount " + (i + 1) + " should succeed", addResponse.errorOccurred());
            discountIds[i] = addResponse.getValue().getId();
        }

        // Verify 3 discounts exist
        Response<List<DiscountDTO>> afterAddResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertEquals("Should have 3 discounts after adding", 3, afterAddResponse.getValue().size());

        // Remove 2 discounts
        Response<Boolean> remove1 = storeService.removeDiscount(this.tokenId, storeId, discountIds[0]);
        Response<Boolean> remove2 = storeService.removeDiscount(this.tokenId, storeId, discountIds[2]);
        
        assertFalse("First removal should succeed", remove1.errorOccurred());
        assertFalse("Second removal should succeed", remove2.errorOccurred());
        assertTrue("First removal should return true", remove1.getValue());
        assertTrue("Second removal should return true", remove2.getValue());

        // Verify final state: 1 discount remaining
        Response<List<DiscountDTO>> finalResponse = storeService.getStoreDiscounts(this.tokenId, storeId);
        assertEquals("Should have 1 discount after removals", 1, finalResponse.getValue().size());
        
        // Verify the correct discount remains
        DiscountDTO remainingDiscount = finalResponse.getValue().get(0);
        assertEquals("Remaining discount should be the middle one", discountIds[1], remainingDiscount.getId());
        
        // Invariant: Sequential operations should maintain consistent state
        // Invariant: Final state should reflect all successful operations
    }


    @Test
    public void GivenInvalidAuction_WhenAcceptBid_ThenReturnError() {
        Response<StoreDTO> storeRes = storeService.addStore(tokenId, "InvalidBidStore", "Test");
        String storeId = storeRes.getValue().getId();
        String fakeAuctionId = "invalid-id";
        String fakeProductId = UUID.randomUUID().toString();

        Response<ItemDTO> result = storeService.acceptBid(tokenId, storeId, fakeProductId, fakeAuctionId);
        assertTrue(result.errorOccurred());
    }

    @Test
    public void GivenOpenStore_WhenCloseStoreNotPermanent_ThenReturnTrue() {
        Response<StoreDTO> storeRes = storeService.addStore(tokenId, "TempStore", "For testing");
        String storeId = storeRes.getValue().getId();

        Response<Boolean> closeRes = storeService.closeStoreNotPermanent(tokenId, storeId);
        assertFalse(closeRes.errorOccurred());
        assertTrue(closeRes.getValue());
    }



    @Test
    public void GivenValidOffer_WhenRejectOffer_ThenReturnRejectedOffer() {
        // Setup store, product, item
        Response<StoreDTO> storeRes = storeService.addStore(tokenId, "RejectStore", "Reject test");
        String storeId = storeRes.getValue().getId();
        ProductService productService = serviceManager.getProductService();
        ItemService itemService = serviceManager.getItemService();
        ShoppingService shoppingService = serviceManager.getShoppingService();

        Response<ProductDTO> prodRes = productService.addProduct(tokenId, "RejectProduct", List.of("c"), List.of("d"));
        String productId = prodRes.getValue().getId();
        itemService.add(tokenId, storeId, productId, 120f, 5, "desc");

        // Create a second user
        UserService userService = serviceManager.getUserService();
        Response<UserDTO> guest = userService.guestEntry();
        Response<UserDTO> buyer = userService.register(
            guest.getValue().getSessionToken(),
            "RejectUser",
            "AnotherPass1!",
            "reject@buy.com"
        );
        String buyerToken = buyer.getValue().getSessionToken();

        // Create an offer
        PaymentDetailsDTO payment = new PaymentDetailsDTO(
            buyer.getValue().getId(),
            "4111111111111111",
            LocalDate.now().plusYears(1),
            "321",
            "Reject User"
        );
        Response<OfferDTO> offerResponse = shoppingService.makeOffer(buyerToken, storeId, productId, 90.0, payment);
        assertFalse("Making offer should succeed", offerResponse.errorOccurred());

        // Reject the offer
        String offerId = offerResponse.getValue().getId();
        Response<OfferDTO> rejected = storeService.rejectOffer(tokenId, offerId);
        assertFalse("Rejecting offer should succeed.\nError: " + (rejected.errorOccurred() ? rejected.getErrorMessage() : ""), rejected.errorOccurred());
        assertEquals("Offer ID should match", offerId, rejected.getValue().getId());
    }

    @Test
    public void GivenTwoManagers_WhenOnlyOneApprovesOffer_ThenOfferNotAccepted() {
        // Setup store and services
        Response<StoreDTO> storeRes = storeService.addStore(tokenId, "DualManagerStore", "Approval Test");
        String storeId = storeRes.getValue().getId();

        ProductService productService = serviceManager.getProductService();
        ItemService itemService = serviceManager.getItemService();
        ShoppingService shoppingService = serviceManager.getShoppingService();
        UserService userService = serviceManager.getUserService();
        MarketService marketService = serviceManager.getMarketService();

        // Add product and item
        Response<ProductDTO> prodRes = productService.addProduct(tokenId, "OfferProduct", List.of("cat"), List.of("desc"));
        String productId = prodRes.getValue().getId();
        itemService.add(tokenId, storeId, productId, 150f, 3, "desc");
        

        // Add second manager
        Response<UserDTO> guest = userService.guestEntry();
        Response<UserDTO> secondManager = userService.register(guest.getValue().getSessionToken(), "Manager2", "Passw0rd!", "m2@store.com");
        String secondToken = secondManager.getValue().getSessionToken();
        marketService.appointStoreManager(tokenId, secondManager.getValue().getId(), storeId);
        marketService.changeManagerPermissions(tokenId, secondManager.getValue().getId(), storeId, List.of(PermissionType.OVERSEE_OFFERS));

        // Create buyer and offer
        Response<UserDTO> guestBuyer = userService.guestEntry();
        Response<UserDTO> buyer = userService.register(guestBuyer.getValue().getSessionToken(), "Buyer1", "Pass1!word", "buyer@store.com");
        String buyerToken = buyer.getValue().getSessionToken();
        PaymentDetailsDTO payment = new PaymentDetailsDTO(buyer.getValue().getId(), "4111111111111111", LocalDate.now().plusYears(1), "123", "Buyer");
        Response<OfferDTO> offerResponse = shoppingService.makeOffer(buyerToken, storeId, productId, 100.0, payment);
        String offerId = offerResponse.getValue().getId();

        // First manager approves
        Response<OfferDTO> partial = storeService.acceptOffer(tokenId, offerId);
        assertFalse(partial.errorOccurred());
        assertNotNull(partial.getValue());
        assertFalse("Offer should not be accepted yet", partial.getValue().isAccepted());
    }

    @Test
    public void GivenTwoManagers_WhenBothApproveOffer_ThenOfferIsAccepted() {
        // Setup store and services
        Response<StoreDTO> storeRes = storeService.addStore(tokenId, "DualManagerStoreAccepted", "Approval Test");
        String storeId = storeRes.getValue().getId();

        ProductService productService = serviceManager.getProductService();
        ItemService itemService = serviceManager.getItemService();
        ShoppingService shoppingService = serviceManager.getShoppingService();
        UserService userService = serviceManager.getUserService();
        MarketService marketService = serviceManager.getMarketService();

        // Add product and item
        Response<ProductDTO> prodRes = productService.addProduct(tokenId, "OfferProduct", List.of("cat"), List.of("desc"));
        String productId = prodRes.getValue().getId();
        itemService.add(tokenId, storeId, productId, 150f, 3, "desc");

        // Add second manager
        Response<UserDTO> guest = userService.guestEntry();
        Response<UserDTO> secondManager = userService.register(
            guest.getValue().getSessionToken(), "Manager2", "Passw0rd!", "m2@store.com");
        String secondToken = secondManager.getValue().getSessionToken();

        marketService.appointStoreManager(tokenId, secondManager.getValue().getId(), storeId);
        marketService.changeManagerPermissions(tokenId, secondManager.getValue().getId(), storeId,
            List.of(PermissionType.OVERSEE_OFFERS));

        // Create buyer and offer
        Response<UserDTO> guestBuyer = userService.guestEntry();
        Response<UserDTO> buyer = userService.register(
            guestBuyer.getValue().getSessionToken(), "Buyer1", "Pass1!word", "buyer@store.com");
        String buyerToken = buyer.getValue().getSessionToken();

        PaymentDetailsDTO payment = new PaymentDetailsDTO(
            buyer.getValue().getId(), "4111111111111111", LocalDate.now().plusYears(1), "123", "Buyer");
        Response<OfferDTO> offerResponse = shoppingService.makeOffer(buyerToken, storeId, productId, 100.0, payment);
        String offerId = offerResponse.getValue().getId();

        // First manager approves
        Response<OfferDTO> partial = storeService.acceptOffer(tokenId, offerId);
        assertFalse(partial.errorOccurred());
        assertNotNull(partial.getValue());
        assertFalse("Offer should not be accepted yet", partial.getValue().isAccepted());

        // Second manager approves
        Response<OfferDTO> full = storeService.acceptOffer(secondToken, offerId);
        assertFalse(full.errorOccurred());
        assertTrue("Offer should now be accepted", full.getValue().isAccepted());
    }

    @Test
    public void GivenValidOffer_WhenRejectOffer_ThenOfferIsRejected() {
        Response<StoreDTO> storeRes = storeService.addStore(tokenId, "RejectableStore", "Rejection test");
        String storeId = storeRes.getValue().getId();
        ProductService productService = serviceManager.getProductService();
        ItemService itemService = serviceManager.getItemService();
        ShoppingService shoppingService = serviceManager.getShoppingService();

        Response<ProductDTO> prodRes = productService.addProduct(tokenId, "RejectProduct", List.of("cat"), List.of("desc"));
        String productId = prodRes.getValue().getId();
        itemService.add(tokenId, storeId, productId, 200f, 5, "desc");

        UserService userService = serviceManager.getUserService();
        Response<UserDTO> guest = userService.guestEntry();
        Response<UserDTO> buyer = userService.register(guest.getValue().getSessionToken(), "RejectUser", "Pass1234!", "reject@store.com");
        String buyerToken = buyer.getValue().getSessionToken();

        PaymentDetailsDTO payment = new PaymentDetailsDTO(buyer.getValue().getId(), "4111111111111111", LocalDate.now().plusYears(1), "123", "Reject User");
        Response<OfferDTO> offerResponse = shoppingService.makeOffer(buyerToken, storeId, productId, 180.0, payment);
        String offerId = offerResponse.getValue().getId();

        Response<OfferDTO> result = storeService.rejectOffer(tokenId, offerId);
        assertFalse(result.errorOccurred());
        assertEquals(offerId, result.getValue().getId());
        assertFalse("Offer should not be accepted", result.getValue().isAccepted());
    }

    @Test
    public void GivenInvalidToken_WhenRejectOffer_ThenErrorReturned() {
        Response<OfferDTO> result = storeService.rejectOffer("invalid_token", "some_offer_id");
        assertTrue(result.errorOccurred());
    }
    
    @Test
    public void GivenValidOffer_WhenCounterOfferByManager_ThenCounterOfferReturned() {
        Response<StoreDTO> storeRes = storeService.addStore(tokenId, "CounterStore", "Counter test");
        String storeId = storeRes.getValue().getId();
        ProductService productService = serviceManager.getProductService();
        ItemService itemService = serviceManager.getItemService();
        ShoppingService shoppingService = serviceManager.getShoppingService();

        Response<ProductDTO> prodRes = productService.addProduct(tokenId, "CounterProduct", List.of("cat"), List.of("desc"));
        String productId = prodRes.getValue().getId();
        itemService.add(tokenId, storeId, productId, 250f, 2, "desc");

        UserService userService = serviceManager.getUserService();
        Response<UserDTO> guest = userService.guestEntry();
        Response<UserDTO> buyer = userService.register(guest.getValue().getSessionToken(), "CounterBuyer", "Pass5678!", "counter@store.com");
        String buyerToken = buyer.getValue().getSessionToken();

        PaymentDetailsDTO payment = new PaymentDetailsDTO(buyer.getValue().getId(), "4111111111111111", LocalDate.now().plusYears(1), "321", "Counter Buyer");
        Response<OfferDTO> offerResponse = shoppingService.makeOffer(buyerToken, storeId, productId, 190.0, payment);
        String offerId = offerResponse.getValue().getId();

        Response<OfferDTO> result = storeService.counterOffer(tokenId, offerId, 220.0);
        assertFalse(result.errorOccurred());
        assertEquals(offerId, result.getValue().getId());
        assertTrue("Offer should be a counter offer", result.getValue().isCounterOffer());
    }

    @Test
    public void GivenInvalidToken_WhenCounterOffer_ThenErrorReturned() {
        Response<OfferDTO> result = storeService.counterOffer("invalid_token", "some_offer_id", 100.0);
        assertTrue(result.errorOccurred());
    }

}