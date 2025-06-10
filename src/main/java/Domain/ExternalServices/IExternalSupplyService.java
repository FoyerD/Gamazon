package Domain.ExternalServices;

import Application.utils.Response;

public interface IExternalSupplyService {
    Response<Void> updateSupplyServiceURL(String newUrl);
    Response<Boolean> handshake();
    Response<Integer> supplyOrder(String name, String address, String city, String country, String zip);
    Response<Boolean> cancelSupply(int transactionId);
}
