package UI.views;

import UI.presenters.IManagementPresenter;
import UI.presenters.IProductPresenter;
import UI.presenters.IStorePresenter;
import UI.views.components.AddItemForm;
import Application.DTOs.ItemDTO;
import Application.DTOs.ProductDTO;
import Application.utils.Response;
import Domain.Store.Item;
import Domain.management.PermissionType;
import Application.UserService;
import Application.DTOs.UserDTO;
import Application.MarketService;
import Application.DTOs.AuctionDTO;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route("manager")
public class ManagerView extends VerticalLayout implements BeforeEnterObserver {

    private final IManagementPresenter managementPresenter;
    private final IProductPresenter productPresenter;
    private final IStorePresenter storePresenter;


    private final UserService userService;
    private final MarketService marketService;

    private String sessionToken;
    private String currentUsername;
    private String currentStoreId;

    private final Grid<UserRole> managersGrid = new Grid<>();
    private final Grid<UserRole> ownersGrid = new Grid<>();
    private final VerticalLayout mainContent = new VerticalLayout();
    private final ComboBox<PermissionType> permissionSelect = new ComboBox<>("Permission");

    private final Map<String, Set<PermissionType>> managerPermissionsMap = new HashMap<>();

    private Grid<UserRole> managersPermissionGrid;

    @Autowired

    public ManagerView(IManagementPresenter managementPresenter, UserService userService, MarketService marketService, 
                       IStorePresenter storePresenter, IProductPresenter productPresenter) {
        this.storePresenter = storePresenter;
        this.productPresenter = productPresenter;
        this.managementPresenter = managementPresenter;
        this.userService = userService;
        this.marketService = marketService;
        
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        
        // Modern gradient background
        getStyle()
            .set("background", "linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)")
            .set("--lumo-primary-color", "#2196f3");

        // Header section
        createHeader();

        // Main content area with glass effect
        mainContent.getStyle()
            .set("background", "rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("padding", "2rem")
            .set("border-radius", "15px")
            .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.1)")
            .set("margin", "1rem");

        // Create tabs for different sections
        Tab managersTab = new Tab(VaadinIcon.USERS.create(), new Span("Managers"));
        Tab ownersTab = new Tab(VaadinIcon.USER_STAR.create(), new Span("Owners"));
        Tab permissionsTab = new Tab(VaadinIcon.KEY.create(), new Span("Permissions"));
        Tab itemsTab = new Tab(VaadinIcon.CHECK.create(), new Span("Items"));
        Tab auctionsTab = new Tab(VaadinIcon.GAVEL.create(), new Span("Auctions"));
        
        // Style all tabs to have white text and icons
        for (Tab tab : new Tab[]{managersTab, ownersTab, permissionsTab, itemsTab, auctionsTab}) {
            tab.getStyle().set("color", "#ffffff");
            // Get the icon and span components from the tab
            tab.getChildren().forEach(component -> {
                component.getElement().getStyle().set("color", "#ffffff");
            });
        }
        
        Tabs tabs = new Tabs(managersTab, ownersTab, permissionsTab, itemsTab, auctionsTab);
        tabs.getStyle()
            .set("margin", "1rem 0")
            .set("--lumo-contrast-60pct", "#ffffff"); // This helps with the selected tab indicator color

        // Setup grids
        setupManagersGrid();
        setupOwnersGrid();
        setupPermissionsSection();

        // Tab change listener
        tabs.addSelectedChangeListener(event -> {
            mainContent.removeAll();
            if (event.getSelectedTab().equals(managersTab)) {
                showManagersView();
            } else if (event.getSelectedTab().equals(ownersTab)) {
                showOwnersView();
            } else if (event.getSelectedTab().equals(permissionsTab)) {
                showPermissionsView();
            } else if (event.getSelectedTab().equals(itemsTab)) {
                showItemsView();
            } else if (event.getSelectedTab().equals(auctionsTab)) {
                showAuctionsView();
            }
        });

        add(tabs, mainContent);
        showManagersView(); // Show managers view by default
    }

    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
            .set("background", "rgba(255, 255, 255, 0.1)")
            .set("padding", "1rem")
            .set("border-radius", "15px")
            .set("margin-bottom", "1rem");

