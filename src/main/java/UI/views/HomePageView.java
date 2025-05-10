package UI.views;

import UI.presenters.IProductPresenter;
import Application.DTOs.ItemDTO;
import Application.utils.Response;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route("home")
public class HomePageView extends VerticalLayout {

    private IProductPresenter productPresenter;
    private final String sessionToken = "demo-session";
    private final String currentUsername = "JohnDoe"; // This should ideally come from session/login context

    private final TextField searchBar = new TextField();
    private final Grid<ItemDTO> productGrid = new Grid<>(ItemDTO.class);

    @Autowired
    public void HomepageView(IProductPresenter productPresenter) {
        this.productPresenter = productPresenter;

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background", "linear-gradient(to right, #e0f7fa, #e1bee7)");

        H1 title = new H1("Gamazon Home");
        title.getStyle().set("color", "#2c3e50");

        Span userInfo = new Span("Logged in as: " + currentUsername);
        userInfo.getStyle().set("color", "#7b1fa2").set("font-weight", "bold");

        searchBar.setPlaceholder("Search for products...");
        searchBar.setWidth("400px");
        searchBar.getStyle().set("background-color", "#ffffff");
        searchBar.addValueChangeListener(e -> searchProducts());

        Button refreshBtn = new Button("Refresh", e -> loadAllProducts());
        refreshBtn.getStyle().set("background-color", "#00897b").set("color", "white");

        HorizontalLayout topBar = new HorizontalLayout(userInfo, title, searchBar, refreshBtn);
        topBar.setAlignItems(Alignment.BASELINE);
        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topBar.getStyle().set("padding", "10px");

        productGrid.setColumns("productName", "description", "price", "amount", "rating");
        productGrid.setWidthFull();
        productGrid.getStyle().set("background-color", "#f3e5f5");

        add(topBar, productGrid);
        loadAllProducts();
    }

    private void loadAllProducts() {
        Set<ItemDTO> products = productPresenter.showAllProducts(sessionToken);
        productGrid.setItems(products);
    }

    private void searchProducts() {
        String query = searchBar.getValue();
        if (query.isBlank()) {
            loadAllProducts();
            return;
        }
        Set<ItemDTO> filtered = productPresenter.showAllProducts(sessionToken).stream()
                .filter(p -> p.getProductName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toSet());
        productGrid.setItems(filtered);
    }
}