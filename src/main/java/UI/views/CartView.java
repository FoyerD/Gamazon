
package UI.views;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import Application.DTOs.CartDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.ShoppingBasketDTO;
import Application.DTOs.StoreDTO;
import Application.MarketService;
import Application.utils.Response;
import Domain.management.PermissionManager;
import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;
import UI.presenters.INotificationPresenter;
import UI.presenters.IProductPresenter;
import UI.presenters.IPurchasePresenter;
import UI.presenters.IStorePresenter;
import UI.presenters.IUserSessionPresenter;
import UI.views.components.BasketLayout;

@Route("cart")
public class CartView extends BaseView implements BeforeEnterObserver {

    private final IPurchasePresenter purchasePresenter;
    private final IStorePresenter storePresenter;
    private String sessionToken;
    private String currentUsername;
    private boolean isBanned = false;

    private final H1 title = new H1("Your Shopping Cart");
    private final VerticalLayout cartContent = new VerticalLayout();
    private final Span totalPriceLabel = new Span("Total: $0.00");
    private final Button checkoutButton = new Button("Proceed to Checkout");
    private final Button homeButton = new Button("Continue Shopping");

    private Map<String, Double> storeBasketTotals = new HashMap<>();
    private double cartTotal = 0.0;

    @Autowired
    public CartView(IPurchasePresenter purchasePresenter, IStorePresenter storePresenter, IProductPresenter productPresenter,
                    IUserSessionPresenter sessionPresenter,
                    @Autowired(required = false) DbHealthStatus dbHealthStatus, @Autowired(required = false) GlobalLogoutManager globalLogoutManager
                        ,INotificationPresenter notificationPresenter) {
        super(dbHealthStatus, globalLogoutManager, sessionPresenter, notificationPresenter);
        this.purchasePresenter = purchasePresenter;
        this.storePresenter = storePresenter;
        
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

        // Add data attribute to identify checkout button for ban handling
        checkoutButton.getElement().setAttribute("data-checkout-button", "true");

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
        
        // Check ban status
        if (sessionToken != null && currentUsername != null) {
            Response<Boolean> isBannedResponse = sessionPresenter.isUserBanned(sessionToken);
            if (!isBannedResponse.errorOccurred()) {
                isBanned = isBannedResponse.getValue();
                if (isBanned) {
                    // Disable checkout button
                    checkoutButton.setEnabled(false);
                    checkoutButton.getStyle()
                        .set("background-color", "#718096")
                        .set("color", "white")
                        .set("cursor", "not-allowed")
                        .set("opacity", "0.5");
                    
                    // Show notification about ban
                    Notification notification = new Notification(
                        "You are currently banned. You can view your cart but cannot proceed to checkout.",
                        5000,
                        Notification.Position.MIDDLE
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                }
            }
        }

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

        // Get cart items from presenter with proper error handling
        Response<CartDTO> cartResponse = purchasePresenter.viewCart(sessionToken);
        
        if (cartResponse.errorOccurred()) {
            Notification.show("Error loading cart: " + cartResponse.getErrorMessage(), 
                3000, Notification.Position.MIDDLE);
            cartContent.add(new Span("Unable to load cart contents"));
            totalPriceLabel.setText("Total: $0.00");
            return;
        }
        
        CartDTO cart = cartResponse.getValue();
        
        if (cart.getBaskets().isEmpty()) {
            cartContent.add(new Span("Your cart is empty"));
            totalPriceLabel.setText("Total: $0.00");
            return;
        }

        // Create a panel for each store basket
        for (Map.Entry<String, ShoppingBasketDTO> basket : cart.getBaskets().entrySet()) {
            // Check if the store is closed
            Response<StoreDTO> storeResponse = storePresenter.getStoreByName(sessionToken, basket.getValue().getStoreName());
            if (!storeResponse.errorOccurred() && !storeResponse.getValue().isOpen() && storeResponse.getValue().isPermanentlyClosed()) {
                // Store is permanently closed, remove the basket
                purchasePresenter.clearBasket(sessionToken, basket.getKey());
                Notification.show(
                    String.format("Basket from store '%s' has been removed because the store is permanently closed", 
                    basket.getValue().getStoreName()),
                    5000, 
                    Notification.Position.MIDDLE
                );
                continue;
            } else if (!storeResponse.errorOccurred() && !storeResponse.getValue().isOpen() && !storeResponse.getValue().isPermanentlyClosed()) {
                // Store is temporarily closed, show notification but keep basket
                Notification.show(
                    String.format("Note: Store '%s' is temporarily closed. Your basket is preserved.", 
                    basket.getValue().getStoreName()),
                    5000, 
                    Notification.Position.MIDDLE
                );
            }

            BasketLayout basketLayout = new BasketLayout(
                basket.getValue(),
                this::removeBasket,
                this::removeProduct,
                this::decrementAmount,
                this::incrementAmount
                );
            
            cartTotal += basketLayout.calculateBasketTotal();
            cartContent.add(basketLayout);
        }
        // Update cart total
        totalPriceLabel.setText(String.format("Total: $%.2f", cartTotal));
    }

    private void decrementAmount(ItemDTO item) {
        Response<Boolean> response = purchasePresenter.removeProductFromCart(
                        sessionToken, item.getProductId(), item.getStoreId(), 1);

        if (!response.errorOccurred() && response.getValue()) {
            Notification.show("Quantity decreased", 1000, Notification.Position.MIDDLE);
            loadCartContents();
        } else {
            String errorMsg = response.errorOccurred() ? 
                response.getErrorMessage() : "Failed to decrease quantity";
            Notification.show(errorMsg, 3000, Notification.Position.MIDDLE);
        }
    }

    private void incrementAmount(ItemDTO item) {
        Response<Boolean> response = purchasePresenter.addProductToCart(
                        sessionToken, item.getProductId(), item.getStoreId(), 1);

        if (!response.errorOccurred() && response.getValue()) {
            Notification.show("Quantity increased", 1000, Notification.Position.MIDDLE);
            loadCartContents();
        } else {
            String errorMsg = response.errorOccurred() ? 
                response.getErrorMessage() : "Failed to increase quantity";
            Notification.show(errorMsg, 3000, Notification.Position.MIDDLE);
        }
    }

    private void removeProduct(ItemDTO item) {
        Response<Boolean> response = purchasePresenter.removeProductFromCart(sessionToken, item.getProductId(), item.getStoreId());
        if (!response.errorOccurred() && response.getValue()) {
            Notification.show("Product removed from cart", 3000, Notification.Position.MIDDLE);
            loadCartContents();
        } else {
            String errorMsg = response.errorOccurred() ? 
                response.getErrorMessage() : "Failed to remove product";
            Notification.show(errorMsg, 3000, Notification.Position.MIDDLE);
        }
    }

    private void removeBasket(String storeId) {
        Response<Boolean> response = purchasePresenter.clearBasket(sessionToken, storeId);
        if (!response.errorOccurred() && response.getValue()) {
            Notification.show("Basket removed from cart", 3000, Notification.Position.MIDDLE);
            loadCartContents();
        } else {
            String errorMsg = response.errorOccurred() ? 
                response.getErrorMessage() : "Failed to remove basket";
            Notification.show(errorMsg, 3000, Notification.Position.MIDDLE);
        }
    }
    
    private void clearCart() {
        Response<Boolean> response = purchasePresenter.clearCart(sessionToken);
        if (!response.errorOccurred() && response.getValue()) {
            Notification.show("Cart has been cleared", 3000, Notification.Position.MIDDLE);
            loadCartContents();
        } else {
            String errorMsg = response.errorOccurred() ? 
                response.getErrorMessage() : "Failed to clear cart";
            Notification.show(errorMsg, 3000, Notification.Position.MIDDLE);
        }
    }

    private void proceedToCheckout() {
        if (isBanned) {
            Notification.show("You cannot checkout while banned", 3000, Notification.Position.MIDDLE);
            return;
        }
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
