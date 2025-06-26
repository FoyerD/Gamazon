package UI.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.Route;

import Application.DTOs.ItemDTO;
import Application.DTOs.OfferDTO;
import Application.DTOs.ProductDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;
import UI.presenters.IManagementPresenter;
import UI.presenters.INotificationPresenter;
import UI.presenters.IProductPresenter;
import UI.presenters.IPurchasePresenter;
import UI.presenters.IStorePresenter;
import UI.presenters.IUserSessionPresenter;
import UI.views.components.ItemLayout;
import UI.views.components.MakeOfferDialog;
import UI.views.components.StoreBrowser;
import UI.views.components.StoreLayout;


@Route("store-search")
public class StoreSearchView extends BaseView implements BeforeEnterObserver {

    private final IStorePresenter storePresenter;
    private final IManagementPresenter managementPresenter;
    private final IPurchasePresenter purchasePresenter;
    private final IProductPresenter productPresenter;
    private String sessionToken;

    private final TextField storeNameField = new TextField("Search Store by Name");
    private final StoreBrowser storeBrowser;
    private final VerticalLayout mainContent;


    @Autowired
    public StoreSearchView(IStorePresenter storePresenter, IManagementPresenter managementPresenter, 
                          IPurchasePresenter purchasePresenter, IProductPresenter productPresenter, @Autowired(required = false) DbHealthStatus dbHealthStatus, 
                          @Autowired(required = false) GlobalLogoutManager logoutManager, IUserSessionPresenter sessionPresenter, INotificationPresenter notificationPresenter) {
        super(dbHealthStatus, logoutManager, sessionPresenter, notificationPresenter);
        this.storePresenter = storePresenter;
        this.managementPresenter = managementPresenter;
        this.purchasePresenter = purchasePresenter;
        this.productPresenter = productPresenter;

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background", "linear-gradient(to right, #fce4ec, #f3e5f5)");

        H1 title = new H1("Marketplace Store Browser");
        title.getStyle().setColor(" #6a1b9a").setFontWeight("bold");

        Supplier<List<StoreDTO>> storesFetcher =  () -> {
                Response<List<StoreDTO>> storesResponse = storePresenter.getAllStores(sessionToken);
                if (storesResponse.errorOccurred()) {
                    Notification.show("Failed to fetch stores: " + storesResponse.getErrorMessage(), 5000, Notification.Position.MIDDLE);
                    return null;
                }
                List<StoreDTO> stores = storesResponse.getValue();
                return stores;
        }; 
        
        Function<StoreDTO, List<ItemDTO>> itemsFetcher = s -> {
            Response<List<ItemDTO>> response = storePresenter.getItemsByStoreId(sessionToken, s.getId());
            if (response.errorOccurred()) {
                Notification.show("Failed to fetch items: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
                return null;
            } else {
                Notification.show("Fetched items", 3000, Notification.Position.MIDDLE);
                return response.getValue();
            }
        };

        Consumer<ItemDTO> onAddToCart = i -> {
            Response<Boolean> response = purchasePresenter.addProductToCart(sessionToken, i.getProductId(), i.getStoreId(), 1);
            if (!response.errorOccurred()) {
                Notification.show("Product added to cart successfully!", 3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Failed to add product to cart: " + response.getErrorMessage(), 
                                3000, Notification.Position.MIDDLE);
            }
        };

        Consumer<ItemDTO> onReview = i -> UI.getCurrent().navigate("product-review/" + i.getProductId());

        Consumer<ItemDTO> onMakeOffer = i -> {
            MakeOfferDialog offerDialog = new MakeOfferDialog(i, (price, details) -> {
                Response<OfferDTO> offerResponse = purchasePresenter.makeOffer(sessionToken, i.getStoreId(), i.getProductId(), price, details);
                if (offerResponse.errorOccurred()) {
                    Notification.show("Failed to make offer: " + offerResponse.getErrorMessage(), 5000, Notification.Position.BOTTOM_END);
                } else {
                    OfferDTO offer = offerResponse.getValue();
                    Notification.show("Offer on " + offer.getItem().getProductName() + " for $" + offer.getLastPrice() + " was made successfully");
                }
            });

            offerDialog.open();
        };

        Consumer<StoreDTO> onManager = s -> {            // Keep manager action
            UI.getCurrent().getSession().setAttribute("currentStoreId", s.getId());
            UI.getCurrent().navigate("manager");
        };
        storeBrowser = new StoreBrowser(
            storesFetcher,
            () -> this.expandedStoreMode(true),
            itemsFetcher,
            onAddToCart,
            onReview,
            onMakeOffer,
            onManager
        );

        storeNameField.setPlaceholder("e.g., SuperTech");
        storeNameField.setWidth("300px");
        storeNameField.addValueChangeListener(e -> this.storeBrowser.setNameFilter(e.getValue()));

        Button homeButton = new Button("Return to Homepage", VaadinIcon.HOME.create());
        homeButton.addClickListener(e -> UI.getCurrent().navigate("home"));
        homeButton.getStyle().set("background-color", "#7e57c2").set("color", "white");

        // Initialize Create Store button
        Button createStoreButton = new Button("Create New Store", VaadinIcon.PLUS.create());
        createStoreButton.getStyle()
            .set("background-color", " #4caf50")
            .set("color", "white")
            .set("margin-left", "10px");
        createStoreButton.addClickListener(e -> showCreateStoreDialog());

        // Initialize Add Product button
        Button addProductButton = new Button("Add New Product", VaadinIcon.PLUS_CIRCLE.create());
        addProductButton.getStyle()
            .set("background-color", " #2196f3")
            .set("color", "white")
            .set("margin-left", "10px");
        addProductButton.addClickListener(e -> showAddProductDialog());

        Button backButton = new Button("Back to Stores", VaadinIcon.ARROW_BACKWARD.create());
        backButton.getStyle()
            .set("background-color", "rgb(141, 59, 171)")
            .set("color", "white")
            .set("margin-left", "10px");
        backButton.addClickListener(e -> {
            showStoreBrowser();
        });
        // Create a horizontal layout for buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.add(createStoreButton, addProductButton, homeButton, backButton);


        
        mainContent = new VerticalLayout();
        mainContent.setSizeFull();

        add(title, buttonLayout, storeNameField, mainContent);
        expand(mainContent);
    }

    public void expandedStoreMode(boolean expanded) {
        storeNameField.setVisible(!expanded);
    }

    // private void fetchStoreByName() {
    //     String storeName = storeNameField.getValue();
    //     if (storeName == null || storeName.isEmpty()) {
    //         return;
    //     }
        
    //     Response<StoreDTO> response = storePresenter.getStoreByName(sessionToken, storeName);

    //     if (response.errorOccurred()) {
    //         Notification.show("Store not found: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
    //     } else {
    //         StoreDTO store = response.getValue();
    //         if (!store.isOpen()) {
    //             Notification.show("This store is currently closed", 3000, Notification.Position.MIDDLE);
    //             return;
    //         }
    //         showStoreLayout(store);
    //     }
    // }


    private void showStoreBrowser() {
        mainContent.removeAll();
        mainContent.add(storeBrowser);
        storeBrowser.refresh();
    }

    private void showStoreLayout(StoreDTO store) {
        mainContent.removeAll();
        Function<StoreDTO, List<ItemDTO>> refresher = s -> {
            Response<List<ItemDTO>> response = storePresenter.getItemsByStoreId(sessionToken, s.getId());
            if (response.errorOccurred()) {
                Notification.show("Failed to fetch items: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
                return null;
            } else {
                Notification.show("Fetched items", 3000, Notification.Position.MIDDLE);
                return response.getValue();
            }
        };

        ItemLayout itemLayout = new ItemLayout(store,
            refresher,
            i -> {
                    Response<Boolean> response = purchasePresenter.addProductToCart(sessionToken, i.getProductId(), i.getStoreId(), 1);
                    if (!response.errorOccurred()) {
                        Notification.show("Product added to cart successfully!", 3000, Notification.Position.MIDDLE);
                    } else {
                        Notification.show("Failed to add product to cart: " + response.getErrorMessage(), 
                                        3000, Notification.Position.MIDDLE);
                    }
            },
            i -> UI.getCurrent().navigate("product-review/" + i.getProductId()),
            i -> {
                MakeOfferDialog offerDialog = new MakeOfferDialog(i, (price, paymentDetails, supplyDetails) -> {
                    Response<OfferDTO> offerResponse = purchasePresenter.makeOffer(sessionToken, i.getStoreId(), i.getProductId(), price, paymentDetails, supplyDetails);
                    if (offerResponse.errorOccurred()) {
                        Notification.show("Failed to make offer: " + offerResponse.getErrorMessage(), 5000, Notification.Position.BOTTOM_END);
                    } else {
                        OfferDTO offer = offerResponse.getValue();
                        Notification.show("Offer on " + offer.getItem().getProductName() + " for $" + offer.getLastPrice() + " was made successfully");
                    }
                });

                offerDialog.open();
            }
        );

        StoreLayout storelayout = new StoreLayout(store,
        itemLayout,
        s -> {            // Keep manager action
            UI.getCurrent().getSession().setAttribute("currentStoreId", s.getId());
            UI.getCurrent().navigate("manager");
        });

        itemLayout.setItems(refresher.apply(store));
        mainContent.add(storelayout);
    }

    private void showCreateStoreDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create New Store");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextField nameField = new TextField("Store Name");
        TextField descriptionField = new TextField("Store Description");
        Button createButton = new Button("Create Store", e -> {
            String name = nameField.getValue();
            String description = descriptionField.getValue();
            
            if (name == null || name.isEmpty()) {
                Notification.show("Store name is required", 3000, Notification.Position.MIDDLE);
                return;
            }

            Response<StoreDTO> response = managementPresenter.addStore(sessionToken, name, description);
            if (response.errorOccurred()) {
                Notification.show("Failed to create store: " + response.getErrorMessage(), 
                    3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Store created successfully!", 
                    3000, Notification.Position.MIDDLE);
                dialog.close();
            }
        });

        dialogLayout.add(nameField, descriptionField, createButton);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void showAddProductDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Product");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextField nameField = new TextField("Product Name");
        
        // Create components for categories
        TextField categoryField = new TextField("Category");
        TextField categoryDescField = new TextField("Category Description");
        
        List<String> categories = new ArrayList<>();
        List<String> categoryDescriptions = new ArrayList<>();
        
        Button addCategoryButton = new Button("Add Category", e -> {
            String category = categoryField.getValue();
            String description = categoryDescField.getValue();
            if (!category.isEmpty() && !description.isEmpty()) {
                categories.add(category);
                categoryDescriptions.add(description);
                categoryField.clear();
                categoryDescField.clear();
                Notification.show("Category added", 2000, Notification.Position.MIDDLE);
            }
        });

        Button createButton = new Button("Create Product", e -> {
            String name = nameField.getValue();
            
            if (name == null || name.isEmpty()) {
                Notification.show("Product name is required", 3000, Notification.Position.MIDDLE);
                return;
            }
            
            if (categories.isEmpty()) {
                Notification.show("At least one category is required", 3000, Notification.Position.MIDDLE);
                return;
            }

            Response<ProductDTO> response = productPresenter.addProduct(sessionToken, name, categories, categoryDescriptions);
            if (response.errorOccurred()) {
                Notification.show("Failed to create product: " + response.getErrorMessage(), 
                    3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Product created successfully!", 
                    3000, Notification.Position.MIDDLE);
                dialog.close();
            }
        });

        dialogLayout.add(
            nameField,
            new H3("Add Categories"),
            categoryField,
            categoryDescField,
            addCategoryButton,
            createButton
        );
        
        dialog.add(dialogLayout);
        dialog.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        if (sessionToken == null) {
            Notification.show("Access denied. Please log in.", 4000, Notification.Position.MIDDLE);
            event.forwardTo("");
            return;
        }

        showStoreBrowser();
        
        // Check for query parameters
        Location location = event.getLocation();
        Map<String, List<String>> queryParameters = location.getQueryParameters().getParameters();
        
        if (queryParameters.containsKey("storeName") && !queryParameters.get("storeName").isEmpty()) {
            String storeName = queryParameters.get("storeName").get(0);
            if (storeName != null && !storeName.isEmpty()) {
                // Set the store name in the field and trigger search
                storeNameField.setValue(storeName);
                // fetchStoreByName will be triggered by the value change listener
            }
        }
    }
}