package UI.views;

import UI.presenters.IProductPresenter;
import UI.presenters.IStorePresenter;
import Application.DTOs.ItemDTO;
import Application.utils.Response;
import Domain.Store.FeedbackDTO;
import Domain.Store.ItemFilter;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route("product-review/:productId")
public class ProductReviewView extends VerticalLayout implements BeforeEnterObserver {

    private final IProductPresenter productPresenter;
    private final IStorePresenter storePresenter;
    private String sessionToken = null;
    private String currentUsername = null;
    private ItemDTO currentProduct = null;

    private final TextArea reviewText = new TextArea("Your Review");
    private final NumberField ratingField = new NumberField("Rating (1-5)");
    private final VerticalLayout reviewsLayout = new VerticalLayout();

    @Autowired
    public ProductReviewView(IProductPresenter productPresenter, IStorePresenter storePresenter) {
        this.productPresenter = productPresenter;
        this.storePresenter = storePresenter;

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background", "linear-gradient(to right, #edf2f7, #e2e8f0)");

        H1 title = new H1("Product Review");
        title.getStyle().set("color", "#1a202c");

        // Product details section
        VerticalLayout productDetails = new VerticalLayout();
        productDetails.setSpacing(true);
        productDetails.setPadding(true);
        productDetails.getStyle().set("background-color", "#ffffff")
                               .set("border-radius", "8px")
                               .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // Review form
        reviewText.setWidthFull();
        reviewText.setMinHeight("150px");
        reviewText.setPlaceholder("Write your review here...");

        ratingField.setMin(1);
        ratingField.setMax(5);
        ratingField.setValue(5.0);
        ratingField.setStep(1);
        ratingField.setStepButtonsVisible(true);

        Button submitRatingButton = new Button("Submit Rating", e -> submitRating());
        submitRatingButton.getStyle()
            .set("background-color", "#38a169")
            .set("color", "white")
            .set("margin-right", "10px");

        Button submitReviewButton = new Button("Submit Review", e -> submitReview());
        submitReviewButton.getStyle()
            .set("background-color", "#805ad5")
            .set("color", "white")
            .set("margin-right", "10px");

        Button backButton = new Button("Back to Home", e -> UI.getCurrent().navigate("home"));
        backButton.getStyle()
            .set("background-color", "#4299e1")
            .set("color", "white");

        HorizontalLayout buttons = new HorizontalLayout(submitRatingButton, submitReviewButton, backButton);
        buttons.setSpacing(true);

        // Reviews section
        reviewsLayout.setWidthFull();
        reviewsLayout.getStyle().set("background-color", "#ffffff")
                              .set("border-radius", "8px")
                              .set("margin-top", "20px");

        add(title, productDetails, reviewText, ratingField, buttons, reviewsLayout);

        this.sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        this.currentUsername = (String) UI.getCurrent().getSession().getAttribute("username");
    }

    private void loadProductDetails(String productId) {
        if (sessionToken == null) return;
        
        Response<List<ItemDTO>> response = productPresenter.showAllItems(sessionToken);
        if (!response.errorOccurred()) {
            List<ItemDTO> products = response.getValue();
            currentProduct = products.stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
                
            if (currentProduct != null) {
                updateProductDetails();
                loadReviews();
            } else {
                Notification.show("Product not found", 
                    3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("home");
            }
        } else {
            Notification.show("Failed to load product details: " + response.getErrorMessage(),
                            3000, Notification.Position.MIDDLE);
        }
    }

    private void loadReviews() {
        if (currentProduct == null) return;

        Response<List<FeedbackDTO>> response = storePresenter.getAllFeedbacksByStoreId(
            sessionToken, 
            currentProduct.getStoreId()
        );

        if (!response.errorOccurred()) {
            reviewsLayout.removeAll();
            reviewsLayout.add(new H2("Previous Reviews"));
            
            if (response.getValue().isEmpty()) {
                reviewsLayout.add(new Paragraph("No reviews yet"));
            } else {
                response.getValue().forEach(feedback -> {
                    Div reviewDiv = new Div();
                    reviewDiv.getStyle()
                        .set("background-color", "#f7fafc")
                        .set("padding", "16px")
                        .set("border-radius", "4px")
                        .set("margin-bottom", "8px");
                    
                    reviewDiv.add(new Paragraph(feedback.getComment()));
                    reviewsLayout.add(reviewDiv);
                });
            }
        } else {
            Notification.show("Failed to load reviews: " + response.getErrorMessage(),
                            3000, Notification.Position.MIDDLE);
        }
    }

    private void updateProductDetails() {
        if (currentProduct == null) return;

        removeAll();

        H1 title = new H1("Review: " + currentProduct.getProductName());
        title.getStyle().set("color", "#1a202c");

        Div productInfo = new Div();
        productInfo.add(
            new H3("Product Details"),
            new Paragraph("Name: " + currentProduct.getProductName()),
            new Paragraph("Description: " + currentProduct.getDescription()),
            new Paragraph("Current Rating: " + currentProduct.getRating())
        );
        productInfo.getStyle()
            .set("background-color", "#ffffff")
            .set("padding", "20px")
            .set("border-radius", "8px")
            .set("margin-bottom", "20px");

        // Re-create buttons
        Button submitRatingButton = new Button("Submit Rating", e -> submitRating());
        submitRatingButton.getStyle()
            .set("background-color", "#38a169")
            .set("color", "white")
            .set("margin-right", "10px");

        Button submitReviewButton = new Button("Submit Review", e -> submitReview());
        submitReviewButton.getStyle()
            .set("background-color", "#805ad5")
            .set("color", "white")
            .set("margin-right", "10px");

        Button backButton = new Button("Back to Home", e -> UI.getCurrent().navigate("home"));
        backButton.getStyle()
            .set("background-color", "#4299e1")
            .set("color", "white");

        HorizontalLayout buttons = new HorizontalLayout(submitRatingButton, submitReviewButton, backButton);
        buttons.setSpacing(true);

        add(
            title,
            productInfo,
            reviewText,
            ratingField,
            buttons,
            reviewsLayout
        );
    }

    private void submitRating() {
        if (currentProduct == null || sessionToken == null) return;

        double rating = ratingField.getValue();

        // Submit the rating
        ItemDTO ratedItem = new ItemDTO(
            currentProduct.getStoreId(),
            currentProduct.getProductId(),
            currentProduct.getPriceBreakDown(),
            currentProduct.getAmount(),
            currentProduct.getDescription(),
            currentProduct.getCategories(),
            currentProduct.getProductName(),
            rating
        );

        Response<Void> ratingResponse = productPresenter.rateProduct(sessionToken, ratedItem);
        
        if (!ratingResponse.errorOccurred()) {
            Notification.show("Rating submitted successfully!",
                            3000, Notification.Position.MIDDLE);
            loadProductDetails(currentProduct.getProductId()); // Refresh to show new rating
        } else {
            Notification.show("Failed to submit rating: " + ratingResponse.getErrorMessage(),
                            3000, Notification.Position.MIDDLE);
        }
    }

    private void submitReview() {
        if (currentProduct == null || sessionToken == null) return;

        String review = reviewText.getValue();

        if (review.trim().isEmpty()) {
            Notification.show("Please write a review before submitting",
                            3000, Notification.Position.MIDDLE);
            return;
        }

        // Submit the review text
        Response<Boolean> reviewResponse = storePresenter.addFeedback(
            sessionToken,
            currentProduct.getStoreId(),
            currentProduct.getProductId(),
            review
        );

        if (!reviewResponse.errorOccurred()) {
            Notification.show("Review submitted successfully!",
                            3000, Notification.Position.MIDDLE);
            reviewText.clear();
            loadReviews(); // Refresh the reviews section
        } else {
            Notification.show("Failed to submit review: " + reviewResponse.getErrorMessage(),
                            3000, Notification.Position.MIDDLE);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        if (sessionToken == null) {
            event.forwardTo("login");
            return;
        }

        String productId = event.getRouteParameters().get("productId")
                              .orElse(null);
        if (productId == null) {
            event.forwardTo("home");
            return;
        }

        loadProductDetails(productId);
    }
} 