package UI.views;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import Application.utils.Response;
import UI.presenters.IPurchasePresenter;

@Route("checkout")
public class CheckoutView extends VerticalLayout implements BeforeEnterObserver {

    private final IPurchasePresenter purchasePresenter;
    private String sessionToken;

    private final TextField userSSN = new TextField("User SSN");
    private final TextField cardNumberField = new TextField("Credit Card Number");
    private final DatePicker expirationDatePicker = new DatePicker("Expiration Date");
    private final TextField cvvField = new TextField("CVV");
    private final TextField nameField = new TextField("Full Name");
    private final TextField addressField = new TextField("Shipping Address");
    private final TextField cityField = new TextField("City");
    private final TextField countryField = new TextField("Country");
    private final TextField zipField = new TextField("Zip code");


    private final Button completeOrderButton = new Button("Complete Order");
    private final Button backToCartButton = new Button("Return to Cart");

    @Autowired
    public CheckoutView(IPurchasePresenter purchasePresenter) {
        this.purchasePresenter = purchasePresenter;

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background", "linear-gradient(to right, #f0f9ff, #e6fffb)");

        H1 title = new H1("Checkout");
        title.getStyle().set("color", "#0d6efd");

        // Style the form fields
        userSSN.setWidthFull();
        nameField.setWidthFull();
        addressField.setWidthFull();
        cityField.setWidthFull();
        countryField.setWidthFull();
        zipField.setWidthFull();
        cardNumberField.setWidthFull();
        expirationDatePicker.setWidth("200px");
        cvvField.setWidth("100px");

        expirationDatePicker.setPlaceholder("MM/yyyy");
        expirationDatePicker.setClearButtonVisible(true);

        // Create a form layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(userSSN, nameField, addressField, cityField, countryField, zipField);
        formLayout.setColspan(addressField, 2);
        formLayout.setColspan(userSSN, 2);
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.getStyle()
            .set("background-color", "white")
            .set("padding", "20px")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.1)");


        // Payment form
        FormLayout paymentForm = new FormLayout();
        paymentForm.add(cardNumberField, expirationDatePicker, cvvField);
        paymentForm.setColspan(cardNumberField, 2);
        paymentForm.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        paymentForm.getStyle()
            .set("background-color", "white")
            .set("padding", "20px")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.1)")
            .set("margin-top", "20px");

        // Style buttons
        completeOrderButton.getStyle()
            .set("background-color", "#198754")
            .set("color", "white")
            .set("font-weight", "bold");

        backToCartButton.getStyle()
            .set("background-color", "#0d6efd")
            .set("color", "white");

        // Button click listeners
        completeOrderButton.addClickListener(e -> placeOrder());
        backToCartButton.addClickListener(e -> UI.getCurrent().navigate("cart"));

        // Create button layout
        HorizontalLayout buttonLayout = new HorizontalLayout(backToCartButton, completeOrderButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Add all components to the main layout
        add(title,
            new H1("Shipping Information"), formLayout,
            new H1("Payment Details"), paymentForm,
            buttonLayout);
    }

    private void placeOrder() {
        if (!validateForm()) {
            Notification.show("Please fill in all required fields", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Create expiration date from selected DatePicker value
        Date expiryDate;
        LocalDate localExpiry = expirationDatePicker.getValue();
        if (localExpiry == null) {
            Notification.show("Please select an expiration date", 3000, Notification.Position.MIDDLE);
            return;
        }
        expiryDate = Date.from(localExpiry.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Response<Boolean> response = purchasePresenter.purchaseCart(
            sessionToken,
            userSSN.getValue(),
            cardNumberField.getValue(),
            expiryDate,
            cvvField.getValue(),
            nameField.getValue(),
            addressField.getValue(),
            cityField.getValue(),
            countryField.getValue(),
            zipField.getValue()
        );

        if (!response.errorOccurred() && response.getValue()) {
            Notification.show("Order placed successfully!", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("home");
        } else {
            String errorMessage = response.errorOccurred() ?
                response.getErrorMessage() : "There was an error processing your order";
            Notification.show(errorMessage, 3000, Notification.Position.MIDDLE);
        }
    }

    private boolean validateForm() {
        return !nameField.isEmpty() &&
               !userSSN.isEmpty() &&
               !cityField.isEmpty() &&
               !countryField.isEmpty() &&
               !zipField.isEmpty() &&
               !addressField.isEmpty() &&
               !cardNumberField.isEmpty() &&
               expirationDatePicker.getValue() != null &&
               !cvvField.isEmpty();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        if (sessionToken == null) {
            Notification.show("Access denied. Please log in.", 4000, Notification.Position.MIDDLE);
            event.forwardTo("login");
        }
    }
}
