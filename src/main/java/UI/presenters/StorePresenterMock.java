package UI.presenters;

import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.CategoryDTO;
import Application.utils.Response;
import Domain.Store.FeedbackDTO;
import Application.DTOs.AuctionDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

public class StorePresenterMock implements IStorePresenter {

    @Override
    public Response<StoreDTO> getStoreByName(String sessionToken, String name) {
        StoreDTO store = new StoreDTO(
                "dummy-store-id",
                name,
                "This is a mock store for UI testing",
                "founder123",
                true,
                Set.of("ownerA"),
                Set.of("managerB")
        );
        return Response.success(store);
    }

    @Override
    public Response<List<ItemDTO>> getItemsByStoreId(String sessionToken, String storeId) {
        CategoryDTO category = new CategoryDTO("mock-category", "almog"); // Ensure this constructor exists

        ItemDTO item1 = new ItemDTO(
                storeId,
                "prod-001",
                19.99,
                15,
                "Mock item 1 description",
                Set.of(category),
                "Mock Product 1",
                4.5
        );

        ItemDTO item2 = new ItemDTO(
                storeId,
                "prod-002",
                29.99,
                8,
                "Mock item 2 description",
                Set.of(category),
                "Mock Product 2",
                3.8
        );

        return Response.success(List.of(item1, item2));
    }

    // Unused in view — basic stubs
    @Override
    public Response<List<AuctionDTO>> getAllStoreAuctions(String sessionToken, String storeId) {
        return Response.success(List.of());
    }

    @Override
    public Response<Boolean> updateItemPrice(String sessionToken, String storeId, String productId, float newPrice) {
        return Response.success(true);
    }

    @Override
    public Response<Boolean> addFeedback(String sessionToken, String storeId, String productId, String comment) {
        return Response.success(true);
    }

    @Override
    public Response<List<FeedbackDTO>> getAllFeedbacksByStoreId(String sessionToken, String storeId) {
        return Response.success(List.of());
    }
}
