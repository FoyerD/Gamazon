package UI.views.components;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;

import Application.DTOs.CategoryDTO;
import Application.DTOs.ConditionDTO;
import Application.DTOs.ConditionDTO.ConditionType;
import Application.DTOs.ItemDTO;

public class AddConditionForm extends VerticalLayout {

    private final ComboBox<ConditionType> typeComboBox;
    private final NumberField minPriceField;
    private final NumberField maxPriceField;
    private final ComboBox<ItemDTO> productComboBox;
    private final ComboBox<CategoryDTO> categoryComboBox;
    private final NumberField minQuantityField;
    private final NumberField maxQuantityField;


    public AddConditionForm(Supplier<List<ItemDTO>> itemSupplier, 
                            Supplier<List<CategoryDTO>> categorySupplier) {
        Span header = new Span("Add New Condition");

        // Create form fields
        // TextField idField = new TextField("Condition ID");
        typeComboBox = new ComboBox<>("Condition Type");
        typeComboBox.setItems(Stream.of(ConditionType.values()).filter(type -> !List.of(ConditionType.AND, ConditionType.OR, ConditionType.XOR).contains(type)).toList());

        minPriceField = new NumberField("Minimum Price");
        minPriceField.setMin(0);
        maxPriceField = new NumberField("Maximum Price");
        maxPriceField.setMin(0);

        // Create ComboBoxes but don't populate them yet
        productComboBox = new ComboBox<>("Product");
        productComboBox.setItemLabelGenerator(ItemDTO::getProductName);

        categoryComboBox = new ComboBox<>("Category");
        categoryComboBox.setItemLabelGenerator(CategoryDTO::getName);

        minQuantityField = new NumberField("Minimum Quantity");
        maxQuantityField = new NumberField("Maximum Quantity");

        VerticalLayout additionalFields = new VerticalLayout(
            minPriceField,
            maxPriceField,
            productComboBox,
            categoryComboBox,
            minQuantityField,
            maxQuantityField
        );
        additionalFields.setSpacing(false);
        additionalFields.setPadding(false);

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
            List<ItemDTO> products = itemSupplier.get();
            if (products != null) {
                productComboBox.setItems(products);
            }
            
            List<CategoryDTO> categories = categorySupplier.get();
            if (categories != null) {
                categoryComboBox.setItems(categories);
            }
        });

    
        this.add(
            header,
            typeComboBox, 
            additionalFields
        );

        this.setSpacing(false);
        this.setPadding(false);
    }

    public ConditionDTO getCondition() {
        if (typeComboBox.isEmpty()) {
            Notification.show("Condition Type is required");
            return null;
        }

        ConditionDTO newCondition = new ConditionDTO();
        // newCondition.setId(idField.getValue());
        newCondition.setType(typeComboBox.getValue());

        switch (typeComboBox.getValue()) {
            case MIN_PRICE:
                if (minPriceField.isEmpty()) {
                    Notification.show("Minimum Price is required");
                    return null;
                }
                newCondition.setMinPrice(minPriceField.getValue());
                break;
            case MAX_PRICE:
                if (maxPriceField.isEmpty()) {
                    Notification.show("Maximum Price is required");
                    return null;
                }
                newCondition.setMaxPrice(maxPriceField.getValue());
                break;
            case MIN_QUANTITY:
                if (productComboBox.isEmpty() || minQuantityField.isEmpty()) {
                    Notification.show("Product and Minimum Quantity are required");
                    return null;
                }
                newCondition.setProductId(productComboBox.getValue().getProductId());
                newCondition.setMinQuantity(minQuantityField.getValue().intValue());
                break;
            case MAX_QUANTITY:
                if (productComboBox.isEmpty() || maxQuantityField.isEmpty()) {
                    Notification.show("Product and Maximum Quantity are required");
                    return null;
                }
                newCondition.setProductId(productComboBox.getValue().getProductId());
                newCondition.setMaxQuantity(maxQuantityField.getValue().intValue());
                break;
            case TRUE: 
                // TRUE condition does not require any additional fields
                break;
            default:
                Notification.show("Please select a valid condition type");
                return null;
        }
        return newCondition;
    }

    public void clearFields() {
        typeComboBox.clear();
        minPriceField.clear();
        maxPriceField.clear();
        productComboBox.clear();
        categoryComboBox.clear();
        minQuantityField.clear();
        maxQuantityField.clear();
    }
    
}
