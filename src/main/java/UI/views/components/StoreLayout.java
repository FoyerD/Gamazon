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
        this.add(actionButtons());
        this.add(itemLayout);
    }

    private HorizontalLayout actionButtons() {
        // Initialize owner dashboard button
        Button ownerDashboardButton = new Button("Store Owner Dashboard", VaadinIcon.USER.create(), e -> onOwnerButton.accept(store));
        ownerDashboardButton.getStyle()
            .set("background-color", "#6b46c1")
            .set("color", "white")
            .set("margin-left", "10px")
            .set("cursor", "pointer");

        // Initialize manager button
        Button managerButton = new Button("Store Management", VaadinIcon.COGS.create(), e -> onManagerButton.accept(store));
        managerButton.getStyle()
            .set("background-color", "#2196f3")
            .set("color", "white")
            .set("margin-left", "10px");
        
        HorizontalLayout layout = new HorizontalLayout();
        layout.add(ownerDashboardButton, managerButton);
        return layout;

    }

    private VerticalLayout storeDetails() {
        H2 name = new H2(store.getName());
        Span description = new Span(store.getDescription());
        description.getStyle().set("color", "gray");
        Icon statusIcon = store.isOpen() ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.CLOSE_CIRCLE.create();
        statusIcon.setColor(store.isOpen() ? "green" : "red");

        Span statusText = new Span(store.isOpen() ? "Open" : "Closed");
        statusText.getStyle().set("margin-left", "0.5em");
        HorizontalLayout statusLayout = new HorizontalLayout(statusIcon, statusText);
        statusLayout.setAlignItems(Alignment.CENTER);
        
        VerticalLayout layout = new VerticalLayout();

        layout.add(name, description, statusLayout);

        return layout;
    }



    




    
}