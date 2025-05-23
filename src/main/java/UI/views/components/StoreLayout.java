package UI.views.components;

import java.util.function.Consumer;
import com.vaadin.flow.component.button.Button;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.StoreDTO;

public class StoreLayout extends VerticalLayout {
    private final StoreDTO store;
    private final Consumer<StoreDTO> onOwnerButton;
    private final Consumer<StoreDTO> onManagerButton;

    public StoreLayout(StoreDTO store,
                        ItemLayout itemLayout,
                        Consumer<StoreDTO> onOwnerButton,
                        Consumer<StoreDTO> onManagerButton) {

        this.store = store;
        this.onOwnerButton = onOwnerButton;
        this.onManagerButton = onManagerButton;
        setSpacing(true);
        setPadding(true);
        setWidthFull();

        this.add(storeDetails());
        if (store.isOpen()) {
            this.add(actionButtons());
            this.add(itemLayout);
        } else {
            Span closedMessage = new Span("This store is currently closed");
            closedMessage.getStyle()
                .set("color", "red")
                .set("font-weight", "bold")
                .set("font-size", "1.2em")
                .set("margin", "20px 0");
            this.add(closedMessage);
        }
    }

    private HorizontalLayout storeDetails() {
        H2 storeName = new H2(store.getName());
        storeName.getStyle().set("margin", "0");

        Span storeDescription = new Span(store.getDescription());
        storeDescription.getStyle()
            .set("color", "#666")
            .set("margin-left", "20px")
            .set("align-self", "center");

        Span storeStatus = new Span(store.isOpen() ? "Open" : "Closed");
        storeStatus.getStyle()
            .set("color", store.isOpen() ? "green" : "red")
            .set("font-weight", "bold")
            .set("margin-left", "20px")
            .set("align-self", "center");

        HorizontalLayout details = new HorizontalLayout(storeName, storeDescription, storeStatus);
        details.setAlignItems(Alignment.CENTER);
        return details;
    }

    private HorizontalLayout actionButtons() {
        Button ownerButton = new Button("Owner Actions", e -> onOwnerButton.accept(store));
        ownerButton.getStyle()
            .set("background-color", "#4a9eff")
            .set("color", "white");

        Button managerButton = new Button("Manager Actions", e -> onManagerButton.accept(store));
        managerButton.getStyle()
            .set("background-color", "#4a9eff")
            .set("color", "white");

        HorizontalLayout buttons = new HorizontalLayout(ownerButton, managerButton);
        buttons.setSpacing(true);
        return buttons;
    }
}