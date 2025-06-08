package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Domain.Store.Item;

// the price breakdown class is used to store the price breakdown of a product
// along the calculation of the discounts

public class ItemPriceBreakdown {
    // Class implementation

    private List<String> descriptions;
    private double originalPrice;
    private double discount; // INV: between 0 and 1 (percantage)

    
    public ItemPriceBreakdown(Item item){
        this(item.getPrice(), 0, null);
    }

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

    public static Map<String, ItemPriceBreakdown> combineMaxMap(List<Map<String, ItemPriceBreakdown>> breakdowns) {
        Map<String, ItemPriceBreakdown> combined = new HashMap<>(breakdowns.get(0));
        List<Map<String, ItemPriceBreakdown>> subBreakdowns = breakdowns.subList(1, breakdowns.size());
        subBreakdowns.forEach((m) -> {
                                m.forEach((id, ipb) -> {
                                                    combined.merge(id, ipb, ItemPriceBreakdown::combineMax);
                                                });
                                });
        return combined;
    }

    public static Map<String, ItemPriceBreakdown> combineMultiplicateMaps(List<Map<String, ItemPriceBreakdown>> breakdowns) {
        Map<String, ItemPriceBreakdown> combined = new HashMap<>(breakdowns.get(0));
        List<Map<String, ItemPriceBreakdown>> subBreakdowns = breakdowns.subList(1, breakdowns.size());
        subBreakdowns.forEach((m) -> {
                                m.forEach((id, ipb) -> {
                                                    combined.merge(id, ipb, ItemPriceBreakdown::combineMultiplicate);
                                                });
                                });
        return combined;
    }

    public static ItemPriceBreakdown combineMultiplicate(ItemPriceBreakdown breakdown1, ItemPriceBreakdown breakdown2) {
        assert breakdown1.getOriginalPrice() == breakdown2.getOriginalPrice();
        double currPayPercentage = 1 - breakdown1.getDiscount();
        double newPayPercentage = (1 - breakdown2.getDiscount()) * currPayPercentage;
        List<String> descriptions = breakdown1.getDescriptions();
        descriptions.addAll(breakdown2.getDescriptions());
        ItemPriceBreakdown combined = new ItemPriceBreakdown(breakdown1.getOriginalPrice(), 1-newPayPercentage, descriptions);
        return combined;
    }

    public static ItemPriceBreakdown combineMax(ItemPriceBreakdown breakdown1, ItemPriceBreakdown breakdown2) {
        assert breakdown1.getOriginalPrice() == breakdown2.getOriginalPrice();
        ItemPriceBreakdown combined = breakdown1.getDiscount() > breakdown2.getDiscount() ? breakdown1 : breakdown2;
        return combined;
    }

    public static double calculateFinalPrice(List<ItemPriceBreakdown> breakdowns) {
        if (breakdowns == null || breakdowns.isEmpty()) {
            throw new IllegalArgumentException("Breakdowns cannot be null or empty");
        }
        double finalPrice = 0;
        for (ItemPriceBreakdown breakdown : breakdowns) {
            finalPrice += breakdown.getFinalPrice();
        }
        return finalPrice;
    }

    public static double calculateFinalPrice(Map<String, ItemPriceBreakdown> breakdowns) {
        return calculateFinalPrice(new LinkedList<>(breakdowns.values()));
    }

}
