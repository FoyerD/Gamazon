package Application.DTOs;

public class PriceBreakDownDTO {
    private double totalPrice;
    private double totalDiscount; // INV: between 0 and 1 (percentage discount)
    // finalPrice = totalPrice - totalDiscount * totalPrice

    public PriceBreakDownDTO(double totalPrice, double totalDiscount) {
        this.totalPrice = totalPrice;
        this.totalDiscount = totalDiscount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    

    @Override
    public String toString() {
        return "PriceBreakDownDTO{" +
                "totalPrice=" + totalPrice +
                ", totalDiscount=" + totalDiscount +
                ", finalPrice=" + (totalPrice - totalDiscount * totalPrice) +
                '}';
    }
}
