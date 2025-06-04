package Domain.Store;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "auctions")
public class Auction {
    @Id
    private String auctionId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date auctionStartDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date auctionEndDate;
    
    private double startPrice;
    private double currentPrice;
    private String storeId;
    private String productId;

    private String currentBidderId;
    private String cardNumber;
    private String cvv;
    private String clientName;
    
    @Temporal(TemporalType.DATE)
    private Date cardExpiryDate;



    public Auction(String auctionId, Date auctionStartDate,
                   Date auctionEndDate, double startPrice, double currentPrice,
                   String storeId, String productId) {
        this.auctionId = auctionId;
        this.auctionStartDate = auctionStartDate;
        this.auctionEndDate = auctionEndDate;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.storeId = storeId;
        this.productId = productId;
        this.currentBidderId = null;
        this.cardNumber = null;
        this.cvv = null;
        this.clientName = null;
        this.cardExpiryDate = null;
    }

    protected Auction() { // changed to protected for JPA
        this.auctionId = null;
        this.auctionStartDate = null;
        this.auctionEndDate = null;
        this.startPrice = -1;
        this.currentPrice = -1;
        this.storeId = null;
        this.productId = null;
        this.currentBidderId = null;
        this.cardNumber = null;
        this.cvv = null;
        this.clientName = null;
        this.cardExpiryDate = null;
    }

    public String getAuctionId() {
        return auctionId;
    }
    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }
    public Date getAuctionStartDate() {
        return auctionStartDate;
    }
    public void setAuctionStartDate(Date auctionStartDate) {
        this.auctionStartDate = auctionStartDate;
    }
    public Date getAuctionEndDate() {
        return auctionEndDate;
    }
    public void setAuctionEndDate(Date auctionEndDate) {
        this.auctionEndDate = auctionEndDate;
    }
    public double getStartPrice() {
        return startPrice;
    }
    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }
    public double getCurrentPrice() {
        return currentPrice;
    }
    
    public String getStoreId() {
        return storeId;
    }
    public void setStoreId(String storeId) {
        this.storeId = storeId.toString();
    }
    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getCurrentBidderId() {
        return currentBidderId;
    }
    
    public Date getCardExpiryDate() {
        return cardExpiryDate;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public String getClientName() {
        return clientName;
    }

    public void setHighestBidder(String clientId, double price,
                          String cardNumber, Date expiryDate, String cvv, String clientName) {
        this.currentBidderId = clientId;
        this.currentPrice = price;
        this.cardNumber = cardNumber;
        this.cardExpiryDate = expiryDate;
        this.cvv = cvv;
        this.clientName = clientName;
    }
    
    public boolean isAuctionOpen() {
        Date currentDate = new Date();
        return currentDate.before(auctionEndDate);
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Auction)) return false;

        Auction auction = (Auction) o;

        if (Double.compare(auction.startPrice, startPrice) != 0) return false;
        if (Double.compare(auction.currentPrice, currentPrice) != 0) return false;
        if (!auctionId.equals(auction.auctionId)) return false;
        if (!auctionStartDate.equals(auction.auctionStartDate)) return false;
        if (!auctionEndDate.equals(auction.auctionEndDate)) return false;
        if (!storeId.equals(auction.storeId)) return false;
        if (!productId.equals(auction.productId)) return false;
        return currentBidderId != null ? currentBidderId.equals(auction.currentBidderId) : auction.currentBidderId == null;
    }
}
