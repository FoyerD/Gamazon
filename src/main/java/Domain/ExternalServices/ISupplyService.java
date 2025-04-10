package Domain.ExternalServices;

import java.util.List;

import Domain.Store.Store;
import Domain.Store.Item;

public interface ISupplyService {

    void placeOrder(Store store, String deliveryAddress, List<Item> items);

    void supplyItem(String itemId, int quantity);

    void checkSupplyStatus(String itemId);

    void initialize();
} 
