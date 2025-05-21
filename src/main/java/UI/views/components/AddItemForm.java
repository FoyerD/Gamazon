package UI.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;


import Application.DTOs.ProductDTO;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class AddItemForm extends FormLayout {

    private final ComboBox<ProductDTO> productField = new ComboBox<>("Product");
    private final NumberField priceField = new NumberField("Price");
    private final NumberField amountField = new NumberField("Amount");
    private final TextArea descriptionField = new TextArea("Description");
    private final Button submitButton = new Button("Add Item");
    private final Button cancelButton = new Button("Cancel");

    public AddItemForm(Supplier<Set<ProductDTO>> productsFetcher, Consumer<ItemFormData> onAddItem, Runnable onCancel) {
        // Setup combo box
        Set<ProductDTO> products = productsFetcher.get();
        productField.setItems(products);
        productField.setItemLabelGenerator(ProductDTO::getName);
        productField.setPlaceholder("Select a product...");
        productField.setRequired(true);
        productField.setClearButtonVisible(true);
        productField.setAllowCustomValue(false); // one selection only

        priceField.setMin(0.01);
        amountField.setMin(1);
        priceField.setRequiredIndicatorVisible(true);
        amountField.setRequiredIndicatorVisible(true);
        descriptionField.setPlaceholder("Optional description");

        // Layout
        add(productField, priceField, amountField, descriptionField);

        HorizontalLayout actions = new HorizontalLayout(submitButton, cancelButton);
        add(actions);

        // Handlers
        submitButton.addClickListener(e -> {
            if (isValid()) {
                ItemFormData data = new ItemFormData(
                        productField.getValue().getId(),
                        priceField.getValue(),
                        amountField.getValue().intValue(),
                        descriptionField.getValue().trim()
                );
                onAddItem.accept(data);
            } else {
                Notification.show("Please fill in all required fields correctly.", 3000, Notification.Position.TOP_CENTER);
            }
        });

        cancelButton.addClickListener(e -> onCancel.run());
    }

    private boolean isValid() {
        return productField.getValue() != null
                && priceField.getValue() != null && priceField.getValue() > 0
                && amountField.getValue() != null && amountField.getValue() > 0;
    }

    // Inner class for form data transfer
    public static class ItemFormData {
        public final String productId;
        public final double price;
        public final int amount;
        public final String description;

        public ItemFormData(String productId, double price, int amount, String description) {
            this.productId = productId;
            this.price = price;
            this.amount = amount;
            this.description = description;
        }
    }
}
