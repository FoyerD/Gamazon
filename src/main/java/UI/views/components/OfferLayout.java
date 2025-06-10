package UI.views.components;

import Application.DTOs.OfferDTO;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OfferLayout extends VerticalLayout {

    private final Supplier<List<OfferDTO>> offersSupplier;
    private final Supplier<String> userIdSupplier;
    private final Consumer<OfferDTO> offerApprover;
    private final Consumer<OfferDTO> offerRejecter;
    private final Consumer<OfferDTO> offerCounterer;

    private final FlexLayout tileContainer = new FlexLayout();

    public OfferLayout(Supplier<List<OfferDTO>> offersSupplier,
                       Supplier<String> userIdSupplier,
                       Consumer<OfferDTO> offerAccepter,
                       Consumer<OfferDTO> offerRejecter,
                       Consumer<OfferDTO> offerCounterer) {
        this.offersSupplier = offersSupplier;
        this.userIdSupplier = userIdSupplier;
        this.offerApprover = offerAccepter;
        this.offerRejecter = offerRejecter;
        this.offerCounterer = offerCounterer;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        tileContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        tileContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        tileContainer.setWidthFull();
        tileContainer.getStyle()
            .set("display", "flex")
            .set("flex-wrap", "wrap")
            .set("gap", "16px")
            .set("padding", "12px");

        add(tileContainer);
    }

    public void refreshOffers() {
        String userId = userIdSupplier.get();
        tileContainer.removeAll();
        List<OfferDTO> offers = offersSupplier.get();
        if (offers == null || offers.isEmpty()) return;

        for (OfferDTO offer : offers) {
            tileContainer.add(new OfferTile(offer, userId, this::refreshOffers, offerApprover, offerRejecter, offerCounterer));
        }
    }
}
