package UI.views;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import Application.DTOs.AuctionDTO;
import Application.DTOs.CategoryDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.UserDTO;
import Application.MarketService;
import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.Store.ItemFilter;
import Domain.management.PermissionManager;
import UI.DatabaseRelated.DbHealthStatus;
import UI.presenters.ILoginPresenter;
import UI.presenters.INotificationPresenter;
import UI.presenters.IProductPresenter;
import UI.presenters.IPurchasePresenter;
import UI.presenters.IUserSessionPresenter;



@JsModule("./ws-client.js")
@Route("home")
public class HomePageView extends VerticalLayout implements BeforeEnterObserver {

    private final IProductPresenter productPresenter;
    private final IPurchasePresenter purchasePresenter;
    private final ILoginPresenter loginPresenter;
    private final IUserSessionPresenter sessionPresenter;
    private final MarketService marketService;
    private final PermissionManager permissionManager;
    private String sessionToken = null;
    private String currentUsername = null;
    private UserDTO user = null;

    private boolean isBanned = false;


    private final TextField searchBar = new TextField();
    private final Grid<ItemDTO> productGrid = new Grid<>(ItemDTO.class);
    
    // Filter components
    private final NumberField minPriceField = new NumberField("Min Price");
    private final NumberField maxPriceField = new NumberField("Max Price");
    private final NumberField minRatingField = new NumberField("Min Rating");
    private final NumberField maxRatingField = new NumberField("Max Rating");
    private final MultiSelectComboBox<CategoryDTO> categoryFilter = new MultiSelectComboBox<>("Categories");
    private final NumberField minAmountField = new NumberField("Min Amount");
    private final Dialog filterDialog = new Dialog();
    private final Span activeFiltersLabel = new Span();

    private final Button filterBtn = new Button("Filters");
    private final Button refreshBtn = new Button("Refresh");
    private final Button cartBtn = new Button("View Cart");
    private final Button goToSearchBtn = new Button("Search Stores");

    private final INotificationPresenter notificationPresenter;

    private final DbHealthStatus dbHealthStatus;

