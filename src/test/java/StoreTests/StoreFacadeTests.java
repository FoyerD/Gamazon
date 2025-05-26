package StoreTests;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.fail;

import Domain.User.Member;

import org.junit.Before;
import org.junit.Test;

import Domain.Pair;
import Domain.ExternalServices.INotificationService;
import Domain.Store.Auction;
import Domain.Store.Feedback;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IStoreRepository;
import Domain.Store.Item;
import Domain.Store.Store;
import Domain.Store.StoreFacade;
import Domain.User.IUserRepository;

public class StoreFacadeTests {
    private StoreFacade storeFacade;
    private IStoreRepository storeRepository;
    private IItemRepository itemRepository;
    private IFeedbackRepository feedbackRepository;
    private IAuctionRepository auctionRepository;
    private IUserRepository userRepository;
    private INotificationService notificationService;

    @Before
    public void setUp(){
        userRepository = mock(IUserRepository.class);
        storeRepository = mock(IStoreRepository.class);
        itemRepository = mock(IItemRepository.class);
        feedbackRepository = mock(IFeedbackRepository.class);
        auctionRepository = mock(IAuctionRepository.class);
        notificationService = mock(INotificationService.class);
        
        storeFacade = new StoreFacade(
            storeRepository,
            feedbackRepository,
            itemRepository,
            userRepository,
            auctionRepository,
            notificationService
        );
    }
    
    @Test
    public void givenInitializedFacade_whenIsInitialized_thenReturnTrue() {
        assertTrue(storeFacade.isInitialized());
    }

    @Test
    public void givenInitializedFacadeAndExistingStore_whenGetStore_thenReturnStore() {
        Store store = mock(Store.class);
        String storeId = "storeId";
        when(storeRepository.get(storeId)).thenReturn(store);
        when(store.getId()).thenReturn(storeId);
        assertTrue(storeFacade.getStore(storeId).getId().equals(storeId));
    }

    @Test
    public void givenInitializedFacadeAndNoStores_whenGetStore_thenReturnNull() {
        String storeId = "nonExistingStoreId";
        when(storeRepository.get(storeId)).thenReturn(null);
        assertTrue(storeFacade.getStore(storeId) == null);
    }

    @Test
    public void givenInitializedFacade_whenAddStoreNewName_thenReturnTrue(){
        Member founder = mock(Member.class);
        String storeName = "storewhatwhat";
        String storeDesc = "storeDesc";
        String founderId = "founderId";

        when(founder.getId()).thenReturn(founderId);
        when(userRepository.get(founderId)).thenReturn(founder);
        when(storeRepository.getStoreByName(storeName)).thenReturn(null);
        when(storeRepository.add(anyString(), any())).thenReturn(true);
        
        assertTrue(storeFacade.addStore(storeName, storeDesc, founderId) != null);
    }

    @Test
    public void givenInitializedFacade_whenAddStoreExistingName_thenReturnFalse(){
        Store store = mock(Store.class);
        String storeName = "storewhatwhat";
        String storeDesc = "storeDesc";
        String founderId = "founderId";

        when(store.getName()).thenReturn(storeName);
        when(store.getId()).thenReturn("storeId");
        
        when(storeRepository.getStoreByName(storeName)).thenReturn(store);
        when(storeRepository.add(anyString(), any())).thenReturn(true);
        
        try {
            assertTrue(storeFacade.addStore(storeName, storeDesc, founderId) == null);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Store name already exists");
        }
    }

