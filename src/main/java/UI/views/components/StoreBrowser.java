package UI.views.components;

import java.util.List;
import java.util.function.Supplier;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.StoreDTO;

public class StoreBrowser extends VerticalLayout {
    private final Supplier<List<StoreDTO>> storesSupplier;
    private final FlexLayout tileContainer = new FlexLayout();

    public StoreBrowser(Supplier<List<StoreDTO>> storesSupplier) {
        this.storesSupplier = storesSupplier;

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

    public void refreresh() {
        tileContainer.removeAll();
        List<StoreDTO> stores = storesSupplier.get();
        if (stores == null || stores.isEmpty()) return;

        stores.stream().forEach(s -> tileContainer.add(new StoreTile(s)));
    }
}
