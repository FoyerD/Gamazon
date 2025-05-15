package UI.views;

import UI.presenters.IStorePresenter;
import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.List;
import java.util.Map;

@Route("store-search")
public class StoreSearchView extends VerticalLayout implements BeforeEnterObserver {

    private final IStorePresenter storePresenter;
    private String sessionToken;

    private final TextField storeNameField = new TextField("Search Store by Name");
    private final TextField storeIdField = new TextField("Store ID");
    private final Grid<ItemDTO> productGrid = new Grid<>(ItemDTO.class);
    private final Button fetchSupplyButton;
    private final Button homeButton = new Button("Return to Homepage");
    private final Button ownerDashboardButton;
    private final Button managerButton;

    @Autowired
    public StoreSearchView(IStorePresenter storePresenter) {
        this.storePresenter = storePresenter;

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background", "linear-gradient(to right, #fce4ec, #f3e5f5)");

        H1 title = new H1("Marketplace Store Browser");
        title.getStyle().set("color", "#6a1b9a");

        storeNameField.setPlaceholder("e.g., SuperMart");
        storeNameField.setWidth("300px");
        storeNameField.getStyle().set("background-color", "#ffffff");
        storeNameField.addValueChangeListener(e -> fetchStoreByName());

        storeIdField.setPlaceholder("Auto-filled upon search");
        storeIdField.setReadOnly(true);

        // Initialize Check Supply button
        fetchSupplyButton = new Button("Check Supply Amounts");
        fetchSupplyButton.getStyle()
            .set("background-color", "#ab47bc")
            .set("color", "white")
            .set("cursor", "pointer");
        fetchSupplyButton.addClickListener(e -> {
            if (storeIdField.getValue() == null || storeIdField.getValue().isEmpty()) {
                Notification.show("Please search for a store first!", 3000, Notification.Position.TOP_CENTER);
            } else {
                fetchStoreInventory();
            }
        });

        homeButton.addClickListener(e -> UI.getCurrent().navigate("home"));
        homeButton.getStyle().set("background-color", "#7e57c2").set("color", "white");

        // Initialize owner dashboard button
        ownerDashboardButton = new Button("Store Owner Dashboard", e -> {
            if (storeIdField.getValue() == null || storeIdField.getValue().isEmpty()) {
                Notification.show("Please search for a store first!", 3000, Notification.Position.TOP_CENTER);
            } else {
                UI.getCurrent().navigate("owner");
            }
        });
        ownerDashboardButton.getStyle()
            .set("background-color", "#6b46c1")
            .set("color", "white")
            .set("margin-left", "10px")
            .set("cursor", "pointer");

        // Initialize manager button
        managerButton = new Button("Store Management", VaadinIcon.COGS.create(), e -> {
            String storeId = storeIdField.getValue();
            if (storeId == null || storeId.isEmpty()) {
                Notification.show("Please select a store first!", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            UI.getCurrent().getSession().setAttribute("currentStoreId", storeId);
            UI.getCurrent().navigate("manager");
        });
        managerButton.getStyle()
            .set("background-color", "#2196f3")
            .set("color", "white")
            .set("margin-left", "10px");

        productGrid.setColumns("productName", "price", "amount", "description");
        productGrid.getStyle().set("background-color", "#f3e5f5");

        HorizontalLayout actionsLayout = new HorizontalLayout(fetchSupplyButton, homeButton, ownerDashboardButton, managerButton);
        actionsLayout.setSpacing(true);

        add(title, storeNameField, storeIdField, actionsLayout, productGrid);
    }

    private void fetchStoreByName() {
        String storeName = storeNameField.getValue();
        if (storeName == null || storeName.isEmpty()) {
            return;
        }
        
        Response<StoreDTO> response = storePresenter.getStoreByName(sessionToken, storeName);

        if (response.errorOccurred()) {
            Notification.show("Store not found: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
        } else {
            StoreDTO store = response.getValue();
            storeIdField.setValue(store.getId());
            Notification.show("Store found: " + store.getName());
            // Automatically fetch inventory when store is found
            fetchStoreInventory();
        }
    }

    private void fetchStoreInventory() {
        String storeId = storeIdField.getValue();
        if (storeId == null || storeId.isEmpty()) {
            return;
        }

        Response<List<ItemDTO>> response = storePresenter.getItemsByStoreId(sessionToken, storeId);
        if (response.errorOccurred()) {
            Notification.show("Failed to fetch items: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
        } else {
            productGrid.setItems(response.getValue());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        if (sessionToken == null) {
            Notification.show("Access denied. Please log in.", 4000, Notification.Position.MIDDLE);
            event.forwardTo("login");
            return;
        }
        
        // Check for query parameters
        Location location = event.getLocation();
        Map<String, List<String>> queryParameters = location.getQueryParameters().getParameters();
        
        if (queryParameters.containsKey("storeName") && !queryParameters.get("storeName").isEmpty()) {
            String storeName = queryParameters.get("storeName").get(0);
            if (storeName != null && !storeName.isEmpty()) {
                // Set the store name in the field and trigger search
                storeNameField.setValue(storeName);
                // fetchStoreByName will be triggered by the value change listener
            }
        }
    }
}