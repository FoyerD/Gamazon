package Domain.Store;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Domain.Pair;

public class ItemFacadeTest {

    private ItemFacade facade;
    private IItemRepository repo;
    private IProductRepository productRepo;
    private IStoreRepository storeRepo;

    @Before
    public void setUp() {
        repo = mock(IItemRepository.class);
        productRepo = mock(IProductRepository.class);
        storeRepo = mock(IStoreRepository.class);
        facade = new ItemFacade(repo, productRepo, storeRepo);
    }

    @Test
    public void givenAvailableItems_whenFilterItems_thenOnlyMatches() {
        Item i1 = mock(Item.class), i2 = mock(Item.class);
        ItemFilter f = mock(ItemFilter.class);
        when(repo.getAvailabeItems()).thenReturn(List.of(i1, i2));
        when(f.matchesFilter(i1)).thenReturn(true);
        when(f.matchesFilter(i2)).thenReturn(false);

        List<Item> out = facade.filterItems(f);
        assertEquals(1, out.size());
        assertSame(i1, out.get(0));
    }

    @Test
    public void givenProductId_whenGetItemsProductId_thenDelegates() {
        List<Item> list = List.of(mock(Item.class));
        when(repo.getByProductId("X")).thenReturn(list);
        assertSame(list, facade.getItemsProductId("X"));
    }

    @Test
    public void givenExistingItem_whenGetItem_thenReturns() {
        when(storeRepo.get("s")).thenReturn(mock(Store.class));
        when(productRepo.get("p")).thenReturn(mock(Product.class));
        Item item = mock(Item.class);
        when(repo.getItem("s", "p")).thenReturn(item);
        assertSame(item, facade.getItem("s", "p"));
    }

    @Test(expected = NoSuchElementException.class)
    public void givenNoItem_whenGetItem_thenThrows() {
        when(storeRepo.get("s")).thenReturn(mock(Store.class));
        when(productRepo.get("p")).thenReturn(mock(Product.class));
        when(repo.getItem("s", "p")).thenReturn(null);
        facade.getItem("s", "p");
    }

    @Test
    public void givenStoreId_whenGetItemsByStoreId_thenDelegates() {
        when(storeRepo.get("S")).thenReturn(mock(Store.class));
        List<Item> list = List.of(mock(Item.class));
        when(repo.getByStoreId("S")).thenReturn(list);
        assertSame(list, facade.getItemsByStoreId("S"));
    }

    @Test
    public void whenGetAvailableItems_thenDelegates() {
        List<Item> list = List.of(mock(Item.class));
        when(repo.getAvailabeItems()).thenReturn(list);
        assertSame(list, facade.getAvailableItems());
    }

    @Test
    public void givenIdAndItem_whenUpdate_thenRepoCalled() {
        Pair<String,String> id = new Pair<>("s","p");
        Item it = mock(Item.class);
        when(storeRepo.get("s")).thenReturn(mock(Store.class));
        when(productRepo.get("p")).thenReturn(mock(Product.class));
        facade.update(id, it);
        verify(repo).update(id, it);
    }

@Test
public void givenExistingItem_whenAdd_thenReturnsFalseAndSkipsAdd() {
    // Setup
    Pair<String, String> id = new Pair<>("s", "p");

    Item existingItem = mock(Item.class);
    when(existingItem.getStoreId()).thenReturn("s");
    when(existingItem.getProductId()).thenReturn("p");

    // Mock store and product existence
    when(storeRepo.get("s")).thenReturn(mock(Store.class));
    when(productRepo.get("p")).thenReturn(mock(Product.class));

    // VERY IMPORTANT: use argThat or eq, and test with .equals()
    when(repo.get(argThat(arg -> 
        arg.getFirst().equals("s") && arg.getSecond().equals("p")
    ))).thenReturn(existingItem);

    // Act
    boolean result = facade.add(id, existingItem);

    // Assert
    assertFalse("Expected add(...) to return false since item exists", result);

    // Don't throw if it was called — capture and inspect
    verify(repo, times(1)).get(any());
    verify(repo, times(0)).add(any(), any());
}



    

    @Test
    public void givenNewItem_whenAdd_thenReturnsTrueAndAdds() {
        Pair<String,String> id = new Pair<>("s","p");
        Item it = mock(Item.class);
        when(storeRepo.get("s")).thenReturn(mock(Store.class));
        when(productRepo.get("p")).thenReturn(mock(Product.class));
        when(repo.get(id)).thenReturn(null);
        when(repo.getByProductId("p")).thenReturn(List.of(it));
        when(repo.add(id, it)).thenReturn(true);
        when(it.getProductId()).thenReturn("p");

        assertTrue(facade.add(id, it));
        verify(repo).add(id, it);
    }

    @Test
    public void givenExistingItem_whenRemove_thenReturns() {
        Pair<String,String> id = new Pair<>("s","p");
        when(storeRepo.get("s")).thenReturn(mock(Store.class));
        when(productRepo.get("p")).thenReturn(mock(Product.class));
        Item it = mock(Item.class);
        when(repo.remove(id)).thenReturn(it);
        assertSame(it, facade.remove(id));
    }

    @Test(expected = NoSuchElementException.class)
    public void givenMissingItem_whenRemove_thenThrows() {
        Pair<String,String> id = new Pair<>("s","p");
        when(storeRepo.get("s")).thenReturn(mock(Store.class));
        when(productRepo.get("p")).thenReturn(mock(Product.class));
        when(repo.remove(id)).thenReturn(null);
        facade.remove(id);
    }

    @Test
    public void givenIdAndAmt_whenIncreaseAmount_thenDelegates() {
        Pair<String,String> id = new Pair<>("s","p");
        Item it = mock(Item.class);
        when(storeRepo.get("s")).thenReturn(mock(Store.class));
        when(productRepo.get("p")).thenReturn(mock(Product.class));
        when(repo.get(id)).thenReturn(it);
        facade.increaseAmount(id, 4);
        verify(it).increaseAmount(4);
    }

    @Test
    public void givenIdAndAmt_whenDecreaseAmount_thenDelegates() {
        Pair<String,String> id = new Pair<>("s","p");
        Item it = mock(Item.class);
        when(storeRepo.get("s")).thenReturn(mock(Store.class));
        when(productRepo.get("p")).thenReturn(mock(Product.class));
        when(repo.get(id)).thenReturn(it);
        facade.decreaseAmount(id, 2);
        verify(it).decreaseAmount(2);
    }
}