        H2 title = new H2("Store Management Dashboard");
        title.getStyle()
            .set("color", "white")
            .set("margin", "0");

        Button homeButton = new Button("Return to Home", VaadinIcon.HOME.create());
        styleButton(homeButton, "var(--lumo-primary-color)");
        homeButton.addClickListener(e -> UI.getCurrent().navigate("home"));

        Button tempCloseButton = new Button("Temporarily Close Store", VaadinIcon.LOCK.create());
        styleButton(tempCloseButton, "#ff9800"); // Orange color for temporary action
        tempCloseButton.addClickListener(e -> {
            Response<Boolean> response = managementPresenter.closeStoreNotPermanent(sessionToken, currentStoreId);
            if (response.errorOccurred()) {
                Notification.show("Failed to close store: " + response.getErrorMessage(), 
                    3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Store temporarily closed. Customer baskets are preserved.", 
                    3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("home");
            }
        });

        header.add(title, new HorizontalLayout(tempCloseButton, homeButton));
        add(header);
    }

    private void setupManagersGrid() {
        managersGrid.addColumn(UserRole::getUsername).setHeader("Username");
        managersGrid.addColumn(UserRole::getRole).setHeader("Role");
        managersGrid.addComponentColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            Button permissionsButton = new Button("Permissions", VaadinIcon.KEY.create());
            styleButton(permissionsButton, "#2196f3");
            permissionsButton.addClickListener(e -> showPermissionsDialog(user.getUsername()));
            
            Button removeButton = new Button("Remove", VaadinIcon.TRASH.create());
            styleButton(removeButton, "#f44336");
            removeButton.addClickListener(e -> {
                removeManager(user.getUsername());
                refreshGrids(); // Refresh immediately after removal
            });
            
            actions.add(permissionsButton, removeButton);
            return actions;
        }).setHeader("Actions");

        managersGrid.setHeight("300px");
    }

    private void setupOwnersGrid() {
        ownersGrid.addColumn(UserRole::getUsername).setHeader("Username");
        ownersGrid.addColumn(UserRole::getRole).setHeader("Role");
        ownersGrid.addComponentColumn(user -> {
            Button removeButton = new Button("Remove", VaadinIcon.TRASH.create());
            styleButton(removeButton, "#f44336");
            removeButton.addClickListener(e -> {
                removeOwner(user.getUsername());
                refreshGrids(); // Refresh immediately after removal
            });
            return removeButton;
        }).setHeader("Actions");

        ownersGrid.setHeight("300px");
    }

    private void setupPermissionsSection() {
        permissionSelect.setItems(PermissionType.values());
        permissionSelect.setItemLabelGenerator(PermissionType::name);
    }

    private void showManagersView() {
        mainContent.removeAll();
        
        Button addButton = new Button("Add Manager", VaadinIcon.PLUS.create());
        styleButton(addButton, "#4caf50");
        addButton.addClickListener(e -> showAddUserDialog(true));

        H3 title = new H3("Store Managers");
        title.getStyle().set("color", "#ffffff");
        mainContent.add(title, addButton, managersGrid);
        refreshGrids();
    }

    private void showOwnersView() {
        mainContent.removeAll();
        
        Button addButton = new Button("Add Owner", VaadinIcon.PLUS.create());
        styleButton(addButton, "#4caf50");
        addButton.addClickListener(e -> showAddUserDialog(false));

        H3 title = new H3("Store Owners");
        title.getStyle().set("color", "#ffffff");
        mainContent.add(title, addButton, ownersGrid);
        refreshGrids();
    }

    private void showPermissionsView() {
        mainContent.removeAll();
        
        managersPermissionGrid = new Grid<>();
        managersPermissionGrid.addColumn(UserRole::getUsername).setHeader("Manager");
        
        // Add a column to show current permissions with remove buttons for each
        managersPermissionGrid.addComponentColumn(manager -> {
            HorizontalLayout permissionsLayout = new HorizontalLayout();
            permissionsLayout.setSpacing(true);
            permissionsLayout.getStyle().set("flex-wrap", "wrap");
            
            Set<PermissionType> permissions = managerPermissionsMap.getOrDefault(manager.getUsername(), new HashSet<>());
            permissions.forEach(permission -> {
                Span permissionChip = new Span(permission.name().replace("_", " ").toLowerCase());
                permissionChip.getStyle()
                    .set("background-color", "var(--lumo-primary-color-10)")
                    .set("border-radius", "16px")
                    .set("padding", "4px 8px")
                    .set("margin", "2px");

                Button removeButton = new Button(VaadinIcon.CLOSE_SMALL.create());
                removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                removeButton.addClickListener(e -> {
                    Set<PermissionType> currentPermissions = new HashSet<>(permissions);
                    currentPermissions.remove(permission);
                    updatePermissions(manager.getUsername(), new ArrayList<>(currentPermissions));
                    managerPermissionsMap.put(manager.getUsername(), currentPermissions);
                    refreshPermissionsGrid();
                });

                HorizontalLayout chip = new HorizontalLayout(permissionChip, removeButton);
                chip.setSpacing(false);
                chip.setAlignItems(Alignment.CENTER);
                permissionsLayout.add(chip);
            });
            return permissionsLayout;
        }).setHeader("Current Permissions");

        // Add actions column with add permissions button
        managersPermissionGrid.addComponentColumn(manager -> {
            Button addButton = new Button("Add Permissions", VaadinIcon.PLUS.create());
            styleButton(addButton, "#4caf50");
            addButton.addClickListener(e -> showAddPermissionsDialog(manager.getUsername()));
            return addButton;
        }).setHeader("Actions");

        managersPermissionGrid.setItems(getStoreManagers());
        managersPermissionGrid.setHeight("300px");

        H3 title = new H3("Manager Permissions");
        title.getStyle().set("color", "#ffffff");
        mainContent.add(title, managersPermissionGrid);
    }

    private void showItemsView() {
        mainContent.removeAll();
        Grid<ItemDTO> itemsGrid = new Grid<>();

        itemsGrid.addColumn(ItemDTO::getProductName).setHeader("Item Name");
        itemsGrid.addColumn(ItemDTO::getPrice).setHeader("Price");
        itemsGrid.addColumn(ItemDTO::getAmount).setHeader("Amount");
        itemsGrid.addColumn(ItemDTO::getDescription).setHeader("Description");
        itemsGrid.addColumn(ItemDTO::getRating).setHeader("Rating");

        itemsGrid.addComponentColumn(item -> {
            Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
            styleButton(deleteButton, "#f44336");
            deleteButton.addClickListener(e -> {
                Response<ItemDTO> response = managementPresenter.removeItem(sessionToken, currentStoreId, item.getProductId());
                if (response.errorOccurred()) {
                    Notification.show("Failed to delete item: " + response.getErrorMessage());
                } else {
                    Notification.show("Item deleted successfully");
                    loadItems(itemsGrid); 
                }
            });

            return new HorizontalLayout(deleteButton);
        });

        itemsGrid.setHeight("300px");

        loadItems(itemsGrid);

        Button addButton = new Button("Add Item", VaadinIcon.PLUS.create());
        styleButton(addButton, "#4caf50");
        addButton.addClickListener(e -> {
            Dialog dialog = new Dialog();
            AddItemForm addItemForm = new AddItemForm(
                () -> {
                    Response<Set<ProductDTO>> response = productPresenter.showAllProducts(sessionToken);
                    if (response.errorOccurred()) {
                        Notification.show("Failed to fetch products: " + response.getErrorMessage());
                        return Collections.emptySet();
                    }
                    return response.getValue();
                }, 
                newItem -> {
                    Response<ItemDTO> response = managementPresenter.addItem(sessionToken, currentStoreId, newItem.productId, newItem.price, newItem.amount, newItem.description);
                    if (response.errorOccurred()) {
                        Notification.show("Failed to add item: " + response.getErrorMessage());
                    } else {
                        Notification.show("Item added successfully");
                        dialog.close(); // Close first
                        loadItems(itemsGrid);
                    }
                },                
                dialog::close
            );
            dialog.add(addItemForm);
            dialog.open();
        });

        H3 title = new H3("Store Items");
        title.getStyle().set("color", "#ffffff");
        mainContent.add(title, addButton, itemsGrid);
    }

    private void showAuctionsView() {
        mainContent.removeAll();
        
        // Create grid for existing auctions
        Grid<AuctionDTO> auctionsGrid = new Grid<>();
        
        // Get all items for product name lookup
        Map<String, String> productNames = new HashMap<>();
        Response<List<ItemDTO>> itemsResponse = storePresenter.getItemsByStoreId(sessionToken, currentStoreId);
        if (!itemsResponse.errorOccurred()) {
            itemsResponse.getValue().forEach(item -> 
                productNames.put(item.getProductId(), item.getProductName())
            );
        }
        
        auctionsGrid.addColumn(auction -> 
            productNames.getOrDefault(auction.getProductId(), "Unknown Product")
        ).setHeader("Product");
        auctionsGrid.addColumn(auction -> {
            Date startDate = auction.getAuctionStartDate();
            return startDate != null ? startDate.toString() : "";
        }).setHeader("Start Date");
        auctionsGrid.addColumn(auction -> {
            Date endDate = auction.getAuctionEndDate();
            return endDate != null ? endDate.toString() : "";
        }).setHeader("End Date");
        auctionsGrid.addColumn(AuctionDTO::getStartPrice).setHeader("Start Price");
        auctionsGrid.addColumn(AuctionDTO::getCurrentPrice).setHeader("Current Price");
        auctionsGrid.addColumn(auction -> auction.getCurrentBidderId() != null ? 
            "Yes" : "No"
        ).setHeader("Has Bidder");
        
        // Add Accept Bid button column
        auctionsGrid.addComponentColumn(auction -> {
            if (auction.getCurrentBidderId() != null) {
                Button acceptButton = new Button("Accept Bid", e -> {
                    // Confirm dialog
                    Dialog confirmDialog = new Dialog();
                    confirmDialog.setHeaderTitle("Confirm Bid Acceptance");
                    
                    VerticalLayout dialogLayout = new VerticalLayout();
                    dialogLayout.setSpacing(true);
                    dialogLayout.setPadding(true);
                    
                    dialogLayout.add(new H4("Are you sure you want to accept the current bid?"));
                    dialogLayout.add(new Span("Product: " + productNames.getOrDefault(auction.getProductId(), "Unknown Product")));
                    dialogLayout.add(new Span("Current Price: $" + auction.getCurrentPrice()));
                    
                    Button confirmButton = new Button("Accept", event -> {
                        Response<ItemDTO> response = storePresenter.acceptBid(
                            sessionToken,
                            currentStoreId,
                            auction.getProductId(),
                            auction.getAuctionId()
                        );
                        
                        if (!response.errorOccurred()) {
                            Notification.show("Bid accepted successfully!");
                            confirmDialog.close();
                            
                            // Refresh the auctions grid
                            Response<List<AuctionDTO>> refreshResponse = storePresenter.getAllStoreAuctions(sessionToken, currentStoreId);
                            if (!refreshResponse.errorOccurred()) {
                                auctionsGrid.setItems(refreshResponse.getValue());
                            }
                        } else {
                            Notification.show("Failed to accept bid: " + response.getErrorMessage());
                        }
                    });
                    
                    Button cancelButton = new Button("Cancel", event -> confirmDialog.close());
                    
                    // Style buttons
                    confirmButton.getStyle()
                        .set("background-color", "#38a169")
                        .set("color", "white");
                    cancelButton.getStyle()
                        .set("background-color", "#e53e3e")
                        .set("color", "white");
                    
                    HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
                    buttons.setJustifyContentMode(JustifyContentMode.END);
                    dialogLayout.add(buttons);
                    
                    confirmDialog.add(dialogLayout);
                    confirmDialog.open();
                });
                
                acceptButton.getStyle()
                    .set("background-color", "#38a169")
                    .set("color", "white");
                
                return acceptButton;
            }
            return new Span("No bids yet");
        }).setHeader("Actions");
        
        // Load existing auctions
        Response<List<AuctionDTO>> auctionsResponse = storePresenter.getAllStoreAuctions(sessionToken, currentStoreId);
        if (!auctionsResponse.errorOccurred()) {
            auctionsGrid.setItems(auctionsResponse.getValue());
        } else {
            Notification.show("Failed to load auctions: " + auctionsResponse.getErrorMessage());
        }
        
        // Create form for new auction
        Dialog addAuctionDialog = new Dialog();
        addAuctionDialog.setHeaderTitle("Add New Auction");
        
        // Product selection
        ComboBox<ItemDTO> productSelect = new ComboBox<>("Select Product");
        Response<List<ItemDTO>> itemsResponse2 = storePresenter.getItemsByStoreId(sessionToken, currentStoreId);
        if (!itemsResponse2.errorOccurred()) {
            productSelect.setItems(itemsResponse2.getValue());
            productSelect.setItemLabelGenerator(item -> item.getProductName());
        }
        
        // Date and Time pickers for end date
        DatePicker endDatePicker = new DatePicker("Auction End Date");
        endDatePicker.setMin(LocalDate.now()); // Today is allowed if time is later
        
        TimePicker endTimePicker = new TimePicker("Auction End Time");
        endTimePicker.setStep(Duration.ofMinutes(1)); // Allow 1-minute intervals
        endTimePicker.setMin(LocalTime.now().plusMinutes(1)); // At least 1 minute from now
        
        // If today is selected, ensure time is in the future
        endDatePicker.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue().equals(LocalDate.now())) {
                endTimePicker.setMin(LocalTime.now().plusMinutes(1));
            } else {
                endTimePicker.setMin(null);
            }
        });
        
        // Price field
        NumberField startPriceField = new NumberField("Start Price");
        startPriceField.setMin(0);
        startPriceField.setStep(0.01);
        
        // Add auction button
        Button addButton = new Button("Add Auction", e -> {
            if (productSelect.getValue() == null || endDatePicker.getValue() == null || 
                endTimePicker.getValue() == null || startPriceField.getValue() == null) {
                Notification.show("Please fill in all fields");
                return;
            }
            
            // Validate that if date is today, time must be in the future
            LocalDateTime selectedDateTime = LocalDateTime.of(endDatePicker.getValue(), endTimePicker.getValue());
            if (selectedDateTime.isBefore(LocalDateTime.now())) {
                Notification.show("End time must be in the future");
                return;
            }
            
            String productId = productSelect.getValue().getProductId();
            // Format the date and time as required by the backend (yyyy-MM-dd HH:mm)
            String endDateTime = selectedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            double startPrice = startPriceField.getValue();
            
            Response<AuctionDTO> response = storePresenter.addAuction(
                sessionToken,
                currentStoreId,
                productId,
                endDateTime,
                startPrice
            );
            
            if (response.errorOccurred()) {
                Notification.show("Failed to create auction: " + response.getErrorMessage());
            } else {
                Notification.show("Auction created successfully");
                addAuctionDialog.close();
                
                // Refresh the grid
                Response<List<AuctionDTO>> refreshResponse = storePresenter.getAllStoreAuctions(sessionToken, currentStoreId);
                if (!refreshResponse.errorOccurred()) {
                    auctionsGrid.setItems(refreshResponse.getValue());
                }
            }
        });
        
        Button cancelButton = new Button("Cancel", e -> addAuctionDialog.close());
        
        // Layout for the dialog
        VerticalLayout dialogLayout = new VerticalLayout(
            productSelect,
            new HorizontalLayout(endDatePicker, endTimePicker),
            startPriceField,
            new HorizontalLayout(addButton, cancelButton)
        );
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);
        addAuctionDialog.add(dialogLayout);
        
        // Button to open the dialog
        Button openDialogButton = new Button("Add New Auction", VaadinIcon.PLUS.create());
        styleButton(openDialogButton, "#4caf50");
        openDialogButton.addClickListener(e -> addAuctionDialog.open());
        
        // Add refresh button
        Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        styleButton(refreshButton, "#2196f3");
        refreshButton.addClickListener(e -> {
            // Refresh the auctions grid
            Response<List<AuctionDTO>> refreshResponse = storePresenter.getAllStoreAuctions(sessionToken, currentStoreId);
            if (!refreshResponse.errorOccurred()) {
                auctionsGrid.setItems(refreshResponse.getValue());
                Notification.show("Auctions refreshed", 2000, Notification.Position.BOTTOM_START);
            } else {
                Notification.show("Failed to refresh auctions: " + refreshResponse.getErrorMessage());
            }
        });
        
        // Create button layout
        HorizontalLayout buttonLayout = new HorizontalLayout(openDialogButton, refreshButton);
        buttonLayout.setSpacing(true);
        
        // Add components to the main content
        H3 title = new H3("Store Auctions");
        title.getStyle().set("color", "#ffffff");
        mainContent.add(title, buttonLayout, auctionsGrid);
        
        auctionsGrid.setHeight("300px");
    }

    private void loadItems(Grid<ItemDTO> itemsGrid) {
        Response<List<ItemDTO>> itemsResponse = storePresenter.getItemsByStoreId(sessionToken, currentStoreId);
        if (itemsResponse.errorOccurred()) {
            Notification.show("Failed to fetch items: " + itemsResponse.getErrorMessage());
        } else {
            itemsGrid.setItems(itemsResponse.getValue());
        }
    }

    private void showAddUserDialog(boolean isManager) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isManager ? "Add New Manager" : "Add New Owner");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        TextField usernameField = new TextField("Username");
        usernameField.setRequired(true);
        usernameField.setWidth("100%");

        if (isManager) {
            MultiSelectComboBox<PermissionType> permissionsSelect = new MultiSelectComboBox<>("Initial Permissions");
            permissionsSelect.setItems(PermissionType.values());
            permissionsSelect.setItemLabelGenerator(permission -> 
                permission.name().replace("_", " ").toLowerCase());
            permissionsSelect.setWidth("100%");
            permissionsSelect.setHelperText("Select initial permissions for the manager");
            dialogLayout.add(usernameField, permissionsSelect);

            Button saveButton = new Button("Save", e -> {
                String username = usernameField.getValue();
                if (username != null && !username.isEmpty()) {
                    // Check if user exists
                    Response<Boolean> existsResponse = marketService.userExists(username);
                    if (existsResponse.errorOccurred() || !existsResponse.getValue()) {
                        Notification.show("User does not exist in the system");
                        return;
                    }
                    Set<PermissionType> initialPermissions = permissionsSelect.getSelectedItems();
                    dialog.close(); // Close first
                    appointManagerWithPermissions(username, initialPermissions);
                } else {
                    Notification.show("Please enter a username");
                }
            });
            styleButton(saveButton, "var(--lumo-primary-color)");

            Button cancelButton = new Button("Cancel", e -> dialog.close());
            styleButton(cancelButton, "#9e9e9e");

            HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
            buttons.setJustifyContentMode(JustifyContentMode.END);
            dialogLayout.add(buttons);
        } else {
            dialogLayout.add(usernameField);

            Button saveButton = new Button("Save", e -> {
                String username = usernameField.getValue();
                if (username != null && !username.isEmpty()) {
                    // Check if user exists
                    Response<Boolean> existsResponse = marketService.userExists(username);
                    if (existsResponse.errorOccurred() || !existsResponse.getValue()) {
                        Notification.show("User does not exist in the system");
                        return;
                    }
                    dialog.close(); // Close first
                    Response<Void> response = managementPresenter.appointStoreOwner(
                        sessionToken, currentUsername, username, currentStoreId);
                    if (response.errorOccurred()) {
                        Notification.show("Failed to appoint owner: " + response.getErrorMessage());
                    } else {
                        storeOwners.add(new UserRole(username, "Owner"));
                        refreshGrids();
                        Notification.show("Owner appointed successfully");
                    }
                } else {
                    Notification.show("Please enter a username");
                }
            });
            styleButton(saveButton, "var(--lumo-primary-color)");

            Button cancelButton = new Button("Cancel", e -> dialog.close());
            styleButton(cancelButton, "#9e9e9e");

            HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
            buttons.setJustifyContentMode(JustifyContentMode.END);
            dialogLayout.add(buttons);
        }

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void showPermissionsDialog(String username) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Manage Permissions for " + username);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        // Use MultiSelectComboBox for permissions
        MultiSelectComboBox<PermissionType> permissionsSelect = new MultiSelectComboBox<>("Permissions");
        permissionsSelect.setItems(PermissionType.values());
        permissionsSelect.setItemLabelGenerator(permission -> 
            permission.name().replace("_", " ").toLowerCase());
        permissionsSelect.setWidth("100%");
        
        // Set current permissions from our map
        Set<PermissionType> currentPermissions = managerPermissionsMap.getOrDefault(username, new HashSet<>());
        permissionsSelect.setValue(currentPermissions);

        Button saveButton = new Button("Save", e -> {
            List<PermissionType> newPermissions = new ArrayList<>(permissionsSelect.getSelectedItems());
            updatePermissions(username, newPermissions);
            managerPermissionsMap.put(username, new HashSet<>(newPermissions));
            dialog.close();
            refreshGrids(); // Refresh to update permissions display
        });
        styleButton(saveButton, "var(--lumo-primary-color)");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        styleButton(cancelButton, "#9e9e9e");

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);

        dialogLayout.add(permissionsSelect, buttons);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void showAddPermissionsDialog(String username) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Permissions for " + username);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        Set<PermissionType> currentPermissions = managerPermissionsMap.getOrDefault(username, new HashSet<>());
        
        // Show only permissions that aren't already assigned
        Set<PermissionType> availablePermissions = EnumSet.allOf(PermissionType.class);
        availablePermissions.removeAll(currentPermissions);

        if (availablePermissions.isEmpty()) {
            Notification.show("All permissions have been assigned");
            dialog.close();
            return;
        }

        MultiSelectComboBox<PermissionType> permissionsSelect = new MultiSelectComboBox<>("Add Permissions");
        permissionsSelect.setItems(availablePermissions);
        permissionsSelect.setItemLabelGenerator(permission -> 
            permission.name().replace("_", " ").toLowerCase());
        permissionsSelect.setWidth("100%");

        Button saveButton = new Button("Add", e -> {
            Set<PermissionType> selectedPermissions = permissionsSelect.getSelectedItems();
            if (!selectedPermissions.isEmpty()) {
                dialog.close(); // Close first
                Set<PermissionType> newPermissions = new HashSet<>(currentPermissions);
                newPermissions.addAll(selectedPermissions);
                managerPermissionsMap.put(username, newPermissions);
                updatePermissions(username, new ArrayList<>(newPermissions));
                refreshPermissionsGrid();
                Notification.show("Permissions added successfully");
            } else {
                Notification.show("Please select at least one permission");
            }
        });
        styleButton(saveButton, "var(--lumo-primary-color)");

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        styleButton(cancelButton, "#9e9e9e");

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);

        dialogLayout.add(permissionsSelect, buttons);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void refreshPermissionsGrid() {
        if (managersPermissionGrid != null) {
            managersPermissionGrid.getDataProvider().refreshAll();
            UI.getCurrent().push(); // Force UI update
        }
    }

    private void refreshGrids() {
        // Create lists to store our data
        List<UserRole> managers = new ArrayList<>();
        List<UserRole> owners = new ArrayList<>();
        List<ItemDTO> items = new ArrayList<>();

        // Add the current store's managers and owners
        if (currentStoreId != null) {
            managers.addAll(getStoreManagers());
            owners.addAll(getStoreOwners());
            items.addAll(storePresenter.getItemsByStoreId(sessionToken, currentStoreId).getValue());
        }

        // Update the grids
        managersGrid.setItems(managers);
        ownersGrid.setItems(owners);
        
        // Also refresh the permissions grid if we're in that view
        refreshPermissionsGrid();
    }

    private void updatePermissions(String username, List<PermissionType> permissions) {
        Response<Void> response = managementPresenter.changeManagerPermissions(
            sessionToken,
            currentUsername,
            username,
            currentStoreId,
            permissions
        );
        
        if (response.errorOccurred()) {
            Notification.show("Failed to update permissions: " + response.getErrorMessage());
        } else {
            managerPermissionsMap.put(username, new HashSet<>(permissions));
            Notification.show("Permissions updated successfully");
            refreshPermissionsGrid();
        }
    }

    // Helper method to maintain managers list (in a real app this would come from the presenter)
    private Set<UserRole> storeManagers = new HashSet<>();
    private Set<UserRole> storeOwners = new HashSet<>();

    private List<UserRole> getStoreManagers() {
        return new ArrayList<>(storeManagers);
    }

    private List<UserRole> getStoreOwners() {
        return new ArrayList<>(storeOwners);
    }

    private void appointManager(String username) {
        Response<Void> response = managementPresenter.appointStoreManager(
            sessionToken, currentUsername, username, currentStoreId);
        if (response.errorOccurred()) {
            Notification.show("Failed to appoint manager: " + response.getErrorMessage());
        } else {
            storeManagers.add(new UserRole(username, "Manager"));
            // Initialize with basic permissions
            Set<PermissionType> basicPermissions = new HashSet<>(Arrays.asList(
                PermissionType.VIEW_EMPLOYEE_INFO,
                PermissionType.ACCESS_PURCHASE_RECORDS
            ));
            managerPermissionsMap.put(username, basicPermissions);
            updatePermissions(username, new ArrayList<>(basicPermissions));
            Notification.show("Manager appointed successfully");
            refreshGrids();
        }
    }

    private void appointOwner(String username) {
        Response<Void> response = managementPresenter.appointStoreOwner(
            sessionToken, currentUsername, username, currentStoreId);
        if (response.errorOccurred()) {
            Notification.show("Failed to appoint owner: " + response.getErrorMessage());
        } else {
            storeOwners.add(new UserRole(username, "Owner"));
            Notification.show("Owner appointed successfully");
            refreshGrids();
        }
    }

    private void removeManager(String username) {
        Response<Void> response = managementPresenter.removeStoreManager(
            sessionToken, currentUsername, username, currentStoreId);
        if (response.errorOccurred()) {
            Notification.show("Failed to remove manager: " + response.getErrorMessage());
        } else {
            storeManagers.removeIf(user -> user.getUsername().equals(username));
            managerPermissionsMap.remove(username); // Remove permissions when manager is removed
            Notification.show("Manager removed successfully");
            refreshGrids();
        }
    }

    private void removeOwner(String username) {
        // In a real implementation, this would call the appropriate method
        Notification.show("Owner removal not implemented");
        storeOwners.removeIf(user -> user.getUsername().equals(username));
        refreshGrids();
    }

    private void appointManagerWithPermissions(String username, Set<PermissionType> initialPermissions) {
        Response<Void> response = managementPresenter.appointStoreManager(
            sessionToken, currentUsername, username, currentStoreId);
        if (response.errorOccurred()) {
            Notification.show("Failed to appoint manager: " + response.getErrorMessage());
        } else {
            storeManagers.add(new UserRole(username, "Manager"));
            managerPermissionsMap.put(username, initialPermissions);
            updatePermissions(username, new ArrayList<>(initialPermissions));
            Notification.show("Manager appointed successfully");
            refreshGrids();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        currentUsername = (String) UI.getCurrent().getSession().getAttribute("username");
        currentStoreId = (String) UI.getCurrent().getSession().getAttribute("currentStoreId");
        
        if (sessionToken == null || currentStoreId == null) {
            Notification.show("Please select a store first", 3000, Notification.Position.MIDDLE);
            event.forwardTo("store-search");
        }

        // Clear permissions map when entering a new store
        managerPermissionsMap.clear();
    }

    private void styleButton(Button button, String color) {
        button.getStyle()
            .set("background-color", color)
            .set("color", "white")
            .set("border-radius", "25px")
            .set("font-weight", "500")
            .set("padding", "0.5em 1.5em")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");

        // Add hover effect
        button.getElement().addEventListener("mouseover", e -> 
            button.getStyle()
                .set("transform", "translateY(-2px)")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.3)"));

        button.getElement().addEventListener("mouseout", e -> 
            button.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));
    }

    private static class UserRole {
        private final String username;
        private final String role;

        public UserRole(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getRole() { return role; }
    }

    private static class MultiSelectComboBox<T> extends com.vaadin.flow.component.combobox.ComboBox<T> {
        private final Set<T> selected = new HashSet<>();

        public MultiSelectComboBox() {
            super();
            addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    selected.add(e.getValue());
                    clear();
                    updateDisplay();
                }
            });
        }

        public MultiSelectComboBox(String label) {
            this();
            setLabel(label);
        }

        public void setValue(Set<T> values) {
            selected.clear();
            if (values != null) {
                selected.addAll(values);
            }
            updateDisplay();
        }

        public Set<T> getSelectedItems() {
            return Collections.unmodifiableSet(selected);
        }

        private void updateDisplay() {
            String display = selected.isEmpty() ? "" : 
                selected.size() + " selected";
            setPlaceholder(display);
        }
    }
} 