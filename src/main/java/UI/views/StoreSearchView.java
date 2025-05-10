package UI.views;

import UI.presenters.IStorePresenter;
import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import java.util.List;

import org.springframework.stereotype.Component;

@Route("store-search")
public class StoreSearchView extends VerticalLayout {

    private final IStorePresenter storePresenter;
    private final String sessionToken = "demo-session"; // Replace with actual session token logic

    private final TextField storeNameField = new TextField("Search Store by Name");
    private final TextField storeIdField = new TextField("Store ID");
    private final Grid<ItemDTO> productGrid = new Grid<>(ItemDTO.class);
    private final Button fetchSupplyButton = new Button("Check Supply Amounts");
    private final Button homeButton = new Button("Return to Homepage");

    public StoreSearchView(IStorePresenter storePresenter) {
        this.storePresenter = storePresenter;

        H1 title = new H1("Marketplace Store Browser");

        storeNameField.setPlaceholder("e.g., SuperMart");
        storeNameField.setWidth("300px");
        storeNameField.addValueChangeListener(e -> fetchStoreByName());

        storeIdField.setPlaceholder("Auto-filled upon search");
        storeIdField.setReadOnly(true);

        fetchSupplyButton.addClickListener(e -> fetchStoreInventory());

        productGrid.setColumns("productName", "price", "amount", "description");

        // Navigation Button
        homeButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        // Layout
        HorizontalLayout actionsLayout = new HorizontalLayout(fetchSupplyButton, homeButton);
        add(title, storeNameField, storeIdField, actionsLayout, productGrid);
        setSpacing(true);
        setSizeFull();
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
}
