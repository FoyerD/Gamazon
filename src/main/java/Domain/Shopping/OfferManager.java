package Domain.Shopping;


import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Application.utils.Response;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.ExternalServices.INotificationService;
import Domain.Pair;
import Domain.Repos.IItemRepository;
import Domain.Repos.IOfferRepository;
import Domain.Repos.IProductRepository;
import Domain.Repos.IReceiptRepository;
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
    private final IExternalSupplyService supplyService;
    private final IReceiptRepository receiptRepository;
    private final IProductRepository productRepository;


    @Autowired
    public OfferManager(IOfferRepository offerRepository, 
    PermissionManager permissionManager, 
    IItemRepository itemRepository,
    StoreFacade storeFacade,
    IExternalPaymentService paymentService,
    IReceiptRepository receiptRepository,
    IProductRepository productRepository, IExternalSupplyService supplyService) {
        this.offerRepository = offerRepository;
        this.permissionManager = permissionManager;
        this.itemRepository = itemRepository;
        this.paymentService = paymentService;
        this.receiptRepository = receiptRepository;
        this.productRepository = productRepository;
        this.supplyService = supplyService;
    }

    
    public Offer makeOffer(String memberId, String storeId, String productId, double newPrice, PaymentDetails paymentDetails, SupplyDetails supplyDetails) {
        Offer offer = new Offer(memberId, storeId, productId, newPrice, paymentDetails, supplyDetails);
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
    private Offer acceptOffer(String userId, Offer offer) {

        if(paymentService == null) {
            throw new RuntimeException("Payment service is not set");
        }

        if (supplyService == null) {
            throw new IllegalArgumentException("Supply service is not set");
        }

        synchronized (offerRepository.getLock(offer.getId())) {        
            offer.approveOffer(userId); 

            Set<String> offerApprovers  = new HashSet<>(permissionManager.getUsersWithPermission(offer.getStoreId(), PermissionType.OVERSEE_OFFERS));
            offerApprovers.add(offer.getMemberId()); // Include the member who made the offer
            if (offer.getApprovedBy().equals(offerApprovers)) {
                // Process payment
                Offer acceptedOffer = processOrder(offer);
                offerRepository.remove(offer.getId());
                return acceptedOffer;
            }
            else {
                offerRepository.update(offer.getId(), offer);
                return offer;
            }
        }

        
    }

    public Offer acceptOfferByMember(String userId, String offerId){
        Offer offer = getOffer(userId, offerId);
        if (!userId.equals(offer.getMemberId())) {
            throw new IllegalArgumentException("Only the member who made the offer can accept it by this methd.");
        }

        return acceptOffer(userId, offer);        
    }

    public Offer acceptOfferByEmployee(String userId, String offerId){
        Offer offer = getOffer(userId, offerId);
        permissionManager.checkPermission(userId, offer.getStoreId(), PermissionType.OVERSEE_OFFERS);

        return acceptOffer(userId, offer);
    }


    private Offer rejectOffer(String userId, String offerId) {
        return offerRepository.remove(offerId);
    }

    public Offer rejectOfferByMember(String memberId, String offerId) {
        Offer offer = getOffer(memberId, offerId);
        if (!memberId.equals(offer.getMemberId())) {
            throw new IllegalArgumentException("Only the member who made the offer can accept it by this methd.");
        }
        return rejectOffer(memberId, offerId);
    }

    public Offer rejectOfferByEmplee(String employeeId, String offerId) {
        Offer offer = getOffer(employeeId, offerId);
        permissionManager.checkPermission(employeeId, offer.getStoreId(), PermissionType.OVERSEE_OFFERS);
        return rejectOffer(employeeId, offerId);
    }

    private Offer processOrder(Offer offer) {
        
        
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
            if (paymentResponse.getValue() == null || paymentResponse.errorOccurred() || paymentResponse.getValue() == -1) {
                throw new RuntimeException("Payment Service failed to proccess transaction");
            }

            SupplyDetails supplyDetails = offer.getSupplyDetails();
            Response<Integer> supplyResponse = supplyService.supplyOrder(paymentDetails.getHolder(), supplyDetails.getDeliveryAddress(), supplyDetails.getCity(), supplyDetails.getCountry(), supplyDetails.getZipCode()); 
            if (supplyResponse.getValue() == null || supplyResponse.errorOccurred() || supplyResponse.getValue() == -1) {
                if(paymentResponse.getValue() != -1)
                    paymentService.cancelPayment(paymentResponse.getValue());
                throw new RuntimeException("Supply Service failed to proccess transaction");
            }

            item.decreaseAmount(1);
            itemRepository.update(itemId, item);
            offer.setAccepted(true);
        }
        
        try{
        this.receiptRepository.savePurchase(
            offer.getMemberId(),
            offer.getStoreId(),
            Map.of(productRepository.get(offer.getProductId()), new Pair<>(1, offer.getLastPrice())),
            offer.getLastPrice(),
            //TODO change this
            offer.getPaymentDetails().toString(),
            offer.getSupplyDetails().toString()
        );
        } catch (Exception e) {
            // dont know what to do surely not rollback!!!
        }

        for(String managerId : offer.getApprovedBy()){
            if(!managerId.equals(offer.getMemberId())){
                
            }
        }

        return offer;
    }


    public Offer counterOfferByMember(String userId, String offerId, double newPrice) {
        Offer offer = getOffer(userId, offerId);
        if (!userId.equals(offer.getMemberId())) {
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
