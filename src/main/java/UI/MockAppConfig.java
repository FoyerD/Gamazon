package UI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import UI.presenters.ILoginPresenter;
import UI.presenters.IMarketPresenter;
import UI.presenters.IProductPresenter;
import UI.presenters.IPurchasePresenter;
import UI.presenters.IStorePresenter;
import UI.presenters.LoginPresenterMock;
import UI.presenters.MarketPresenterMock;
import UI.presenters.ProductPresenterMock;
import UI.presenters.PurchasePresenterMock;
import UI.presenters.StorePresenterMock;

@Configuration
public class MockAppConfig {
    
    @Bean
    @Primary
    public IStorePresenter storePresenter() {
        return new StorePresenterMock();
    }
    
    @Bean
    @Primary
    public ILoginPresenter loginPresenter() {
        return new LoginPresenterMock();
    }
    
    @Bean
    @Primary
    public IProductPresenter productPresenter() {
        return new ProductPresenterMock();
    }
    
    @Bean
    @Primary
    public IPurchasePresenter purchasePresenter() {
        return new PurchasePresenterMock();
    }
    
    @Bean
    @Primary
    public IMarketPresenter marketPresenter() {
        return new MarketPresenterMock();
    }
} 