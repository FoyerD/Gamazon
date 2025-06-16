package UI.views.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.StoreDTO;
public class StoreTile extends Div {

    private final StoreDTO offer;

    public StoreTile(StoreDTO offer) {
        this.offer = offer;


        getStyle()
            .set("width", "280px")
            .set("min-height", "180px")
            .set("border", "5px solid " + "rgb(165, 133, 192)")
            .set("border-radius", "10px")
            .set("padding", "16px")
            .set("background-color", "rgb(222, 196, 228)")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "space-between")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");

        getElement().addEventListener("mouseover", e ->
            getStyle()
            .set("transform", "translateY(-2px)")
            .set("box-shadow", "0 4px 10px rgba(0,0,0,0.3)"));

        getElement().addEventListener("mouseout", e ->
            getStyle()
            .set("transform", "translateY(0)")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));

        VerticalLayout storePreview = storePreview();

        add(storePreview);
    }


    private VerticalLayout storePreview() {
        VerticalLayout storePreview = new VerticalLayout();
        storePreview.setSpacing(false);
        storePreview.setPadding(false);
        storePreview.setWidthFull();

        // Store name as title
        Span title = new Span(offer.getName());
        title.getStyle()
            .set("font-weight", "bold")
            .set("font-size", "18px")
            .set("color", "#4B306A");

        // Description
        Span desc = new Span(offer.getDescription());
        desc.getStyle()
            .set("font-size", "14px")
            .set("color", "#3F3F3F")
            .set("margin-bottom", "8px");

        // Store status
        Span status = new Span(offer.isOpen() ? "Status: Open" : "Status: Closed");
        status.getStyle()
            .set("font-weight", "bold")
            .set("color", offer.isOpen() ? "green" : "red")
            .set("margin-top", "8px");

        if (offer.isPermanentlyClosed()) {
            status.setText("Status: Permanently Closed");
            status.getStyle().set("color", "darkred");
        }

        storePreview.add(title, desc, status);
        return storePreview;
    }
}
