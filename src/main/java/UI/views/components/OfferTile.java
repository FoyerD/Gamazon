package UI.views.components;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;

import Application.DTOs.OfferDTO;
import Application.DTOs.UserDTO;
import Domain.Pair;

public class OfferTile extends Div {

    private final OfferDTO offer;
    private final String userId;
    private final Runnable offersRefresher;
    private final Consumer<OfferDTO> offerApprover;
    private final Consumer<OfferDTO> offerRejecter;
    private final Consumer<OfferDTO> offerCounterer;
    private final Function<String, String> storeNameFethcer;

    public OfferTile(boolean isManagement, 
                    OfferDTO offer, 
                    String userId, 
                    Runnable offersRefresher, 
                    Consumer<OfferDTO> offerApprover, 
                    Consumer<OfferDTO> offerRejecter, 
                    Consumer<OfferDTO> offerCounterer, 
                    Function<String, String> storeNameFethcer) {
        this.offer = offer;
        this.userId = userId;
        this.offersRefresher = offersRefresher;
        this.offerApprover = offerApprover;
        this.offerRejecter = offerRejecter;
        this.offerCounterer = offerCounterer;
        this.storeNameFethcer = storeNameFethcer;

        getStyle()
            .set("width", "280px")
            .set("min-height", "180px")
            .set("border", "5px solid " + (offer.isCounterOffer() ? "rgb(133, 192, 175)" : "rgb(192, 163, 133)"))
            .set("border-radius", "10px")
            .set("padding", "16px")
            .set("background-color", (offer.isCounterOffer() ? "rgb(196, 228, 221)" : "rgb(228, 209, 196)"))
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "space-between")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");

        getElement().addEventListener("mouseover", e ->
            getStyle()
            .set("transform", "translateY(-2px)")
            .set("box-shadow", "0 4px 10px rgba(0,0,0,0.3)"));

