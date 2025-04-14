package Domain.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class ItemFilter {
    private List<Category> categories; 
    private float minPrice;
    private float maxPrice;
    private float maxRating;
    private float minRating;
    
    public ItemFilter(List<Category> categories, float minPrice, float maxPrice, float maxRating, float minRating){
        this.categories = categories;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.maxRating = maxRating;
        this.minRating = minRating;
    }

    public ItemFilter(List<Category> categories){
        this.categories = categories;
        this.maxPrice = -1;
        this.minPrice = -1;
    }

    public ItemFilter(float maxPrice, float minPrice){
        this.categories = Collections.synchronizedList(new ArrayList<>());
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
    }
    
    public ItemFilter(){
        this.categories = Collections.synchronizedList(new ArrayList<>());
        this.maxPrice = -1;
        this.minPrice = -1;
    }

    public boolean matchesFilter(Item item){
        return false; // to implement
    }

    public List<Category> getCategories(){
        return this.categories;
    }
    
    public float getMinPrice(){
        return this.minPrice;
    }

    public float getMaxPrice(){
        return this.maxPrice;
    }

    public float getMaxRating() {
        return maxRating;
    }

    public float getMinRating() {
        return minRating;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }



}
