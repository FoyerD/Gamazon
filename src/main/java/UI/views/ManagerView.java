package UI.views;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import Application.DTOs.AuctionDTO;
import Application.DTOs.ClientOrderDTO;
import Application.DTOs.EmployeeInfo;
import Application.DTOs.ItemDTO;
import Application.DTOs.ProductDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import Domain.management.PermissionType;
import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;
import UI.presenters.ILoginPresenter;
import UI.presenters.IManagementPresenter;
import UI.presenters.IProductPresenter;
import UI.presenters.IStorePresenter;
import UI.presenters.LoginPresenter;
import UI.views.components.AddItemForm;
import UI.views.components.AddUserRoleDialog;
import UI.views.components.ChangeUserRoleDialog;
import UI.views.components.EmployeesLayout;
import UI.views.dataobjects.UserPermission;

@Route("manager")
public class ManagerView extends BaseView implements BeforeEnterObserver {

    private final IManagementPresenter managementPresenter;
    private final IProductPresenter productPresenter;
    private final IStorePresenter storePresenter;
    private final ILoginPresenter loginPresenter;



    private String sessionToken;
    private String currentStoreId;

    private final EmployeesLayout employeesLayout;
    private final VerticalLayout mainContent = new VerticalLayout();


    private final Map<String, Set<PermissionType>> managerPermissionsMap = new HashMap<>();

