package UI.presenters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import Application.CustomerServiceService;
import Application.ItemService;
import Application.ProductService;
import Application.StoreService;
import Application.DTOs.AuctionDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.ProductDTO;
import Application.utils.Error;
import Application.utils.Response;
import Domain.Store.FeedbackDTO;
import Domain.Store.ItemFilter;

@Component
public class ProductPresenter implements IProductPresenter {
    private final StoreService storeService;
    private final ItemService itemService;
    private final CustomerServiceService customerServiceService;
    private final ProductService productService;
    
    public ProductPresenter(StoreService storeService, ItemService itemService, CustomerServiceService customerServiceService, ProductService productService) {
        this.productService = productService;
        this.storeService = storeService;
        this.itemService = itemService;
        this.customerServiceService = customerServiceService;
    }

    @Override
    public Response<List<ItemDTO>> showProductDetails(String sessionToken, ItemFilter filters) {
        return this.itemService.filterItems(sessionToken, filters);
    }

    @Override
    public Response<List<ItemDTO>> showProductDetailsOfaStore(String sessionToken, ItemFilter filters, String storeId) {
        Response<List<ItemDTO>> items = this.itemService.filterItems(sessionToken, filters);

        if (items.errorOccurred()) 
        {
            return items;
        }

        List<ItemDTO> filteredItems = items.getValue().stream()
            .filter(item -> storeId.equals(item.getStoreId()))
            .toList(); 

        List<ItemDTO> result = filteredItems.isEmpty() ? null : filteredItems;
        return new Response<>(result);
    }

    @Override
    public Response<List<ItemDTO>> showAllItems(String sessionToken) {
        ItemFilter filters = new ItemFilter.Builder().build();
        return this.itemService.filterItems(sessionToken, filters);
    }

    public Response<Set<ProductDTO>> showAllProducts(String sessionToken) {
        return this.productService.getAllProducts(sessionToken);
    }

    @Override
    public Response<Void> rateProduct(String sessionToken, ItemDTO item) {
        return this.itemService.addRating(sessionToken, item.getStoreId(), item.getProductId(), (int)item.getRating());
    }

    @Override
    public Response<List<AuctionDTO>> showAuctionedProduct(String sessionToken, ItemFilter filters) {
        Response<List<ItemDTO>> allItems = this.itemService.filterItems(sessionToken, filters);
        if (allItems.errorOccurred()) 
        {
            return new Response<>(new Error(allItems.getErrorMessage()));
        }

        List<AuctionDTO> auctionedItems = new ArrayList<>();
        for (ItemDTO item : allItems.getValue()) {
            Response<List<AuctionDTO>> auctions = this.storeService.getAllProductAuctions(sessionToken, item.getProductId());
            if (auctions.errorOccurred()) 
        {
            return new Response<>(new Error(auctions.getErrorMessage()));
        }
            auctionedItems.addAll(auctions.getValue());
        }

        return new Response<>(auctionedItems);
    }

    @Override
    public Response<List<FeedbackDTO>> showFeedbacks(String sessionToken, ItemFilter filters) {
        Response<List<ItemDTO>> allItems = this.itemService.filterItems(sessionToken, filters);
        if (allItems.errorOccurred()) 
        {
            return new Response<>(new Error(allItems.getErrorMessage()));
        }

        List<FeedbackDTO> feedbacks = new ArrayList<>();
        for (ItemDTO item : allItems.getValue()) {
            Response<List<FeedbackDTO>> auctions = this.customerServiceService.getAllFeedbacksByProductId(sessionToken, item.getProductId());
            if (auctions.errorOccurred()) 
        {
            return new Response<>(new Error(auctions.getErrorMessage()));
        }
            feedbacks.addAll(auctions.getValue());
        }

        return new Response<>(feedbacks);
    }
}
