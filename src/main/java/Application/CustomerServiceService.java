package Application;

import Application.utils.Error;
import Application.utils.Response;
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
            return new Response<>(result);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<FeedbackDTO> getFeedback(String sessionToken, String storeId, String productId) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("CustomerServiceService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String customerId = this.tokenService.extractId(sessionToken);

            Feedback feedback = this.storeFacade.getFeedback(storeId, productId, customerId);
            if (feedback != null) {
                return new Response<>(new FeedbackDTO(feedback));
            } else {
                throw new Exception("Feedback not found");
            }
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}
