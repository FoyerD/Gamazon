package Application;

import java.util.List;
import java.util.stream.Collectors;

import Domain.TokenService;
import Domain.Store.Feedback;
import Domain.Store.FeedbackDTO;
import Domain.Store.StoreFacade;

public class CustomerServiceService {
    private StoreFacade storeFacade;
    private TokenService tokenService;
    
    public CustomerServiceService(StoreFacade storeFacade, TokenService tokenService) {
        this.tokenService = tokenService;
        this.storeFacade = storeFacade;
    }
    public CustomerServiceService() {
        this.storeFacade = null;
        this.tokenService = null;
    }
    public void setStoreFacade(StoreFacade storeFacade) {
        this.storeFacade = storeFacade;
    }

    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public boolean isInitialized() {
        return this.storeFacade != null && this.tokenService != null;
    }

    public Response<Boolean> addFeedback(String sessionToken, String storeId, String productId, String comment) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("CustomerServiceService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String customerId = this.tokenService.extractId(sessionToken);
            
            boolean result = this.storeFacade.addFeedback(storeId, productId, customerId, comment);
            if(!result) {
                return new Response<>(new Error("Failed to add feedback."));
            }
            return new Response<>(result);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<FeedbackDTO>> getAllFeedbacksByStoreId(String sessionToken, String storeId) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("CustomerServiceService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String customerId = this.tokenService.extractId(sessionToken);
            
            List<Feedback> feedbacks = this.storeFacade.getAllFeedbacksByStoreId(storeId);
            List<FeedbackDTO> feedbackDTOs = feedbacks.stream().map(FeedbackDTO::new).collect(Collectors.toList());
            return new Response<>(feedbackDTOs);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<FeedbackDTO>> getAllFeedbacksByProductId(String sessionToken, String productId) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("CustomerServiceService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String customerId = this.tokenService.extractId(sessionToken);
            
            List<Feedback> feedbacks = this.storeFacade.getAllFeedbacksByProductId(productId);
            List<FeedbackDTO> feedbackDTOs = feedbacks.stream().map(FeedbackDTO::new).collect(Collectors.toList());
            return new Response<>(feedbackDTOs);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<FeedbackDTO>> getAllFeedbacksByUserId(String sessionToken, String userId) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("CustomerServiceService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String customerId = this.tokenService.extractId(sessionToken);
            
            List<Feedback> feedbacks = this.storeFacade.getAllFeedbacksByUserId(userId);
            List<FeedbackDTO> feedbackDTOs = feedbacks.stream().map(FeedbackDTO::new).collect(Collectors.toList());
            return new Response<>(feedbackDTOs);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}
