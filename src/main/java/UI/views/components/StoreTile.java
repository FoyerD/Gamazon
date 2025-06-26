package UI.views.components;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;

public class StoreTile extends Div {

    private final StoreDTO store;
    
    private final Runnable onClick;
    private final VerticalLayout content = new VerticalLayout();

    private boolean isExpanded = false;


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
            .set("font-size", "22px")
            .set("color", " #4B306A");

        Span desc = new Span(store.getDescription());
        desc.getStyle()
            .set("font-size", "14px")
            .set("color", " #3F3F3F");

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
        if (!isExpanded) {
            isExpanded = true;
            onClick.run();
            // Animate to fullscreen
            getElement().executeJs("""
                const el = this;
                const rect = el.getBoundingClientRect();
                el.style.position = 'relative';
                el.style.marginTop = '-50px';
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
            
            Button closeButton = new Button("✕");
            closeButton.getElement().executeJs("this.addEventListener('click', function(e) { e.stopPropagation(); }, { once: true });");

                closeButton.addClickListener(e -> collapseFromFullView());
            closeButton.getStyle()
                .setBackground("transparent")
                .setBorder("none")
                .setFontSize("24px")
                .setColor("rgb(75, 54, 106)")
                .setCursor("pointer")
                .set("line-height", "1")
                .set("min-width", "32px")
                .set("min-height", "32px");
    
            HorizontalLayout closeLayout = new HorizontalLayout(closeButton);
            closeLayout.setWidthFull();
            closeLayout.setJustifyContentMode(JustifyContentMode.END);
            closeLayout.setPadding(false);
            closeLayout.setSpacing(false);
            closeLayout.getStyle()
                .set("position", "absolute")
                .set("top", "10px")
                .set("right", "10px")
                .set("z-index", "1000");
    
            content.addComponentAtIndex(0, closeLayout);  // Add to top of content

            // Manager button
            Button managerBtn = new Button("Manager Actions", VaadinIcon.USER_STAR.create(), e -> onManager.accept(store));
            managerBtn.getStyle().set("background-color", "#4a9eff").set("color", "white");
            content.add(managerBtn);

            // Create FlexLayout for items
            FlexLayout itemTileLayout = new FlexLayout();
            itemTileLayout.setFlexWrap(FlexWrap.WRAP);
            itemTileLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
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

    private void collapseFromFullView() {
        if (isExpanded) {
            isExpanded = false;
            getElement().executeJs("""
                const el = this;
                el.style.transition = 'all 400ms ease-in-out';
                el.style.width = '280px';
                el.style.marginTop = '0px';
                el.style.zIndex = '1';
                el.style.borderRadius = '10px';
                el.style.position = ''; // ← revert to default
                el.style.left = '';
                el.style.top = '';
                el.style.overflow = '';
            """);

            content.removeAll(); // Clear expanded content
            remove(content);
        }
    }
}
