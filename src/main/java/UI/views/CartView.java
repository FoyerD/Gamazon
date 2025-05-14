package UI.views;

import UI.presenters.IPurchasePresenter;
import UI.presenters.IProductPresenter;
import Application.DTOs.OrderDTO;
import Application.DTOs.ItemDTO;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

@Route("cart")
public class CartView extends VerticalLayout implements BeforeEnterObserver {

    private final IPurchasePresenter purchasePresenter;
    private final IProductPresenter productPresenter;
    private String sessionToken;
    private String currentUsername;

    private final H1 title = new H1("Your Shopping Cart");
    private final VerticalLayout cartContent = new VerticalLayout();
    private final Span totalPriceLabel = new Span("Total: $0.00");
    private final Button checkoutButton = new Button("Proceed to Checkout");
    private final Button homeButton = new Button("Continue Shopping");

    private Map<String, Double> storeBasketTotals = new HashMap<>();
    private double cartTotal = 0.0;

    @Autowired
    public CartView(IPurchasePresenter purchasePresenter, IProductPresenter productPresenter) {
        this.purchasePresenter = purchasePresenter;
        this.productPresenter = productPresenter;

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background", "linear-gradient(to right, #e6f7ff, #e6fffb)");

        title.getStyle().set("color", "#1890ff");

        // Style the total price label
        totalPriceLabel.getStyle()
            .set("font-size", "1.5em")
            .set("font-weight", "bold")
            .set("color", "#1890ff")
            .set("margin", "10px 0");

        // Style the buttons
        checkoutButton.getStyle()
            .set("background-color", "#52c41a")
            .set("color", "white")
            .set("font-weight", "bold");

        homeButton.getStyle()
            .set("background-color", "#1890ff")
            .set("color", "white");

        // Set up button click listeners
        checkoutButton.addClickListener(e -> proceedToCheckout());
        homeButton.addClickListener(e -> UI.getCurrent().navigate("home"));

        // Create the bottom layout with total and buttons
        HorizontalLayout bottomLayout = new HorizontalLayout(totalPriceLabel, homeButton, checkoutButton);
        bottomLayout.setWidthFull();
        bottomLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        bottomLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Add components to the main layout - the header will be created in loadCartContents
        add(cartContent, bottomLayout);
    }

    private void loadCartContents() {
        cartContent.removeAll();
        storeBasketTotals.clear();
        cartTotal = 0.0;

        // Update title with username
        this.currentUsername = (String) UI.getCurrent().getSession().getAttribute("username");
        
        // Create the header with title and cart clear button
        H1 titleElement = new H1("Hello " + (currentUsername != null ? currentUsername : "User"));
        titleElement.getStyle().set("color", "#1890ff").set("margin", "0");
        
        // Create simple minus button for clearing cart - red with just the minus icon
        Button clearCartButton = new Button(new Icon(VaadinIcon.MINUS));
        clearCartButton.getStyle()
            .set("background-color", "#ff4d4f")
            .set("color", "white")
            .set("border", "none")
            .set("border-radius", "4px")
            .set("padding", "4px 8px");
        clearCartButton.addClickListener(e -> clearCart());
        
        // Cart icon displayed next to the button
        Icon cartIcon = new Icon(VaadinIcon.CART);
        cartIcon.getStyle().set("color", "#1890ff").set("font-size", "1.5em");
        
        // Create a horizontal layout for the clear button and cart icon
        HorizontalLayout clearCartLayout = new HorizontalLayout(clearCartButton, cartIcon);
        clearCartLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        clearCartLayout.setSpacing(true);

        // Create header layout
        HorizontalLayout headerLayout = new HorizontalLayout(titleElement, clearCartLayout);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.getStyle().set("margin-bottom", "20px");
        
        // Add header to cart content
        cartContent.add(headerLayout);

        Set<OrderDTO> cartItems = purchasePresenter.viewCart(sessionToken);
        
        if (cartItems.isEmpty()) {
            cartContent.add(new Span("Your cart is empty"));
            totalPriceLabel.setText("Total: $0.00");
            return;
        }

        // Group items by store
        Map<String, Set<OrderDTO>> storeBaskets = cartItems.stream()
            .collect(Collectors.groupingBy(OrderDTO::getStoreName, Collectors.toSet()));

        // Create a panel for each store basket
        for (Map.Entry<String, Set<OrderDTO>> storeEntry : storeBaskets.entrySet()) {
            String storeName = storeEntry.getKey();
            Set<OrderDTO> basketItems = storeEntry.getValue();
            
            // Create store section
            VerticalLayout storeLayout = new VerticalLayout();
            storeLayout.setSpacing(true);
            storeLayout.setPadding(true);
            storeLayout.getStyle()
                .set("background-color", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.1)")
                .set("margin", "10px 0");

            // Store header with name and remove basket button
            Button storeLink = new Button(storeName + "'s basket");
            storeLink.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "bold")
                .set("color", "#1890ff")
                .set("text-decoration", "none")
                .set("background", "none")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("padding", "0");
            
            storeLink.addClickListener(e -> {
                // Create query parameters with the store name
                Map<String, List<String>> parameters = new HashMap<>();
                parameters.put("storeName", Collections.singletonList(storeName));
                
                // Navigate to the store search view with the parameters
                UI.getCurrent().navigate(StoreSearchView.class, new QueryParameters(parameters));
            });
            
            // Add basket icon for display only
            Icon basketDisplayIcon = new Icon(VaadinIcon.SHOP);
            basketDisplayIcon.getStyle().set("color", "#1890ff");
            
            // Create simple minus button for removing basket
            Button removeBasketButton = new Button(new Icon(VaadinIcon.MINUS));
            removeBasketButton.getStyle()
                .set("background-color", "#ff4d4f")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "4px")
                .set("padding", "4px 8px");
            removeBasketButton.addClickListener(e -> removeBasket(storeName));
            
            // Shop icon displayed next to the button
            Icon shopIcon = new Icon(VaadinIcon.SHOP);
            shopIcon.getStyle().set("color", "#1890ff");
            
            // Create a horizontal layout for the remove button and shop icon
            HorizontalLayout basketActionLayout = new HorizontalLayout(removeBasketButton, shopIcon);
            basketActionLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            basketActionLayout.setSpacing(true);

            HorizontalLayout storeHeader = new HorizontalLayout(storeLink, basketActionLayout);
            storeHeader.setWidthFull();
            storeHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
            storeLayout.add(storeHeader);

            // Create grid for basket items
            Grid<OrderDTO> basketGrid = new Grid<>();
            basketGrid.addColumn(OrderDTO::getName).setHeader("Product").setKey("product");
            
            // Replace simple quantity column with component column that includes +/- buttons
            basketGrid.addComponentColumn(order -> {
                // Create container for quantity display and buttons
                HorizontalLayout quantityLayout = new HorizontalLayout();
                quantityLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                quantityLayout.setSpacing(true);
                
                // Display current quantity
                Span quantityLabel = new Span(String.valueOf(order.getQuantity()));
                quantityLabel.getStyle()
                    .set("font-weight", "bold")
                    .set("min-width", "20px")
                    .set("text-align", "center");
                
                // Create horizontal layout for increment/decrement buttons
                HorizontalLayout buttonLayout = new HorizontalLayout();
                buttonLayout.setSpacing(false);
                buttonLayout.setPadding(false);
                buttonLayout.setMargin(false);
                
                // Create - button (decrement)
                Button decrementButton = new Button(new Icon(VaadinIcon.MINUS));
                decrementButton.getStyle()
                    .set("background-color", "#f0f0f0")
                    .set("color", "#ff4d4f")
                    .set("border", "none")
                    .set("border-radius", "4px 0 0 4px")
                    .set("padding", "2px 6px")
                    .set("min-width", "30px")
                    .set("margin", "0");
                
                // Disable decrement button if quantity is 1 to prevent going to 0
                if (order.getQuantity() <= 1) {
                    decrementButton.setEnabled(false);
                    decrementButton.getStyle().set("opacity", "0.5");
                }
                
                decrementButton.addClickListener(e -> {
                    boolean success = purchasePresenter.removeProductFromCart(
                        sessionToken, order.getName(), order.getStoreName(), 1);
                    if (success) {
                        Notification.show("Quantity decreased", 1000, Notification.Position.MIDDLE);
                        loadCartContents();
                    }
                });
                
                // Create + button (increment)
                Button incrementButton = new Button(new Icon(VaadinIcon.PLUS));
                incrementButton.getStyle()
                    .set("background-color", "#f0f0f0")
                    .set("color", "#52c41a")
                    .set("border", "none")
                    .set("border-radius", "0 4px 4px 0")
                    .set("padding", "2px 6px")
                    .set("min-width", "30px")
                    .set("margin", "0");
                incrementButton.addClickListener(e -> {
                    boolean success = purchasePresenter.addProductToCart(
                        sessionToken, order.getName(), order.getStoreName(), 1);
                    if (success) {
                        Notification.show("Quantity increased", 1000, Notification.Position.MIDDLE);
                        loadCartContents();
                    }
                });
                
                // Add buttons to the layout (decrement first, increment second)
                buttonLayout.add(decrementButton, incrementButton);
                
                // Add quantity label and buttons to layout
                quantityLayout.add(quantityLabel, buttonLayout);
                
                return quantityLayout;
            }).setHeader("Quantity").setKey("quantity").setAutoWidth(true);
            
            // Add price column - needs to fetch from product info
            basketGrid.addColumn(order -> {
                ItemDTO product = productPresenter.showProductDetailsOfaStore(
                    sessionToken, order.getName(), order.getStoreName());
                if (product != null) {
                    return String.format("$%.2f", product.getPrice());
                }
                return "N/A";
            }).setHeader("Price").setKey("price");
            
            // Add subtotal column
            basketGrid.addColumn(order -> {
                ItemDTO product = productPresenter.showProductDetailsOfaStore(
                    sessionToken, order.getName(), order.getStoreName());
                if (product != null) {
                    double subtotal = product.getPrice() * order.getQuantity();
                    return String.format("$%.2f", subtotal);
                }
                return "N/A";
            }).setHeader("Subtotal").setKey("subtotal");
            
            // Add remove product button column - with empty header
            basketGrid.addComponentColumn(order -> {
                Button removeButton = new Button(new Icon(VaadinIcon.MINUS));
                removeButton.getStyle()
                    .set("background-color", "#ff4d4f")
                    .set("color", "white")
                    .set("border", "none")
                    .set("border-radius", "4px")
                    .set("padding", "4px 8px")
                    .set("min-width", "auto");
                removeButton.addClickListener(e -> removeProduct(order.getName(), order.getStoreName()));
                return removeButton;
            }).setHeader("").setKey("remove").setFlexGrow(0).setWidth("80px");
            
            // Configure grid to size based on content
            basketGrid.setItems(basketItems);
            
            // Improved height calculation for the grid
            // Base height for header + a bit extra padding 
            int baseHeight = 56 + 10;
            // Row height including the new vertical buttons (slightly taller)
            int rowHeight = 55;
            int itemCount = basketItems.size();
            int gridHeight = baseHeight + (itemCount * rowHeight);
            basketGrid.setHeight(gridHeight + "px");
            
            // Enable text wrapping for product names
            basketGrid.getColumns().forEach(col -> {
                // Safe null check before comparing the key
                String key = col.getKey();
                if (key == null || !key.equals("remove")) {
                    col.setAutoWidth(true);
                }
            });
            
            // Remove default grid styling but maintain grid visibility
            basketGrid.getStyle()
                .set("margin", "0")
                .set("overflow", "visible");
            
            storeLayout.add(basketGrid);

            // Calculate and show the basket total price
            double basketTotal = calculateBasketTotal(basketItems);
            storeBasketTotals.put(storeName, basketTotal);
            cartTotal += basketTotal;
            
            Span basketTotalLabel = new Span("Basket Total: $" + String.format("%.2f", basketTotal));
            basketTotalLabel.getStyle()
                .set("font-weight", "bold")
                .set("margin-top", "5px")
                .set("align-self", "flex-end");
                
            storeLayout.add(basketTotalLabel);
            
            cartContent.add(storeLayout);
        }

