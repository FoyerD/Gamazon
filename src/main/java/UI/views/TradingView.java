package UI.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.dialog.Dialog;
import org.springframework.beans.factory.annotation.Autowired;

import UI.presenters.ITradingPresenter;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Route("trading")
public class TradingView extends VerticalLayout implements BeforeEnterObserver {
    private final ITradingPresenter tradingPresenter;
    private final TextField storeNameField;
    private final Button closeStoreButton;
    private String sessionToken;

    // New fields for ban/unban functionality
    private final TextField usernameToBanField;
    private final DatePicker banEndDatePicker;
    private final TimePicker banEndTimePicker;
    private final Button banUserButton;
    private final TextField usernameToUnbanField;
    private final Button unbanUserButton;

    @Autowired
    public TradingView(ITradingPresenter tradingPresenter) {
        this.tradingPresenter = tradingPresenter;
        
        H2 title = new H2("Store Trading Operations");
        
        // Store management section
        storeNameField = new TextField("Store Name");
        storeNameField.setPlaceholder("e.g., SuperTech");
        storeNameField.setWidth("300px");
        storeNameField.getStyle().set("background-color", "#ffffff");
        
        closeStoreButton = new Button("Close Store", e -> closeStore());
        closeStoreButton.getStyle()
            .set("background-color", "#e53935")
            .set("color", "white");

        // User ban section
        H2 banTitle = new H2("User Management");
        
        usernameToBanField = new TextField("Username to Ban");
        usernameToBanField.setPlaceholder("Enter username");
        usernameToBanField.setWidth("300px");
        
        banEndDatePicker = new DatePicker("Ban End Date");
        banEndDatePicker.setMin(LocalDate.now());
        
        banEndTimePicker = new TimePicker("Ban End Time");
        
        banUserButton = new Button("Ban User", e -> banUser());
        banUserButton.getStyle()
            .set("background-color", "#ff5722")
            .set("color", "white");

        // User unban section
        usernameToUnbanField = new TextField("Username to Unban");
        usernameToUnbanField.setPlaceholder("Enter username");
        usernameToUnbanField.setWidth("300px");
        
        unbanUserButton = new Button("Unban User", e -> unbanUser());
        unbanUserButton.getStyle()
            .set("background-color", "#4caf50")
            .set("color", "white");

        Button homeButton = new Button("Return to Homepage", e -> UI.getCurrent().navigate("home"));
        homeButton.getStyle()
            .set("background-color", "#4caf50")
            .set("color", "white");

        // Layout organization
        HorizontalLayout storeSection = new HorizontalLayout(storeNameField, closeStoreButton);
        storeSection.setAlignItems(Alignment.BASELINE);
        
        HorizontalLayout banSection = new HorizontalLayout(usernameToBanField, banEndDatePicker, banEndTimePicker, banUserButton);
        banSection.setAlignItems(Alignment.BASELINE);
        
        HorizontalLayout unbanSection = new HorizontalLayout(usernameToUnbanField, unbanUserButton);
        unbanSection.setAlignItems(Alignment.BASELINE);

        add(title, storeSection, banTitle, banSection, unbanSection, homeButton);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        // Set background style
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background", "linear-gradient(to right, #fce4ec, #f3e5f5)");
    }

    private void banUser() {
        String username = usernameToBanField.getValue();
        LocalDate endDate = banEndDatePicker.getValue();
        LocalDateTime endDateTime = endDate.atTime(banEndTimePicker.getValue());
        
        if (username == null || username.trim().isEmpty()) {
            Notification.show("Please enter a username");
            return;
        }
        
        if (endDate == null || banEndTimePicker.getValue() == null) {
            Notification.show("Please select both date and time for ban duration");
            return;
        }

        Date banEndDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Response<Boolean> response = tradingPresenter.banUser(sessionToken, username, banEndDate);
        
        if (response.errorOccurred()) {
            Notification.show("Failed to ban user: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
        } else {
            Notification.show("User '" + username + "' banned successfully until " + endDateTime, 3000, Notification.Position.MIDDLE);
            usernameToBanField.clear();
            banEndDatePicker.clear();
            banEndTimePicker.clear();
            // Force page refresh for the banned user
            UI.getCurrent().getPage().executeJs("setTimeout(() => window.location.reload(), 1000);");
        }
    }

    private void unbanUser() {
        String username = usernameToUnbanField.getValue();
        if (username == null || username.trim().isEmpty()) {
            Notification.show("Please enter a username");
            return;
        }

        Response<Boolean> response = tradingPresenter.unbanUser(sessionToken, username);
        if (response.errorOccurred()) {
            Notification.show("Failed to unban user: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
        } else {
            Notification.show("User '" + username + "' unbanned successfully", 3000, Notification.Position.MIDDLE);
            usernameToUnbanField.clear();
            // Force page refresh for the unbanned user
            UI.getCurrent().getPage().executeJs("setTimeout(() => window.location.reload(), 1000);");
        }
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