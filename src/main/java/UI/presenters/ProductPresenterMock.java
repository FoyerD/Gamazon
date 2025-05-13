package UI.presenters;

import Application.DTOs.AuctionDTO;
import Application.DTOs.CategoryDTO;
import Domain.Store.FeedbackDTO;
import Application.DTOs.ItemDTO;
import Application.utils.Response;
import Domain.Store.ItemFilter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProductPresenterMock implements IProductPresenter {

    private final Set<ItemDTO> mockProducts = Stream.of(
        new ItemDTO("store1", "prod1", 49.99, 20, "Bluetooth speaker", Set.of(new CategoryDTO("Tech", "Tech gadgets")), "Speaker", 4.6),
        new ItemDTO("store2", "prod2", 99.99, 5, "Wireless headphones", Set.of(new CategoryDTO("Tech", "Tech gadgets")), "Headphones", 4.8),
        new ItemDTO("store3", "prod3", 299.99, 2, "Smartwatch", Set.of(new CategoryDTO("Wearables", "Smart accessories")), "Watch", 4.2)
    ).collect(Collectors.toSet());

    @Override
    public Response<List<ItemDTO>> showProductDetails(String sessionToken, ItemFilter filters) {
        List<ItemDTO> results = mockProducts.stream()
            .filter(item -> item.getProductName().toLowerCase().contains(filters.getItemName().toLowerCase()))
            .collect(Collectors.toList());
        return new Response<>(results);
    }

    @Override
    public Response<List<ItemDTO>> showProductDetailsOfaStore(String sessionToken, ItemFilter filters, String storeId) {
        List<ItemDTO> results = mockProducts.stream()
            .filter(item -> item.getProductName().equalsIgnoreCase(filters.getItemName()) &&
                            item.getStoreId().equalsIgnoreCase(storeId))
            .collect(Collectors.toList());
        return new Response<>(results);
    }

    @Override
    public Response<List<ItemDTO>> showAllProducts(String sessionToken) {
        return new Response<>(new ArrayList<>(mockProducts));
    }

    @Override
    public Response<Void> rateProduct(String sessionToken, ItemDTO item) {
        System.out.println("Mock: Rated product " + item.getProductName() + " from " + item.getStoreId() + " with rating " + item.getRating());
        return new Response<>((Void) null); // Void response
    }

    @Override
    public Response<List<AuctionDTO>> showAuctionedProduct(String sessionToken, ItemFilter filters) {
        // Mock: no actual auctions, return empty list
        return new Response<>(new ArrayList<>());
    }

    @Override
    public Response<List<FeedbackDTO>> showFeedbacks(String sessionToken, ItemFilter filters) {
        // Mock: return empty feedback list
        return new Response<>(new ArrayList<>());
    }
}
