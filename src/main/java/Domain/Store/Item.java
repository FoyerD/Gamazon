package Domain.Store;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Item {

    private String storeId;
    private String productId;
    private double price;
    private int amount;
    private String description;

    private int[] rates;
    private Supplier<Set<Category>> categoryFetcher;
    private Supplier<String> nameFetcher;

    public Item(String storeId, String productId, double price, int amount,  String description) {
        this.storeId = storeId;
        this.productId = productId;
        this.price = price;
        this.amount = amount;
        this.description = description;
        this.rates = new int[5];
    }

    public String getStoreId() {
        return storeId;
    }

    public String getProductId() {
        return productId;
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public void setCategoryFetcher(Supplier<Set<Category>> fetcher) {
        this.categoryFetcher = fetcher;
    }

    public void setNameFetcher(Supplier<String> fetcher) {
        this.nameFetcher = fetcher;
    }

    public Set<Category> getCategories() {
        if (categoryFetcher == null) {
            throw new IllegalStateException("Category fetcher not set");
        }
        return categoryFetcher.get();
    }

    public String getProductName(){
        if (nameFetcher == null){
            throw new IllegalStateException("Name fetcher not set");
        }
        return nameFetcher.get();
    }
    
    public void setAmount(int amount) {
        if (amount < 0) 
            throw new IllegalArgumentException("Amount cannot be negative");
        
        this.amount = amount;
    }

    public void setPrice(float newPrice){
        if(newPrice < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.price = newPrice;
    }

    public void addRating(int newRating){
        if( newRating <= 0 | newRating > 5)
            throw new IllegalArgumentException("newRating must be between 1 to 5");
        rates[newRating - 1]++;
    }   

    public double getRating() {
        return IntStream.range(0, rates.length).map(i -> (i+1) * rates[i]).sum() / (Arrays.stream(rates).sum() + 0.0);
    }
    public void decreaseAmount(int amount) {
        if (amount < 0) 
            throw new IllegalArgumentException("Amount cannot be negative");
        
        if (this.amount - amount < 0) 
            throw new IllegalArgumentException("Not enough items in stock");
        
        this.amount -= amount;
    }
    public void increaseAmount(int amount) {
        if (amount < 0) 
            throw new IllegalArgumentException("Amount cannot be negative");
        
        this.amount += amount;
    }
}
