package Infrastructure;

import java.util.List;

import Domain.Store.IStore;
import Domain.Store.Item;
import Domain.ExternalServices.ISupplyService;

public class SupplyService implements ISupplyService {
    @Override
    public void supplyItem(String itemId, int quantity) {
        // Implementation for supplying the item
        System.out.println("Supplying " + quantity + " of item with ID: " + itemId);
    }

    @Override
    public void checkSupplyStatus(String itemId) {
        // Implementation for checking supply status
        System.out.println("Checking supply status for item with ID: " + itemId);
    }

    @Override
    public void placeOrder(IStore store, String deliveryAddress, List<Item> items) {
        // Implementation for placing an order
        System.out.println("Placing order to be delivered at: " + deliveryAddress);
    }
    
} 
