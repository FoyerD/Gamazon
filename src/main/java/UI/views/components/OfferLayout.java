package UI.views.components;

import Application.DTOs.OfferDTO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OfferLayout extends VerticalLayout {

    private final Supplier<List<OfferDTO>> offersSupplier;
    private final Consumer<OfferDTO> offerAccepter;
    private final Consumer<OfferDTO> offerRejecter;

    private final FlexLayout tileContainer = new FlexLayout();

    public OfferLayout(Supplier<List<OfferDTO>> offersSupplier,
                       Consumer<OfferDTO> offerAccepter,
                       Consumer<OfferDTO> offerRejecter) {
        this.offersSupplier = offersSupplier;
        this.offerAccepter = offerAccepter;
        this.offerRejecter = offerRejecter;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        tileContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        tileContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        tileContainer.setWidthFull();
        tileContainer.getStyle()
            .set("display", "flex")
            .set("flex-wrap", "wrap")
            .set("gap", "16px")
            .set("padding", "12px");

        add(tileContainer);
    }

    public void refreshOffers() {
        tileContainer.removeAll();
        List<OfferDTO> offers = offersSupplier.get();
        if (offers == null || offers.isEmpty()) return;

        for (OfferDTO offer : offers) {
            tileContainer.add(buildOfferTile(offer));
        }
    }

    private Div buildOfferTile(OfferDTO offer) {
        Div tile = new Div();
        boolean offeredPriceGreater = offer.getNewPrice() > offer.getItem().getPrice();
        tile.getStyle()
            .set("width", "280px")
            .set("min-height", "160px")
            .set("border", "5px solid " + (offeredPriceGreater ? "rgb(133, 192, 175)" : "rgb(192, 163, 133)"))
            .set("border-radius", "10px")
            .set("padding", "16px")
            .set("background-color", (offeredPriceGreater ? "rgb(196, 228, 221)" : "rgb(228, 209, 196)"))
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "space-between")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");

        tile.getElement().addEventListener("mouseover", e ->
            tile.getStyle()
                .set("transform", "translateY(-2px)")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.3)"));

        tile.getElement().addEventListener("mouseout", e ->
            tile.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();

        content.add(new Span("Item: " + offer.getItem().getProductName()));
        content.add(new Span("Offered by: " + offer.getMember().getUsername()));
        content.add(new Span("New Price: $" + offer.getNewPrice()));

        Button acceptBtn = new Button("Accept", VaadinIcon.CHECK.create(), e -> {
            offerAccepter.accept(offer);
            refreshOffers();
        });

        Button rejectBtn = new Button("Reject", VaadinIcon.CLOSE.create(), e -> {
            offerRejecter.accept(offer);
            refreshOffers();
        });

        styleButton(acceptBtn, "green");
        styleButton(rejectBtn, "red");

        HorizontalLayout actions = new HorizontalLayout(acceptBtn, rejectBtn);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setWidthFull();

        tile.add(content, actions);
        return tile;
    }

    private void styleButton(Button button, String color) {
        button.getStyle()
            .set("background-color", color)
            .set("color", "white")
            .set("border-radius", "20px")
            .set("font-weight", "500")
            .set("padding", "0.3em 1.2em")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");

        button.getElement().addEventListener("mouseover", e ->
            button.getStyle()
                .set("transform", "translateY(-1px)")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.3)"));

        button.getElement().addEventListener("mouseout", e ->
            button.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));
    }
}
