package UI.views.components;

import Application.DTOs.PolicyDTO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style.AlignItems;
import com.vaadin.flow.dom.Style.JustifyContent;
import com.vaadin.flow.dom.Style.TextAlign;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PoliciesLayout extends VerticalLayout {

    private final Supplier<List<PolicyDTO>> policySupplier;
    private final Runnable onAdd;
    private final Consumer<PolicyDTO> onRemove;

    //private final Grid<PolicyDTO> policyGrid = new Grid<>(PolicyDTO.class, false);

    private final FlexLayout tileContainer = new FlexLayout();

    public PoliciesLayout(Supplier<List<PolicyDTO>> policySupplier,
                      Runnable onAdd,
                      Consumer<PolicyDTO> onRemove) {
        this.policySupplier = policySupplier;
        this.onAdd = onAdd;
        this.onRemove = onRemove;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        buildLayout();
    }

    private void buildLayout() {
        Button addButton = new Button("Add Policy", VaadinIcon.PLUS.create(), e -> onAdd.run());
        addButton.setWidth("150px");

        styleButton(addButton, "rgb(6, 177, 0)");
        
        tileContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        tileContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        tileContainer.setWidthFull();
        tileContainer.getStyle()
            .set("display", "flex")
            .set("flex-wrap", "wrap")
            .set("gap", "16px") // Adds gap between tiles
            .set("padding", "12px");
        add(addButton, tileContainer);
    }

    // private Button buildRemoveButton(PolicyDTO policy) {
    //     Button remove = new Button("Remove", e -> {
    //         onRemove.accept(policy);
    //         refreshPolicies();
    //     });
    //     remove.getStyle().set("color", "red");
    //     return remove;
    // }

    public void refreshPolicies() {
        tileContainer.removeAll();
        if (policySupplier == null) return;

        List<PolicyDTO> policies = policySupplier.get();
        if (policies == null || policies.isEmpty()) return;

        for (PolicyDTO policy : policies) {
            tileContainer.add(buildPolicyTile(policy));
        }
    }

    private Div buildPolicyTile(PolicyDTO policy) {
        Div tile = new Div();

        tile.getStyle()
            .set("width", "280px")
            .set("min-height", "200px")
            .set("border", "5px solid rgb(165, 190, 240)") // dark border
            .set("border-radius", "10px")
            .set("padding", "16px")
            .set("background-color", "rgb(214, 228, 255)") // light background
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");


        // hover effect
        tile.getElement().addEventListener("mouseover", e -> 
        tile.getStyle()
            .set("transform", "translateY(-2px)")
            .set("box-shadow", "0 4px 10px rgba(0,0,0,0.3)"));

        tile.getElement().addEventListener("mouseout", e -> 
        tile.getStyle()
            .set("transform", "translateY(0)")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));


        Div title = new Div(policy.getType().toString());
        title.getStyle()
            .setFontWeight("bold")
            .set("color", "rgb(76, 76, 76)")
            .setTextAlign(TextAlign.CENTER);


        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);


        if (policy.getTargetProduct() != null) {
            content.add(new Span("Product: " + policy.getTargetProduct().getProductName()));
        }
        if (policy.getTargetCategory() != null) {
            content.add(new Span("Category: " + policy.getTargetCategory().getName()));
        }
        if (policy.getDisallowedCategory() != null) {
            content.add(new Span("Disallowed: " + policy.getDisallowedCategory().getName()));
        }
        if (policy.getAgeCategory() != null) {
            content.add(new Span("Age Category: " + policy.getAgeCategory().getName()));
        }
        if (policy.getMinAge() != null && policy.getMinAge() >= 0) {
            content.add(new Span("Min Age: " + policy.getMinAge()));
        }
        if (policy.getMinItemsAll() != null && policy.getMinItemsAll() > 0) {
            content.add(new Span("Min quantity: " + policy.getMinItemsAll()));
        }
        if (policy.getMaxItemsAll() != null && policy.getMaxItemsAll() > 0) {
            content.add(new Span("Max quantity: " + policy.getMaxItemsAll()));
        }
        if (policy.getMinItemsProduct() != null && policy.getMinItemsProduct() > 0) {
            content.add(new Span("Min quantity: " + policy.getMinItemsProduct()));
        }
        if (policy.getMaxItemsProduct() != null && policy.getMaxItemsProduct() > 0) {
            content.add(new Span("Max quantity: " + policy.getMaxItemsProduct()));
        }
        if (policy.getMinItemsCategory() != null && policy.getMinItemsCategory() > 0) {
            content.add(new Span("Min quantity: " + policy.getMinItemsCategory()));
        }
        if (policy.getMaxItemsCategory() != null && policy.getMaxItemsCategory() > 0) {
            content.add(new Span("Max quantity: " + policy.getMaxItemsCategory()));
        }

        // if (policy.getSubPolicies() != null && !policy.getSubPolicies().isEmpty()) {
        //     content.add(new Span("Sub-Policies: " + policy.getSubPolicies().size()));
        // }

        // Remove button is not required

        // Button removeBtn = new Button("Remove", e -> {
        //     onRemove.accept(policy);
        //     refreshPolicies();
        // });
        // removeBtn.getStyle().set("color", "red");

        // HorizontalLayout footer = new HorizontalLayout(removeBtn);
        // footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        // footer.setWidthFull();

        tile.add(title, content);
        return tile;
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