    public HomePageView(IProductPresenter productPresenter, IUserSessionPresenter sessionPresenter, 
                        IPurchasePresenter purchasePresenter, ILoginPresenter loginPresenter, INotificationPresenter notificationPresenter,
                        MarketService marketService, PermissionManager permissionManager, @Autowired(required = false) DbHealthStatus dbHealthStatus) {
        this.notificationPresenter = notificationPresenter;
        this.productPresenter = productPresenter;
        this.sessionPresenter = sessionPresenter;
        this.purchasePresenter = purchasePresenter;
        this.loginPresenter = loginPresenter;
        this.marketService = marketService;
        this.permissionManager = permissionManager;
        this.dbHealthStatus = dbHealthStatus;

        setSizeFull();
        setSpacing(false);
        setPadding(true);
        
        // Modern gradient background
        getStyle()
            .set("background", "linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)")
            .set("--lumo-primary-color", "#2196f3");

        H1 title = new H1("Gamazon Home");

        title.getStyle().set("color", "#ffffff");
        
        this.currentUsername = (String) UI.getCurrent().getSession().getAttribute("username");

        Button userInfo = new Button("Logged in as: " + (currentUsername != null ? currentUsername : "Unknown"),
                                        VaadinIcon.USER.create(),
                                        e -> {
                                            UI.getCurrent().getSession().setAttribute("user", user);
                                            UI.getCurrent().navigate("user-profile");
                                        });
        userInfo.getStyle().set("color", " #2d3748").set("background-color", " #ebc934").set("font-weight", "bold");


        // Configure filter components
        setupFilterComponents();
        setupFilterDialog();

        searchBar.setPlaceholder("Search for products...");
        searchBar.setWidth("300px");
        searchBar.getStyle().set("background-color", "#ffffff");
        searchBar.addValueChangeListener(e -> applyFilters());

        // Initialize buttons with click handlers
        filterBtn.addClickListener(e -> filterDialog.open());
        filterBtn.getStyle()
            .set("background-color", " #4299e1")
            .set("color", "white");



        refreshBtn.addClickListener(e -> loadAllProducts());
        refreshBtn.getStyle()
            .set("background-color", "#2b6cb0")
            .set("color", "white");

        goToSearchBtn.addClickListener(e -> UI.getCurrent().navigate("store-search"));
        goToSearchBtn.getStyle()
            .set("background-color", "#3182ce")
            .set("color", "white");

        cartBtn.addClickListener(e -> UI.getCurrent().navigate("cart"));
        cartBtn.getStyle()
            .set("background-color", "#38a169")
            .set("color", "white");

        Button registerBtn = new Button("Register", e -> UI.getCurrent().navigate("register"));
        registerBtn.getStyle().set("background-color", " #6b46c1").set("color", "white");

        Button logoutBtn = new Button("Logout", e -> {
            Response<Void> response = loginPresenter.logout(sessionToken);
            if (!response.errorOccurred()) {
                UI.getCurrent().getSession().close();
                UI.getCurrent().navigate("");
            } else {
                Notification.show("Failed to logout: " + response.getErrorMessage(), 
                                3000, Notification.Position.MIDDLE);
            }
        });
        logoutBtn.getStyle().set("background-color", "#e53e3e").set("color", "white");

        HorizontalLayout searchAndFilter = new HorizontalLayout(searchBar, filterBtn, refreshBtn);
        searchAndFilter.setAlignItems(Alignment.BASELINE);

        HorizontalLayout welcomeLayout = new HorizontalLayout(title, userInfo);
        welcomeLayout.setAlignItems(Alignment.BASELINE);
        welcomeLayout.setWidthFull();
        welcomeLayout.setJustifyContentMode(JustifyContentMode.START);
        HorizontalLayout navButtons = new HorizontalLayout(searchAndFilter, goToSearchBtn, cartBtn, registerBtn, logoutBtn);
        navButtons.setJustifyContentMode(JustifyContentMode.END);
        navButtons.getStyle().set("padding", "10px");

        HorizontalLayout toptopBar = new HorizontalLayout(welcomeLayout, navButtons);
        toptopBar.setWidthFull();
        toptopBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        VerticalLayout topBar = new VerticalLayout(toptopBar, searchAndFilter);
        // Style active filters label
        activeFiltersLabel.getStyle()
            .set("color", "#4a5568")
            .set("font-size", "0.9em")
            .set("margin-left", "10px");

        productGrid.setColumns("productName", "description", "price", "amount", "rating");
        
        // Add review button column
        productGrid.addComponentColumn(item -> {
            Button reviewButton = new Button("Review", e -> {
                UI.getCurrent().navigate("product-review/" + item.getProductId());
            });
            reviewButton.getStyle()
                .set("background-color", "#805ad5")
                .set("color", "white");
            return reviewButton;
        }).setHeader("Actions");

        // Add bid button column for auctions
        productGrid.addComponentColumn(item -> {
            Response<List<AuctionDTO>> auctionsResponse = productPresenter.showAuctionedProduct(sessionToken, 
                new ItemFilter.Builder().itemName(item.getProductName()).build());
            
            if (!auctionsResponse.errorOccurred() && !auctionsResponse.getValue().isEmpty()) {
                AuctionDTO auction = auctionsResponse.getValue().get(0); // Get the first auction for this product
                
                Button bidButton = new Button("Bid", e -> {
                    Dialog bidDialog = new Dialog();
                    bidDialog.setHeaderTitle("Place Bid for " + item.getProductName());

                    // Create bid form similar to checkout form
                    FormLayout bidForm = new FormLayout();
                    
                    // Bid amount field
                    NumberField bidAmount = new NumberField("Bid Amount");
                    bidAmount.setMin(auction.getCurrentPrice() + 0.01); // Must be higher than current price
                    bidAmount.setStep(0.01);
                    bidAmount.setValue(auction.getCurrentPrice() + 1.0); // Default to current price + 1
                    
                    // Payment details
                    TextField cardNumber = new TextField("Credit Card Number");
                    DatePicker expiryDate = new DatePicker("Expiration Date");
                    expiryDate.setMin(LocalDate.now());
                    TextField cvv = new TextField("CVV");
                    cvv.setMaxLength(3);
                    
                    // Delivery details
                    TextField name = new TextField("Full Name");
                    TextField address = new TextField("Delivery Address");
                    
                    bidForm.add(
                        bidAmount,
                        cardNumber,
                        expiryDate,
                        cvv,
                        name,
                        address
                    );
                    
                    Button placeBidButton = new Button("Place Bid", event -> {
                        if (bidAmount.getValue() == null || cardNumber.isEmpty() || 
                            expiryDate.isEmpty() || cvv.isEmpty() || 
                            name.isEmpty() || address.isEmpty()) {
                            Notification.show("Please fill in all fields");
                            return;
                        }
                        
                        // Convert expiry date to Date object
                        Date expiryDateValue = Date.from(
                            expiryDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()
                        );
                        
                        // Use timestamp as unique increment
                        long increment = System.currentTimeMillis();
                        
                        Response<Boolean> response = purchasePresenter.makeBid(
                            auction.getAuctionId(),
                            sessionToken,
                            bidAmount.getValue().floatValue(),
                            cardNumber.getValue(),
                            expiryDateValue,
                            cvv.getValue(),
                            increment,
                            name.getValue(),
                            address.getValue()
                        );
                        
                        if (!response.errorOccurred() && response.getValue()) {
                            Notification.show("Bid placed successfully!");
                            bidDialog.close();
                            loadAllProducts(); // Refresh to show updated prices
                        } else {
                            Notification.show(
                                "Failed to place bid: " + 
                                (response.errorOccurred() ? response.getErrorMessage() : "Unknown error")
                            );
                        }
                    });
                    
                    placeBidButton.getStyle()
                        .set("background-color", "#38a169")
                        .set("color", "white");
                    
                    Button cancelButton = new Button("Cancel", evt -> bidDialog.close());
                    cancelButton.getStyle()
                        .set("background-color", "#e53e3e")
                        .set("color", "white");
                    
                    HorizontalLayout buttons = new HorizontalLayout(placeBidButton, cancelButton);
                    buttons.setJustifyContentMode(JustifyContentMode.END);
                    
                    VerticalLayout dialogLayout = new VerticalLayout(
                        new H3("Current Price: $" + auction.getCurrentPrice()),
                        bidForm,
                        buttons
                    );
                    dialogLayout.setPadding(true);
                    dialogLayout.setSpacing(true);
                    
                    bidDialog.add(dialogLayout);
                    bidDialog.open();
                });
                
                bidButton.getStyle()
                    .set("background-color", "#9f7aea")
                    .set("color", "white");
                
                return new HorizontalLayout(
                    bidButton
                );
            }
            return new Span(); // Return empty if no auction
        }).setHeader("Auction");

        // Add to cart button column
        productGrid.addComponentColumn(item -> {
            Button addToCartButton = new Button("Add to Cart", e -> {
                Response<Boolean> response = purchasePresenter.addProductToCart(sessionToken, item.getProductId(), item.getStoreId(), 1);
                if (!response.errorOccurred()) {
                    Notification.show("Product added to cart successfully!", 3000, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Failed to add product to cart: " + response.getErrorMessage(), 
                                    3000, Notification.Position.MIDDLE);
                }
            });
            addToCartButton.getStyle()
                .set("background-color", "#38a169")
                .set("color", "white");
            return addToCartButton;
        }).setHeader("Cart");

        productGrid.setWidthFull();
        productGrid.getStyle().set("background-color", "#f7fafc");

        VerticalLayout mainContent = new VerticalLayout(topBar, activeFiltersLabel, productGrid);
        mainContent.setPadding(false);
        mainContent.setSpacing(true);
        add(mainContent);

        this.sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");

        // !TODO: Change
        if (sessionToken != null) {
            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: sessionToken is not null. Attempting to extract userId and inject into JS.");

            String userId = sessionPresenter.extractUserIdFromToken(sessionToken);

            // Inject userId to JavaScript for WebSocket
            
            UI.getCurrent().getPage().executeJs("window.currentUserId = $0;", userId);
            UI.getCurrent().getPage().executeJs("sessionStorage.setItem('currentUserId', $0); window.connectWebSocket && window.connectWebSocket($0);", userId);

            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: Injected userId to JS: " + userId);

            // Flush pending messages
            List<String> messages = notificationPresenter.getNotifications(userId);
            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: Consumed " + messages.size() + " pending messages for userId=" + userId);

            for (String msg : messages) {
                Notification.show("🔔 " + msg, 4000, Notification.Position.TOP_CENTER);
            }

            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: Displayed all pending messages for userId=" + userId);
            }
        else {
            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: sessionToken is null. Skipping userId injection and pending message handling.");
        }

        loadAllProducts();

        setupNavigation();
    
        if (dbHealthStatus != null){
            // Poll for DB health status every 10 seconds
            UI.getCurrent().addPollListener(event -> {
                    if (!dbHealthStatus.isDbAvailable()) {
                        Notification.show("⚠️ DB connection lost. You will be logged out.", 3000, Notification.Position.TOP_CENTER);
                        UI.getCurrent().getPage().setLocation("/");
                    }
                });
            UI.getCurrent().setPollInterval(4000); // 4 seconds
        }

    }

