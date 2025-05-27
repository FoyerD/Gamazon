package Domain.Shopping;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.Product;

/**
 * Handles the creation and management of purchase receipts.
 * This class is responsible for building receipt records after successful checkouts.
 */
@Component
public class ReceiptBuilder {
    private final IReceiptRepository receiptRepo;
    private final ItemFacade itemFacade;

    @Autowired
    public ReceiptBuilder(IReceiptRepository receiptRepo, ItemFacade itemFacade) {
        this.receiptRepo = receiptRepo;
        this.itemFacade = itemFacade;
    }

    /**
     * Creates receipts for all stores involved in a purchase.
     * 
     * @param clientId The ID of the client who made the purchase
     * @param storeProductsMap Map of store IDs to their purchased products and quantities
     * @param cardNumber The card number used for payment (will be masked in receipt)
     */
    public void createReceipts(String clientId, Map<String, Map<Product, Integer>> storeProductsMap, String cardNumber) {
        String maskedCardNumber = maskCardNumber(cardNumber);
        String paymentDetails = "Card: " + maskedCardNumber;
        
        for (Map.Entry<String, Map<Product, Integer>> entry : storeProductsMap.entrySet()) {
            String storeId = entry.getKey();
            Map<Product, Integer> products = entry.getValue();
            
            double storeTotal = calculateStoreTotal(storeId, products);
            receiptRepo.savePurchase(clientId, storeId, products, storeTotal, paymentDetails);
        }
    }

    /**
     * Creates receipts for all stores involved in a purchase with discounted prices.
     * 
     * @param clientId The ID of the client who made the purchase
     * @param storeProductsMap Map of store IDs to their purchased products and quantities
     * @param storeProductPricesMap Map of store IDs to their product final unit prices after discounts
     * @param cardNumber The card number used for payment (will be masked in receipt)
     */
    public void createReceiptsWithDiscounts(String clientId, 
                                          Map<String, Map<Product, Integer>> storeProductsMap,
                                          Map<String, Map<Product, Double>> storeProductPricesMap,
                                          String cardNumber) {
        String maskedCardNumber = maskCardNumber(cardNumber);
        String paymentDetails = "Card: " + maskedCardNumber;
        
        for (Map.Entry<String, Map<Product, Integer>> entry : storeProductsMap.entrySet()) {
            String storeId = entry.getKey();
            Map<Product, Integer> products = entry.getValue();
            Map<Product, Double> productPrices = storeProductPricesMap.get(storeId);
            
            double storeTotal = calculateStoreTotalWithDiscounts(storeId, products, productPrices);
            receiptRepo.savePurchase(clientId, storeId, products, storeTotal, paymentDetails);
        }
    }

    /**
     * Calculates the total amount for a specific store's purchases using original prices.
     * 
     * @param storeId The ID of the store
     * @param products Map of products to their quantities
     * @return The total amount for the store's purchases
     */
    private double calculateStoreTotal(String storeId, Map<Product, Integer> products) {
        double storeTotal = 0.0;
        
        if (products != null) {
            for (Map.Entry<Product, Integer> productEntry : products.entrySet()) {
                if (productEntry.getKey() != null && productEntry.getValue() != null) {
                    Product product = productEntry.getKey();
                    int quantity = productEntry.getValue();
                    Item item = itemFacade.getItem(storeId, product.getProductId());
                    
                    if (item != null) {
                        storeTotal += item.getPrice() * quantity;
                    }
                }
            }
        }
        
        return storeTotal;
    }

    /**
     * Calculates the total amount for a specific store's purchases using discounted prices.
     * 
     * @param storeId The ID of the store
     * @param products Map of products to their quantities
     * @param productPrices Map of products to their final unit prices after discounts
     * @return The total amount for the store's purchases with discounts applied
     */
    private double calculateStoreTotalWithDiscounts(String storeId, Map<Product, Integer> products, Map<Product, Double> productPrices) {
        double storeTotal = 0.0;
        
        if (products != null) {
            for (Map.Entry<Product, Integer> productEntry : products.entrySet()) {
                if (productEntry.getKey() != null && productEntry.getValue() != null) {
                    Product product = productEntry.getKey();
                    int quantity = productEntry.getValue();
                    
                    // Use discounted price if available, otherwise fall back to original price
                    Double unitPrice = (productPrices != null) ? productPrices.get(product) : null;
                    if (unitPrice == null) {
                        // Fallback to original price if discounted price not available
                        Item item = itemFacade.getItem(storeId, product.getProductId());
                        if (item != null) {
                            unitPrice = item.getPrice();
                        } else {
                            unitPrice = 0.0; // Should not happen, but safe fallback
                        }
                    }
                    
                    storeTotal += unitPrice * quantity;
                }
            }
        }
        
        return storeTotal;
    }

    /**
     * Masks a card number to show only the last 4 digits for security.
     * 
     * @param cardNumber The full card number
     * @return The masked card number (e.g., "xxxx-xxxx-xxxx-1234")
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "xxxx-xxxx-xxxx-xxxx";
        }
        return "xxxx-xxxx-xxxx-" + cardNumber.substring(cardNumber.length() - 4);
    }
}