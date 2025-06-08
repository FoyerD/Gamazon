package UI.views.components;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;

import Application.DTOs.ConditionDTO;
import Application.DTOs.DiscountDTO;
import Application.DTOs.ConditionDTO.ConditionType;

public class DiscountsLayout extends VerticalLayout {
    private final Supplier<List<DiscountDTO>> discountsSupplier;
    private final Consumer<DiscountDTO> onRemoveDiscount;

    private List<DiscountDTO> discounts;
    private List<ConditionDTO> conditions;
    private Dialog addConditionDialog;

    public DiscountsLayout(
        Supplier<List<DiscountDTO>> discountsSupplier,
        Consumer<DiscountDTO> onRemoveDiscount
    ) {
        this.discountsSupplier = discountsSupplier;
        this.onRemoveDiscount = onRemoveDiscount;

        this.discounts = new ArrayList<>();
        this.conditions = new ArrayList<>();

        // Create header
        H3 title = new H3("Store Discounts");
        title.getStyle().set("color", "#ffffff");

        setupAddConditionDialog();
        
        // Add condition button
        Button addConditionButton = new Button("Add Condition", VaadinIcon.PLUS.create());
        styleButton(addConditionButton, "#4caf50");
        addConditionButton.addClickListener(e -> addConditionDialog.open());
        
        add(title, addConditionButton);
    }

    private void setupAddConditionDialog() {
        addConditionDialog = new Dialog();
        addConditionDialog.setHeaderTitle("Add New Condition");

        // Create form fields
        TextField idField = new TextField("Condition ID");
        ComboBox<ConditionType> typeComboBox = new ComboBox<>("Condition Type");
        typeComboBox.setItems(ConditionType.values());

        NumberField minPriceField = new NumberField("Minimum Price");
        NumberField maxPriceField = new NumberField("Maximum Price");
        TextField productIdField = new TextField("Product ID");
        NumberField minQuantityField = new NumberField("Minimum Quantity");
        NumberField maxQuantityField = new NumberField("Maximum Quantity");

        // Initially hide optional fields
        minPriceField.setVisible(false);
        maxPriceField.setVisible(false);
        productIdField.setVisible(false);
        minQuantityField.setVisible(false);
        maxQuantityField.setVisible(false);

        // Show/hide fields based on condition type
        typeComboBox.addValueChangeListener(event -> {
            ConditionType selectedType = event.getValue();
            if (selectedType == null) return;

            minPriceField.setVisible(selectedType == ConditionType.MIN_PRICE);
            maxPriceField.setVisible(selectedType == ConditionType.MAX_PRICE);
            productIdField.setVisible(selectedType == ConditionType.MIN_QUANTITY || selectedType == ConditionType.MAX_QUANTITY);
            minQuantityField.setVisible(selectedType == ConditionType.MIN_QUANTITY);
            maxQuantityField.setVisible(selectedType == ConditionType.MAX_QUANTITY);
        });

        // Create buttons
        Button saveButton = new Button("Save", e -> {
            if (idField.isEmpty()) {
                Notification.show("Condition ID is required");
                return;
            }
            if (typeComboBox.isEmpty()) {
                Notification.show("Condition Type is required");
                return;
            }

            ConditionDTO newCondition = new ConditionDTO();
            newCondition.setId(idField.getValue());
            newCondition.setType(typeComboBox.getValue());

            switch (typeComboBox.getValue()) {
                case MIN_PRICE:
                    if (minPriceField.isEmpty()) {
                        Notification.show("Minimum Price is required");
                        return;
                    }
                    newCondition.setMinPrice(minPriceField.getValue());
                    break;
                case MAX_PRICE:
                    if (maxPriceField.isEmpty()) {
                        Notification.show("Maximum Price is required");
                        return;
                    }
                    newCondition.setMaxPrice(maxPriceField.getValue());
                    break;
                case MIN_QUANTITY:
                    if (productIdField.isEmpty() || minQuantityField.isEmpty()) {
                        Notification.show("Product ID and Minimum Quantity are required");
                        return;
                    }
                    newCondition.setProductId(productIdField.getValue());
                    newCondition.setMinQuantity(minQuantityField.getValue().intValue());
                    break;
                case MAX_QUANTITY:
                    if (productIdField.isEmpty() || maxQuantityField.isEmpty()) {
                        Notification.show("Product ID and Maximum Quantity are required");
                        return;
                    }
                    newCondition.setProductId(productIdField.getValue());
                    newCondition.setMaxQuantity(maxQuantityField.getValue().intValue());
                    break;
            }

            conditions.add(newCondition);
            Notification.show("Condition added successfully");
            addConditionDialog.close();
            clearFields(idField, typeComboBox, minPriceField, maxPriceField, productIdField, minQuantityField, maxQuantityField);
        });

        Button cancelButton = new Button("Cancel", e -> {
            clearFields(idField, typeComboBox, minPriceField, maxPriceField, productIdField, minQuantityField, maxQuantityField);
            addConditionDialog.close();
        });

        styleButton(saveButton, "var(--lumo-primary-color)");
        styleButton(cancelButton, "#9e9e9e");

        // Layout for the dialog
        VerticalLayout dialogLayout = new VerticalLayout(
            idField,
            typeComboBox,
            minPriceField,
            maxPriceField,
            productIdField,
            minQuantityField,
            maxQuantityField,
            new HorizontalLayout(saveButton, cancelButton)
        );
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        addConditionDialog.add(dialogLayout);
    }

    private void clearFields(TextField idField, ComboBox<ConditionType> typeComboBox,
                           NumberField minPriceField, NumberField maxPriceField,
                           TextField productIdField, NumberField minQuantityField,
                           NumberField maxQuantityField) {
        idField.clear();
        typeComboBox.clear();
        minPriceField.clear();
        maxPriceField.clear();
        productIdField.clear();
        minQuantityField.clear();
        maxQuantityField.clear();
    }

    private void styleButton(Button button, String color) {
        button.getStyle()
            .set("background-color", color)
            .set("color", "white");
    }
} 