package Domain.Store;

import java.util.Date;


public class Auction {
    String auctionId;
    Date auctionStartDate;
    Date auctionEndDate;
    double startPrice;
    double currentPrice;
    String storeId;
    String productId;
    String currentBidderId;


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
    }

    public Auction() {
        this.auctionId = null;
        this.auctionStartDate = null;
        this.auctionEndDate = null;
        this.startPrice = -1;
        this.currentPrice = -1;
        this.storeId = null;
        this.productId = null;
        this.currentBidderId = null;
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
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
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
    public void setCurrentBidderId(String currentBidderId) {
        this.currentBidderId = currentBidderId;
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
