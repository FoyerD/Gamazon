package Domain.Shopping;


import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

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
    private final IExternalPaymentService paymentService;
    @Autowired
    public OfferManager(IOfferRepository offerRepository, 
    PermissionManager permissionManager, 
    IItemRepository itemRepository,
    StoreFacade storeFacade,
    IExternalPaymentService paymentService) {
        this.offerRepository = offerRepository;
        this.permissionManager = permissionManager;
        this.itemRepository = itemRepository;
        this.paymentService = paymentService;
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

    public List<Offer> getOffersOfMember(String memberId) {
        return offerRepository.getOffersOfMember(memberId);
    }

    public Offer getOffer(String memberId, String offerId) {
        
        Offer offer = offerRepository.get(offerId);
        if (offer == null) {
            throw new NoSuchElementException("Offer not found");
        }
        return offer;
    }

    // NOTE: supply service is not used right now
    public Offer acceptOffer(String userId, Offer offer) {

        if(paymentService == null) {
            throw new RuntimeException("Payment service is not set");
        }

        // if (supplyService == null) {
        //     throw new IllegalArgumentException("Supply service is not set");
        // }

        synchronized (offerRepository.getLock(offer.getId())) {        
            offer.approveOffer(userId); 

            Set<String> offerApprovers  = new HashSet<>(permissionManager.getUsersWithPermission(offer.getStoreId(), PermissionType.OVERSEE_OFFERS));
            offerApprovers.add(offer.getMemberId()); // Include the member who made the offer
            if (offer.getApprovedBy().equals(offerApprovers)) {
                // Process payment
                processPayment(offer);
                offerRepository.remove(offer.getId());
            }
            else {
                offerRepository.update(offer.getId(), offer);
            }
        }

        return offer;
    }

    public Offer acceptOfferByMember(String userId, String offerId){
        Offer offer = getOffer(userId, offerId);
        if (userId != offer.getMemberId()) {
            throw new IllegalArgumentException("Only the member who made the offer can accept it by this methd.");
        }

        return acceptOffer(userId, offer);        
    }

    public Offer acceptOfferByEmployee(String userId, String offerId){
        Offer offer = getOffer(userId, offerId);
        permissionManager.checkPermission(userId, offer.getStoreId(), PermissionType.OVERSEE_OFFERS);

        return acceptOffer(userId, offer);
    }


    public Offer rejectOffer(String employeeId, String offerId) {
        Offer offer = getOffer(employeeId, offerId);
        permissionManager.checkPermission(employeeId, offer.getStoreId(), PermissionType.OVERSEE_OFFERS);
        return offerRepository.remove(offerId);
    }

    private void processPayment(Offer offer) {
        
        
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
                                                    offer.getLastPrice());
            if (paymentResponse.errorOccurred()) {
                throw new RuntimeException("Payment Service failed to proccess transaction: " + paymentResponse.getErrorMessage());
            }

            item.decreaseAmount(1);
            itemRepository.update(itemId, item);
        }
    }


    public Offer counterOfferByMember(String userId, String offerId, double newPrice) {
        Offer offer = getOffer(userId, offerId);
        if (userId != offer.getMemberId()) {
            throw new IllegalArgumentException("Only the member who made the offer can counter it by this method.");
        }

        return counterOffer(userId, offer, newPrice);
    }

    
    public Offer counterOfferByEmployee(String userId, String offerId, double newPrice) {
        Offer offer = getOffer(userId, offerId);
        permissionManager.checkPermission(userId, offer.getStoreId(), PermissionType.OVERSEE_OFFERS);

        return counterOffer(userId, offer, newPrice);
    }

    private Offer counterOffer(String userId, Offer offer, double newPrice) {
        offer.counterOffer(userId, newPrice);
        offerRepository.update(offer.getId(), offer);
        return offer;
    }
}
