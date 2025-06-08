package Domain.Shopping;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Domain.Repos.IOfferRepository;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;

@Component
public class OfferManager {

    private final IOfferRepository offerRepository;
    private final PermissionManager permissionManager;
    
    @Autowired
    public OfferManager(IOfferRepository offerRepository, PermissionManager permissionManager) {
        this.offerRepository = offerRepository;
        this.permissionManager = permissionManager;
    }

    
    public Offer makeOffer(String memberId, String storeId, String productId, double newPrice) {
        Offer offer = new Offer(memberId, storeId, productId, newPrice);
        offerRepository.add(offer.getId(), offer);
        return offer;
    }

    public List<Offer> getOffersOfStore(String memberId, String storeId) {
        permissionManager.checkPermission(memberId, storeId, PermissionType.ACCEPT_OFFERS);
        return offerRepository.getOffersOfStore(storeId);
    }
}
