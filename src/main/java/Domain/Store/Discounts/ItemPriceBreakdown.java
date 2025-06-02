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

    public static ItemPriceBreakdown combineMax(List<ItemPriceBreakdown> breakdowns) {
        if (breakdowns == null || breakdowns.isEmpty()) {
            return null;
        }
        ItemPriceBreakdown maxBreakdown = breakdowns.get(0);
        for (int i = 1; i < breakdowns.size(); i++) {
            ItemPriceBreakdown current = breakdowns.get(i);
            if (current.getOriginalPrice() != maxBreakdown.getOriginalPrice()) {
                throw new IllegalArgumentException("All breakdowns must have the same original price to combine");
            }
            if (current.getDiscount() < maxBreakdown.getDiscount()) {
                maxBreakdown = current;
            }
        }
        return maxBreakdown;
    }

    public static ItemPriceBreakdown combineMultiplicate(List<ItemPriceBreakdown> breakdowns) {
        if (breakdowns == null || breakdowns.isEmpty()) {
            return null;
        }
        double payPercentage = 1;
        for (ItemPriceBreakdown breakdown : breakdowns) {
            if (breakdown == null) {
                throw new IllegalArgumentException("Breakdowns cannot contain null values");
            }
            payPercentage *= 1 - breakdown.getDiscount();
        }
        ItemPriceBreakdown combined = new ItemPriceBreakdown(breakdowns.get(0).getOriginalPrice(), 1 - payPercentage);
        return combined;
    }

}
