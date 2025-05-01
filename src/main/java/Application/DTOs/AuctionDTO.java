package Application.DTOs;

import java.util.Date;

import Domain.Store.Auction;

public class AuctionDTO {
    private String auctionId;
    private Date auctionStartDate;
    private Date auctionEndDate;
    private float startPrice;
    private float currentPrice;
    private String storeId;
    private String productId;
    private String currentBidderId;

    public AuctionDTO(String auctionId, Date auctionStartDate, Date auctionEndDate, float startPrice, float currentPrice, String storeId, String productId, String currentBidderId) {
        this.auctionId = auctionId;
        this.auctionStartDate = auctionStartDate;
        this.auctionEndDate = auctionEndDate;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.storeId = storeId;
        this.productId = productId;
        this.currentBidderId = currentBidderId;
    }

    public AuctionDTO(Auction auction) {
        this.auctionId = auction.getAuctionId();
        this.auctionStartDate = auction.getAuctionStartDate();
        this.auctionEndDate = auction.getAuctionEndDate();
        this.startPrice = auction.getStartPrice();
        this.currentPrice = auction.getCurrentPrice();
        this.storeId = auction.getStoreId();
        this.productId = auction.getProductId();
        this.currentBidderId = auction.getCurrentBidderId();
    }

    public String getAuctionId() {
        return auctionId;
    }

    public Date getAuctionStartDate() {
        return auctionStartDate;
    }

    public Date getAuctionEndDate() {
        return auctionEndDate;
    }

    public float getStartPrice() {
        return startPrice;
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getProductId() {
        return productId;
    }

    public String getCurrentBidderId() {
        return currentBidderId;
    }
}
