package UI.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.component.html.H2;
import org.springframework.beans.factory.annotation.Autowired;

import UI.presenters.ITradingPresenter;
import Application.DTOs.StoreDTO;
import Application.utils.Response;

@Route("trading")
public class TradingView extends VerticalLayout implements BeforeEnterObserver {
    private final ITradingPresenter tradingPresenter;
    private final TextField storeNameField;
    private final Button closeStoreButton;
    private String sessionToken;

    @Autowired
    public TradingView(ITradingPresenter tradingPresenter) {
        this.tradingPresenter = tradingPresenter;
        
        H2 title = new H2("Store Trading Operations");
        storeNameField = new TextField("Store Name");
        storeNameField.setPlaceholder("e.g., SuperTech");
        storeNameField.setWidth("300px");
        storeNameField.getStyle().set("background-color", "#ffffff");
        
        closeStoreButton = new Button("Close Store", e -> closeStore());
        closeStoreButton.getStyle()
            .set("background-color", "#e53935")
            .set("color", "white");

        Button homeButton = new Button("Return to Homepage", e -> UI.getCurrent().navigate("home"));
        homeButton.getStyle()
            .set("background-color", "#4caf50")
            .set("color", "white");

        add(title, storeNameField, closeStoreButton, homeButton);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        // Set background style
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background", "linear-gradient(to right, #fce4ec, #f3e5f5)");
    }

    private void closeStore() {
        String storeName = storeNameField.getValue();
        if (storeName == null || storeName.trim().isEmpty()) {
            Notification.show("Please enter a store name");
            return;
        }

        Response<StoreDTO> response = tradingPresenter.getStoreByName(sessionToken, storeName);
        if (response.errorOccurred()) {
            Notification.show("Store not found: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
            return;
        }

        StoreDTO store = response.getValue();
        boolean success = tradingPresenter.closeStore(sessionToken, store.getId());
        if (success) {
            Notification.show("Store '" + store.getName() + "' closed successfully", 3000, Notification.Position.MIDDLE);
            storeNameField.clear();
        } else {
            Notification.show("Failed to close store - you may not have permission", 3000, Notification.Position.MIDDLE);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        if (sessionToken == null) {
            Notification.show("Please log in first", 3000, Notification.Position.MIDDLE);
            event.forwardTo("");
        }
    }
} 