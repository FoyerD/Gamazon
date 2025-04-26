package Domain.Store;

import java.sql.Date;
import java.util.UUID;

enum AuctionStatus {
    ACTIVE,
    INACTIVE,
    COMPLETED
}

public class Auction {
    UUID auctionId;
    Date auctionStartDate;
    Date auctionEndDate;
    float startPrice;
    float currentPrice;
    AuctionStatus auctionStatus;
    UUID storeId;
    UUID productId;
    UUID currentBidderId;


    public Auction(UUID auctionId, Date auctionStartDate,
                   Date auctionEndDate, float startPrice, float currentPrice, AuctionStatus auctionStatus,
                   UUID storeId, UUID productId, UUID currentBidderId) {
        this.auctionId = auctionId;
        this.auctionStartDate = auctionStartDate;
        this.auctionEndDate = auctionEndDate;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.auctionStatus = auctionStatus;
        this.storeId = storeId;
        this.productId = productId;
        this.currentBidderId = currentBidderId;
    }

    public Auction() {
        this.auctionId = null;
        this.auctionStartDate = null;
        this.auctionEndDate = null;
        this.startPrice = -1;
        this.currentPrice = -1;
        this.auctionStatus = null;
        this.storeId = null;
        this.productId = null;
        this.currentBidderId = null;
    }

    public String getAuctionId() {
        return auctionId.toString();
    }
    public void setAuctionId(String auctionId) {
        this.auctionId = UUID.nameUUIDFromBytes(auctionId.toString().getBytes());
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
    public float getStartPrice() {
        return startPrice;
    }
    public void setStartPrice(float startPrice) {
        this.startPrice = startPrice;
    }
    public float getCurrentPrice() {
        return currentPrice;
    }
    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }
    public AuctionStatus getAuctionStatus() {
        return auctionStatus;
    }
    public void setAuctionStatus(AuctionStatus auctionStatus) {
        this.auctionStatus = auctionStatus;
    }
    public String getStoreId() {
        return storeId.toString();
    }
    public void setStoreId(String storeId) {
        this.storeId = UUID.nameUUIDFromBytes(storeId.toString().getBytes());
    }
    public String getProductId() {
        return productId.toString();
    }
    public void setProductId(String productId) {
        this.productId = UUID.nameUUIDFromBytes(productId.toString().getBytes());
    }
    public String getCurrentBidderId() {
        return currentBidderId.toString();
    }
    public void setCurrentBidderId(String currentBidderId) {
        this.currentBidderId = UUID.nameUUIDFromBytes(currentBidderId.toString().getBytes());
    }
    
}