    private void setupFilterComponents() {
        // Configure price fields
        minPriceField.setMin(0);
        maxPriceField.setMin(0);
        minPriceField.setStep(0.01);
        maxPriceField.setStep(0.01);
        minPriceField.setWidth("150px");
        maxPriceField.setWidth("150px");

        // Configure rating fields
        minRatingField.setMin(0);
        minRatingField.setMax(5);
        maxRatingField.setMin(0);
        maxRatingField.setMax(5);
        minRatingField.setStep(0.1);
        maxRatingField.setStep(0.1);
        minRatingField.setWidth("150px");
        maxRatingField.setWidth("150px");

        // Configure amount field
        minAmountField.setMin(0);
        minAmountField.setStep(1);
        minAmountField.setWidth("150px");

        // Configure category filter
        categoryFilter.setWidth("300px");
        categoryFilter.setItemLabelGenerator(CategoryDTO::getName);
    }

    private void setupFilterDialog() {
        filterDialog.setHeaderTitle("Filter Products");
        filterDialog.setWidth("600px");

        // Create filter section
        HorizontalLayout priceFilter = new HorizontalLayout(minPriceField, maxPriceField);
        HorizontalLayout ratingFilter = new HorizontalLayout(minRatingField, maxRatingField);
        HorizontalLayout otherFilters = new HorizontalLayout(categoryFilter, minAmountField);

        priceFilter.setWidthFull();
        ratingFilter.setWidthFull();
        otherFilters.setWidthFull();

        Button applyFiltersBtn = new Button("Apply", e -> {
            applyFilters();
            filterDialog.close();
        });
        applyFiltersBtn.getStyle()
            .set("background-color", "#4299e1")
            .set("color", "white");

        Button clearFiltersBtn = new Button("Clear All", e -> {
            clearFilters();
            filterDialog.close();
        });
        clearFiltersBtn.getStyle()
            .set("background-color", "#718096")
            .set("color", "white");

        Button cancelBtn = new Button("Cancel", e -> filterDialog.close());
        cancelBtn.getStyle().set("margin-right", "auto");

        HorizontalLayout buttons = new HorizontalLayout(cancelBtn, clearFiltersBtn, applyFiltersBtn);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout dialogContent = new VerticalLayout(
            priceFilter,
            ratingFilter,
            otherFilters,
            buttons
        );
        dialogContent.setPadding(true);
        dialogContent.setSpacing(true);

        filterDialog.add(dialogContent);
    }

