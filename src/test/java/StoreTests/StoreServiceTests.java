package StoreTests;

import java.security.Permission;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import Application.DTOs.AuctionDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.OfferDTO;
import Application.DTOs.PaymentDetailsDTO;
import Application.DTOs.ProductDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.ItemService;
import Application.MarketService;
import Application.ProductService;
import Application.ServiceManager;
import Application.ShoppingService;
import Application.StoreService;
import Application.UserService;
import Application.utils.Response;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.management.PermissionType;
import Domain.FacadeManager;
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
        FacadeManager facadeManager = new FacadeManager(repositoryManager, mockPaymentService);
        
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

    @Test
    public void GivenValidAuctionWithRealBid_WhenAcceptBid_ThenReturnUpdatedItem() {
        // Step 1: Setup Store, Product, and Item
        Response<StoreDTO> storeRes = storeService.addStore(tokenId, "BidStore", "Test Store");
        String storeId = storeRes.getValue().getId();

        ProductService productService = serviceManager.getProductService();
        ItemService itemService = serviceManager.getItemService();
        ShoppingService shoppingService = serviceManager.getShoppingService();
        UserService userService = serviceManager.getUserService();

        Response<ProductDTO> prodRes = productService.addProduct(tokenId, "BidProduct", List.of("cat"), List.of("desc"));
        String productId = prodRes.getValue().getId();

        Response<ItemDTO> itemRes = itemService.add(tokenId, storeId, productId, 100f, 10, "desc");
        assertFalse("Item addition should succeed", itemRes.errorOccurred());

        // Step 2: Create Auction
        String endDate = "2077-01-01 00:00";
        Response<AuctionDTO> aucRes = storeService.addAuction(tokenId, storeId, productId, endDate, 50.0);
        assertFalse("Auction creation should succeed", aucRes.errorOccurred());
        String auctionId = aucRes.getValue().getAuctionId();

        // Step 3: Create a second user and place a bid
        Response<UserDTO> guest = userService.guestEntry();
        Response<UserDTO> bidder = userService.register(
            guest.getValue().getSessionToken(),
            "Bidder1",
            "SecurePass123!",
            "bidder@example.com"
        );
        String bidderToken = bidder.getValue().getSessionToken();

        Response<Boolean> bidResponse = shoppingService.makeBid(
            auctionId,
            bidderToken,
            60.0f,
            "4111111111111111",               // dummy card number
            new java.sql.Date(System.currentTimeMillis() + 1000000),  // expiry in future
            "12234242343",                             // dummy cvv
            0,                                 // andIncrement
            "Bidder One",
            "123 Test St."
        );

        assertFalse("Bid placement should succeed", bidResponse.errorOccurred());
        assertTrue("Bid should be accepted", bidResponse.getValue());

        // Step 4: Accept the bid by the original store owner
        Response<ItemDTO> acceptRes = storeService.acceptBid(tokenId, storeId, productId, auctionId);

        assertFalse("Accepting bid should succeed", acceptRes.errorOccurred());
        assertEquals("Product ID should match", productId, acceptRes.getValue().getProductId());
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
    public void GivenValidOffer_WhenAcceptOffer_ThenReturnAcceptedOffer() {
        // Setup store, product, item
        Response<StoreDTO> storeRes = storeService.addStore(tokenId, "OfferStore", "Accept test");
        String storeId = storeRes.getValue().getId();
        ProductService productService = serviceManager.getProductService();
        ItemService itemService = serviceManager.getItemService();
        ShoppingService shoppingService = serviceManager.getShoppingService();

        Response<ProductDTO> prodRes = productService.addProduct(tokenId, "OfferProduct", List.of("c"), List.of("d"));
        String productId = prodRes.getValue().getId();
        itemService.add(tokenId, storeId, productId, 100f, 10, "desc");

        // Create a second user
        UserService userService = serviceManager.getUserService();
        Response<UserDTO> guest = userService.guestEntry();
        Response<UserDTO> buyer = userService.register(guest.getValue().getSessionToken(), "OfferUser", "StrongPass1!", "user@buy.com");
        String buyerToken = buyer.getValue().getSessionToken();

        // Create an offer
        PaymentDetailsDTO payment = new PaymentDetailsDTO(
            buyer.getValue().getId(), "4111111111111111", LocalDate.now().plusYears(1), "123", "Offer User"
        );
        Response<OfferDTO> offerResponse = shoppingService.makeOffer(buyerToken, storeId, productId, 80.0, payment);
        assertFalse("Making offer should succeed", offerResponse.errorOccurred());

        // Accept the offer
        String offerId = offerResponse.getValue().getId();
        Response<OfferDTO> accepted = storeService.acceptOffer(tokenId, offerId);
        assertFalse("Accepting offer should succeed.\nError: " + (accepted.errorOccurred() ? accepted.getErrorMessage() : ""), accepted.errorOccurred());
        assertEquals("Offer ID should match", offerId, accepted.getValue().getId());
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