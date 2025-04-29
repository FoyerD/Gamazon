package Domain.Store;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Represents an item available in a store.
 * Encapsulates inventory, pricing, description, and rating logic.
 */
public class Item {

    private String storeId;
    private String productId;
    private double price;
    private int amount;
    private String description;

    private int[] rates;
    private Supplier<Set<Category>> categoryFetcher;
    private Supplier<String> nameFetcher;

    /**
     * Constructs a new item with the specified details.
     *
     * @param storeId     the ID of the store offering the item
     * @param productId   the ID of the product this item represents
     * @param price       the price of the item
     * @param amount      the quantity available
     * @param description a description of the item
     */
    public Item(String storeId, String productId, double price, int amount, String description) {
        this.storeId = storeId;
        this.productId = productId;
        this.price = price;
        this.amount = amount;
        this.description = description;
        this.rates = new int[5];
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
     * Sets a supplier for fetching the item's categories dynamically.
     * @param fetcher a function returning a set of categories
     */
    public void setCategoryFetcher(Supplier<Set<Category>> fetcher) {
        this.categoryFetcher = fetcher;
    }

    /**
     * Sets a supplier for fetching the item's product name dynamically.
     * @param fetcher a function returning the product name
     */
    public void setNameFetcher(Supplier<String> fetcher) {
        this.nameFetcher = fetcher;
    }

    /**
     * @return the set of categories associated with the item
     * @throws IllegalStateException if the fetcher was not set
     */
    public Set<Category> getCategories() {
        if (categoryFetcher == null) {
            throw new IllegalStateException("Category fetcher not set");
        }
        return categoryFetcher.get();
    }

    /**
     * @return the name of the product
     * @throws IllegalStateException if the fetcher was not set
     */
    public String getProductName() {
        if (nameFetcher == null) {
            throw new IllegalStateException("Name fetcher not set");
        }
        return nameFetcher.get();
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
        if (newRating == 0) return;
        if (newRating < 0 || newRating > 5)
            throw new IllegalArgumentException("newRating must be between 1 and 5");
        rates[newRating - 1]++;
    }

    /**
     * @return the average user rating for this item (0.0 if unrated)
     */
    public double getRating() {
        double totalCount = Arrays.stream(rates).sum();
        if (totalCount == 0) return 0.0;
        double weightedSum = IntStream.range(0, rates.length)
                                      .mapToDouble(i -> (i + 1) * rates[i])
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
