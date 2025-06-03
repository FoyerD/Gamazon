package UI.views.components;

import Application.DTOs.*;
import Domain.Store.Policy;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class PolicyDialog extends Dialog {

    private final Supplier<List<ItemDTO>> productSupplier;
    private final Supplier<List<CategoryDTO>> categorySupplier;
    private final String storeId;
    private final Function<PolicyDTO, Boolean> onSave;

    private final Binder<PolicyFormModel> binder = new Binder<>(PolicyFormModel.class);

    private final ComboBox<Policy.Type> typeCombo = new ComboBox<>("Policy Type");
    private final ComboBox<ItemDTO> productCombo = new ComboBox<>("Target Product");
    private final ComboBox<CategoryDTO> categoryCombo = new ComboBox<>("Target Category");
    private final IntegerField minField = new IntegerField("Min Value");
    private final IntegerField maxField = new IntegerField("Max Value");
    private final IntegerField ageField = new IntegerField("Minimum Age");


    public PolicyDialog(String storeId,
                        Supplier<List<ItemDTO>> productSupplier,
                        Supplier<List<CategoryDTO>> categorySupplier,
                        Function<PolicyDTO, Boolean> onSave) {

        this.storeId = storeId;
        this.productSupplier = productSupplier;
        this.categorySupplier = categorySupplier;
        this.onSave = onSave;

        setHeaderTitle("Create Store Policy");
        setWidth("600px");

        setupForm();
        refreshVisibleFields();
    }


    private void setupForm() {
        typeCombo.setItems(Arrays.asList(Policy.Type.values()).stream().filter(t -> !t.equals(Policy.Type.AND)).toList());
        typeCombo.setRequired(true);
        binder.forField(typeCombo)
                .asRequired("Policy type is required")
                .bind(PolicyFormModel::getType, PolicyFormModel::setType);

        productCombo.setItemLabelGenerator(ItemDTO::getProductName);
        List<ItemDTO> products = productSupplier.get();
        if (products != null)
            productCombo.setItems(products);
        binder.forField(productCombo)
                .bind(PolicyFormModel::getProduct, PolicyFormModel::setProduct);

        categoryCombo.setItemLabelGenerator(CategoryDTO::getName);
        List<CategoryDTO> categories = categorySupplier.get();
        if (categories != null)
            categoryCombo.setItems(categories);
        binder.forField(categoryCombo)
                .bind(PolicyFormModel::getCategory, PolicyFormModel::setCategory);

        minField.setMin(0);
        maxField.setMin(0);
        ageField.setMin(0);

        binder.forField(minField)
                .withValidator(val -> val == null || val >= 0, "Must be ≥ 0")
                .bind(PolicyFormModel::getMinValue, PolicyFormModel::setMinValue);
        binder.forField(maxField)
                .withValidator(val -> val == null || val >= 0, "Must be ≥ 0")
                .bind(PolicyFormModel::getMaxValue, PolicyFormModel::setMaxValue);
        binder.forField(ageField)
                .withValidator(val -> val == null || val >= 0, "Must be ≥ 0")
                .bind(PolicyFormModel::getMinAge, PolicyFormModel::setMinAge);

        typeCombo.addValueChangeListener(e -> refreshVisibleFields());

        FormLayout form = new FormLayout(typeCombo, minField, maxField, productCombo, categoryCombo, ageField);


     
        Button saveSinglePolicy = new Button("Save Single Policy", e -> {
            PolicyDTO dto = buildSinglePolicy();
            if (dto != null) {
                if (onSave.apply(dto));
                    close();
            }
        });

        styleButton(saveSinglePolicy, "rgb(0, 81, 180)");

        Button cancel = new Button("Cancel", VaadinIcon.ARROW_BACKWARD.create(),  e -> close());
        styleButton(cancel, "rgb(235, 0, 0)");

        HorizontalLayout buttonLayout = new HorizontalLayout(saveSinglePolicy, cancel);
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        VerticalLayout layout = new VerticalLayout(form, buttonLayout);
        add(layout);
    }

    private void refreshVisibleFields() {
        Policy.Type type = typeCombo.getValue();
        minField.setVisible(false);
        maxField.setVisible(false);
        productCombo.setVisible(false);
        categoryCombo.setVisible(false);
        ageField.setVisible(false);

        if (type == null) return;
        switch (type) {
            case MIN_QUANTITY_ALL -> minField.setVisible(true);
            case MAX_QUANTITY_ALL -> maxField.setVisible(true);
            case MIN_QUANTITY_PRODUCT -> {
                productCombo.setVisible(true);
                minField.setVisible(true);
            }
            case MAX_QUANTITY_PRODUCT -> {
                productCombo.setVisible(true);
                maxField.setVisible(true);
            }
            case MIN_QUANTITY_CATEGORY -> {
                categoryCombo.setVisible(true);
                minField.setVisible(true);
            }
            case MAX_QUANTITY_CATEGORY -> {
                categoryCombo.setVisible(true);
                maxField.setVisible(true);
            }
            case CATEGORY_DISALLOW -> categoryCombo.setVisible(true);
            case CATEGORY_AGE -> {
                ageField.setVisible(true);
                categoryCombo.setVisible(true);
            }
            case AND -> Notification.show("cannot choose AND type");
        }
    }


    private PolicyDTO buildSinglePolicy() {
        PolicyFormModel formModel = new PolicyFormModel();
        try {
            binder.writeBean(formModel);
        } catch (ValidationException e) {
            Notification.show("Validation failed", 3000, Notification.Position.MIDDLE);
            return null;
        }

        PolicyDTO.Builder builder = new PolicyDTO.Builder(storeId, formModel.getType());

        try {
            return switch (formModel.getType()) {
                case MIN_QUANTITY_ALL -> builder.createMinQuantityAllPolicy(formModel.getMinValue()).build();
                case MAX_QUANTITY_ALL -> builder.createMaxQuantityAllPolicy(formModel.getMaxValue()).build();
                case MIN_QUANTITY_PRODUCT -> builder.createMinQuantityProductPolicy(formModel.getProduct(), formModel.getMinValue()).build();
                case MAX_QUANTITY_PRODUCT -> builder.createMaxQuantityProductPolicy(formModel.getProduct(), formModel.getMaxValue()).build();
                case MIN_QUANTITY_CATEGORY -> builder.createMinQuantityCategoryPolicy(formModel.getCategory(), formModel.getMinValue()).build();
                case MAX_QUANTITY_CATEGORY -> builder.createMaxQuantityCategoryPolicy(formModel.getCategory(), formModel.getMaxValue()).build();
                case CATEGORY_DISALLOW -> builder.createCategoryDisallowPolicy(formModel.getCategory()).build();
                case CATEGORY_AGE -> builder.createCategoryAgePolicy(formModel.getCategory(), formModel.getMinAge()).build();
                default -> null;
            };
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
            return null;
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


class PolicyFormModel {
    private Policy.Type type;
    private ItemDTO product;
    private CategoryDTO category;
    private Integer minValue;
    private Integer maxValue;
    private Integer minAge;

    public Policy.Type getType() { return type; }
    public void setType(Policy.Type type) { this.type = type; }

    public ItemDTO getProduct() { return product; }
    public void setProduct(ItemDTO product) { this.product = product; }

    public CategoryDTO getCategory() { return category; }
    public void setCategory(CategoryDTO category) { this.category = category; }

    public Integer getMinValue() { return minValue; }
    public void setMinValue(Integer minValue) { this.minValue = minValue; }

    public Integer getMaxValue() { return maxValue; }
    public void setMaxValue(Integer maxValue) { this.maxValue = maxValue; }

    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }


}
