package UI.views.components;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;

import Application.DTOs.CategoryDTO;
import Application.DTOs.ConditionDTO;
import Application.DTOs.DiscountDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.ConditionDTO.ConditionType;
import Application.DTOs.DiscountDTO.DiscountType;
import Application.DTOs.DiscountDTO.QualifierType;

public class DiscountsLayout extends VerticalLayout {
    private String storeId;
    private final Supplier<List<DiscountDTO>> discountsSupplier;
    private final Consumer<DiscountDTO> onRemoveDiscount;
    private final Consumer<DiscountDTO> onAddDiscount;

    private Supplier<List<ItemDTO>> itemSupplier;
    private Supplier<List<CategoryDTO>> categorySupplier;

    private List<DiscountDTO> discounts;
    private List<ConditionDTO> conditions;
    private Dialog addConditionDialog;
    private Dialog addDiscountDialog;
    private Grid<DiscountDTO> discountGrid;

    public DiscountsLayout(
        String storeId,
        Supplier<List<DiscountDTO>> discountsSupplier,
        Consumer<DiscountDTO> onRemoveDiscount,
        Supplier<List<ItemDTO>> itemSupplier,
        Supplier<List<CategoryDTO>> categorySupplier,
        Consumer<DiscountDTO> onAddDiscount
    ) {
        this.storeId = storeId;

        this.discountsSupplier = discountsSupplier;
        this.onRemoveDiscount = onRemoveDiscount;
        this.onAddDiscount = onAddDiscount;

        this.discounts = new ArrayList<>();
        this.conditions = new ArrayList<>();

        this.itemSupplier = itemSupplier;
        this.categorySupplier = categorySupplier;
        

        // Create header
        H3 title = new H3("Store Discounts");
        title.getStyle().set("color", "#ffffff");

        setupAddConditionDialog();
        setupAddDiscountDialog();
        setupDiscountGrid();
        
        // Add buttons
        Button addConditionButton = new Button("Add Condition", VaadinIcon.PLUS.create());
        Button addDiscountButton = new Button("Add Discount", VaadinIcon.PLUS.create());
        
        styleButton(addConditionButton, "#4caf50");
        styleButton(addDiscountButton, "#2196f3");
        
        addConditionButton.addClickListener(e -> addConditionDialog.open());
        addDiscountButton.addClickListener(e -> addDiscountDialog.open());
        
        HorizontalLayout buttons = new HorizontalLayout(addConditionButton, addDiscountButton);
        buttons.setSpacing(true);

        // Add save discounts button
        Button saveDiscountsButton = new Button("Save All Discounts", VaadinIcon.CHECK.create());
        styleButton(saveDiscountsButton, "#ff9800");
        saveDiscountsButton.addClickListener(e -> saveDiscounts());
        
        add(title, buttons, discountGrid, saveDiscountsButton);
        
        // Set alignment and spacing
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setPadding(true);

        // Load initial discounts
        // refreshDiscounts();
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
        
        // Create ComboBoxes but don't populate them yet
        ComboBox<ItemDTO> productComboBox = new ComboBox<>("Product");
        productComboBox.setItemLabelGenerator(ItemDTO::getProductName);

        ComboBox<CategoryDTO> categoryComboBox = new ComboBox<>("Category");
        categoryComboBox.setItemLabelGenerator(CategoryDTO::getName);

        NumberField minQuantityField = new NumberField("Minimum Quantity");
        NumberField maxQuantityField = new NumberField("Maximum Quantity");

        // Initially hide optional fields
        minPriceField.setVisible(false);
        maxPriceField.setVisible(false);
        productComboBox.setVisible(false);
        categoryComboBox.setVisible(false);
        minQuantityField.setVisible(false);
        maxQuantityField.setVisible(false);

        // Show/hide fields based on condition type
        typeComboBox.addValueChangeListener(event -> {
            ConditionType selectedType = event.getValue();
            if (selectedType == null) return;

            minPriceField.setVisible(selectedType == ConditionType.MIN_PRICE);
            maxPriceField.setVisible(selectedType == ConditionType.MAX_PRICE);
            productComboBox.setVisible(selectedType == ConditionType.MIN_QUANTITY || selectedType == ConditionType.MAX_QUANTITY);
            minQuantityField.setVisible(selectedType == ConditionType.MIN_QUANTITY);
            maxQuantityField.setVisible(selectedType == ConditionType.MAX_QUANTITY);
        });

        // Add dialog open listener to populate ComboBoxes
        addConditionDialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                List<ItemDTO> products = itemSupplier.get();
                if (products != null) {
                    productComboBox.setItems(products);
                }
                
                List<CategoryDTO> categories = categorySupplier.get();
                if (categories != null) {
                    categoryComboBox.setItems(categories);
                }
            }
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
                    if (productComboBox.isEmpty() || minQuantityField.isEmpty()) {
                        Notification.show("Product and Minimum Quantity are required");
                        return;
                    }
                    newCondition.setProductId(productComboBox.getValue().getProductId());
                    newCondition.setMinQuantity(minQuantityField.getValue().intValue());
                    break;
                case MAX_QUANTITY:
                    if (productComboBox.isEmpty() || maxQuantityField.isEmpty()) {
                        Notification.show("Product and Maximum Quantity are required");
                        return;
                    }
                    newCondition.setProductId(productComboBox.getValue().getProductId());
                    newCondition.setMaxQuantity(maxQuantityField.getValue().intValue());
                    break;
            }

            conditions.add(newCondition);
            Notification.show("Condition added successfully");
            addConditionDialog.close();
            clearFields(idField, typeComboBox, minPriceField, maxPriceField, productComboBox, categoryComboBox, minQuantityField, maxQuantityField);
        });

        Button cancelButton = new Button("Cancel", e -> {
            clearFields(idField, typeComboBox, minPriceField, maxPriceField, productComboBox, categoryComboBox, minQuantityField, maxQuantityField);
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
            productComboBox,
            categoryComboBox,
            minQuantityField,
            maxQuantityField,
            new HorizontalLayout(saveButton, cancelButton)
        );
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        addConditionDialog.add(dialogLayout);
    }

    private void setupAddDiscountDialog() {
        addDiscountDialog = new Dialog();
        addDiscountDialog.setHeaderTitle("Add New Simple Discount");

        // Create form fields
        TextField idField = new TextField("Discount ID");
        
        NumberField percentageField = new NumberField("Discount Percentage");
        percentageField.setMin(0);
        percentageField.setMax(100);
        percentageField.setStep(1);
        percentageField.setValue(0.0);
        percentageField.setHelperText("Enter a value between 0 and 100");
        
        ComboBox<QualifierType> qualifierTypeComboBox = new ComboBox<>("Qualifier Type");
        qualifierTypeComboBox.setItems(QualifierType.values());
        
        ComboBox<ItemDTO> productComboBox = new ComboBox<>("Product");
        productComboBox.setItemLabelGenerator(ItemDTO::getProductName);
        productComboBox.setVisible(false);
        
        ComboBox<CategoryDTO> categoryComboBox = new ComboBox<>("Category");
        categoryComboBox.setItemLabelGenerator(CategoryDTO::getName);
        categoryComboBox.setVisible(false);
        
        ComboBox<ConditionDTO> conditionComboBox = new ComboBox<>("Condition");
        conditionComboBox.setItemLabelGenerator(condition -> 
            condition.getType().toString() + " (ID: " + condition.getId() + ")");
        
        // Add dialog open listener to populate ComboBoxes
        addDiscountDialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                List<ItemDTO> products = itemSupplier.get();
                if (products != null) {
                    productComboBox.setItems(products);
                }
                
                List<CategoryDTO> categories = categorySupplier.get();
                if (categories != null) {
                    categoryComboBox.setItems(categories);
                }
                
                if (!conditions.isEmpty()) {
                    conditionComboBox.setItems(conditions);
                }
            }
        });

        // Show/hide fields based on qualifier type
        qualifierTypeComboBox.addValueChangeListener(event -> {
            QualifierType selectedType = event.getValue();
            if (selectedType == null) return;

            productComboBox.setVisible(selectedType == QualifierType.PRODUCT);
            categoryComboBox.setVisible(selectedType == QualifierType.CATEGORY);
        });

        // Create buttons
        Button saveButton = new Button("Save", e -> {
            if (idField.isEmpty()) {
                Notification.show("Discount ID is required");
                return;
            }
            if (qualifierTypeComboBox.isEmpty()) {
                Notification.show("Qualifier Type is required");
                return;
            }
            if (percentageField.isEmpty()) {
                Notification.show("Discount Percentage is required");
                return;
            }

            DiscountDTO newDiscount = new DiscountDTO();
            newDiscount.setId(idField.getValue());
            newDiscount.setType(DiscountType.SIMPLE);
            Float precentage = percentageField.getValue().floatValue();
            newDiscount.setDiscountPercentage(precentage / 100);
            newDiscount.setQualifierType(qualifierTypeComboBox.getValue());
            
            String qualifierValue = null;
            switch (qualifierTypeComboBox.getValue()) {
                case PRODUCT:
                    if (productComboBox.isEmpty()) {
                        Notification.show("Product selection is required");
                        return;
                    }
                    qualifierValue = productComboBox.getValue().getProductId();
                    break;
                case CATEGORY:
                    if (categoryComboBox.isEmpty()) {
                        Notification.show("Category selection is required");
                        return;
                    }
                    qualifierValue = categoryComboBox.getValue().getName();
                    break;
                case STORE:
                    qualifierValue = storeId;
                    break;
            }

            newDiscount.setQualifierValue(qualifierValue);

            if (!conditionComboBox.isEmpty()) {
                newDiscount.setCondition(conditionComboBox.getValue());
            }
            
            newDiscount.setStoreId(storeId);
            newDiscount.setSubDiscounts(new ArrayList<>());

            discounts.add(newDiscount);
            Notification.show("Discount added successfully");
            addDiscountDialog.close();
            clearDiscountFields(idField, percentageField, qualifierTypeComboBox, productComboBox, categoryComboBox, conditionComboBox);
            refreshDiscounts();
        });

        Button cancelButton = new Button("Cancel", e -> {
            clearDiscountFields(idField, percentageField, qualifierTypeComboBox, productComboBox, categoryComboBox, conditionComboBox);
            addDiscountDialog.close();
        });

        styleButton(saveButton, "var(--lumo-primary-color)");
        styleButton(cancelButton, "#9e9e9e");

        // Layout for the dialog
        VerticalLayout dialogLayout = new VerticalLayout(
            idField,
            percentageField,
            qualifierTypeComboBox,
            productComboBox,
            categoryComboBox,
            conditionComboBox,
            new HorizontalLayout(saveButton, cancelButton)
        );
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        addDiscountDialog.add(dialogLayout);
    }

    private void setupDiscountGrid() {
        discountGrid = new Grid<>();
        
        // Add columns
        discountGrid.addColumn(d -> d.getType().toString()).setHeader("Type");
        discountGrid.addColumn(d -> String.format("%.0f%%", d.getDiscountPercentage() * 100))
            .setHeader("Discount");
        discountGrid.addColumn(d -> d.getQualifierType().toString()).setHeader("Qualifier Type");
        discountGrid.addColumn(DiscountDTO::getQualifierValue).setHeader("Qualifier Value");
        discountGrid.addColumn(d -> d.getCondition() != null ? 
            d.getCondition().getType().toString() + " (ID: " + d.getCondition().getId() + ")" : 
            "None"
        ).setHeader("Condition");

        // Add remove button column
        discountGrid.addComponentColumn(discount -> {
            Button removeButton = new Button("Remove", VaadinIcon.TRASH.create());
            styleButton(removeButton, "#f44336");
            removeButton.addClickListener(e -> {
                onRemoveDiscount.accept(discount);
                refreshDiscounts();
            });
            return removeButton;
        }).setHeader("Actions");

        // Style the grid
        discountGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        discountGrid.setWidthFull();
        discountGrid.setHeight("400px");
    }

    public void refreshDiscounts() {
        List<DiscountDTO> currentDiscounts = discountsSupplier.get();
        if (currentDiscounts != null) {
            if (currentDiscounts.isEmpty()) {
                Notification.show("No discounts available");
            } else {
                Notification.show("Discounts loaded successfully");
            }
            discountGrid.setItems(currentDiscounts);
        }
    }

    private void clearFields(TextField idField, ComboBox<ConditionType> typeComboBox,
                           NumberField minPriceField, NumberField maxPriceField,
                           ComboBox<ItemDTO> productComboBox, ComboBox<CategoryDTO> categoryComboBox,
                           NumberField minQuantityField, NumberField maxQuantityField) {
        idField.clear();
        typeComboBox.clear();
        minPriceField.clear();
        maxPriceField.clear();
        productComboBox.clear();
        categoryComboBox.clear();
        minQuantityField.clear();
        maxQuantityField.clear();
    }

    private void clearDiscountFields(
        TextField idField,
        NumberField percentageField,
        ComboBox<QualifierType> qualifierTypeComboBox,
        ComboBox<ItemDTO> productComboBox,
        ComboBox<CategoryDTO> categoryComboBox,
        ComboBox<ConditionDTO> conditionComboBox
    ) {
        idField.clear();
        percentageField.setValue(0.0);
        qualifierTypeComboBox.clear();
        productComboBox.clear();
        categoryComboBox.clear();
        conditionComboBox.clear();
    }

    private void styleButton(Button button, String color) {
        button.getStyle()
            .set("background-color", color)
            .set("color", "white");
    }

    private void saveDiscounts() {
        for (DiscountDTO discount : discounts) {
            onAddDiscount.accept(discount);
        }
        discounts.clear();
        Notification.show("Discounts saved successfully");
        refreshDiscounts();
    }

    
} 