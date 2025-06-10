package UI.views;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import Application.DTOs.OfferDTO;
import Application.DTOs.ReceiptDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import Domain.Pair;
import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;
import UI.presenters.INotificationPresenter;
import UI.presenters.IPurchasePresenter;
import UI.presenters.IStorePresenter;
import UI.presenters.IUserSessionPresenter;
import UI.views.components.OfferLayout;

@Route("user-profile")
public class UserProfileView extends BaseView implements BeforeEnterObserver {

    private String sessionToken = null;
    private UserDTO userDTO = null;

    private final IPurchasePresenter purchasePresenter;

    private final VerticalLayout contentArea = new VerticalLayout(); // dynamic content container
    private final OfferLayout offersLayout;

    public UserProfileView(IPurchasePresenter purchasePresenter, IStorePresenter storePresenter,
                           @Autowired(required = false) DbHealthStatus dbHealthStatus,
                           @Autowired(required = false) GlobalLogoutManager logoutManager,
                           IUserSessionPresenter sessionPresenter, INotificationPresenter notificationPresenter) {
        super(dbHealthStatus, logoutManager, sessionPresenter, notificationPresenter);
        this.purchasePresenter = purchasePresenter;

        // Style base layout
        setWidthFull();
        setHeightFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        setPadding(true);
        getStyle().set("background", " #fffbe7");

        // Back to Home Button
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
        add(backToHomeButton);

        offersLayout = new OfferLayout(false,
            () -> {
                Response<List<OfferDTO>> offersResponse = purchasePresenter.getAllOffersOfUser(sessionToken);
                if (offersResponse.errorOccurred()) {
                    Notification.show("Failed to fetch offers for user: " + offersResponse.getErrorMessage(), 5000, Notification.Position.MIDDLE);
                    return null;
                } 
                return offersResponse.getValue();
            },
            () -> userDTO.getId(),
            o -> {
                Response<OfferDTO> approvedResponse = purchasePresenter.approveCounterOffer(sessionToken, o.getId());
                if (approvedResponse.errorOccurred()) {
                    Notification.show("Failed approve counter offer: " + approvedResponse.getErrorMessage(), 5000, Notification.Position.MIDDLE);
                    
                }
                Notification.show("Counter offer approved successfully!", 3000, Notification.Position.BOTTOM_END);
            },
            o -> {
                Response<OfferDTO> approvedResponse = purchasePresenter.rejectCounterOffer(sessionToken, o.getId());
                if (approvedResponse.errorOccurred()) {
                    Notification.show("Failed reject counter offer: " + approvedResponse.getErrorMessage(), 5000, Notification.Position.MIDDLE);
                    
                }
                Notification.show("Counter offer rejected successfully!", 3000, Notification.Position.BOTTOM_END);
            }, 
            this::showCounterOffer, 
            storeId -> {
                Response<StoreDTO> storeResponse = storePresenter.getStoreById(sessionToken, storeId);
                if (storeResponse.errorOccurred()) {
                    Notification.show("Failed to fetch store: " + storeResponse.getErrorMessage(), 5000, Notification.Position.MIDDLE);
                    return "Unknown";
                } 
                return storeResponse.getValue().getName();
            });

            contentArea.setSizeFull();
    }

    private void showCounterOffer(OfferDTO offer) {
        Dialog counterOfferDialog = new Dialog();
        counterOfferDialog.setHeaderTitle("Submit Counter Offer");

        NumberField newPriceField = new NumberField("New Price");
        newPriceField.setPlaceholder("Enter new price");
        newPriceField.setWidthFull();
        newPriceField.setMin(0.01);
        newPriceField.setStep(0.01);

        VerticalLayout historyLayout = new VerticalLayout();
        historyLayout.setSpacing(false);
        historyLayout.setPadding(false);

        List<Pair<String, Double>> prices = offer.getUsernamesPrice();
        for (int i = 0; i < prices.size(); i++) {
            Pair<String, Double> p = prices.get(i);
            Span entry = new Span(p.getFirst() + ": $" + p.getSecond());
            if (i == prices.size() - 1) {
                entry.getStyle().set("font-weight", "bold");
            } else {
                entry.getStyle().set("color", "gray");
            }
            historyLayout.add(entry);
        }
        Button submitBtn = new Button("Submit", event -> {
            Double price = newPriceField.getValue();
            if (price != null && price > 0) {
                Response<OfferDTO> counterResponse = purchasePresenter.counterCounterOffer(sessionToken, offer.getId(), price);
                if (counterResponse.errorOccurred()) {
                    Notification.show("Failed to counter counter-offer: " + counterResponse.getErrorMessage(), 5000, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Counter-Offer countered successfully!", 3000, Notification.Position.BOTTOM_END);
                }
                counterOfferDialog.close();
            }
        });

        Button cancelBtn = new Button("Cancel", event -> counterOfferDialog.close());

        HorizontalLayout buttons = new HorizontalLayout(submitBtn, cancelBtn);
        VerticalLayout dialogLayout = new VerticalLayout(newPriceField, buttons);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);

        counterOfferDialog.add(dialogLayout);
        counterOfferDialog.open();

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        userDTO = (UserDTO) UI.getCurrent().getSession().getAttribute("user");

        if (sessionToken == null || userDTO == null) {
            Notification.show("Access denied. Please log in.", 4000, Notification.Position.MIDDLE);
            event.forwardTo("");
        } else {
            setupTabs();
        }
    }

