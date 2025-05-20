package UI.views;

import UI.presenters.IStorePresenter;
import UI.views.components.ItemLayout;
import UI.views.components.StoreLayout;
import UI.presenters.IManagementPresenter;
import UI.presenters.IPurchasePresenter;
import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.component.dialog.Dialog;
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
    private final IManagementPresenter managementPresenter;
    private final IPurchasePresenter purchasePresenter;
    private String sessionToken;

    private final TextField storeNameField = new TextField("Search Store by Name");
    private final Grid<ItemDTO> productGrid = new Grid<>(ItemDTO.class);
    private final Button homeButton = new Button("Return to Homepage");

    private final Button createStoreButton;

    @Autowired
    public StoreSearchView(IStorePresenter storePresenter, IManagementPresenter managementPresenter, IPurchasePresenter purchasePresenter) {
        this.storePresenter = storePresenter;
        this.managementPresenter = managementPresenter;
        this.purchasePresenter = purchasePresenter;

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


        homeButton.addClickListener(e -> UI.getCurrent().navigate("home"));
        homeButton.getStyle().set("background-color", "#7e57c2").set("color", "white");

        // Initialize Create Store button
        createStoreButton = new Button("Create New Store", VaadinIcon.PLUS.create());
        createStoreButton.getStyle()
            .set("background-color", "#4caf50")
            .set("color", "white")
            .set("margin-left", "10px");
        createStoreButton.addClickListener(e -> showCreateStoreDialog());


        
        add(title, storeNameField);
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
            showStoreLayout(store);
            Notification.show("Store found: " + store.getName());

        }
    }


    private void showStoreLayout(StoreDTO store) {
        ItemLayout itemLayout = new ItemLayout(store,
            s -> {
                Response<List<ItemDTO>> response = storePresenter.getItemsByStoreId(sessionToken, s.getId());
                if (response.errorOccurred()) {
                    Notification.show("Failed to fetch items: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
                } else {
                    productGrid.setItems(response.getValue());
                }
            },
            i -> {
                    Response<Boolean> response = purchasePresenter.addProductToCart(sessionToken, i.getProductId(), i.getStoreId(), 1);
                    if (!response.errorOccurred()) {
                        Notification.show("Product added to cart successfully!", 3000, Notification.Position.MIDDLE);
                    } else {
                        Notification.show("Failed to add product to cart: " + response.getErrorMessage(), 
                                        3000, Notification.Position.MIDDLE);
                    }
            },
            i -> UI.getCurrent().navigate("product-review/" + i.getProductId())
        );

        StoreLayout storelayout = new StoreLayout(store,
        itemLayout,
        s -> UI.getCurrent().navigate("owner"),
        s -> {            
            UI.getCurrent().getSession().setAttribute("currentStoreId", s.getId());
            UI.getCurrent().navigate("manager");
        });

        this.add(storelayout);
    }

    private void showCreateStoreDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create New Store");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextField nameField = new TextField("Store Name");
        TextField descriptionField = new TextField("Store Description");
        Button createButton = new Button("Create Store", e -> {
            String name = nameField.getValue();
            String description = descriptionField.getValue();
            
            if (name == null || name.isEmpty()) {
                Notification.show("Store name is required", 3000, Notification.Position.MIDDLE);
                return;
            }

            Response<StoreDTO> response = managementPresenter.addStore(sessionToken, name, description);
            if (response.errorOccurred()) {
                Notification.show("Failed to create store: " + response.getErrorMessage(), 
                    3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Store created successfully!", 
                    3000, Notification.Position.MIDDLE);
                dialog.close();
                // // Refresh the view or update as needed
                // storeNameField.setValue(name);
                // fetchStoreByName();
            }
        });

        dialogLayout.add(nameField, descriptionField, createButton);
        dialog.add(dialogLayout);
        dialog.open();
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