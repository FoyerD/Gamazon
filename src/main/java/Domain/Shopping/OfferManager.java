package Domain.Shopping;


import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.utils.Response;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.Pair;
import Domain.Repos.IItemRepository;
import Domain.Repos.IOfferRepository;
import Domain.Store.Item;
import Domain.Store.StoreFacade;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;

@Component
public class OfferManager {

    private final IOfferRepository offerRepository;
    private final PermissionManager permissionManager;
    private final IItemRepository itemRepository;
    
    @Autowired
    public OfferManager(IOfferRepository offerRepository, 
    PermissionManager permissionManager, 
    IItemRepository itemRepository,
    StoreFacade storeFacade) {
        this.offerRepository = offerRepository;
        this.permissionManager = permissionManager;
        this.itemRepository = itemRepository;
    }

    
    public Offer makeOffer(String memberId, String storeId, String productId, double newPrice, PaymentDetails paymentDetails) {
        Offer offer = new Offer(memberId, storeId, productId, newPrice, paymentDetails);
        offerRepository.add(offer.getId(), offer);
        return offer;
    }

    public List<Offer> getOffersOfStore(String memberId, String storeId) {
        permissionManager.checkPermission(memberId, storeId, PermissionType.OVERSEE_OFFERS);
        return offerRepository.getOffersOfStore(storeId);
    }

    public Offer getOffer(String memberId, String offerId) {
        
        Offer offer = offerRepository.get(offerId);
        if (offer == null) {
            throw new NoSuchElementException("offer not found");
        }
        permissionManager.checkPermission(memberId, offer.getStoreId(), PermissionType.OVERSEE_OFFERS);
        return offer;
    }
    // NOTE: supply service is not used right now
    public Offer acceptOffer(String employeeId, String offerId, IExternalPaymentService paymentService) {

        if(paymentService == null) {
            throw new RuntimeException("Payment service is not set");
        }

        // if (supplyService == null) {
        //     throw new IllegalArgumentException("Supply service is not set");
        // }

        Offer offer = getOffer(employeeId, offerId);
        permissionManager.checkPermission(employeeId, offer.getStoreId(), PermissionType.OVERSEE_OFFERS);

        Pair<String, String> itemId = new Pair<>(offer.getStoreId(), offer.getProductId());

        Object itemLock = itemRepository.getLock(itemId);
        synchronized(itemLock) {
            Item item = itemRepository.getItem(offer.getStoreId(), offer.getProductId());
            int currentAmount = item.getAmount();
            if (currentAmount <= 0) {
                throw new RuntimeException("Insufficient item quantity to accept offer");
            }
    
            PaymentDetails paymentDetails = offer.getPaymentDetails();
            Response<Integer> paymentResponse = paymentService.processPayment(paymentDetails.getUserId(), 
                                                    paymentDetails.getCardNumber(), 
                                                    Date.from(paymentDetails.getExpiryDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                                                    paymentDetails.getCvv(),
                                                    paymentDetails.getHolder(),
                                                    offer.getNewPrice());
            if (paymentResponse.errorOccurred()) {
                throw new RuntimeException("Payment Service failed to proccess transaction: " + paymentResponse.getErrorMessage());
            }

            item.decreaseAmount(1);
            itemRepository.update(itemId, item);
        }

        offerRepository.remove(offerId);
        return offer;
    }


    public Offer rejectOffer(String employeeId, String offerId) {
        Offer offer = getOffer(employeeId, offerId);
        permissionManager.checkPermission(employeeId, offer.getStoreId(), PermissionType.OVERSEE_OFFERS);
        return offerRepository.remove(offerId);
    }
}
