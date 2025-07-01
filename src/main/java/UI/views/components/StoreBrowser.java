package UI.views.components;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;

public class StoreBrowser extends VerticalLayout {
    private final Supplier<List<StoreDTO>> storesSupplier;
    private final Runnable onStoreSelect;
    private final Function<StoreDTO, List<ItemDTO>> itemFetcher;
    private final Consumer<ItemDTO> onAddToCart;
    private final Consumer<ItemDTO> onReview;
    private final Consumer<ItemDTO> onOffer;
    private final Consumer<StoreDTO> onManager;
    private final FlexLayout tileContainer = new FlexLayout();
    private String nameFilter;
    private boolean isGuest;
    private UserDTO user;

    public StoreBrowser(Supplier<List<StoreDTO>> storesSupplier,
                        Runnable onStoreSelect,
                        Function<StoreDTO, List<ItemDTO>> itemFetcher,
                        Consumer<ItemDTO> onAddToCart,
                        Consumer<ItemDTO> onReview,
                        Consumer<ItemDTO> onOffer,
                        Consumer<StoreDTO> onManager,
                        boolean isGuest,
                        UserDTO user) {
        this.nameFilter = "";
        this.storesSupplier = storesSupplier;
        this.onStoreSelect = onStoreSelect;
        this.itemFetcher = itemFetcher;
        this.onAddToCart = onAddToCart;
        this.onReview = onReview;
        this.onOffer = onOffer;
        this.onManager = onManager;
        this.isGuest = isGuest;
        this.user = user;

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

    public void setNameFilter(String name) {
        this.nameFilter = name;
    }

    public void refresh() {
    tileContainer.removeAll();
    List<StoreDTO> stores = storesSupplier.get();
    if (stores == null || stores.isEmpty()) return;
    
    stores.stream()
        .filter(s -> nameFilter.isEmpty() || s.getName().toLowerCase().contains(nameFilter.toLowerCase()))
        .forEach(s -> {
            StoreTile tile = new StoreTile(s, onStoreSelect, itemFetcher, onAddToCart, onReview, onOffer, onManager, isGuest,
            s.getManagers().contains(user.getId()) || s.getOwners().contains(user.getId()) || s.getFounderId().equals(user.getId()));
            tileContainer.add(tile);
        });
    }

}
