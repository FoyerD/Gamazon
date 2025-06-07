package UI.views.components;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.DiscountDTO;

public class DiscountsLayout extends VerticalLayout {
    private final Grid<DiscountDTO> discountsGrid;
    private final Supplier<List<DiscountDTO>> discountsSupplier;
    private final Consumer<Void> onAddDiscount;
    private final Consumer<DiscountDTO> onRemoveDiscount;

    public DiscountsLayout(
        Supplier<List<DiscountDTO>> discountsSupplier,
        Consumer<Void> onAddDiscount,
        Consumer<DiscountDTO> onRemoveDiscount
    ) {
        this.discountsSupplier = discountsSupplier;
        this.onAddDiscount = onAddDiscount;
        this.onRemoveDiscount = onRemoveDiscount;

        // Create header
        H3 title = new H3("Store Discounts");
        title.getStyle().set("color", "#ffffff");

        // Create add button
        Button addButton = new Button("Add Discount", VaadinIcon.PLUS.create());
        styleButton(addButton, "#4caf50");
        addButton.addClickListener(e -> onAddDiscount.accept(null));

        // Create grid
        discountsGrid = new Grid<>();
        setupGrid();

        // Add components
        add(new HorizontalLayout(title, addButton), discountsGrid);
        setSpacing(true);
        setPadding(true);

        // Initial load
        refreshDiscounts();
    }

    private void setupGrid() {
        discountsGrid.addColumn(d -> d.getType().toString()).setHeader("Type");
        discountsGrid.addColumn(d -> {
            if (d.getType() == DiscountDTO.DiscountType.SIMPLE) {
                return d.getDiscountPercentage() + "%";
            }
            return "Composite";
        }).setHeader("Discount");
        discountsGrid.addColumn(d -> {
            if (d.getType() == DiscountDTO.DiscountType.SIMPLE) {
                switch (d.getQualifierType()) {
                    case PRODUCT:
                        return "Product: " + d.getQualifierValue();
                    case CATEGORY:
                        return "Category: " + d.getQualifierValue();
                    case STORE:
                        return "Entire Store";
                    default:
                        return "Unknown";
                }
            }
            return "Multiple";
        }).setHeader("Applies To");
        discountsGrid.addColumn(d -> {
            if (d.getCondition() != null) {
                return d.getCondition().getType().toString();
            }
            return "None";
        }).setHeader("Condition");

        // Add remove button column
        discountsGrid.addComponentColumn(discount -> {
            Button removeButton = new Button("Remove", VaadinIcon.TRASH.create());
            styleButton(removeButton, "#f44336");
            removeButton.addClickListener(e -> onRemoveDiscount.accept(discount));
            return removeButton;
        });

        discountsGrid.setHeight("300px");
    }

    public void refreshDiscounts() {
        List<DiscountDTO> discounts = discountsSupplier.get();
        if (discounts != null) {
            discountsGrid.setItems(discounts);
        }
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