import org.junit.Before;
import org.junit.Test;

import Application.MarketService;
import Domain.IMarketFacade;
import Domain.MarketFacade;
import Domain.Permission;
import Domain.ExternalServices.INotificationService;
import Domain.ExternalServices.IPaymentService;
import Domain.ExternalServices.ISupplyService;

public class MarketServiceTest {
    
    private MarketService marketService;
    private INotificationService notificationService;
    private IPaymentService paymentService;
    private ISupplyService supplyService;

    @Before
    public void setUp() {
        IMarketFacade marketFacade = MarketFacade.getInstance();
        this.marketService = new MarketService(marketFacade);
        this.notificationService = null;
        this.paymentService = null;
        this.supplyService = null;
    }

    public 
    @Test
    public void givenOwnerAndMember_whenChangeManagerPermissions_thenChangePermissions(){
        
    }
}
