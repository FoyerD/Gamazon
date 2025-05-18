package UI.presenters;

import UI.presenters.IMarketPresenter;

import org.springframework.stereotype.Component;

import Application.MarketService;
import Application.StoreService;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import Domain.ExternalServices.INotificationService;

import Domain.Store.Store;
import Application.utils.Error;
import Domain.TokenService;


@Component
public class MarketPresenter implements IMarketPresenter {
    private final MarketService marketService;
    private final StoreService storeService;

    public MarketPresenter(MarketService marketService, StoreService storeService) {
        this.marketService = marketService;
        this.storeService = storeService;
    }


    @Override
    public Response<Void> openMarketplace(String sessionToken) {
        // Delegates to MarketService.openMarket
        Response<Void> resp = marketService.openMarket(sessionToken);
        if (resp.errorOccurred()) {
            return Response.error(resp.getErrorMessage());
        }
        return Response.success(null);
    }

    @Override
    public Response<Void> closeStore(String sessionToken, String storeId) {
        // First close via StoreService (member closing their own store)
        Response<Boolean> resp = storeService.closeStore(sessionToken, storeId);
        if (resp.errorOccurred()) {
            // fallback: if user is a trading manager closing any store
            Response<Boolean> marketResp = storeService.closeStore(sessionToken, storeId);
            if (marketResp.errorOccurred()) {
                return Response.error(marketResp.getErrorMessage());
            }
            return Response.success(null);
        }
        return Response.success(null);
    }
}
