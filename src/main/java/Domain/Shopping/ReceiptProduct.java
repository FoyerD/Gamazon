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
    private String productName;

    protected ReceiptProduct() {
        // Required by JPA
    }

    public ReceiptProduct(int quantity, double price, String productName) {
        this.quantity = quantity;
        this.price = price;
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getProductName() {
        return productName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
} 