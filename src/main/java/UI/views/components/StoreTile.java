package UI.views.components;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;

public class StoreTile extends Div {

    private final StoreDTO store;
    private final Runnable onClick;
    private final VerticalLayout content = new VerticalLayout();

    public StoreTile(StoreDTO store,
                     Runnable onClick,
                     Function<StoreDTO, List<ItemDTO>> itemFetcher,
                     Consumer<ItemDTO> onAddToCart,
                     Consumer<ItemDTO> onReview,
                     Consumer<ItemDTO> onOffer,
                     Consumer<StoreDTO> onManager) {

        this.store = store;
        this.onClick = onClick;

        // Initial appearance
        getStyle()
            .set("width", "280px")
            .set("min-height", "180px")
            .set("border", "5px solid rgb(165, 133, 192)")
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

        add(createSummaryLayout());

        // Expand content when clicked
        addClickListener(e -> {
            expandToFullView(itemFetcher, onAddToCart, onReview, onOffer, onManager);
        });
    }

    private VerticalLayout createSummaryLayout() {
        VerticalLayout summary = new VerticalLayout();
        summary.setSpacing(false);
        summary.setPadding(false);

        Span title = new Span(store.getName());
        title.getStyle()
            .set("font-weight", "bold")
            .set("font-size", "18px")
            .set("color", "#4B306A");

        Span desc = new Span(store.getDescription());
        desc.getStyle()
            .set("font-size", "14px")
            .set("color", "#3F3F3F");

        Span status = new Span(store.isOpen() ? "Open" : "Closed");
        status.getStyle()
            .set("font-weight", "bold")
            .set("color", store.isOpen() ? "green" : "red");

        summary.add(title, desc, status);
        return summary;
    }

    private void expandToFullView(Function<StoreDTO, List<ItemDTO>> itemFetcher,
                                Consumer<ItemDTO> onAddToCart,
                                Consumer<ItemDTO> onReview,
                                Consumer<ItemDTO> onOffer,
                                Consumer<StoreDTO> onManager) {
        
        onClick.run();
        // Animate to fullscreen
        getElement().executeJs("""
            const el = this;
            const rect = el.getBoundingClientRect();
            el.style.position = 'relative';
            el.style.marginTop = '-100px';
            el.style.transition = 'all 400ms ease-in-out';
            el.style.zIndex = '9999';
            el.style.borderRadius = '10px';

            void el.offsetWidth;

            requestAnimationFrame(() => {
                el.style.left = '0px';
                el.style.top = '0px'; // ⬅️ below header+buttons
                el.style.width = '100%';
                el.style.opacity = '1';
            });
        """);


        content.removeAll();
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidthFull();

        // Store name and description
        H2 title = new H2(store.getName());
        Span description = new Span(store.getDescription());
        content.add(title, description);

        // Manager button
        Button managerBtn = new Button("Manager Actions", e -> onManager.accept(store));
        managerBtn.getStyle().set("background-color", "#4a9eff").set("color", "white");
        content.add(managerBtn);

        // Create FlexLayout for items
        com.vaadin.flow.component.orderedlayout.FlexLayout itemTileLayout = new com.vaadin.flow.component.orderedlayout.FlexLayout();
        itemTileLayout.setFlexWrap(com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap.WRAP);
        itemTileLayout.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.START);
        itemTileLayout.setWidthFull();
        itemTileLayout.getStyle()
            .set("gap", "12px")
            .set("padding", "8px");

        // Add item previews as tiles
        List<ItemDTO> items = itemFetcher.apply(store);
        if (items != null) {
            items.forEach(item -> {
                StoreItemPreview preview = new StoreItemPreview(item, onAddToCart, onReview, onOffer);
                itemTileLayout.add(preview);
            });
        }

        content.add(itemTileLayout);
        add(content);
    }

}