        getElement().addEventListener("mouseout", e ->
            getStyle()
            .set("transform", "translateY(0)")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));

        VerticalLayout content = isManagement ? managerContent() : memberContent();
        HorizontalLayout actions = buttons();

        add(content, actions);
    }

    private VerticalLayout managerContent() {
                VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();

        // Member and product info
        content.add(new Span("Offered by: " + offer.getMember().getUsername()));
        content.add(new Span("Item: " + offer.getItem().getProductName()));

        // Approvals
        Set<UserDTO> approved = offer.getApprovedBy();
        Set<UserDTO> remaining = offer.getRemainingEmployeesToApprove();
        //int approvedCount = approved.size();



        // Price history
        List<Pair<String, Double>> prices = offer.getUsernamesPrice();
        if (!prices.isEmpty()) {
            VerticalLayout priceHistory = new VerticalLayout();
            priceHistory.setPadding(false);
            priceHistory.setSpacing(false);

            for (int i = 0; i < prices.size(); i++) {
                Pair<String, Double> p = prices.get(i);
                Span priceSpan = new Span(p.getFirst() + ": $" + p.getSecond());
                if (i == prices.size() - 1) {
                    priceSpan.getStyle().set("font-weight", "bold");
                } else {
                    priceSpan.getStyle().set("color", "gray");
                }
                priceHistory.add(priceSpan);
            }

            content.add(priceHistory);
        }

        // Approval summary
        //CircularProgressBar approvalProgress = new CircularProgressBar(approvedCount, offer.getApprovers().size());

        // content.add(new Span("progress: " + approvedCount + " / " + offer.getApprovers().size()));

        // List of approvers
        if (!approved.isEmpty()) {
            Span approvedList = new Span("✔ Approved by: " + approved.stream().map(UserDTO::getUsername).collect(Collectors.joining(", ")));
            approvedList.getStyle().set("color", "green");
            content.add(approvedList);
        }

        if (!remaining.isEmpty()) {
            Span pendingList = new Span("⏳ Pending: " + remaining.stream().map(UserDTO::getUsername).collect(Collectors.joining(", ")));
            pendingList.getStyle().set("color", "darkorange");
            content.add(pendingList);
        }
        return content;
    }

    private VerticalLayout memberContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();

        String storeName = storeNameFethcer.apply(offer.getItem().getStoreId());
        // Store name
        content.add(new Span("Store: " + storeName)); // assuming you have this field
        content.add(new Span("Product: " + offer.getItem().getProductName()));

        // Price history
        List<Pair<String, Double>> prices = offer.getUsernamesPrice(); //offer.getUsernamesPrice();
        if (!prices.isEmpty()) {
            VerticalLayout priceHistory = new VerticalLayout();
            priceHistory.setPadding(false);
            priceHistory.setSpacing(false);

            for (int i = 0; i < prices.size(); i++) {
                Pair<String, Double> p = prices.get(i);
                Span priceSpan = new Span(p.getFirst() + ": $" + p.getSecond());
                if (i == prices.size() - 1) {
                    priceSpan.getStyle().set("font-weight", "bold");
                } else {
                    priceSpan.getStyle().set("color", "gray");
                }
                priceHistory.add(priceSpan);
            }

            content.add(priceHistory);
        }

        // Approval progress
        int approvedCount = offer.getApprovedBy().size();
        int totalApprovers = offer.getApprovers().size();
        Span approvalStatus = new Span("Waiting for approval: " + approvedCount + " / " + totalApprovers);
        approvalStatus.getStyle().set("color", approvedCount == totalApprovers ? "green" : "darkorange");
        content.add(approvalStatus);

        return content;
    }

    private HorizontalLayout buttons() {
        // Buttons
        Button approveBtn = new Button(VaadinIcon.CHECK.create(), e -> {
            offerApprover.accept(offer);
            offersRefresher.run();
        });
        Button rejectBtn = new Button(VaadinIcon.CLOSE.create(), e -> {
            offerRejecter.accept(offer);
            offersRefresher.run();
        });

        Button counterOfferBtn = new Button(VaadinIcon.REFRESH.create(), e -> {
            offerCounterer.accept(offer);
            offersRefresher.run();
        });

        if (userId != null) {
            approveBtn.setEnabled(!offer.hasUserApproved(userId));
            rejectBtn.setEnabled(!offer.hasUserApproved(userId));
            counterOfferBtn.setEnabled(!offer.hasUserApproved(userId));
        }

        styleButton(approveBtn, "rgb(0, 155, 0)");
        styleButton(rejectBtn, "rgb(255, 0, 0)");
        styleButton(counterOfferBtn, "rgb(142, 94, 3)");


        HorizontalLayout actions = new HorizontalLayout(approveBtn, rejectBtn, counterOfferBtn);
        actions.getStyle()
            .set("flex-wrap", "wrap")
            .set("gap", "8px");
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);
        return actions;
    }
    
    private void styleButton(Button button, String color) {
        if (!button.isEnabled()) {
            // Style as visually disabled
            String grayColor = washedOutColor(color);
            button.getStyle()
                .set("background-color", grayColor)
                .set("color", " #eeeeee")
                .set("border-radius", "20px")
                .set("cursor", "not-allowed")
                .set("box-shadow", "none")
                .set("opacity", "0.6");

            // Remove hover effects
            button.getElement().addEventListener("mouseover", e -> {});
            button.getElement().addEventListener("mouseout", e -> {});
            return;
        }
        button.getStyle()
            .set("background-color", color)
            .set("color", "white")
            .set("border-radius", "20px")
            .set("font-weight", "500")
            .set("padding", "0.3em 1.2em")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");

        button.getElement().addEventListener("mouseover", e ->
            button.getStyle()
                .set("transform", "translateY(-1px)")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.3)"));

        button.getElement().addEventListener("mouseout", e ->
            button.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));
    }

    private String grayOutColor(String rgb) {
        // Extract RGB values from "rgb(r, g, b)"
        if (rgb.contains("rgb")) {
            String[] parts = rgb.replace("rgb(", "").replace(")", "").split(",");
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());

            // Calculate average or use luminance formula
            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

            return "rgb(" + gray + "," + gray + "," + gray + ")";
        }
        return rgb;
    }

    private String washedOutColor(String rgb) {
        // Parse original RGB values
        if (rgb.contains("rgb")) {
            String[] parts = rgb.replace("rgb(", "").replace(")", "").split(",");
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());

            // Blend 70% original color + 30% white
            int blend = 30;
            int newR = (r * (100 - blend) + 255 * blend) / 100;
            int newG = (g * (100 - blend) + 255 * blend) / 100;
            int newB = (b * (100 - blend) + 255 * blend) / 100;

            return "rgb(" + newR + "," + newG + "," + newB + ")";
        }
        return rgb;
    }
}
