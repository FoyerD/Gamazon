package UI.views.components;

import Application.DTOs.ItemDTO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.formlayout.FormLayout;

import java.util.function.Consumer;

public class MakeOfferDialog extends Dialog {

    public MakeOfferDialog(ItemDTO item, Consumer<Double> onMakeOffer) {
        setCloseOnOutsideClick(true);
        setCloseOnEsc(true);

        H3 title = new H3("Make an Offer");

        // Use FormLayout for a grid-like form layout
        FormLayout itemDetails = new FormLayout();

        Paragraph productName = new Paragraph(item.getProductName());
        Paragraph price = new Paragraph(item.getPrice() + "$");
        Paragraph amount = new Paragraph(String.valueOf(item.getAmount()));

        itemDetails.addFormItem(productName, "Product");
        itemDetails.addFormItem(price, "Current Price");
        itemDetails.addFormItem(amount, "Available");

        Paragraph description = new Paragraph("Description: " + item.getDescription());
        Paragraph rating = new Paragraph("Rating: " + item.getRating());

        NumberField offerField = new NumberField("Your Offer ($)");
        offerField.setMin(0);
        offerField.setStep(0.1);
        offerField.setPlaceholder("Enter your price");
        offerField.setWidthFull();

        Button submit = new Button("Submit Offer", e -> {
            Double offerValue = offerField.getValue();
            if (offerValue == null || offerValue <= 0) {
                Notification.show("Please enter a valid price", 5000, Notification.Position.MIDDLE);
                return;
            }

            onMakeOffer.accept(offerValue);
            close();
        });
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        VerticalLayout layout = new VerticalLayout(
            title,
            itemDetails,
            description,
            rating,
            offerField,
            submit
        );
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setWidth("400px");

        add(layout);
    }
}
