package Domain.Shopping;

import jakarta.persistence.Embeddable;

@Embeddable
public class PriceEntry {
    private String userId;
    private Double price;

    protected PriceEntry() {} // Required by JPA

    public PriceEntry(String userId, Double price) {
        this.userId = userId;
        this.price = price;
    }

    public String getUserId() { return userId; }
    public Double getPrice() { return price; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setPrice(Double price) { this.price = price; }
}
