package UI.views;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import Application.DTOs.ReceiptDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;
import UI.presenters.IPurchasePresenter;
import UI.presenters.IStorePresenter;

@Route("user-profile")
public class UserProfileView extends BaseView implements BeforeEnterObserver {

    private String sessionToken = null;
    private UserDTO userDTO = null;

    private final IPurchasePresenter purchasePresenter;

    public UserProfileView(IPurchasePresenter purchasePresenter, IStorePresenter storePresenter, @Autowired(required = false) DbHealthStatus dbHealthStatus, @Autowired(required = false) GlobalLogoutManager logoutManager) {
        super(dbHealthStatus, logoutManager);
        this.purchasePresenter = purchasePresenter;

        // Set yellow-themed layout styles
        setWidthFull();
        setHeightFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        setPadding(true);
        getStyle().set("background", " #fffbe7"); // light yellow background
    }

    public void setupPage() {
        // Header
        H1 header = new H1("Hello " + userDTO.getUsername() + "!");
        header.getStyle()
              .set("color", "#222")
              .set("background-color", " #fff")  // very light yellow/white
              .set("font-family", "'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif")
              .set("padding", "1rem 2rem")
              .set("border-radius", "8px")
              .set("box-shadow", "0 4px 8px rgba(0,0,0,0.08)")
              .set("margin-bottom", "1rem")
              .set("transition", "transform 0.2s, box-shadow 0.2s");
        
        // Add hover interaction via JS
        header.getElement().executeJs(
            "this.addEventListener('mouseenter', () => {" +
            "  this.style.boxShadow = '0 8px 16px rgba(0,0,0,0.1)';" +
            "  this.style.transform = 'scale(1.01)';" +
            "});" +
            "this.addEventListener('mouseleave', () => {" +
            "  this.style.boxShadow = '0 4px 8px rgba(0,0,0,0.05)';" +
            "  this.style.transform = 'scale(1)';" +
            "});"
        );

        // Card-style container
        Div card = new Div();
        card.getStyle()
            .set("background-color", " #fff") // very light white
            .set("border-radius", "10px")
            .set("box-shadow", "0 4px 8px rgba(0,0,0,0.10)")
            .set("padding", "2rem")
            .set("width", "100%")
            .set("max-width", "400px")
            .set("transition", "transform 0.2s, box-shadow 0.2s");

        // Hover interaction
        card.getElement().executeJs(
            "this.addEventListener('mouseenter', () => {" +
            "  this.style.boxShadow = '0 8px 16px rgba(0,0,0,0.1)';" +
            "  this.style.transform = 'scale(1.02)';" +
            "});" +
            "this.addEventListener('mouseleave', () => {" +
            "  this.style.boxShadow = '0 4px 8px rgba(0,0,0,0.05)';" +
            "  this.style.transform = 'scale(1)';" +
            "});"
        );

        // Username and email
        Span username = new Span("Username: " + userDTO.getUsername());
        Span email = new Span("Email: " + userDTO.getEmail());
        Stream.of(username, email).forEach(span -> span.getStyle()
                .set("display", "block")
                .set("margin", "0.5rem 0")
                .set("color", "#222")
                .set("font-size", "1.1rem")
        );

        card.add(username, email);
        add(header, card);

        // Move the back-to-home button to the top left and make the grid floaty like the others

        // Adjusting the layout to make the table centered and the back-to-home button pushed to the end

        // Update the back-to-home button styles
        Button backToHomeButton = new Button("Back to Home", e -> UI.getCurrent().navigate("home"));
        backToHomeButton.getStyle()
                        .set("background-color", " #ffe066")
                        .set("color", "#222")
                        .set("font-weight", "bold")
                        .set("position", "absolute")
                        .set("top", "1rem")
                        .set("right", "1rem")
                        .set("padding", "0.5rem 1rem")
                        .set("border-radius", "5px")
                        .set("box-shadow", "0 4px 8px rgba(0,0,0,0.10)");

        // Add the button to the layout
        add(backToHomeButton);

        // Receipt Grid
        Grid<ReceiptDTO> receiptGrid = new Grid<>(ReceiptDTO.class, false);
        setupReceiptGrid(receiptGrid, purchasePresenter);
        receiptGrid.getStyle()
                   .set("margin", "0 auto")
                   .set("background-color", " #fff")
                   .set("border-radius", "10px")
                   .set("box-shadow", "0 4px 8px rgba(0,0,0,0.10)")
                   .set("padding", "1rem")
                   .set("margin-top", "2rem")
                   .set("width", "100%")
                   .set("max-width", "800px")
                   .set("transition", "transform 0.2s, box-shadow 0.2s");


        // Add hover interaction for the grid
        receiptGrid.getElement().executeJs(
            "this.addEventListener('mouseenter', () => {" +
            "  this.style.boxShadow = '0 8px 16px rgba(0,0,0,0.1)';" +
            "  this.style.transform = 'scale(1.01)';" +
            "});" +
            "this.addEventListener('mouseleave', () => {" +
            "  this.style.boxShadow = '0 4px 8px rgba(0,0,0,0.05)';" +
            "  this.style.transform = 'scale(1)';" +
            "});"
        );

        // Add components to the layout
        add(receiptGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        userDTO = (UserDTO) UI.getCurrent().getSession().getAttribute("user");
        if (sessionToken == null || userDTO == null) {
            Notification.show("Access denied. Please log in.", 4000, Notification.Position.MIDDLE);
            event.forwardTo("");
        }
        else {
            setupPage();
        }
    }
    
    private void setupReceiptGrid(Grid<ReceiptDTO> receiptGrid, IPurchasePresenter purchasePresenter) {
        // Fetch purchase history using the last method in IPurchasePresenter
        Response<List<ReceiptDTO>> receipts = purchasePresenter.getPersonalPurcahses(sessionToken);
        if (receipts.errorOccurred()) {
            Notification.show("Error fetching purchase history: " + receipts.getErrorMessage(), 3000, Notification.Position.BOTTOM_END);
        } else if (receipts.getValue().isEmpty()) {
            Notification.show("No purchase history found.", 3000, Notification.Position.BOTTOM_END);
        } else {


            receiptGrid.addColumn(ReceiptDTO::getStoreName).setHeader("Store").setAutoWidth(true);
            receiptGrid.addColumn(r -> String.valueOf(r.getTotalPrice()) + "$").setHeader("Total Price").setAutoWidth(true);
            receiptGrid.addComponentColumn(receipt -> {
                Div detailsDiv = new Div();

                // Populate receipt details
                receipt.getItems().forEach(item -> {
                    Span itemDetail = new Span(item.getName() + " - " + item.getQuantity() + " x $" + item.getPrice());
                    itemDetail.getStyle().set("display", "block");
                    detailsDiv.add(itemDetail);
                });

                return detailsDiv;
            }).setHeader("Items");

            receiptGrid.setItems(receipts.getValue());
            receiptGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
            receiptGrid.setWidthFull();
        }
    }
}
