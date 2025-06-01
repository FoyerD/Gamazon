package Application.DTOs;

import java.util.List;
import java.util.ArrayList;

import Domain.Store.Discounts.ItemPriceBreakdown;

public class ItemPriceBreakdownDTO {
    private double originalPrice;
    private double discount; // INV: between 0 and 1 (percentage discount)
    private List<String> descriptions; // Discount descriptions
    
    // Default constructor for JSON serialization
    public ItemPriceBreakdownDTO() {
        this.descriptions = new ArrayList<>();
    }
    
    public ItemPriceBreakdownDTO(double originalPrice, double discount) {
        this.originalPrice = originalPrice;
        this.discount = discount;
        this.descriptions = new ArrayList<>();
    }
    
    public ItemPriceBreakdownDTO(double originalPrice, double discount, List<String> descriptions) {
        this.originalPrice = originalPrice;
        this.discount = discount;
        this.descriptions = descriptions != null ? new ArrayList<>(descriptions) : new ArrayList<>();
    }
    
    // Factory method to create from domain object
    public static ItemPriceBreakdownDTO fromPriceBreakDown(ItemPriceBreakdown priceBreakDown) {
        if (priceBreakDown == null) {
            return null;
        }
        
        return new ItemPriceBreakdownDTO(
            priceBreakDown.getOriginalPrice(),
            priceBreakDown.getDiscount(),
            priceBreakDown.getDescriptions()
        );
    }
    
    // Getters and setters
    public double getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
        this.finalPrice = originalPrice * (1 - discount); // Recalculate final price
    }
    
    public double getDiscount() {
        return discount;
    }
    
    public void setDiscount(double discount) {
        this.discount = discount;
    }
    
    public double getFinalPrice() {
        return originalPrice * (1 - discount);
    }
    
    // Note: setFinalPrice is not provided as this should be calculated automatically
    
    public List<String> getDescriptions() {
        return new ArrayList<>(descriptions);
    }
    
    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions != null ? new ArrayList<>(descriptions) : new ArrayList<>();
    }
    
    public void addDescription(String description) {
        if (description != null) {
            this.descriptions.add(description);
        }
    }
    
    public double getDiscountAmount() {
        return originalPrice * discount;
    }
    
    public double getDiscountPercentage() {
        return discount * 100;
    }
    
    public boolean hasDiscount() {
        return discount > 0;
    }
    
    @Override
    public String toString() {
        return "ItemPriceBreakdownDTO{" +
                "originalPrice=" + originalPrice +
                ", discount=" + String.format("%.1f%%", discount * 100) +
                ", discountAmount=" + String.format("%.2f", getDiscountAmount()) +
                ", finalPrice=" + String.format("%.2f", getFinalPrice()) +
                ", descriptions=" + descriptions +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ItemPriceBreakdownDTO that = (ItemPriceBreakdownDTO) obj;
        
        return Double.compare(that.originalPrice, originalPrice) == 0 &&
               Double.compare(that.discount, discount) == 0 &&
               descriptions.equals(that.descriptions);
    }
    
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(originalPrice);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(discount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + descriptions.hashCode();
        return result;
    }
}