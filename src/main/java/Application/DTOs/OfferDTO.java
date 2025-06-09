package Application.DTOs;

import java.util.List;

import ch.qos.logback.core.joran.sanity.Pair;

public class OfferDTO {
    private final String offerId;
    private final UserDTO member;
    private final ItemDTO item;

    // offered prices, from old to new
    private final List<Pair<String, Double>> offeredPrices; 
    private final boolean counterOffer; // true if the offer is a counter offer from store employees


    public OfferDTO(String offerId, UserDTO member, ItemDTO item, List<Pair<String, Double>> prices, boolean counterOffer) {
        this.offerId = offerId;
        this.member = member;
        this.item = item;
        this.offeredPrices = prices;
        this.counterOffer = counterOffer;
    }



    public String getId() { return offerId.toString(); }
    public UserDTO getMember() { return member; }
    public ItemDTO getItem() { return item; }
    public List<Pair<String, Double>> getOfferedPrices() { return offeredPrices; }
}
