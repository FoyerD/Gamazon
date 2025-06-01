package Domain.Store.Discounts;

import java.util.LinkedList;
import java.util.List;

// the price breakdown class is used to store the price breakdown of a product
// along the calculation of the discounts

public class ItemPriceBreakdown {
    // Class implementation

    private List<String> descriptions;
    private double originalPrice;
    private double discount; // INV: between 0 and 1 (percantage)


    public ItemPriceBreakdown(double originalPrice, double discount) {
        this.originalPrice = originalPrice;
        this.discount = discount;
        this.descriptions = new LinkedList<>();
    }

    public ItemPriceBreakdown(double originalPrice, double discount, List<String> descriptions) {
        this.originalPrice = originalPrice;
        this.discount = discount;
        if (descriptions == null) {
            this.descriptions = new LinkedList<>();
        } else {
            this.descriptions = new LinkedList<>(descriptions);
        }
    }

    public void addDescription(String description) {
        descriptions.add(description);
    }

    public List<String> getDescriptions() {
        return new LinkedList<>(descriptions);
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public double getFinalPrice() {
        return originalPrice * (1 - discount);
    }

}