    private void loadAllProducts() {
        if (sessionToken == null) return;
        Response<List<ItemDTO>> response = productPresenter.showAllItems(sessionToken);
        if (!response.errorOccurred()) {
            productGrid.setItems(response.getValue());
            // Update category filter options
            Set<CategoryDTO> allCategories = new HashSet<>();
            response.getValue().forEach(item -> allCategories.addAll(item.getCategories()));
            categoryFilter.setItems(allCategories);
            updateActiveFiltersLabel();
        } else {
            Notification.show("Failed to load products: " + response.getErrorMessage(), 
                            3000, Notification.Position.MIDDLE);
        }
    }

    private void applyFilters() {
        if (sessionToken == null) return;
        
        ItemFilter.Builder filterBuilder = new ItemFilter.Builder();

        // Add name filter
        if (!searchBar.getValue().trim().isEmpty()) {
            filterBuilder.itemName(searchBar.getValue().trim());
        }

        // Add price filters
        if (minPriceField.getValue() != null) {
            filterBuilder.minPrice(minPriceField.getValue());
        }
        if (maxPriceField.getValue() != null) {
            filterBuilder.maxPrice(maxPriceField.getValue());
        }

        // Add rating filters
        if (minRatingField.getValue() != null) {
            filterBuilder.minRating(minRatingField.getValue());
        }
        if (maxRatingField.getValue() != null) {
            filterBuilder.maxRating(maxRatingField.getValue());
        }

        // Add amount filter
        if (minAmountField.getValue() != null) {
            filterBuilder.amount(minAmountField.getValue().intValue());
        }

        // Add category filters
        Set<CategoryDTO> selectedCategories = categoryFilter.getSelectedItems();
        if (!selectedCategories.isEmpty()) {
            selectedCategories.forEach(category -> 
                filterBuilder.addCategory(new Domain.Store.Category(category.getName(), category.getDescription()))
            );
        }

        Response<List<ItemDTO>> response = productPresenter.showProductDetails(sessionToken, filterBuilder.build());
        if (!response.errorOccurred()) {
            productGrid.setItems(response.getValue());
            updateActiveFiltersLabel();
        } else {
            Notification.show("Failed to apply filters: " + response.getErrorMessage(), 
                            3000, Notification.Position.MIDDLE);
        }
    }

