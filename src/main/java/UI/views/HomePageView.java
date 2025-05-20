package UI.views;

import UI.presenters.IProductPresenter;
import UI.presenters.IUserSessionPresenter;
import UI.presenters.IPurchasePresenter;
import UI.presenters.ILoginPresenter;
import Application.DTOs.ItemDTO;
import Application.utils.Response;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.util.List;
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

        searchBar.setPlaceholder("Search for products...");
        searchBar.setWidth("300px");
        searchBar.getStyle().set("background-color", "#ffffff");
        searchBar.addValueChangeListener(e -> searchProducts());

        Button refreshBtn = new Button("Refresh", e -> loadAllProducts());
        refreshBtn.getStyle().set("background-color", "#2b6cb0").set("color", "white");

        Button goToSearchBtn = new Button("Search Stores", e -> UI.getCurrent().navigate("store-search"));
        goToSearchBtn.getStyle().set("background-color", "#3182ce").set("color", "white");
        
        Button cartBtn = new Button("View Cart", e -> UI.getCurrent().navigate("cart"));
        cartBtn.getStyle().set("background-color", "#38a169").set("color", "white");

        Button registerBtn = new Button("Register New Account", e -> UI.getCurrent().navigate("register"));
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

        HorizontalLayout topBar = new HorizontalLayout(userInfo, title, searchBar, refreshBtn, goToSearchBtn, cartBtn, registerBtn, logoutBtn);
        topBar.setAlignItems(Alignment.BASELINE);
        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topBar.getStyle().set("padding", "10px");

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

        add(topBar, productGrid);

        this.sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");

        if (sessionToken != null) {
            String userId = sessionPresenter.extractUserIdFromToken(sessionToken);
            UI.getCurrent().getPage().executeJs("window.currentUserId = $0;", userId);
        }


        this.currentUsername = (String) UI.getCurrent().getSession().getAttribute("username");
        userInfo.setText("Logged in as: " + (currentUsername != null ? currentUsername : "Unknown"));

        loadAllProducts();
    }

    private void loadAllProducts() {
        if (sessionToken == null) return;
        Response<List<ItemDTO>> response = productPresenter.showAllItems(sessionToken);
        if (!response.errorOccurred()) {
            productGrid.setItems(response.getValue());
        } else {
            Notification.show("Failed to load products: " + response.getErrorMessage(), 
                            3000, Notification.Position.MIDDLE);
        }
    }

    private void searchProducts() {
        if (sessionToken == null) return;
        String query = searchBar.getValue();
        if (query.isBlank()) {
            loadAllProducts();
            return;
        }
        
        Response<List<ItemDTO>> response = productPresenter.showAllItems(sessionToken);
        if (!response.errorOccurred()) {
            List<ItemDTO> filtered = response.getValue().stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            productGrid.setItems(filtered);
        } else {
            Notification.show("Failed to search products: " + response.getErrorMessage(), 
                            3000, Notification.Position.MIDDLE);
        }
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