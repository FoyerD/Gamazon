package Application;

import java.util.List;
import java.util.stream.Collectors;

import Domain.TokenService;
import Domain.Store.Store;
import Domain.Store.StoreFacade;

public class StoreService {

    private StoreFacade storeFacade;
    private TokenService tokenService;


    public StoreService() {
        this.storeFacade = null;
        this.tokenService = null;
    }

    public StoreService(StoreFacade storeFacade, TokenService tokenService) {
        this.storeFacade = storeFacade;
        this.tokenService = tokenService;
    }

    private boolean isInitialized() {
        return this.storeFacade != null && this.tokenService != null;
    }

    public Response<StoreDTO> addStore(String sessionToken, String name, String description) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);

            Store store = storeFacade.addStore(name, description, userId);
            if(store == null) {
                return new Response<>(new Error("Failed to create store."));
            }
            return new Response<>(new StoreDTO(store));

        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }



    public Response<Boolean> openStore(String sessionToken, String storeId){
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);

            boolean result = this.storeFacade.openStore(storeId);
            return new Response<>(result);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }
    public Response<Boolean> closeStore(String sessionToken, String storeId){
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);

            boolean result = this.storeFacade.closeStore(storeId);
            return new Response<>(result);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<StoreDTO> getStoreByName(String sessionToken, String name) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);

            Store store = this.storeFacade.getStoreByName(name);
            if(store == null) {
                return new Response<>(new Error("Store not found."));
            }
            return new Response<>(new StoreDTO(store));
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<AuctionDTO> addAuction(String sessionToken, String storeId, String productId, String auctionEndDate, double startPrice) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                throw new RuntimeException("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);

            return new Response<>(new AuctionDTO(this.storeFacade.addAuction(storeId, productId, auctionEndDate, startPrice)));
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public Response<List<AuctionDTO>> getAllStoreAuctions(String sessionToken, String storeId) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                throw new RuntimeException("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);

            List<AuctionDTO> auctions = this.storeFacade.getAllStoreAuctions(storeId).stream().map(AuctionDTO::new).collect(Collectors.toList());
            return new Response<>(auctions);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public Response<List<AuctionDTO>> getAllProductAuctions(String sessionToken, String productId) {
        try {
            if(!this.isInitialized()) return new Response<>(new Error("StoreService is not initialized."));
            
            if (!tokenService.validateToken(sessionToken)) {
                throw new RuntimeException("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);

            List<AuctionDTO> auctions = this.storeFacade.getAllProductAuctions(productId).stream().map(AuctionDTO::new).collect(Collectors.toList());
            return new Response<>(auctions);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
