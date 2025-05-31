package Domain.Shopping;

import jakarta.persistence.Embeddable;

/**
 * Represents a product entry in a receipt with its quantity and price.
 * This is an embeddable class used by Receipt entity.
 */
@Embeddable
public class ReceiptProduct {
    private int quantity;
    private double price;

    protected ReceiptProduct() {
        // Required by JPA
    }

    public ReceiptProduct(int quantity, double price) {
        this.quantity = quantity;
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }
} 