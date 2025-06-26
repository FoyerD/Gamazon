package UI.views.components;

import Application.DTOs.ItemDTO;
import Application.DTOs.PaymentDetailsDTO;
import Application.DTOs.SupplyDetailsDTO;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

import org.apache.commons.lang3.function.TriConsumer;

public class MakeOfferDialog extends Dialog {

    public MakeOfferDialog(ItemDTO item, TriConsumer<Double, PaymentDetailsDTO, SupplyDetailsDTO> onMakeOffer) {
        setCloseOnOutsideClick(true);
        setCloseOnEsc(true);

        H3 title = new H3("Make an Offer");

        // Item display
        FormLayout itemInfoLayout = new FormLayout();
        itemInfoLayout.addFormItem(new Paragraph(item.getProductName()), "Product");
        itemInfoLayout.addFormItem(new Paragraph(item.getPrice() + "$"), "Current Price");
        itemInfoLayout.addFormItem(new Paragraph(String.valueOf(item.getAmount())), "Available");

        Paragraph description = new Paragraph("Description: " + item.getDescription());
        Paragraph rating = new Paragraph("Rating: " + item.getRating());

        // Offer input
        NumberField offerField = new NumberField("Offer Price");
        offerField.setMin(0);
        offerField.setStep(0.1);
        offerField.setPlaceholder("$");
        offerField.setWidthFull();

        // Payment details
        TextField cardNumber = new TextField("Card Number");

        DatePicker expiryDate = new DatePicker("Expiry Date");


        PasswordField cvv = new PasswordField("CVV");
        cvv.setPlaceholder("3 digits");

        TextField holderName = new TextField("Cardholder Name");
        holderName.setPlaceholder("Full name");

        TextField holderID = new TextField("Cardholder ID");
        holderID.setPlaceholder("ID number");

        TextField address = new TextField("Delivery Address");
        TextField city = new TextField("City");
        TextField country = new TextField("Country");
        TextField zipCode = new TextField("ZIP Code");

        // Payment form layout
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("400px", 2)
        );

        form.add(offerField, cardNumber);
        form.add(expiryDate, cvv);
        form.add(holderName, holderID);
        form.add(address, city);
        form.add(country, zipCode);

        // Submit button
        Button submit = new Button("Submit Offer", e -> {
            Double offer = offerField.getValue();
            if (offer == null || offer <= 0) {
                Notification.show("Enter a valid offer price.", 4000, Notification.Position.MIDDLE);
                return;
            }
            if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty() || 
                holderName.isEmpty() || holderID.isEmpty() ||
                address.isEmpty() || city.isEmpty() || country.isEmpty() || zipCode.isEmpty()) {
                Notification.show("Please complete all payment and delivery details.", 4000, Notification.Position.MIDDLE);
                return;
            }

            PaymentDetailsDTO payment = new PaymentDetailsDTO(
                holderID.getValue(), // Assuming this is the userId
                cardNumber.getValue(),
                expiryDate.getValue(),
                cvv.getValue(),
                holderName.getValue()
            );

            SupplyDetailsDTO supply = new SupplyDetailsDTO(
                address.getValue(),
                city.getValue(),
                country.getValue(),
                zipCode.getValue(),
                holderName.getValue()
            );

            onMakeOffer.accept(offer, payment, supply);
            close();
        });
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        VerticalLayout layout = new VerticalLayout(
            title,
            itemInfoLayout,
            description,
            rating,
            form,
            submit
        );
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setWidth("450px");

        add(layout);
    }
}
