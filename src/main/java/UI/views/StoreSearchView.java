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
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route("store-search")
public class StoreSearchView extends VerticalLayout implements BeforeEnterObserver {

    private final IStorePresenter storePresenter;
    private String sessionToken;

    private final TextField storeNameField = new TextField("Search Store by Name");
    private final TextField storeIdField = new TextField("Store ID");
    private final Grid<ItemDTO> productGrid = new Grid<>(ItemDTO.class);
    private final Button fetchSupplyButton = new Button("Check Supply Amounts");
    private final Button homeButton = new Button("Return to Homepage");

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

        fetchSupplyButton.addClickListener(e -> fetchStoreInventory());
        fetchSupplyButton.getStyle().set("background-color", "#ab47bc").set("color", "white");

        homeButton.addClickListener(e -> UI.getCurrent().navigate("home"));
        homeButton.getStyle().set("background-color", "#7e57c2").set("color", "white");

        productGrid.setColumns("productName", "price", "amount", "description");
        productGrid.getStyle().set("background-color", "#f3e5f5");

        HorizontalLayout actionsLayout = new HorizontalLayout(fetchSupplyButton, homeButton);
        actionsLayout.setSpacing(true);

        add(title, storeNameField, storeIdField, actionsLayout, productGrid);
    }

    private void fetchStoreByName() {
        String storeName = storeNameField.getValue();
        Response<StoreDTO> response = storePresenter.getStoreByName(sessionToken, storeName);

        if (response.errorOccurred()) {
            Notification.show("Store not found: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
        } else {
            StoreDTO store = response.getValue();
            storeIdField.setValue(store.getId());
            Notification.show("Store found: " + store.getName());
        }
    }

    private void fetchStoreInventory() {
        String storeId = storeIdField.getValue();
        if (storeId == null || storeId.isEmpty()) {
            Notification.show("Please search for a store first.");
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
        }
    }
}