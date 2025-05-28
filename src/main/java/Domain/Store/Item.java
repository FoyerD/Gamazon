package Domain.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Represents an item available in a store.
 * Encapsulates inventory, pricing, description, and rating logic.
 */

 @Entity
 @Table(name = "items")
 @IdClass(ItemId.class)
public class Item {

    @Id
    private String storeId;
    @Id
    private String productId;

    private double price;
    private int amount;
    private String description;
    private String productName;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Category> categories;


    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> rates;

    protected Item() {
    // Required by JPA
    this.rates = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
    }

    /**
     * Constructs a new item with the specified details.
     *
     * @param storeId     the ID of the store offering the item
     * @param productId   the ID of the product this item represents
     * @param price       the price of the item
     * @param amount      the quantity available
     * @param description a description of the item
     */
    public Item(String storeId, String productId, double price, int amount, String description, String productName, Set<Category> categories) {
        this.storeId = storeId;
        this.productId = productId;
        this.price = price;
        this.amount = amount;
        this.description = description;
        this.productName = productName;
        this.categories = categories;
        this.rates = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
    }

    /** @return the store ID associated with this item */
    public String getStoreId() {
        return storeId;
    }

    /** @return the product ID associated with this item */
    public String getProductId() {
        return productId;
    }

    /** @return the quantity in stock */
    public int getAmount() {
        return amount;
    }

    /** @return the price of the item */
    public double getPrice() {
        return price;
    }

    /** @return the description of the item */
    public String getDescription() {
        return description;
    }

    /**
     * @return the set of categories associated with the item
     * @throws IllegalStateException if the fetcher was not set
     */
    public Set<Category> getCategories() {
        return categories;
    }

    /**
     * @return the name of the product
     * @throws IllegalStateException if the fetcher was not set
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the available quantity of the item.
     * @param amount the new amount
     * @throws IllegalArgumentException if amount is negative
     */
    public void setAmount(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        this.amount = amount;
    }

    /**
     * Updates the item's price.
     * @param newPrice the new price
     * @throws IllegalArgumentException if newPrice is negative
     */
    public void setPrice(float newPrice) {
        if (newPrice < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.price = newPrice;
    }

    /**
     * Adds a rating to the item.
     * @param newRating a value from 1 to 5 (0 is ignored)
     * @throws IllegalArgumentException if the rating is out of bounds
     */
    public void addRating(int newRating) {
        if (newRating < 1 || newRating > 5)
            throw new IllegalArgumentException("newRating must be between 1 and 5");

        rates.set(newRating - 1, rates.get(newRating - 1) + 1);
    }



    /**
     * @return the average user rating for this item (0.0 if unrated)
     */
    public double getRating() {
        if (rates == null || rates.isEmpty()) return 0.0;

        int totalCount = rates.stream().mapToInt(Integer::intValue).sum();
        if (totalCount == 0) return 0.0;

        double weightedSum = IntStream.range(0, rates.size())
                                    .mapToDouble(i -> (i + 1) * rates.get(i))
                                    .sum();

        return weightedSum / totalCount;
    }


    /**
     * Decreases the available quantity of the item.
     * @param amount amount to remove
     * @throws IllegalArgumentException if amount is negative or more than in stock
     */
    public void decreaseAmount(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        if (this.amount - amount < 0)
            throw new IllegalArgumentException("Not enough items in stock");
        this.amount -= amount;
    }

    /**
     * Increases the available quantity of the item.
     * @param amount amount to add
     * @throws IllegalArgumentException if amount is negative
     */
    public void increaseAmount(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        this.amount += amount;
    }
}
