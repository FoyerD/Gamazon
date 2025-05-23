package UI.views;

import UI.presenters.IProductPresenter;
import UI.presenters.IUserSessionPresenter;
import UI.presenters.IPurchasePresenter;
import UI.presenters.ILoginPresenter;
import Application.DTOs.ItemDTO;
import Application.DTOs.CategoryDTO;
import Application.utils.Response;
import Domain.Store.ItemFilter;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@JsModule("./ws-client.js")
@Route("home")
public class HomePageView extends VerticalLayout implements BeforeEnterObserver {

    private final IProductPresenter productPresenter;
    private final IPurchasePresenter purchasePresenter;
    private final ILoginPresenter loginPresenter;
    private String sessionToken = null;
    private String currentUsername = null;

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

    private final IUserSessionPresenter sessionPresenter;

    public HomePageView(IProductPresenter productPresenter, IUserSessionPresenter sessionPresenter, 
                        IPurchasePresenter purchasePresenter, ILoginPresenter loginPresenter) {
        this.productPresenter = productPresenter;
        this.sessionPresenter = sessionPresenter;
        this.purchasePresenter = purchasePresenter;
        this.loginPresenter = loginPresenter;

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background", "linear-gradient(to right, #edf2f7, #e2e8f0)");

        H1 title = new H1("Gamazon Home");
        title.getStyle().set("color", "#1a202c");

        Span userInfo = new Span();
        userInfo.getStyle().set("color", "#2d3748").set("font-weight", "bold");

        // Configure filter components
        setupFilterComponents();
        setupFilterDialog();

        searchBar.setPlaceholder("Search for products...");
        searchBar.setWidth("300px");
        searchBar.getStyle().set("background-color", "#ffffff");
        searchBar.addValueChangeListener(e -> applyFilters());

        Button filterBtn = new Button(new Icon(VaadinIcon.FILTER), e -> filterDialog.open());
        filterBtn.setText("Filters");
        filterBtn.getStyle()
            .set("background-color", "#4299e1")
            .set("color", "white");

        Button refreshBtn = new Button("Refresh", e -> loadAllProducts());
        refreshBtn.getStyle().set("background-color", "#2b6cb0").set("color", "white");

        Button goToSearchBtn = new Button("Search Stores", e -> UI.getCurrent().navigate("store-search"));
        goToSearchBtn.getStyle().set("background-color", "#3182ce").set("color", "white");
        
        Button cartBtn = new Button("View Cart", e -> UI.getCurrent().navigate("cart"));
        cartBtn.getStyle().set("background-color", "#38a169").set("color", "white");

        Button registerBtn = new Button("Register", e -> UI.getCurrent().navigate("register"));
        registerBtn.getStyle().set("background-color", "#6b46c1").set("color", "white");

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

        HorizontalLayout searchAndFilter = new HorizontalLayout(searchBar, filterBtn);
        searchAndFilter.setAlignItems(Alignment.BASELINE);

        HorizontalLayout topBar = new HorizontalLayout(userInfo, title, searchAndFilter, refreshBtn, goToSearchBtn, cartBtn, registerBtn, logoutBtn);
        topBar.setAlignItems(Alignment.BASELINE);
        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topBar.getStyle().set("padding", "10px");

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

        if (sessionToken != null) {
            String userId = sessionPresenter.extractUserIdFromToken(sessionToken);

            // Inject userId to JavaScript for WebSocket
            UI.getCurrent().getPage().executeJs("window.currentUserId = $0;", userId);

        }

        this.currentUsername = (String) UI.getCurrent().getSession().getAttribute("username");
        userInfo.setText("Logged in as: " + (currentUsername != null ? currentUsername : "Unknown"));

        loadAllProducts();
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
        if (sessionToken == null) {
            Notification.show("Access denied. Please log in.", 4000, Notification.Position.MIDDLE);
            event.forwardTo("");
        }
    }
}