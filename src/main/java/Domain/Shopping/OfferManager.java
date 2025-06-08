package Domain.Shopping;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Domain.Repos.IOfferRepository;

@Component
public class OfferManager {

    private final IOfferRepository offerRepository;
    
    @Autowired
    public OfferManager(IOfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    
    public Offer makeOffer(String memberId, String storeId, String productId, double newPrice) {
        Offer offer = new Offer(memberId, storeId, productId, newPrice);
        offerRepository.add(offer.getId(), offer);
        return offer;
    }
}
