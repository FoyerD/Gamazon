package UI.views.components;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;

import Application.DTOs.ItemDTO;
import Application.DTOs.PolicyDTO;
import Application.DTOs.ShoppingBasketDTO;
import UI.views.StoreSearchView;


public  class BasketLayout extends VerticalLayout{
    private ShoppingBasketDTO basketDTO;
    private List<PolicyDTO> policies;
    public BasketLayout(ShoppingBasketDTO basketDTO,
                        List<PolicyDTO> policies,
                        Consumer<String> basketRemover,
                        Consumer<ItemDTO> itemRemover,
                        Consumer<ItemDTO> itemDecrement, Consumer<ItemDTO> itemIncrement) {


        this.basketDTO = basketDTO;
        this.policies = policies;
        this.outlineStoreLayout();
        this.add(this.storeHeader(basketRemover));
        this.add(this.basketGrid(itemDecrement, itemIncrement, itemRemover));
        this.add(this.violatedPoliciesLayout());
        this.add(this.basketTotalSpan());
        
    }

    private HorizontalLayout storeHeader(Consumer<String> basketRemover) {
        // Store header with name and remove basket button
        Button storeLink = new Button(this.basketDTO.getStoreName() + "'s basket");
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
            parameters.put("storeName", Collections.singletonList(basketDTO.getStoreName()));
            
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
        removeBasketButton.addClickListener(e -> basketRemover.accept(this.basketDTO.getStoreId()));
        
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
        return storeHeader;
    }

    private void outlineStoreLayout() {
        this.getStyle()
            .set("border", "1px solid #e0e0e0")
            .set("border-radius", "8px")
            .set("padding", "10px")
            .set("margin-bottom", "10px");
    }

    private Grid<ItemDTO> basketGrid(Consumer<ItemDTO> itemDecrement, Consumer<ItemDTO> itemIncrement, Consumer<ItemDTO> itemRemover) {
        // Create grid for basket items
        Grid<ItemDTO> basketGrid = new Grid<>();
        basketGrid.addColumn(ItemDTO::getProductName).setHeader("Product").setKey("product");
    


        // Replace simple quantity column with component column that includes +/- buttons
        basketGrid.addComponentColumn(item -> {
            // Create container for quantity display and buttons
            HorizontalLayout quantityLayout = new HorizontalLayout();
            quantityLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            quantityLayout.setSpacing(true);
            
            // Display current quantity
            Span quantityLabel = new Span(String.valueOf(item.getAmount()));
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
            if (item.getAmount() <= 1) {
                decrementButton.setEnabled(false);
                decrementButton.getStyle().set("opacity", "0.5");
            }
            
            decrementButton.addClickListener(e -> {
                itemDecrement.accept(item);
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
            itemIncrement.accept(item);
            });
            
            // Add buttons to the layout (decrement first, increment second)
            buttonLayout.add(decrementButton, incrementButton);
            
            // Add quantity label and buttons to layout
            quantityLayout.add(quantityLabel, buttonLayout);
            
            return quantityLayout;
        }).setHeader("Quantity").setKey("quantity").setAutoWidth(true);
        

        basketGrid.addColumn(item -> String.valueOf(item.getPrice())).setHeader("Price").setKey("price");
        

        basketGrid.addColumn(item -> {
            double unitPrice = item.getPrice(); // Placeholder price
            double subtotal = unitPrice * item.getAmount();
            return String.format("$%.2f", subtotal);
        }).setHeader("Subtotal").setKey("subtotal");
        
        // Add remove product button column - with empty header
        basketGrid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.MINUS));
            removeButton.getStyle()
                .set("background-color", "#ff4d4f")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "4px")
                .set("padding", "4px 8px")
                .set("min-width", "auto");
            removeButton.addClickListener(e -> itemRemover.accept(item));
            return removeButton;
        }).setHeader("").setKey("remove").setFlexGrow(0).setWidth("80px");
        
        // Configure grid to size based on content
        Collection<ItemDTO> basketItems = basketDTO.getOrders().values();
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
        return basketGrid;
    }

    private Span basketTotalSpan() {
        Span basketTotalLabel = new Span("Basket Total: $" + String.format("%.2f", calculateBasketTotal()));
            basketTotalLabel.getStyle()
                .set("font-weight", "bold")
                .set("margin-top", "5px")
                .set("align-self", "flex-end");
        return basketTotalLabel;
                
    }

    private VerticalLayout violatedPoliciesLayout() {
    VerticalLayout violationsLayout = new VerticalLayout();
    violationsLayout.setSpacing(false);
    violationsLayout.setPadding(false);
    violationsLayout.setMargin(false);
    violationsLayout.setWidthFull();

    policies.stream()
        .map(policy -> {
            Span violationSpan = new Span("⚠️ " + policy.toString()); // Customize message if needed
            violationSpan.getStyle()
                .set("color", " #ff4d4f")
                .set("font-weight", "bold")
                .set("font-size", "14px");
            return violationSpan;
        })
        .forEach(violationsLayout::add);

    return violationsLayout;
}

    public double calculateBasketTotal() {
        return this.basketDTO.getOrders().values().stream()
            .mapToDouble(item -> item.getPrice() * item.getAmount())
            .sum();
    }
}

