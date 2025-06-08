package Domain.Repos;

import java.util.List;

import Domain.Shopping.Offer;

public abstract class IOfferRepository extends ILockbasedRepository<Offer, String> {
    public abstract List<Offer> getOffersOfStore(String storeId);
}