    @Test
    public void givenInitializedFacade_whenAddStoreInvalidFounder_thenReturnFalse(){
        String storeName = "storewhatwhat";
        String storeDesc = "storeDesc";
        String founderId = "founderId";

        when(userRepository.get(founderId)).thenReturn(null);
        
        try {
            assertTrue(storeFacade.addStore(storeName, storeDesc, founderId) == null);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "User not found");
        }
    }

    @Test
    public void givenInitializedFacadeAndExistingStore_whenGetStoreByName_thenReturnStore() {
        Store store = mock(Store.class);
        String storeName = "storeName";
        when(storeRepository.getStoreByName(storeName)).thenReturn(store);
        when(store.getName()).thenReturn(storeName);
        assertTrue(storeFacade.getStoreByName(storeName).getName().equals(storeName));
    }

    @Test
    public void givenInitializedFacadeAndNoStores_whenGetStoreByName_thenReturnNull() {
        String storeName = "nonExistingStoreName";
        when(storeRepository.getStoreByName(storeName)).thenReturn(null);
        assertTrue(storeFacade.getStoreByName(storeName) == null);
    }

    @Test
    public void givenInitializedFacadeAndClosedStore_whenOpenStore_thenReturnTrue(){
        Store store = mock(Store.class);
        String storeId = "storeId";
        when(storeRepository.get(storeId)).thenReturn(store);
        when(store.getId()).thenReturn(storeId);
        when(store.isOpen()).thenReturn(false);
        when(storeRepository.getLock(storeId)).thenReturn(new Object());
        when(storeRepository.update(eq(storeId), any(Store.class))).thenReturn(store);
        assertTrue(storeFacade.openStore(storeId));
    }

    @Test
    public void givenInitialaizedFacadeAndOpenStore_whenOpenStore_thenReturnError(){
        Store store = mock(Store.class);
        String storeId = "storeId";
        when(storeRepository.get(storeId)).thenReturn(store);
        when(store.getId()).thenReturn(storeId);
        when(store.isOpen()).thenReturn(true);
        when(storeRepository.getLock(storeId)).thenReturn(new Object());
        try {
            assertTrue(storeFacade.openStore(storeId) == false);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Store is already open");
        }
    }

    @Test
    public void givenInitializedFacadeAndClosedStore_whenCloseStore_thenReturnError(){
        Store store = mock(Store.class);
        String storeId = "storeId";
        when(storeRepository.get(storeId)).thenReturn(store);
        when(store.getId()).thenReturn(storeId);
        when(store.isPermanentlyClosed()).thenReturn(true);
        when(storeRepository.getLock(storeId)).thenReturn(new Object());
        try {
            storeFacade.closeStore(storeId);
            fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e) {
            assertEquals("Store is already closed", e.getMessage());
        }
    }

    @Test
    public void givenInitializedFacadeAndFeedback_whenGetFeedback_returnFeedback(){
        String storeId = "storeId";
        String feedbackId = "feedbackId";
        String userId = "userId";
        String comment = "comment";
        String productId = "productId";
        Pair<String, String> itemId = new Pair<>(storeId, productId);
        
        when(itemRepository.get(itemId)).thenReturn(mock(Item.class));
        when(userRepository.get(userId)).thenReturn(mock(Member.class));
        when(storeRepository.get(storeId)).thenReturn(mock(Store.class));


        Feedback feedback = mock(Domain.Store.Feedback.class);
        when(feedback.getComment()).thenReturn(comment);
        when(feedback.getCustomerId()).thenReturn(comment);
        when(feedback.getStoreId()).thenReturn(storeId);
        when(feedback.getProductId()).thenReturn(productId);


        when(feedbackRepository.get(feedbackId)).thenReturn(feedback);
        assertEquals(storeFacade.getFeedback(feedbackId), feedback);
    }

    @Test
    public void givenInitializedFacadeAndNoFeedback_whenGetFeedback_returnNull(){
        assertTrue(storeFacade.getFeedback("nonexistingfeedback") == null);
    }

    @Test
    public void givenInitializedFacade_whenAddFeedback_returnTrue(){
        String storeId = "storeId";
        String userId = "userId";
        String comment = "comment";
        String productId = "productId";
        Pair<String, String> itemId = new Pair<>(storeId, productId);
        
        when(itemRepository.get(itemId)).thenReturn(mock(Item.class));
        when(userRepository.get(userId)).thenReturn(mock(Member.class));
        when(storeRepository.get(storeId)).thenReturn(mock(Store.class));

        when(feedbackRepository.getLock(any(String.class))).thenReturn(new Object());
        when(feedbackRepository.get(any(String.class))).thenReturn(null);

        when(feedbackRepository.add(any(), any(Feedback.class))).thenReturn(true);
        assertTrue(storeFacade.addFeedback(storeId, productId, userId, comment));
    }

    @Test
    public void givenInitializedFacade_whenAddFeedbackWithNullComment_returnError(){
        String storeId = "storeId";
        String userId = "userId";
        String productId = "productId";
        Pair<String, String> itemId = new Pair<>(storeId, productId);
        
        when(itemRepository.get(itemId)).thenReturn(mock(Item.class));
        when(userRepository.get(userId)).thenReturn(mock(Member.class));
        when(storeRepository.get(storeId)).thenReturn(mock(Store.class));

        try {
            assertTrue(storeFacade.addFeedback(storeId, productId, userId, null) == false);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Comment cannot be null or empty");
        }
    }

    @Test
    public void givenInitializedFacade_whenAddFeedbackWithEmptyComment_returnError(){
        String storeId = "storeId";
        String userId = "userId";
        String productId = "productId";
        Pair<String, String> itemId = new Pair<>(storeId, productId);
        
        when(itemRepository.get(itemId)).thenReturn(mock(Item.class));
        when(userRepository.get(userId)).thenReturn(mock(Member.class));
        when(storeRepository.get(storeId)).thenReturn(mock(Store.class));

        try {
            assertTrue(storeFacade.addFeedback(storeId, productId, userId, "") == false);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Comment cannot be null or empty");
        }
    }

    @Test
    public void givenInitializedFacadeAndFeedback_whenRemoveFeedback_thanReturnTrue(){
        String storeId = "storeId";
        String userId = "userId";
        String comment = "comment";
        String productId = "productId";
        String feedbackId = "feedbackId";
        Pair<String, String> itemId = new Pair<>(storeId, productId);
        
        when(itemRepository.get(itemId)).thenReturn(mock(Item.class));
        when(userRepository.get(userId)).thenReturn(mock(Member.class));
        when(storeRepository.get(storeId)).thenReturn(mock(Store.class));

        Feedback feedback = mock(Domain.Store.Feedback.class);
        when(feedback.getComment()).thenReturn(comment);
        when(feedback.getCustomerId()).thenReturn(comment);
        when(feedback.getStoreId()).thenReturn(storeId);
        when(feedback.getProductId()).thenReturn(productId);

        when(feedbackRepository.getLock(feedbackId)).thenReturn(new Object());
        when(feedbackRepository.get(feedbackId)).thenReturn(feedback);

        when(feedbackRepository.remove(any(String.class))).thenReturn(feedback);
        assertEquals(storeFacade.removeFeedback(feedbackId), feedback);
    }

    @Test
    public void givenInitializedFacadeAndNoFeedback_whenRemoveFeedback_thanReturnError(){
        String feedbackId = "nonexistingfeedback";
        when(feedbackRepository.getLock(feedbackId)).thenReturn(new Object());
        when(feedbackRepository.get(feedbackId)).thenReturn(null);
        try {
            assertTrue(storeFacade.removeFeedback(feedbackId) == null);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Facade not found");
        }
    }

    @Test
    public void givenInitializedFacadeAndNoFeedback_whenGetAllFeedbacksByStoreId_thanReturnEmptyList(){
        String storeId = "storeId";
        when(feedbackRepository.getAllFeedbacksByStoreId(storeId)).thenReturn(new java.util.ArrayList<>());
        assertTrue(storeFacade.getAllFeedbacksByStoreId(storeId).isEmpty());
    }

    @Test
    public void givenInitializedFacadeAndNoFeedback_whenGetAllFeedbacksByProductId_thanReturnEmptyList(){
        String productId = "productId";
        when(feedbackRepository.getAllFeedbacksByProductId(productId)).thenReturn(new java.util.ArrayList<>());
        assertTrue(storeFacade.getAllFeedbacksByProductId(productId).isEmpty());
    }

    @Test
    public void givenInitializedFacadeAndNoFeedback_whenGetAllFeedbacksByUserId_thanReturnEmptyList(){
        String userId = "userId";
        when(feedbackRepository.getAllFeedbacksByUserId(userId)).thenReturn(new java.util.ArrayList<>());
        assertTrue(storeFacade.getAllFeedbacksByUserId(userId).isEmpty());
    }

    @Test
    public void givenInitializedFacadeAndFeedbacks_whenGetAllFeedbacksByStoreId_thanReturnList(){
        String storeId = "storeId";
        Feedback feedback = mock(Feedback.class);
        when(feedback.getStoreId()).thenReturn(storeId);
        java.util.List<Feedback> feedbacks = new java.util.ArrayList<>();
        feedbacks.add(feedback);
        when(feedbackRepository.getAllFeedbacksByStoreId(storeId)).thenReturn(feedbacks);
        assertTrue(storeFacade.getAllFeedbacksByStoreId(storeId).size() == 1);
    }

    @Test
    public void givenInitializedFacadeAndFeedbacks_whenGetAllFeedbacksByProductId_thanReturnList(){
        String productId = "productId";
        Feedback feedback = mock(Feedback.class);
        when(feedback.getProductId()).thenReturn(productId);
        java.util.List<Feedback> feedbacks = new java.util.ArrayList<>();
        feedbacks.add(feedback);
        when(feedbackRepository.getAllFeedbacksByProductId(productId)).thenReturn(feedbacks);
        assertTrue(storeFacade.getAllFeedbacksByProductId(productId).size() == 1);
    }

    @Test
    public void givenInitializedFacadeAndFeedbacks_whenGetAllFeedbacksByUserId_thanReturnList(){
        String userId = "userId";
        Feedback feedback = mock(Feedback.class);
        when(feedback.getCustomerId()).thenReturn(userId);
        java.util.List<Feedback> feedbacks = new java.util.ArrayList<>();
        feedbacks.add(feedback);
        when(feedbackRepository.getAllFeedbacksByUserId(userId)).thenReturn(feedbacks);
        assertTrue(storeFacade.getAllFeedbacksByUserId(userId).size() == 1);
    }

    @Test
    public void givenInitializedFacadeAndFeedbacks_whenGetAllFeedbacksByStoreId_thanReturnListWithCorrectStoreId(){
        String storeId = "storeId";
        Feedback feedback = mock(Feedback.class);
        when(feedback.getStoreId()).thenReturn(storeId);
        java.util.List<Feedback> feedbacks = new java.util.ArrayList<>();
        feedbacks.add(feedback);
        when(feedbackRepository.getAllFeedbacksByStoreId(storeId)).thenReturn(feedbacks);
        assertTrue(storeFacade.getAllFeedbacksByStoreId(storeId).get(0).getStoreId().equals(storeId));
    }

    @Test
    public void givenInitializedFacade_whenAddAuction_thenReturnAuction(){
        String storeId = "storeId";
        String productId = "productId";
        String endDate = "2077-12-31 00:00";
        Pair<String, String> itemId = new Pair<>(storeId, productId);
        Store store = mock(Store.class);
        when(storeRepository.get(storeId)).thenReturn(store);
        when(store.getId()).thenReturn(storeId);
        when(store.isOpen()).thenReturn(true);

        when(userRepository.get(anyString())).thenReturn(mock(Member.class));
        
        when(itemRepository.get(itemId)).thenReturn(mock(Item.class));
        when(storeRepository.get(storeId)).thenReturn(store);

        Auction auction = mock(Auction.class);
        when(auction.getStoreId()).thenReturn(storeId);
        when(auction.getProductId()).thenReturn(productId);

        when(auctionRepository.getLock(any(String.class))).thenReturn(new Object());
        when(auctionRepository.get(any(String.class))).thenReturn(null);


        when(auctionRepository.add(any(String.class), any(Auction.class))).thenReturn(true);
        assertTrue(storeFacade.addAuction(storeId, productId, endDate, 0) != null);
    }

    @Test
    public void givenInitializedFacade_whenAddAuctionWithNullEndDate_thenReturnError(){
        String storeId = "storeId";
        String productId = "productId";
        String endDate = null;
        Pair<String, String> itemId = new Pair<>(storeId, productId);
        Store store = mock(Store.class);
        when(storeRepository.get(storeId)).thenReturn(store);
        when(store.getId()).thenReturn(storeId);
        when(store.isOpen()).thenReturn(true);

        when(userRepository.get(anyString())).thenReturn(mock(Member.class));
        
        when(itemRepository.get(itemId)).thenReturn(mock(Item.class));
        when(storeRepository.get(storeId)).thenReturn(store);

        try {
            assertTrue(storeFacade.addAuction(storeId, productId, endDate, 0) == null);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Invalid date format. Expected format: yyyy-MM-dd HH:mm");
        }
    }

    @Test
    public void givenInitializedFacade_whenAddAuctionWithInvalidEndDate_thenReturnError(){
        String storeId = "storeId";
        String productId = "productId";
        String endDate = "invalid-date";
        Pair<String, String> itemId = new Pair<>(storeId, productId);
        Store store = mock(Store.class);
        when(storeRepository.get(storeId)).thenReturn(store);
        when(store.getId()).thenReturn(storeId);
        when(store.isOpen()).thenReturn(true);

        when(userRepository.get(anyString())).thenReturn(mock(Member.class));
        
        when(itemRepository.get(itemId)).thenReturn(mock(Item.class));
        when(storeRepository.get(storeId)).thenReturn(store);

        try {
            assertTrue(storeFacade.addAuction(storeId, productId, endDate, 0) == null);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Invalid date format. Expected format: yyyy-MM-dd HH:mm");
        }
    }

    @Test
    public void givenInitialaizedFacadeAndAuction_whenGetAuction_thenReturnAuction(){
        String auctionId = "auctionId";
        Auction auction = mock(Auction.class);
        when(auction.getAuctionId()).thenReturn(auctionId);
        when(auctionRepository.get(auctionId)).thenReturn(auction);
        assertTrue(storeFacade.getAuction(auctionId).getAuctionId().equals(auctionId));
    }

    @Test
    public void givenInitializedFacadeAndNoAuction_whenGetAuction_thenReturnNull(){
        String auctionId = "nonExistingAuctionId";
        when(auctionRepository.get(auctionId)).thenReturn(null);
        assertTrue(storeFacade.getAuction(auctionId) == null);
    }

    @Test
    public void givenInitializedFacadeAndNoAuction_whenGetAllStoreAuctions_thenReturnEmptyList(){
        String storeId = "storeId";
        when(storeRepository.get(storeId)).thenReturn(mock(Store.class));
        when(storeRepository.getLock(storeId)).thenReturn(new Object());
        when(auctionRepository.getAllStoreAuctions(storeId)).thenReturn(new java.util.ArrayList<>());
        assertTrue(storeFacade.getAllStoreAuctions(storeId).isEmpty());
    }

    @Test
    public void givenInitializedFacadeAndNoAuction_whenGetAllProductAuctions_thenReturnEmptyList(){
        String productId = "productId";
        when(auctionRepository.getAllProductAuctions(productId)).thenReturn(new java.util.ArrayList<>());
        assertTrue(storeFacade.getAllProductAuctions(productId).isEmpty());
    }

    @Test
    public void givenInitializedFacadeAndNoAuction_whenGetAllStoreAuctions_thenReturnList(){
        String storeId = "storeId";
        Auction auction = mock(Auction.class);
        when(auction.getStoreId()).thenReturn(storeId);
        java.util.List<Auction> auctions = new java.util.ArrayList<>();
        auctions.add(auction);
        when(auctionRepository.getAllStoreAuctions(storeId)).thenReturn(auctions);
        when(storeRepository.get(storeId)).thenReturn(mock(Store.class));
        when(storeRepository.getLock(storeId)).thenReturn(new Object());
        assertTrue(storeFacade.getAllStoreAuctions(storeId).size() == 1);
    }

    @Test
    public void givenInitializedFacadeAndNoAuction_whenGetAllProductAuctions_thenReturnList(){
        String productId = "productId";
        Auction auction = mock(Auction.class);
        when(auction.getProductId()).thenReturn(productId);
        java.util.List<Auction> auctions = new java.util.ArrayList<>();
        auctions.add(auction);
        when(auctionRepository.getAllProductAuctions(productId)).thenReturn(auctions);
        assertTrue(storeFacade.getAllProductAuctions(productId).size() == 1);
    }

    @Test
    public void givenInitializedFacadeAndAuction_whenCloseAuction_thenReturnAuction(){
        String auctionId = "auctionId";
        Auction auction = mock(Auction.class);
        when(auction.getAuctionId()).thenReturn(auctionId);
        when(auctionRepository.remove(auctionId)).thenReturn(auction);
        when(auctionRepository.get(auctionId)).thenReturn(auction);
        when(auctionRepository.getLock(auctionId)).thenReturn(new Object());
        assertTrue(storeFacade.closeAuction(auctionId).getAuctionId().equals(auctionId));
    }

    @Test
    public void givenInitializedFacadeAndNoAuction_whenCloseAuction_thenReturnError(){
        String auctionId = "nonExistingAuctionId";
        when(auctionRepository.get(auctionId)).thenReturn(null);
        when(auctionRepository.getLock(auctionId)).thenReturn(new Object());
        try {
            assertTrue(storeFacade.closeAuction(auctionId) == null);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Auction not found");
        }
    }

    @Test
    public void givenInitializedFacadeAndAuction_whenAddBid_thenReturnAuction(){
        String auctionId = "auctionId";
        String userId = "userId";
        float bid = 10.0f;

        Auction auction = mock(Auction.class);
        when(auction.getAuctionId()).thenReturn(auctionId);
        when(auction.getCurrentPrice()).thenReturn(5.0);
        when(auction.getStartPrice()).thenReturn(5.0);

        when(auctionRepository.get(auctionId)).thenReturn(auction);
        when(userRepository.get(userId)).thenReturn(mock(Member.class));
        when(auctionRepository.update(anyString(), any(Auction.class))).thenReturn(auction);

        assertEquals(auctionId, storeFacade.addBid(auctionId, userId, bid, () -> true).getAuctionId());
    }

    @Test
    public void givenValidAuctionAndItem_whenAcceptBid_thenReturnsUpdatedItem() {
        String storeId = "store1";
        String productId = "product1";
        String auctionId = "auction1";

        Pair<String, String> itemKey = new Pair<>(storeId, productId);
        Item item = mock(Item.class);
        Auction auction = mock(Auction.class);

        when(itemRepository.get(itemKey)).thenReturn(item);
        when(item.getAmount()).thenReturn(1); // Sufficient quantity
        when(auctionRepository.get(auctionId)).thenReturn(auction);
        when(auction.getStoreId()).thenReturn(storeId);
        when(auction.getProductId()).thenReturn(productId);
        when(auction.getCurrentBidderId()).thenReturn("user123");
        when(auction.triggerCharge()).thenReturn(true);

        when(itemRepository.update(itemKey, item)).thenReturn(item);
        when(auctionRepository.remove(auctionId)).thenReturn(auction);

        Item result = storeFacade.acceptBid(storeId, productId, auctionId);
        assertEquals(item, result);
    }

    @Test(expected = RuntimeException.class)
    public void givenNoItemInStore_whenAcceptBid_thenThrows() {
        when(itemRepository.get(any())).thenReturn(null);
        storeFacade.acceptBid("storeX", "productY", "auctionZ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenNoAuction_whenAcceptBid_thenThrows() {
        String storeId = "store1";
        String productId = "product1";
        String auctionId = "auction1";

        Pair<String, String> itemKey = new Pair<>(storeId, productId);
        Item item = mock(Item.class);
        when(item.getAmount()).thenReturn(1);

        when(itemRepository.get(itemKey)).thenReturn(item);
        when(auctionRepository.get(auctionId)).thenReturn(null);
        when(itemRepository.update(itemKey, item)).thenReturn(item); // for rollback

        storeFacade.acceptBid(storeId, productId, auctionId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenMismatchedAuctionData_whenAcceptBid_thenThrows() {
        String storeId = "store1";
        String productId = "product1";
        String auctionId = "auction1";

        Pair<String, String> itemKey = new Pair<>(storeId, productId);
        Item item = mock(Item.class);
        Auction auction = mock(Auction.class);

        when(item.getAmount()).thenReturn(1);
        when(itemRepository.get(itemKey)).thenReturn(item);
        when(auctionRepository.get(auctionId)).thenReturn(auction);
        when(auction.getStoreId()).thenReturn("wrongStore");
        when(auction.getProductId()).thenReturn("wrongProduct");

        storeFacade.acceptBid(storeId, productId, auctionId);
    }

    @Test(expected = IllegalStateException.class)
    public void givenNoCurrentBidder_whenAcceptBid_thenThrows() {
        String storeId = "store1";
        String productId = "product1";
        String auctionId = "auction1";

        Pair<String, String> itemKey = new Pair<>(storeId, productId);
        Item item = mock(Item.class);
        Auction auction = mock(Auction.class);

        when(item.getAmount()).thenReturn(1);
        when(itemRepository.get(itemKey)).thenReturn(item);
        when(auctionRepository.get(auctionId)).thenReturn(auction);
        when(auction.getStoreId()).thenReturn(storeId);
        when(auction.getProductId()).thenReturn(productId);
        when(auction.getCurrentBidderId()).thenReturn(null);

        storeFacade.acceptBid(storeId, productId, auctionId);
    }

    @Test(expected = RuntimeException.class)
    public void givenChargeFails_whenAcceptBid_thenThrows() {
        String storeId = "store1";
        String productId = "product1";
        String auctionId = "auction1";

        Pair<String, String> itemKey = new Pair<>(storeId, productId);
        Item item = mock(Item.class);
        Auction auction = mock(Auction.class);

        when(item.getAmount()).thenReturn(1);
        when(itemRepository.get(itemKey)).thenReturn(item);
        when(auctionRepository.get(auctionId)).thenReturn(auction);
        when(auction.getStoreId()).thenReturn(storeId);
        when(auction.getProductId()).thenReturn(productId);
        when(auction.getCurrentBidderId()).thenReturn("user123");
        when(auction.triggerCharge()).thenReturn(false);

        storeFacade.acceptBid(storeId, productId, auctionId);
    }

}
