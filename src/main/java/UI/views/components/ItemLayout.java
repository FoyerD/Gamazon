package UI.views.components;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ClickableRenderer.ItemClickListener;

import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;


public class ItemLayout extends VerticalLayout {
    private final Grid<ItemDTO> grid;
    private final Button refreshButton;

    public ItemLayout(StoreDTO store, Consumer<StoreDTO> itemRefresher, Consumer<ItemDTO> onAddToCart, Consumer<ItemDTO> itemReviewer) {

        grid = new Grid<>(ItemDTO.class);
        grid.setColumns("productId", "description", "price", "amount");
        grid.getStyle().set("background-color", "#f3e5f5");

                // Add review button column
        grid.addComponentColumn(item -> {
            Button reviewButton = new Button("Review", e -> itemReviewer.accept(item));
            reviewButton.getStyle()
                .set("background-color", "#805ad5")
                .set("color", "white");
            return reviewButton;
        }).setHeader("Actions");

        // Add to cart button column
        grid.addComponentColumn(item -> {
            Button addToCartButton = new Button("Add to Cart", e -> onAddToCart.accept(item));
            addToCartButton.getStyle()
                .set("background-color", "#38a169")
                .set("color", "white");
            return addToCartButton;
        }).setHeader("Cart");

        refreshButton = new Button(VaadinIcon.REFRESH.create());
        refreshButton.getStyle()
            .set("background-color", "#ab47bc")
            .set("color", "white")
            .set("cursor", "pointer");
        refreshButton.addClickListener(e -> {
            itemRefresher.accept(store);
        });


    }

    public void setItems(List<ItemDTO> items) {
        grid.setItems(items);
    }

    public void addItemClickListener(ItemClickListener<ItemDTO> listener) {
        addItemClickListener(listener);
    }
    
}
