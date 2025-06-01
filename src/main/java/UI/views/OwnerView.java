package UI.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;

@Route("owner")
public class OwnerView extends BaseView implements BeforeEnterObserver {

    private final Grid<PermissionEntry> permissionsGrid;
    private final Button addPermissionButton;
    private final TextField searchField;
    private List<PermissionEntry> permissions;
    private String sessionToken;

    @Autowired
    public OwnerView(@Autowired(required = false) DbHealthStatus dbHealthStatus, @Autowired(required = false) GlobalLogoutManager logoutManager) {
        super(dbHealthStatus, logoutManager);
        setSizeFull();
        setSpacing(false);
        setPadding(true);

        // Initialize Grid
        permissionsGrid = new Grid<>();

        // Navigation Bar
        HorizontalLayout navBar = new HorizontalLayout();
        navBar.setWidthFull();
        navBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        Button homeButton = new Button("Back to Home", VaadinIcon.HOME.create());
        homeButton.addClickListener(e -> UI.getCurrent().navigate("home"));
        homeButton.getStyle()
            .set("background-color", "#2ecc71")
            .set("color", "white")
            .set("cursor", "pointer");
        
        navBar.add(homeButton);

        // Header Section
        H2 title = new H2("Owner Dashboard");
        title.getStyle()
            .set("color", "#2c3e50")
            .set("margin", "0");

        Paragraph subtitle = new Paragraph("Manage system permissions and access controls");
        subtitle.getStyle()
            .set("color", "#7f8c8d")
            .set("margin-top", "0.5rem");

        // Search and Add Section
        HorizontalLayout actionBar = new HorizontalLayout();
        actionBar.setWidthFull();
        actionBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        actionBar.setAlignItems(Alignment.CENTER);
        actionBar.getStyle().set("margin", "2rem 0");

        searchField = new TextField();
        searchField.setPlaceholder("Search permissions...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.getStyle()
            .set("width", "300px")
            .set("--lumo-primary-color", "#3498db");
        
        // Add search functionality
        searchField.addValueChangeListener(e -> {
            String searchTerm = e.getValue().toLowerCase();
            List<PermissionEntry> filteredPermissions = permissions.stream()
                .filter(entry -> entry.getUsername().toLowerCase().contains(searchTerm) ||
                               entry.getRole().toLowerCase().contains(searchTerm) ||
                               entry.getPermissions().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
            permissionsGrid.setItems(filteredPermissions);
        });

        addPermissionButton = new Button("Add Permission", VaadinIcon.PLUS.create());
        addPermissionButton.getStyle()
            .set("background-color", "#3498db")
            .set("color", "white")
            .set("cursor", "pointer");
        addPermissionButton.addClickListener(e -> showAddPermissionDialog());

        actionBar.add(searchField, addPermissionButton);

        // Permissions Grid
        permissionsGrid.addColumn(PermissionEntry::getUsername).setHeader("Username")
            .setAutoWidth(true).setFlexGrow(1);
        permissionsGrid.addColumn(PermissionEntry::getRole).setHeader("Role")
            .setAutoWidth(true);
        permissionsGrid.addColumn(PermissionEntry::getPermissions).setHeader("Permissions")
            .setAutoWidth(true).setFlexGrow(2);
        
        permissionsGrid.addComponentColumn(entry -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addClickListener(e -> showEditPermissionDialog(entry));
            editButton.getStyle().set("color", "#2980b9");
            
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addClickListener(e -> showDeleteConfirmation(entry));
            deleteButton.getStyle().set("color", "#e74c3c");
            
            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        permissionsGrid.getStyle()
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 12px rgba(0,0,0,0.1)");

        // Main Container
        Div mainContainer = new Div();
        mainContainer.getStyle()
            .set("background-color", "#ffffff")
            .set("padding", "2rem")
            .set("border-radius", "1rem")
            .set("box-shadow", "0 8px 24px rgba(0,0,0,0.15)")
            .set("width", "100%");

        mainContainer.add(title, subtitle, actionBar, permissionsGrid);
        add(navBar, mainContainer);

        // Load initial data
        loadPermissionsData();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        if (sessionToken == null) {
            Notification.show("Access denied. Please log in.", 4000, Notification.Position.MIDDLE);
            event.forwardTo("login");
            return;
        }
        
        // Load permissions data after validating session
        loadPermissionsData();
    }

    private void showAddPermissionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Permission");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextField usernameField = new TextField("Username");
        ComboBox<String> roleField = new ComboBox<>("Role");
        roleField.setItems("Admin", "Manager", "Staff");
        
        CheckboxGroup<String> permissionsGroup = new CheckboxGroup<>();
        permissionsGroup.setLabel("Permissions");
        permissionsGroup.setItems(
            "View Reports",
            "Manage Users",
            "Edit Content",
            "Delete Content",
            "Approve Changes"
        );

        Button saveButton = new Button("Save", e -> {
            String username = usernameField.getValue();
            String role = roleField.getValue();
            Set<String> selectedPermissions = permissionsGroup.getValue();
            
            if (username.isEmpty() || role == null || selectedPermissions.isEmpty()) {
                Notification.show("Please fill all required fields", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            // Add new permission entry
            PermissionEntry newEntry = new PermissionEntry(
                username,
                role,
                String.join(", ", selectedPermissions)
            );
            permissions.add(newEntry);
            permissionsGrid.setItems(permissions);
            
            Notification.show("Permissions added successfully", 3000, Notification.Position.TOP_CENTER);
            dialog.close();
        });
        saveButton.getStyle()
            .set("background-color", "#3498db")
            .set("color", "white");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialogLayout.add(usernameField, roleField, permissionsGroup, buttons);
        
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void showEditPermissionDialog(PermissionEntry entry) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Permission: " + entry.getUsername());

        VerticalLayout dialogLayout = new VerticalLayout();
        TextField usernameField = new TextField("Username");
        usernameField.setValue(entry.getUsername());
        
        ComboBox<String> roleField = new ComboBox<>("Role");
        roleField.setItems("Admin", "Manager", "Staff");
        roleField.setValue(entry.getRole());
        
        CheckboxGroup<String> permissionsGroup = new CheckboxGroup<>();
        permissionsGroup.setLabel("Permissions");
        permissionsGroup.setItems(
            "View Reports",
            "Manage Users",
            "Edit Content",
            "Delete Content",
            "Approve Changes"
        );
        // Set current permissions
        Set<String> currentPermissions = Set.of(entry.getPermissions().split(", "));
        permissionsGroup.setValue(currentPermissions);

        Button saveButton = new Button("Save", e -> {
            String username = usernameField.getValue();
            String role = roleField.getValue();
            Set<String> selectedPermissions = permissionsGroup.getValue();
            
            if (username.isEmpty() || role == null || selectedPermissions.isEmpty()) {
                Notification.show("Please fill all required fields", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            // Update permission entry
            permissions.remove(entry);
            PermissionEntry updatedEntry = new PermissionEntry(
                username,
                role,
                String.join(", ", selectedPermissions)
            );
            permissions.add(updatedEntry);
            permissionsGrid.setItems(permissions);
            
            Notification.show("Permissions updated successfully", 3000, Notification.Position.TOP_CENTER);
            dialog.close();
        });
        saveButton.getStyle()
            .set("background-color", "#3498db")
            .set("color", "white");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialogLayout.add(usernameField, roleField, permissionsGroup, buttons);
        
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void showDeleteConfirmation(PermissionEntry entry) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirm Deletion");
        
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(new Paragraph("Are you sure you want to remove permissions for " + entry.getUsername() + "?"));
        
        Button deleteButton = new Button("Delete", e -> {
            permissions.remove(entry);
            permissionsGrid.setItems(permissions);
            Notification.show("Permissions removed", 3000, Notification.Position.TOP_CENTER);
            dialog.close();
        });
        deleteButton.getStyle()
            .set("background-color", "#e74c3c")
            .set("color", "white");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        
        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);
        dialogLayout.add(buttons);
        
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void loadPermissionsData() {
        // Initialize with sample data
        permissions = new ArrayList<>(List.of(
            new PermissionEntry("john.doe", "Admin", "All permissions"),
            new PermissionEntry("jane.smith", "Manager", "View Reports, Manage Users"),
            new PermissionEntry("bob.wilson", "Staff", "View Reports")
        ));
        permissionsGrid.setItems(permissions);
    }

    // Data class for permissions
    private static class PermissionEntry {
        private final String username;
        private final String role;
        private final String permissions;

        public PermissionEntry(String username, String role, String permissions) {
            this.username = username;
            this.role = role;
            this.permissions = permissions;
        }

        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getPermissions() { return permissions; }
    }
} 