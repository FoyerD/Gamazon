package UI.views.components;

import java.util.function.Supplier;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.StoreDTO;

public class StorePreviewDialog extends Dialog {
    
    public StorePreviewDialog(Supplier<StoreDTO> storeSupplier) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        StoreDTO store = storeSupplier.get();
        if (store == null) {
            Span notFound = new Span("Store not found");
            add(notFound);
        }
        else {
            H2 name = new H2(store.getName());
            Span description = new Span(store.getDescription());
            description.getStyle().set("color", "gray");
            Icon statusIcon = store.isOpen() ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.CLOSE_CIRCLE.create();
            statusIcon.setColor(store.isOpen() ? "green" : "red");

            Span statusText = new Span(store.isOpen() ? "Open" : "Closed");
            statusText.getStyle().set("margin-left", "0.5em");
            HorizontalLayout statusLayout = new HorizontalLayout(statusIcon, statusText);
            statusLayout.setAlignItems(Alignment.CENTER);
            
            VerticalLayout layout = new VerticalLayout(name, description, statusLayout);
            add(layout);
        }
        // Optional styling
        getElement().getStyle()
            .set("padding", "1rem")
            .set("border-radius", "8px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
            .set("background-color", " #fffde7");
    }

}
