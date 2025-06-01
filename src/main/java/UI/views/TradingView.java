package UI.views;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import Application.DTOs.StoreDTO;
import Application.utils.Response;
import UI.DatabaseRelated.DbHealthStatus;
import UI.presenters.ITradingPresenter;

@Route("trading")
public class TradingView extends BaseView implements BeforeEnterObserver {
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
    private final Button viewBannedUsersButton;

    @Autowired
    public TradingView(ITradingPresenter tradingPresenter, @Autowired(required = false) DbHealthStatus dbHealthStatus) {
        super(dbHealthStatus);
        this.tradingPresenter = tradingPresenter;
        
        H2 title = new H2("Store Trading Operations");
        title.addClassName("view-only");
        
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
        banTitle.addClassName("view-only");
        
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

        // View banned users section
        viewBannedUsersButton = new Button("View Banned Users", e -> showBannedUsers());
        viewBannedUsersButton.addClassName("view-only");
        viewBannedUsersButton.getStyle()
            .set("background-color", "#2196f3")
            .set("color", "white");

        Button homeButton = new Button("Return to Homepage", e -> UI.getCurrent().navigate("home"));
        homeButton.addClassName("view-only");
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

        HorizontalLayout bannedUsersSection = new HorizontalLayout(viewBannedUsersButton);
        bannedUsersSection.setAlignItems(Alignment.BASELINE);

        // Create a user menu container
        Div userMenu = new Div(title, storeSection, banTitle, banSection, unbanSection, bannedUsersSection, homeButton);
        userMenu.addClassName("user-menu");
        
        add(userMenu);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        // Set background style
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background", "linear-gradient(to right, #fce4ec, #f3e5f5)");
    }

    private void showBannedUsers() {
        Response<Map<String, Date>> response = tradingPresenter.getBannedUsers(sessionToken);
        
        if (response.errorOccurred()) {
            Notification.show("Failed to get banned users: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
            return;
        }

        Map<String, Date> bannedUsers = response.getValue();
        
        if (bannedUsers.isEmpty()) {
            Notification.show("No users are currently banned", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Create a dialog to display the banned users
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Banned Users");
        dialog.setWidth("500px");
        dialog.setHeight("400px");
        dialog.getElement().getStyle().set("padding", "24px");

        // Create a grid to display the banned users
        Grid<Map.Entry<String, Date>> grid = new Grid<>();
        grid.setWidthFull();
        grid.setHeight("300px");
        grid.addColumn(Map.Entry::getKey)
            .setHeader("Username")
            .setAutoWidth(true)
            .setFlexGrow(1)
            .setResizable(true)
            .setSortable(true);
        grid.addColumn(entry -> new java.text.SimpleDateFormat("EEE, MMM d, yyyy HH:mm").format(entry.getValue()))
            .setHeader("Ban Expiration")
            .setAutoWidth(true)
            .setFlexGrow(2)
            .setResizable(true)
            .setSortable(true);
        grid.getStyle().set("font-size", "16px");
        grid.setItems(bannedUsers.entrySet());

        dialog.add(grid);

        // Add a close button with spacing
        Button closeButton = new Button("Close", e -> dialog.close());
        closeButton.getStyle().set("margin-top", "16px");
        dialog.getFooter().add(closeButton);

        dialog.open();
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
            // First disable all interactive elements immediately
            UI.getCurrent().getPage().executeJs(
                "if (document.querySelector('.user-menu')) {" +
                "  document.querySelectorAll('button:not(.view-only), input:not(.view-only), select:not(.view-only), a:not(.view-only)').forEach(el => {" +
                "    el.disabled = true;" +
                "    el.style.pointerEvents = 'none';" +
                "    el.style.opacity = '0.5';" +
                "  });" +
                "}"
            );
            
            // Then show notification and clear fields
            Notification.show("User '" + username + "' banned successfully until " + endDateTime, 3000, Notification.Position.MIDDLE);
            usernameToBanField.clear();
            banEndDatePicker.clear();
            banEndTimePicker.clear();
            
            // Finally reload the page
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
            // First enable all interactive elements immediately
            UI.getCurrent().getPage().executeJs(
                "if (document.querySelector('.user-menu')) {" +
                "  document.querySelectorAll('button, input, select, a').forEach(el => {" +
                "    el.disabled = false;" +
                "    el.style.pointerEvents = '';" +
                "    el.style.opacity = '';" +
                "  });" +
                "}"
            );
            
            // Then show notification and clear field
            Notification.show("User '" + username + "' unbanned successfully", 3000, Notification.Position.MIDDLE);
            usernameToUnbanField.clear();
            
            // Finally reload the page
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