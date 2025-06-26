package UI.views.components;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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

    public ItemLayout(StoreDTO store,
                        Function<StoreDTO, List<ItemDTO>> itemRefresher,
                        Consumer<ItemDTO> onAddToCart, 
                        Consumer<ItemDTO> itemReviewer,
                        Consumer<ItemDTO> offerMaker) {

        grid = new Grid<>(ItemDTO.class);
        grid.setColumns("productName", "description", "price", "amount");
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
                .set("background-color", " #38a169")
                .set("color", "white");
            return addToCartButton;
        }).setHeader("Cart");

        grid.addComponentColumn(item -> {
            Button makeOfferButton = new Button("Make an Offer", e -> offerMaker.accept(item));
            makeOfferButton.getStyle()
            .setBackgroundColor("rgb(255, 128, 55)")
            .setColor("white");
            return makeOfferButton;
        }).setHeader("Offer");

        refreshButton = new Button(VaadinIcon.REFRESH.create());
        refreshButton.getStyle()
            .set("background-color", "#ab47bc")
            .set("color", "white")
            .set("cursor", "pointer");
        refreshButton.addClickListener(e -> {
            this.setItems(itemRefresher.apply(store));
  
        });

        this.add(refreshButton, grid);
    }


    public void setItems(List<ItemDTO> items) {
        if (items != null) {
            grid.setItems(items);
        }
    }

    public void addItemClickListener(ItemClickListener<ItemDTO> listener) {
        addItemClickListener(listener);
    }
    
}