    private void updateActiveFiltersLabel() {
        StringBuilder filterText = new StringBuilder("Active Filters: ");
        boolean hasFilters = false;

        if (!searchBar.getValue().trim().isEmpty()) {
            filterText.append("Name: ").append(searchBar.getValue().trim()).append(", ");
            hasFilters = true;
        }
        if (minPriceField.getValue() != null) {
            filterText.append("Min Price: $").append(minPriceField.getValue()).append(", ");
            hasFilters = true;
        }
        if (maxPriceField.getValue() != null) {
            filterText.append("Max Price: $").append(maxPriceField.getValue()).append(", ");
            hasFilters = true;
        }
        if (minRatingField.getValue() != null) {
            filterText.append("Min Rating: ").append(minRatingField.getValue()).append(", ");
            hasFilters = true;
        }
        if (maxRatingField.getValue() != null) {
            filterText.append("Max Rating: ").append(maxRatingField.getValue()).append(", ");
            hasFilters = true;
        }
        if (minAmountField.getValue() != null) {
            filterText.append("Min Amount: ").append(minAmountField.getValue().intValue()).append(", ");
            hasFilters = true;
        }
        if (!categoryFilter.getSelectedItems().isEmpty()) {
            filterText.append("Categories: ")
                .append(categoryFilter.getSelectedItems().stream()
                    .map(CategoryDTO::getName)
                    .collect(Collectors.joining(", ")))
                .append(", ");
            hasFilters = true;
        }

        if (hasFilters) {
            // Remove the trailing comma and space
            filterText.setLength(filterText.length() - 2);
            activeFiltersLabel.setText(filterText.toString());
            activeFiltersLabel.setVisible(true);
        } else {
            activeFiltersLabel.setText("No active filters");
            activeFiltersLabel.setVisible(false);
        }
    }

    private void clearFilters() {
        searchBar.clear();
        minPriceField.clear();
        maxPriceField.clear();
        minRatingField.clear();
        maxRatingField.clear();
        minAmountField.clear();
        categoryFilter.clear();
        loadAllProducts();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        user = (UserDTO) UI.getCurrent().getSession().getAttribute("user");
        if (sessionToken == null || user == null) {
            Notification.show("Access denied. Please log in.", 4000, Notification.Position.MIDDLE);
            event.forwardTo("");
        } else {
            checkBanStatus();
        }
    }

    private void setupNavigation() {
        Button tradingButton = new Button("Trading Operations", e -> UI.getCurrent().navigate("trading"));
        tradingButton.getStyle()
            .set("background-color", "#4299e1")
            .set("color", "white");
        add(tradingButton);
    }

    @ClientCallable
    private void showNotification(String message, String type, Integer duration, String position) {
        Notification notification = new Notification(message);
        notification.setDuration(duration > 0 ? duration : 4000);
        notification.setPosition(Notification.Position.valueOf(position.toUpperCase().replace('-', '_')));
        
        if ("error".equals(type)) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else if ("warning".equals(type)) {
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        } else if ("success".equals(type)) {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        
        notification.open();
    }

    private void checkBanStatus() {
        if (sessionToken != null && currentUsername != null) {
            String userId = sessionPresenter.extractUserIdFromToken(sessionToken);
            Response<Boolean> response = marketService.userExists(currentUsername);
            if (!response.errorOccurred() && response.getValue()) {
                boolean wasBanned = isBanned;  // Store previous state
                isBanned = permissionManager.isBanned(userId);
                
                // If ban status changed, update UI
                if (wasBanned != isBanned) {
                    if (isBanned) {
                        // Remove action columns
                        List<Grid.Column<ItemDTO>> columnsToRemove = new ArrayList<>();
                        productGrid.getColumns().forEach(column -> {
                            String header = column.getHeaderText();
                            if (header != null && (header.equals("Actions") || header.equals("Cart") || header.equals("Auction"))) {
                                columnsToRemove.add(column);
                            }
                        });
                        columnsToRemove.forEach(column -> productGrid.removeColumn(column));
                        
                        // Disable interactive components
                        searchBar.setEnabled(false);
                        filterBtn.setEnabled(false);
                        refreshBtn.setEnabled(false);
                        cartBtn.setEnabled(false);
                        goToSearchBtn.setEnabled(false);
                        
                        // Show ban notification
                        Notification.show("Your account has been banned. Some features are disabled.", 
                                       5000, Notification.Position.MIDDLE);
                    } else {
                        // Refresh the page to restore all functionality
                        UI.getCurrent().getPage().reload();
                    }
                }
            }
        }
        this.user =(UserDTO)UI.getCurrent().getSession().getAttribute("user");
    }
}
