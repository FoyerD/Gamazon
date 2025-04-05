package Domain.ExternalServices;

import java.util.List;

import Domain.Store.IStore;
import Domain.Store.Item;

public interface ISupplyService {

    void placeOrder(IStore store, String deliveryAddress, List<Item> items);

    void supplyItem(String itemId, int quantity);

    void checkSupplyStatus(String itemId);
} 
