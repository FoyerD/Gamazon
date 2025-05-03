package StoreTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import Domain.User.Member;

import org.junit.Before;
import org.junit.Test;

import Domain.Pair;
import Domain.Store.Feedback;
import Domain.Store.IAuctionRepository;
import Domain.Store.IFeedbackRepository;
import Domain.Store.IItemRepository;
import Domain.Store.IStoreRepository;
import Domain.Store.Item;
import Domain.Store.Store;
import Domain.Store.StoreFacade;
import Domain.User.User;
import Domain.User.IUserRepository;

public class StoreFacadeTests {
    private StoreFacade storeFacade;
    private IStoreRepository storeRepository;
    private IItemRepository itemRepository;
    private IFeedbackRepository feedbackRepository;
    private IAuctionRepository auctionRepository;
    private IUserRepository userRepository;


    @Before
    public void setUp(){
        userRepository = mock(IUserRepository.class);
        storeRepository = mock(IStoreRepository.class);
        itemRepository = mock(IItemRepository.class);
        feedbackRepository = mock(IFeedbackRepository.class);
        auctionRepository = mock(IAuctionRepository.class);
        
        storeFacade = new StoreFacade(
            storeRepository,
            feedbackRepository,
            itemRepository,
            userRepository,
            auctionRepository
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
    public void givenInitializedFacadeAndOpenStore_whenCloseStore_thenReturnTrue(){
        Store store = mock(Store.class);
        String storeId = "storeId";
        when(storeRepository.get(storeId)).thenReturn(store);
        when(store.getId()).thenReturn(storeId);
        when(store.isOpen()).thenReturn(true);
        when(storeRepository.getLock(storeId)).thenReturn(new Object());
        when(storeRepository.update(eq(storeId), any(Store.class))).thenReturn(store);
        assertTrue(storeFacade.closeStore(storeId));
    }

    @Test
    public void givenInitializedFacadeAndClosedStore_whenCloseStore_thenReturnError(){
        Store store = mock(Store.class);
        String storeId = "storeId";
        when(storeRepository.get(storeId)).thenReturn(store);
        when(store.getId()).thenReturn(storeId);
        when(store.isOpen()).thenReturn(false);
        when(storeRepository.getLock(storeId)).thenReturn(new Object());
        try {
            assertTrue(storeFacade.closeStore(storeId) == false);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Store is already closed");
        }
    }

    @Test
    public void givenInitializedFacadeAndFeedback_whenGetFeedback_returnFeedback(){
        String storeId = "storeId";
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


        when(feedbackRepository.get(storeId, productId, userId)).thenReturn(feedback);
        assertEquals(storeFacade.getFeedback(storeId, productId, userId), feedback);
    }

    @Test
    public void givenInitializedFacadeAndNoFeedback_whenGetFeedback_returnNull(){
        String storeId = "storeId";
        String userId = "userId";
        String productId = "productId";
        Pair<String, String> itemId = new Pair<>(storeId, productId);
        
        when(itemRepository.get(itemId)).thenReturn(mock(Item.class));
        when(userRepository.get(userId)).thenReturn(mock(Member.class));
        when(storeRepository.get(storeId)).thenReturn(mock(Store.class));

        when(feedbackRepository.get(storeId, productId, userId)).thenReturn(null);
        assertTrue(storeFacade.getFeedback(storeId, productId, userId) == null);
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

        Feedback feedback = mock(Domain.Store.Feedback.class);
        when(feedback.getComment()).thenReturn(comment);
        when(feedback.getCustomerId()).thenReturn(comment);
        when(feedback.getStoreId()).thenReturn(storeId);
        when(feedback.getProductId()).thenReturn(productId);

        when(feedbackRepository.add(any(), any())).thenReturn(true);
        assertTrue(storeFacade.addFeedback(storeId, productId, userId, comment));
    }
}