    private void setupTabs() {
        // Header
        H1 header = new H1("Hello " + userDTO.getUsername() + "!");
        header.getStyle()
              .set("color", "#222")
              .set("background-color", "#fff")
              .set("font-family", "'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif")
              .set("padding", "1rem 2rem")
              .set("border-radius", "8px")
              .set("box-shadow", "0 4px 8px rgba(0,0,0,0.08)")
              .set("margin-bottom", "1rem")
              .set("transition", "transform 0.2s, box-shadow 0.2s");

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

        Div personalInfo = new Div();
        personalInfo.getStyle()
            .set("background-color", "#fff")
            .set("border-radius", "10px")
            .set("box-shadow", "0 4px 8px rgba(0,0,0,0.10)")
            .set("padding", "2rem")
            .set("width", "100%")
            .set("max-width", "400px")
            .set("transition", "transform 0.2s, box-shadow 0.2s");

        personalInfo.getElement().executeJs(
            "this.addEventListener('mouseenter', () => {" +
            "  this.style.boxShadow = '0 8px 16px rgba(0,0,0,0.1)';" +
            "  this.style.transform = 'scale(1.02)';" +
            "});" +
            "this.addEventListener('mouseleave', () => {" +
            "  this.style.boxShadow = '0 4px 8px rgba(0,0,0,0.05)';" +
            "  this.style.transform = 'scale(1)';" +
            "});"
        );

        Span email = new Span("Email: " + userDTO.getEmail());
        Span age = new Span("Birthday: " +
                userDTO.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                ". " + userDTO.getAge() + " years old");

        Stream.of(email, age).forEach(span -> span.getStyle()
                .set("display", "block")
                .set("margin", "0.5rem 0")
                .set("color", "#222")
                .set("font-size", "1.1rem"));

        personalInfo.add(email, age);

        // Tabs
        Tab historyTab = new Tab("Purchase History");
        Tab offersTab = new Tab("Offers");

        Tabs tabs = new Tabs(historyTab, offersTab);
        tabs.setSelectedTab(historyTab);

        tabs.addSelectedChangeListener(e -> {
            contentArea.removeAll();
            if (e.getSelectedTab().equals(historyTab)) {
                showHistory();
            } else if (e.getSelectedTab().equals(offersTab)) {
                showOffers();
            }
        });

        add(header, personalInfo, tabs, contentArea);
        showHistory(); // default
    }

    private void showHistory() {
        contentArea.removeAll();

        Grid<ReceiptDTO> receiptGrid = new Grid<>(ReceiptDTO.class, false);
        setupReceiptGrid(receiptGrid, purchasePresenter);

        
        receiptGrid.getStyle()
                .set("margin", "2rem auto 0 auto")
                .set("background-color", "#fff")
                .set("border-radius", "10px")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.10)")
                .set("padding", "1rem")
                .set("width", "100%")
                .set("max-width", "800px")
                .set("transition", "transform 0.2s, box-shadow 0.2s");

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

        contentArea.add(receiptGrid);
    }

    private void showOffers() {
        contentArea.removeAll();
        offersLayout.refreshOffers();
        contentArea.add(offersLayout);
    }

    private void setupReceiptGrid(Grid<ReceiptDTO> receiptGrid, IPurchasePresenter purchasePresenter) {
        Response<List<ReceiptDTO>> receipts = purchasePresenter.getPersonalPurchases(sessionToken);
        if (receipts.errorOccurred()) {
            Notification.show("Error fetching purchase history: " + receipts.getErrorMessage(), 3000, Notification.Position.BOTTOM_END);
        } else if (receipts.getValue().isEmpty()) {
            Notification.show("No purchase history found.", 3000, Notification.Position.BOTTOM_END);
        } else {
            receiptGrid.addColumn(ReceiptDTO::getStoreName).setHeader("Store").setAutoWidth(true);
            receiptGrid.addColumn(r -> r.getTotalPrice() + "$").setHeader("Total Price").setAutoWidth(true);
            receiptGrid.addComponentColumn(receipt -> {
                Div detailsDiv = new Div();
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
