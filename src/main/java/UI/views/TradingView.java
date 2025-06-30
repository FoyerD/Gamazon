package UI.views;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;
import UI.presenters.INotificationPresenter;
import UI.presenters.ITradingPresenter;
import UI.presenters.IStorePresenter;
import UI.presenters.ILoginPresenter;
import UI.presenters.IUserSessionPresenter;

@Route("trading")
public class TradingView extends BaseView implements BeforeEnterObserver {
    private final ITradingPresenter tradingPresenter;
    private final IStorePresenter storePresenter;
    private final ILoginPresenter loginPresenter;
    private final ComboBox<String> storeNameDropdown;
    private final Button closeStoreButton;
    private String sessionToken;

    // New dropdowns for ban/unban functionality
    private final ComboBox<String> usernameToBanDropdown;
    private final DatePicker banEndDatePicker;
    private final TimePicker banEndTimePicker;
    private final Button banUserButton;
    private final ComboBox<String> usernameToUnbanDropdown;
    private final Button unbanUserButton;
    private final Button viewBannedUsersButton;

    @Autowired
    public TradingView(ITradingPresenter tradingPresenter, ILoginPresenter loginPresenter, IStorePresenter storePresenter,
     @Autowired(required = false) DbHealthStatus dbHealthStatus, @Autowired(required = false) GlobalLogoutManager logoutManager, IUserSessionPresenter sessionPresenter, INotificationPresenter notificationPresenter) {
        super(dbHealthStatus, logoutManager, sessionPresenter, notificationPresenter);
        this.tradingPresenter = tradingPresenter;
        this.loginPresenter = loginPresenter;
        this.storePresenter = storePresenter;

        H2 title = new H2("Store Trading Operations");
        title.addClassName("view-only");

        storeNameDropdown = new ComboBox<>("Store Name");
        storeNameDropdown.setPlaceholder("Select a store");
        storeNameDropdown.setWidth("300px");
        storeNameDropdown.getStyle().set("background-color", "#ffffff");

        closeStoreButton = new Button("Close Store", e -> closeStore());
        closeStoreButton.getStyle()
            .set("background-color", "#e53935")
            .set("color", "white");

        // User ban section
        H2 banTitle = new H2("User Management");
        banTitle.addClassName("view-only");

        usernameToBanDropdown = new ComboBox<>("Username to Ban");
        usernameToBanDropdown.setPlaceholder("Select username");
        usernameToBanDropdown.setWidth("300px");

        banEndDatePicker = new DatePicker("Ban End Date");
        banEndDatePicker.setMin(LocalDate.now());

        banEndTimePicker = new TimePicker("Ban End Time");

        banUserButton = new Button("Ban User", e -> banUser());
        banUserButton.getStyle()
            .set("background-color", "#ff5722")
            .set("color", "white");

        // User unban section
        usernameToUnbanDropdown = new ComboBox<>("Username to Unban");
        usernameToUnbanDropdown.setPlaceholder("Select username");
        usernameToUnbanDropdown.setWidth("300px");

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
        HorizontalLayout storeSection = new HorizontalLayout(storeNameDropdown, closeStoreButton);
        storeSection.setAlignItems(Alignment.BASELINE);

        HorizontalLayout banSection = new HorizontalLayout(usernameToBanDropdown, banEndDatePicker, banEndTimePicker, banUserButton);
        banSection.setAlignItems(Alignment.BASELINE);

        HorizontalLayout unbanSection = new HorizontalLayout(usernameToUnbanDropdown, unbanUserButton);
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

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Banned Users");
        dialog.setWidth("500px");
        dialog.setHeight("400px");
        dialog.getElement().getStyle().set("padding", "24px");

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

        Button closeButton = new Button("Close", e -> dialog.close());
        closeButton.getStyle().set("margin-top", "16px");
        dialog.getFooter().add(closeButton);

        dialog.open();
    }

    private void banUser() {
        String username = usernameToBanDropdown.getValue();
        LocalDate endDate = banEndDatePicker.getValue();
        LocalDateTime endDateTime = endDate != null && banEndTimePicker.getValue() != null
            ? endDate.atTime(banEndTimePicker.getValue()) : null;

        if (username == null || username.trim().isEmpty()) {
            Notification.show("Please select a username");
            return;
        }

        if (endDateTime == null) {
            Notification.show("Please select both date and time for ban duration");
            return;
        }

        Date banEndDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Response<Boolean> response = tradingPresenter.banUser(sessionToken, username, banEndDate);

        if (response.errorOccurred()) {
            Notification.show("Failed to ban user: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
        } else {
            Notification notification = new Notification(
                "User '" + username + "' banned successfully until " + endDateTime,
                3000,
                Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();

            usernameToBanDropdown.clear();
            banEndDatePicker.clear();
            banEndTimePicker.clear();
        }
    }

    private void unbanUser() {
        String username = usernameToUnbanDropdown.getValue();
        if (username == null || username.trim().isEmpty()) {
            Notification.show("Please select a username");
            return;
        }

        Response<Boolean> response = tradingPresenter.unbanUser(sessionToken, username);
        if (response.errorOccurred()) {
            Notification.show("Failed to unban user: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
        } else {
            UI.getCurrent().getPage().executeJs(
                "if (document.querySelector('.user-menu')) {" +
                "  document.querySelectorAll('button, input, select, a').forEach(el => {" +
                "    el.disabled = false;" +
                "    el.style.pointerEvents = '';" +
                "    el.style.opacity = '';" +
                "  });" +
                "}"
            );

            Notification.show("User '" + username + "' unbanned successfully", 3000, Notification.Position.MIDDLE);
            usernameToUnbanDropdown.clear();
            UI.getCurrent().getPage().executeJs("setTimeout(() => window.location.reload(), 1000);");
        }
    }

    private void closeStore() {
        String storeName = storeNameDropdown.getValue();
        if (storeName == null || storeName.trim().isEmpty()) {
            Notification.show("Please select a store");
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
            storeNameDropdown.clear();
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
        } else {
            Response<List<StoreDTO>> stores_res = this.storePresenter.getAllStores(sessionToken);
            if (stores_res.errorOccurred()) {
                Notification.show("Failed to load stores: " + stores_res.getErrorMessage(), 3000, Notification.Position.MIDDLE);
                return;
            }else{
                storeNameDropdown.setItems(stores_res.getValue().stream().map(StoreDTO::getName).toList());
            }
        }

        Response<List<UserDTO>> members_res = this.loginPresenter.getAllMembers(sessionToken);
        if (members_res.errorOccurred()) {
            Notification.show("Failed to load members: " + members_res.getErrorMessage(), 3000, Notification.Position.MIDDLE);
            return;
        } else {
            List<UserDTO> members = members_res.getValue();
            usernameToBanDropdown.setItems(members.stream().map(UserDTO::getUsername).toList());
            usernameToUnbanDropdown.setItems(members.stream().map(UserDTO::getUsername).toList());
        }
    }
}
