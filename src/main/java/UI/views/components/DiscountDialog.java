package UI.views.components;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.value.ValueChangeMode;

import Application.DTOs.CategoryDTO;
import Application.DTOs.ConditionDTO;
import Application.DTOs.DiscountDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.DiscountDTO.DiscountType;
import Application.DTOs.DiscountDTO.QualifierType;
import Domain.Store.Discounts.Discount.MergeType;

public class DiscountDialog extends Dialog {
    private final String storeId;
    private final Supplier<List<ItemDTO>> itemSupplier;
    private final Supplier<List<CategoryDTO>> categorySupplier;
    private final Function<DiscountDTO, Boolean> onSave;

    private ComboBox<DiscountType> typeSelect;
    private NumberField percentageField;
    private ComboBox<QualifierType> qualifierTypeSelect;
    private ComboBox<String> qualifierValueSelect;
    private ComboBox<ConditionDTO.ConditionType> conditionTypeSelect;
    private ComboBox<MergeType> mergeTypeSelect;
    private List<DiscountDTO> selectedSubDiscounts;
    private Grid<DiscountDTO> subDiscountsGrid;
    private VerticalLayout compositeLayout;

    public DiscountDialog(
        String storeId,
        Supplier<List<ItemDTO>> itemSupplier,
        Supplier<List<CategoryDTO>> categorySupplier,
        Function<DiscountDTO, Boolean> onSave
    ) {
        this.storeId = storeId;
        this.itemSupplier = itemSupplier;
        this.categorySupplier = categorySupplier;
        this.onSave = onSave;
        this.selectedSubDiscounts = new ArrayList<>();

        setHeaderTitle("Add New Discount");
        
        // Create form layout
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSpacing(true);
        formLayout.setPadding(true);

        // Discount type selection
        typeSelect = new ComboBox<>("Discount Type");
        typeSelect.setItems(DiscountDTO.DiscountType.values());
        typeSelect.setValue(DiscountDTO.DiscountType.SIMPLE);
        typeSelect.addValueChangeListener(e -> updateFormVisibility());

        // Percentage field for simple discounts
        percentageField = new NumberField("Discount Percentage");
        percentageField.setMin(0);
        percentageField.setMax(100);
        percentageField.setStep(0.1);
        percentageField.setValueChangeMode(ValueChangeMode.EAGER);

        // Qualifier type selection for simple discounts
        qualifierTypeSelect = new ComboBox<>("Applies To");
        qualifierTypeSelect.setItems(QualifierType.values());
        qualifierTypeSelect.setValue(QualifierType.STORE);
        qualifierTypeSelect.addValueChangeListener(e -> updateQualifierValueSelect());

        // Qualifier value selection
        qualifierValueSelect = new ComboBox<>("Select Target");
        qualifierValueSelect.setVisible(false);

        // Condition type selection
        conditionTypeSelect = new ComboBox<>("Condition Type");
        conditionTypeSelect.setItems(ConditionDTO.ConditionType.values());
        conditionTypeSelect.setValue(ConditionDTO.ConditionType.TRUE);

        // Merge type for composite discounts
        mergeTypeSelect = new ComboBox<>("Merge Type");
        mergeTypeSelect.setItems(MergeType.values());
        mergeTypeSelect.setValue(MergeType.MAX);
        mergeTypeSelect.setVisible(false);

        // Create composite discount layout
        setupCompositeLayout();

        // Buttons
        Button saveButton = new Button("Save", e -> saveDiscount());
        Button cancelButton = new Button("Cancel", e -> close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setSpacing(true);

        formLayout.add(
            typeSelect,
            percentageField,
            qualifierTypeSelect,
            qualifierValueSelect,
            conditionTypeSelect,
            mergeTypeSelect,
            compositeLayout,
            buttons
        );

        add(formLayout);
        updateFormVisibility();
    }

    private void setupCompositeLayout() {
        compositeLayout = new VerticalLayout();
        compositeLayout.setSpacing(true);
        compositeLayout.setPadding(true);
        compositeLayout.setVisible(false);

        H4 title = new H4("Sub-Discounts");
        Button addSubDiscountButton = new Button("Add Sub-Discount", e -> openSubDiscountDialog());

        // Create grid for sub-discounts
        subDiscountsGrid = new Grid<>();
        setupSubDiscountsGrid();

        compositeLayout.add(title, addSubDiscountButton, subDiscountsGrid);
    }

    private void setupSubDiscountsGrid() {
        subDiscountsGrid.addColumn(d -> d.getType().toString()).setHeader("Type");
        subDiscountsGrid.addColumn(d -> {
            if (d.getType() == DiscountType.SIMPLE) {
                return d.getDiscountPercentage() + "%";
            }
            return "Composite";
        }).setHeader("Discount");
        subDiscountsGrid.addColumn(d -> {
            if (d.getType() == DiscountType.SIMPLE) {
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

        // Add remove button column
        subDiscountsGrid.addComponentColumn(discount -> {
            Button removeButton = new Button("Remove", e -> {
                selectedSubDiscounts.remove(discount);
                refreshSubDiscountsGrid();
            });
            removeButton.getStyle().set("color", "red");
            return removeButton;
        });

        subDiscountsGrid.setHeight("200px");
    }

    private void refreshSubDiscountsGrid() {
        subDiscountsGrid.setItems(selectedSubDiscounts);
    }

    private void openSubDiscountDialog() {
        DiscountDialog subDialog = new DiscountDialog(
            storeId,
            itemSupplier,
            categorySupplier,
            subDiscount -> {
                selectedSubDiscounts.add(subDiscount);
                refreshSubDiscountsGrid();
                return true;
            }
        );
        subDialog.open();
    }

    private void updateFormVisibility() {
        boolean isSimple = typeSelect.getValue() == DiscountType.SIMPLE;
        percentageField.setVisible(isSimple);
        qualifierTypeSelect.setVisible(isSimple);
        qualifierValueSelect.setVisible(isSimple && qualifierTypeSelect.getValue() != QualifierType.STORE);
        mergeTypeSelect.setVisible(!isSimple);
        compositeLayout.setVisible(!isSimple);
        
        if (isSimple) {
            updateQualifierValueSelect();
        }
    }

    private void updateQualifierValueSelect() {
        if (qualifierTypeSelect.getValue() == QualifierType.STORE) {
            qualifierValueSelect.setVisible(false);
            return;
        }

        qualifierValueSelect.setVisible(true);
        qualifierValueSelect.clear();

        if (qualifierTypeSelect.getValue() == QualifierType.PRODUCT) {
            List<ItemDTO> items = itemSupplier.get();
            if (items != null) {
                qualifierValueSelect.setItems(
                    items.stream()
                        .map(ItemDTO::getProductId)
                        .toList()
                );
                qualifierValueSelect.setItemLabelGenerator(id -> 
                    items.stream()
                        .filter(i -> i.getProductId().equals(id))
                        .findFirst()
                        .map(ItemDTO::getProductName)
                        .orElse(id)
                );
            }
        } else if (qualifierTypeSelect.getValue() == QualifierType.CATEGORY) {
            List<CategoryDTO> categories = categorySupplier.get();
            if (categories != null) {
                qualifierValueSelect.setItems(
                    categories.stream()
                        .map(CategoryDTO::getName)
                        .toList()
                );
            }
        }
    }

    private void saveDiscount() {
        try {
            DiscountDTO discount = new DiscountDTO();
            discount.setId(UUID.randomUUID().toString());
            discount.setStoreId(storeId);
            discount.setType(typeSelect.getValue());

            // Create condition
            ConditionDTO condition = new ConditionDTO();
            condition.setId(UUID.randomUUID().toString());
            condition.setType(conditionTypeSelect.getValue());
            discount.setCondition(condition);

            if (discount.getType() == DiscountType.SIMPLE) {
                // Set simple discount fields
                if (percentageField.getValue() == null) {
                    throw new IllegalArgumentException("Please enter a discount percentage");
                }
                discount.setDiscountPercentage(percentageField.getValue().floatValue());
                discount.setQualifierType(qualifierTypeSelect.getValue());
                if (qualifierTypeSelect.getValue() != QualifierType.STORE) {
                    if (qualifierValueSelect.getValue() == null) {
                        throw new IllegalArgumentException("Please select a target for the discount");
                    }
                    discount.setQualifierValue(qualifierValueSelect.getValue());
                }
            } else {
                // Set composite discount fields
                if (selectedSubDiscounts.isEmpty()) {
                    throw new IllegalArgumentException("Please add at least one sub-discount");
                }
                discount.setMergeType(mergeTypeSelect.getValue());
                discount.setSubDiscounts(selectedSubDiscounts);
            }

            if (onSave.apply(discount)) {
                close();
            }
        } catch (Exception e) {
            Notification.show("Error creating discount: " + e.getMessage());
        }
    }
} 