        // Update cart total
        totalPriceLabel.setText(String.format("Total: $%.2f", cartTotal));
    }

    private double calculateBasketTotal(Set<OrderDTO> basketItems) {
        double total = 0.0;
        for (OrderDTO order : basketItems) {
            ItemDTO product = productPresenter.showProductDetailsOfaStore(
                sessionToken, order.getName(), order.getStoreName());
            if (product != null) {
                total += product.getPrice() * order.getQuantity();
            }
        }
        return total;
    }

    private void removeProduct(String productName, String storeName) {
        boolean success = purchasePresenter.removeProductFromCart(sessionToken, productName, storeName);
        if (success) {
            Notification.show("Product removed from cart", 3000, Notification.Position.MIDDLE);
            loadCartContents();
        } else {
            Notification.show("Failed to remove product", 3000, Notification.Position.MIDDLE);
        }
    }

    private void removeBasket(String storeName) {
        boolean success = purchasePresenter.clearBasket(sessionToken, storeName);
        if (success) {
            Notification.show("Basket removed from cart", 3000, Notification.Position.MIDDLE);
            loadCartContents();
        } else {
            Notification.show("Failed to remove basket", 3000, Notification.Position.MIDDLE);
        }
    }
    
    private void clearCart() {
        boolean success = purchasePresenter.clearCart(sessionToken);
        if (success) {
            Notification.show("Cart has been cleared", 3000, Notification.Position.MIDDLE);
            loadCartContents();
        } else {
            Notification.show("Failed to clear cart", 3000, Notification.Position.MIDDLE);
        }
    }

    private void proceedToCheckout() {
        if (cartTotal <= 0) {
            Notification.show("Your cart is empty", 3000, Notification.Position.MIDDLE);
            return;
        }
        UI.getCurrent().navigate("checkout");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        if (sessionToken == null) {
            Notification.show("Access denied. Please log in.", 4000, Notification.Position.MIDDLE);
            event.forwardTo("login");
        } else {
            loadCartContents();
        }
    }
} 