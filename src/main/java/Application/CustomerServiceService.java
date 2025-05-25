package Application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.Store.Feedback;
import Domain.Store.FeedbackDTO;
import Domain.Store.StoreFacade;
import Domain.management.PermissionManager;


@Service
public class CustomerServiceService {
    private static final String CLASS_NAME = CustomerServiceService.class.getSimpleName();
    private StoreFacade storeFacade;
    private TokenService tokenService;
    private PermissionManager permissionManager;

    @Autowired
    public CustomerServiceService(StoreFacade storeFacade, TokenService tokenService, PermissionManager permissionManager) {
        this.tokenService = tokenService;
        this.storeFacade = storeFacade;
        this.permissionManager = permissionManager;
    }
    public CustomerServiceService() {
        this.storeFacade = null;
        this.tokenService = null;
        this.permissionManager = null;
    }
    @Transactional
    public void setStoreFacade(StoreFacade storeFacade) {
        this.storeFacade = storeFacade;
    }

    @Transactional
    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Transactional
    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Transactional
    public boolean isInitialized() {
        return this.storeFacade != null && this.tokenService != null;
    }

    @Transactional
    public Response<Boolean> addFeedback(String sessionToken, String storeId, String productId, String comment) {
        String method = "addFeedback";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "CustomerServiceService is not initialized.");
                return new Response<>(new Error("CustomerServiceService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            String customerId = this.tokenService.extractId(sessionToken);
            if (permissionManager.isBanned(customerId)){
                throw new Exception("User is banned from adding feedback.");
            }
            boolean result = this.storeFacade.addFeedback(storeId, productId, customerId, comment);
            if(!result) {
                TradingLogger.logError(CLASS_NAME, method, "Failed to add feedback for store %s, product %s", storeId, productId);
                return new Response<>(new Error("Failed to add feedback."));
            }
            TradingLogger.logEvent(CLASS_NAME, method, "Feedback added successfully for product " + productId);
            return new Response<>(result);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    @Transactional
    public Response<List<FeedbackDTO>> getAllFeedbacksByStoreId(String sessionToken, String storeId) {
        String method = "getAllFeedbacksByStoreId";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "CustomerServiceService is not initialized.");
                return new Response<>(new Error("CustomerServiceService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            List<Feedback> feedbacks = this.storeFacade.getAllFeedbacksByStoreId(storeId);
            List<FeedbackDTO> feedbackDTOs = feedbacks.stream().map(FeedbackDTO::new).collect(Collectors.toList());
            TradingLogger.logEvent(CLASS_NAME, method, "Retrieved " + feedbacks.size() + " feedbacks for store " + storeId);
            return new Response<>(feedbackDTOs);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    @Transactional
    public Response<List<FeedbackDTO>> getAllFeedbacksByProductId(String sessionToken, String productId) {
        String method = "getAllFeedbacksByProductId";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "CustomerServiceService is not initialized.");
                return new Response<>(new Error("CustomerServiceService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            List<Feedback> feedbacks = this.storeFacade.getAllFeedbacksByProductId(productId);
            List<FeedbackDTO> feedbackDTOs = feedbacks.stream().map(FeedbackDTO::new).collect(Collectors.toList());
            TradingLogger.logEvent(CLASS_NAME, method, "Retrieved " + feedbacks.size() + " feedbacks for product " + productId);
            return new Response<>(feedbackDTOs);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    @Transactional
    public Response<List<FeedbackDTO>> getAllFeedbacksByUserId(String sessionToken, String userId) {
        String method = "getAllFeedbacksByUserId";
        try {
            if(!this.isInitialized()) {
                TradingLogger.logError(CLASS_NAME, method, "CustomerServiceService is not initialized.");
                return new Response<>(new Error("CustomerServiceService is not initialized."));
            }
            
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            List<Feedback> feedbacks = this.storeFacade.getAllFeedbacksByUserId(userId);
            List<FeedbackDTO> feedbackDTOs = feedbacks.stream().map(FeedbackDTO::new).collect(Collectors.toList());
            TradingLogger.logEvent(CLASS_NAME, method, "Retrieved " + feedbacks.size() + " feedbacks for user " + userId);
            return new Response<>(feedbackDTOs);
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}