    @Autowired
    public ManagerView(IManagementPresenter managementPresenter, 
                       IStorePresenter storePresenter, IProductPresenter productPresenter, LoginPresenter loginPresenter, @Autowired(required = false) DbHealthStatus dbHealthStatus, @Autowired(required = false) GlobalLogoutManager logoutManager) {
        super(dbHealthStatus, logoutManager);
        this.storePresenter = storePresenter;
        this.productPresenter = productPresenter;
        this.managementPresenter = managementPresenter;
        this.loginPresenter = loginPresenter;
        
        setSizeFull();
        setSpacing(false);
        setPadding(true);
        
        // Modern gradient background
        getStyle()
            .set("background", "linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)")
            .set("min-height", "100vh")  // Force full vertical background
            .set("overflow", "auto")     // Allow page scrolling
            .set("--lumo-primary-color", " #2196f3");
                    

        // Header section
        createHeader();

        // Main content area with glass effect
        mainContent.getStyle()
            .set("background", "rgba(255, 255, 255, 0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("padding", "2rem")
            .set("border-radius", "15px")
            .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.1)")
            .set("margin", "1rem")
            .set("width", "90%")
            .set("max-width", "1400px")
            .set("flex-grow", "1");

        // Create tabs for different sections
        Tab employeesTab = new Tab(VaadinIcon.USERS.create(), new Span("Employees"));
        Tab itemsTab = new Tab(VaadinIcon.CHECK.create(), new Span("Items"));
        Tab auctionsTab = new Tab(VaadinIcon.GAVEL.create(), new Span("Auctions"));
        Tab historyTab = new Tab(VaadinIcon.TIME_BACKWARD.create(), new Span("History"));
        

        // Style all tabs to have white text and icons
        for (Tab tab : new Tab[]{employeesTab, itemsTab, auctionsTab, historyTab}) {
            tab.getStyle().set("color", " #ffffff");
            // Get the icon and span components from the tab
            tab.getChildren().forEach(component -> {
                component.getElement().getStyle().set("color", "#ffffff");
            });
        }
        
        Tabs tabs = new Tabs(employeesTab, itemsTab, auctionsTab, historyTab);
        tabs.getStyle()
            .set("margin", "1rem 0")
            .set("--lumo-contrast-60pct", " #ffffff"); 
            
        // Setup grids
        employeesLayout = new EmployeesLayout(
            () -> {
                Response<EmployeeInfo> response = managementPresenter.getEmployeeInfo(sessionToken, currentStoreId);
                if (response.errorOccurred()) {
                    Notification.show("Failed to fetch managers' permissions: " + response.getErrorMessage(), 
                        3000, Notification.Position.MIDDLE);
                    return null;
                }
                return response.getValue();
            }, 
            () -> showAddUserDialog(),
            up -> showChangeManagerPermissionsDialog(up),
            u -> {
                Response<Void> response = managementPresenter.removeStoreOwner(sessionToken, u.getId(), currentStoreId);
                if (response.errorOccurred()) {
                    Notification.show("Failed to remove owner: " + response.getErrorMessage(), 
                        3000, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Owner " + u.getUsername() + " removed successfully", 
                        3000, Notification.Position.MIDDLE);
                }
            }
        );


      

        // Tab change listener
        tabs.addSelectedChangeListener(event -> {
            mainContent.removeAll();
            if (event.getSelectedTab().equals(employeesTab)) {
                showEmployeesInfo();
            } else if (event.getSelectedTab().equals(itemsTab)) {
                showItemsView();
            } else if (event.getSelectedTab().equals(auctionsTab)) {
                showAuctionsView();
            } else if (event.getSelectedTab().equals(historyTab)) {
                showHistoryView();
            }
        });

        mainContent.setSizeFull();

        addAndExpand(tabs, mainContent);
        showEmployeesInfo(); // Show employees view by default
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
    
    private void showEmployeesInfo() {
        mainContent.add(employeesLayout);
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
        mainContent.add(new H3("Store Items"), addButton, itemsGrid);
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

    private void showHistoryView() {
        mainContent.removeAll();
        
        Response<List<ClientOrderDTO>> historyResponse = managementPresenter.getPurchaseHistory(sessionToken, currentStoreId);
        if (historyResponse.errorOccurred()) {
            Notification.show("Failed to fetch purchase history: " + historyResponse.getErrorMessage());
        } else if (historyResponse.getValue().isEmpty()) {
            Notification.show("No purchase history found for this store.", 3000, Notification.Position.BOTTOM_END);
        } else {
            Grid<ClientOrderDTO> historyGrid = new Grid<>(ClientOrderDTO.class, false);

            historyGrid.addColumn(ClientOrderDTO::getClientName).setHeader("Client").setAutoWidth(true);
            historyGrid.addColumn(o -> String.valueOf(o.getTotalPrice()) + "$").setHeader("Total Price").setAutoWidth(true);
            historyGrid.addComponentColumn(receipt -> {
                Div detailsDiv = new Div();

                // Populate receipt details
                receipt.getItems().forEach(item -> {
                    Span itemDetail = new Span(item.getProductName() + " - " + item.getQuantity() + " x $" + item.getPrice());
                    itemDetail.getStyle().set("display", "block");
                    detailsDiv.add(itemDetail);
                });

                return detailsDiv;
            }).setHeader("Items");

            historyGrid.setItems(historyResponse.getValue());
            historyGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
            historyGrid.setWidthFull();
            mainContent.add(historyGrid);
        }
    }

    private void loadItems(Grid<ItemDTO> itemsGrid) {
        Response<List<ItemDTO>> itemsResponse = storePresenter.getItemsByStoreId(sessionToken, currentStoreId);
        if (itemsResponse.errorOccurred()) {
            Notification.show("Failed to fetch items: " + itemsResponse.getErrorMessage());
        } else {
            itemsGrid.setItems(itemsResponse.getValue());
        }
    }
    
    private List<UserDTO> getManagementCandidates() {
        Response<List<UserDTO>> usersResponse = loginPresenter.getAllMembers(sessionToken);
        Response<StoreDTO> storeResponse = storePresenter.getStoreById(sessionToken, currentStoreId);
        if (usersResponse.errorOccurred()) {
            Notification.show("Failed to fetch users: " + usersResponse.getErrorMessage());
            return null;
        }

        if (storeResponse.errorOccurred()) {
            Notification.show("Failed to fetch store info: " + storeResponse.getErrorMessage());
            return null;
        }

        List<UserDTO> members = usersResponse.getValue();
        StoreDTO currentStore = storeResponse.getValue();
        return  members.stream()
            .filter(user -> !currentStore.getOwners().stream()
                                .anyMatch(owner -> owner.equals(user.getId()))
                                && !currentStore.getManagers().stream()
                                .anyMatch(manager -> manager.equals(user.getId())))
            .collect(Collectors.toList());
    }

    private List<UserDTO> getOwnershipCandidates() {
        Response<List<UserDTO>> usersResponse = loginPresenter.getAllMembers(sessionToken);
        Response<StoreDTO> storeResponse = storePresenter.getStoreById(sessionToken, currentStoreId);
        if (usersResponse.errorOccurred()) {
            Notification.show("Failed to fetch users: " + usersResponse.getErrorMessage());
            return null;
        }

        if (storeResponse.errorOccurred()) {
            Notification.show("Failed to fetch store info: " + storeResponse.getErrorMessage());
            return null;
        }

        List<UserDTO> members = usersResponse.getValue();
        StoreDTO currentStore = storeResponse.getValue();
        return  members.stream()
            .filter(user -> !currentStore.getOwners().stream()
                                .anyMatch(owner -> owner.equals(user.getId()))
                                && !currentStore.getOwners().stream()
                                .anyMatch(manager -> manager.equals(user.getId())))
            .collect(Collectors.toList());
    }

    private boolean appointManager(UserPermission userPermissions) {
        Response<Void> response = managementPresenter.appointStoreManager(sessionToken, userPermissions.user.getId(), currentStoreId);
        if (response.errorOccurred()) {
            Notification.show("Failed to appoint manager: " + response.getErrorMessage());
            return false;
        } 
        Response<Void> permissionsResponse = managementPresenter.changeManagerPermissions(
            sessionToken, userPermissions.user.getId(), currentStoreId, userPermissions.permissions);
        if (permissionsResponse.errorOccurred()) {
            Notification.show("Failed to set permissions: " + permissionsResponse.getErrorMessage());
            return false;
        }
        Notification.show("Manager appointed successfully");
        employeesLayout.refreshUsers();
        return true; 
}

    private boolean appointOwner(UserDTO user) {
        Response<Void> response = managementPresenter.appointStoreOwner(sessionToken, user.getId(), currentStoreId);
        if (response.errorOccurred()) {
            Notification.show("Failed to appoint owner: " + response.getErrorMessage());
            return false;
        } 

        Notification.show("Owner appointed successfully");
        employeesLayout.refreshUsers();
        return true; 
    }

    private void showAddUserDialog() {
        AddUserRoleDialog dialog = new AddUserRoleDialog(
            this::getManagementCandidates,
            this::getOwnershipCandidates,
            this::appointManager,
            this::appointOwner
        );

        dialog.open();
    }

    private void showChangeManagerPermissionsDialog(UserPermission user) {
        ChangeUserRoleDialog dialog = new ChangeUserRoleDialog(
            user,
            up -> {
                Response<Void> res = managementPresenter.changeManagerPermissions(sessionToken, 
                                                                                up.user.getId(), 
                                                                                currentStoreId, 
                                                                                up.permissions);
                if (res.errorOccurred()) {
                    Notification.show("Failed to change permissions: " + res.getErrorMessage());
                    return false;
                }
                Notification.show("Permissions updated successfully");
                employeesLayout.refreshUsers();
                return true;
            });

        dialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        currentStoreId = (String) UI.getCurrent().getSession().getAttribute("currentStoreId");
        if (sessionToken == null) {
            Notification.show("Access denied. Please log in.", 4000, Notification.Position.MIDDLE);
            event.forwardTo("");
        }
        if (currentStoreId == null) {
            Notification.show("Please select a store first", 3000, Notification.Position.MIDDLE);
            event.forwardTo("store-search");
        }
        
        // load store managers and owners
        employeesLayout.refreshUsers();
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
} 