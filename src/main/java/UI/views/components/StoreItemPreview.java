package UI.views.components;

import java.text.DecimalFormat;
import java.util.function.Consumer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.ItemDTO;

public class StoreItemPreview extends VerticalLayout {
    public StoreItemPreview(ItemDTO item, Consumer<ItemDTO> onAddToCart, Consumer<ItemDTO> onReview, Consumer<ItemDTO> onOffer) {
        setPadding(true);
        setSpacing(false);

        setWidth("250px");
        setHeight("220px");
        getStyle()
            .remove("width") // avoid redundancy
            .remove("min-height")
            .remove("margin-bottom") // handled by gap in FlexLayout
            .set("border", "1px solid #ccc")
            .set("border-radius", "8px")
            .set("padding", "10px")
            .set("background-color", "#f3e5f5")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "space-between");



        Span name = new Span(item.getProductName());
        name.getStyle().set("font-weight", "bold").set("font-size", "16px");

        Span description = new Span(item.getDescription());
        description.getStyle().set("color", "#555");

        DecimalFormat df = new DecimalFormat("#.##");
        Span price = new Span("Price: $" + df.format(item.getPrice()));
        Span amount = new Span("Qty: " + item.getAmount());

        Button reviewButton = new Button("Review", e -> onReview.accept(item));
        reviewButton.getStyle().set("background-color", "#805ad5").set("color", "white");

        Button addToCartButton = new Button("Add to Cart", e -> onAddToCart.accept(item));
        addToCartButton.getStyle().set("background-color", "#38a169").set("color", "white");

        Button makeOfferButton = new Button("Offer", e -> onOffer.accept(item));
        makeOfferButton.getStyle().set("background-color", "rgb(255, 128, 55)").set("color", "white");

        HorizontalLayout buttons = new HorizontalLayout(reviewButton, addToCartButton, makeOfferButton);
        buttons.setSpacing(true);
        buttons.getStyle().set("margin-top", "8px");

        add(name, description, price, amount, buttons);
    }
}

