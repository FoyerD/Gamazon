package StoreTests;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import Application.ItemService;
import Application.ProductService;
import Application.ServiceManager;
import Application.StoreService;
import Application.TokenService;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IStoreRepository;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;
import Domain.User.Member;
import Domain.User.User;
import Domain.management.IPermissionRepository;
import Domain.management.PermissionManager;
import Infrastructure.MemoryRepoManager;
import Infrastructure.NotificationService;
import Infrastructure.Repositories.MemoryAuctionRepository;
import Infrastructure.Repositories.MemoryFeedbackRepository;
import Infrastructure.Repositories.MemoryItemRepository;
import Infrastructure.Repositories.MemoryPermissionRepository;
import Infrastructure.Repositories.MemoryStoreRepository;
import Infrastructure.Repositories.MemoryUserRepository;
import Domain.FacadeManager;
import Domain.Pair;
import Domain.Store.Item;
import Application.DTOs.AuctionDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.ProductDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;


public class StoreServiceTests {

    private StoreService storeService;
    private StoreFacade storeFacade;
    private IStoreRepository storeRepository;
    private IAuctionRepository auctionRepository;
    private IItemRepository itemRepository;
    private IFeedbackRepository feedbackRepository;
    private IUserRepository userRepository;
    private TokenService tokenService;
    private PermissionManager permissionManager;
    private IPermissionRepository permissionRepository;
    UUID userId;
    String tokenId = null;

    @Before
    public void setUp() {
        // Initialize repository manager
        MemoryRepoManager repositoryManager = new MemoryRepoManager();
        
        // Initialize facades and services
        FacadeManager facadeManager = new FacadeManager(repositoryManager, null);
        ServiceManager serviceManager = new ServiceManager(facadeManager);
        
        // Get repositories through the manager (for field assignments)
        this.storeRepository = repositoryManager.getStoreRepository();
        this.auctionRepository = repositoryManager.getAuctionRepository();
        this.itemRepository = repositoryManager.getItemRepository();
        this.feedbackRepository = repositoryManager.getFeedbackRepository();
        this.userRepository = repositoryManager.getUserRepository();
        this.permissionRepository = repositoryManager.getPermissionRepository();
        
        // Get services
        this.tokenService = serviceManager.getTokenService();
        this.permissionManager = new PermissionManager(permissionRepository);
        this.storeFacade = facadeManager.getStoreFacade();
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
        
        // Get the user ID from the token
        this.userId = UUID.fromString(tokenService.extractId(tokenId));
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
        
        // Create a product using ProductService
        ProductService productService = new ServiceManager(new FacadeManager(new MemoryRepoManager(), null)).getProductService();
        Response<ProductDTO> productResponse = productService.addProduct(
            tokenId,
            "Test Product",
            List.of("category1"),
            List.of("A test product description")
        );
        assertFalse("Product creation should succeed", productResponse.errorOccurred());
        String productId = productResponse.getValue().getId();
        
        // Add the product to the store using ItemService
        ItemService itemService = new ServiceManager(new FacadeManager(new MemoryRepoManager(), null)).getItemService();
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
        String endDate = "2077-01-01";
        Response<AuctionDTO> auctionResult = storeService.addAuction(this.tokenId, storeId, productId, endDate, 5.0);
        
        // Verify the auction was created successfully with correct details
        assertFalse("Auction creation should succeed", auctionResult.errorOccurred());
        assertEquals("Auction should be for the correct store", storeId, auctionResult.getValue().getStoreId());
        assertEquals("Auction should be for the correct product", productId, auctionResult.getValue().getProductId());
    }

    @Test
    public void GivenExistingMemberAndAndNewProduct_WhenAddAuctionForNoneexistingStore_ThenReturnError() {
        // Create a product using ProductService
        ProductService productService = new ServiceManager(new FacadeManager(new MemoryRepoManager(), null)).getProductService();
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
        
        // Try to add an item to the non-existent store
        ItemService itemService = new ServiceManager(new FacadeManager(new MemoryRepoManager(), null)).getItemService();
        Response<ItemDTO> itemResponse = itemService.add(
            tokenId,
            nonExistentStoreId,
            productId,
            10.0f,
            10,
            "A new product"
        );
        assertTrue("Item addition to non-existent store should fail", itemResponse.errorOccurred());
        
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

        // Create and add products using ProductService and ItemService
        // Assuming we have access to these services either directly or through ServiceManager
        ProductService productService = new ServiceManager(new FacadeManager(new MemoryRepoManager(), null)).getProductService();
        ItemService itemService = new ServiceManager(new FacadeManager(new MemoryRepoManager(), null)).getItemService();
        
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
        String endDate = "2077-01-01";
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
        // Create a product using ProductService
        ProductService productService = new ServiceManager(new FacadeManager(new MemoryRepoManager(), null)).getProductService();
        ItemService itemService = new ServiceManager(new FacadeManager(new MemoryRepoManager(), null)).getItemService();
        
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
        String endDate = "2077-01-01";
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
}