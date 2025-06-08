package Application.DTOs;

public class OfferDTO {
    private final String offerId;
    private final UserDTO member;
    private final ItemDTO item;

    private final double newPrice;


    public OfferDTO(String offerId, UserDTO member, ItemDTO item, double newPrice) {
        this.offerId = offerId;
        this.member = member;
        this.item = item;
        this.newPrice = newPrice;
        
    }

    public String getId() { return offerId.toString(); }
    public UserDTO getMemberId() { return member; }
    public ItemDTO getItem() { return item; }
    public double getNewPrice() { return newPrice; }
}
