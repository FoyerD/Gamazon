package UI.presenters;






import Application.utils.Response;





/**


 * Mock implementation of IMarketPresenter for testing and development.


 */


public class MarketPresenterMock implements IMarketPresenter {





    @Override


    public Response<Void> openMarketplace(String sessionToken) {


        return Response.success(null);


    }





    @Override


    public Response<Void> closeStore(String sessionToken, String storeName) {


        return Response.success(null);


    }


}