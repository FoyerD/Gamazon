package Domain.Store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;



public class ItemFilter {
    private Set<Category> categories; 
    private float minPrice;
    private float maxPrice;
    private float maxRating;
    private float minRating;
    
    public ItemFilter(){
        this.categories = Collections.synchronizedSet(new HashSet<>());
        this.maxPrice = -1;
        this.minPrice = -1;
        this.maxRating = -1;
        this.minRating = -1;
    }

    public boolean matchesFilter(Item item){
        return false; // to implement
    }

    public void addCategory(Category c){
        this.categories.add(c);
    }
    
    public void addCategory(Collection<Category> categoriesToAdd){
        this.categories.addAll(categoriesToAdd);
    }

    public boolean removeCategory(Category c){
        return this.categories.remove(c);
    }

    public Set<Category> getCategories(){
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

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public void setMinPrice(float minPrice) {
        this.minPrice = minPrice;
    }

    public void setMaxRating(float maxRating) {
        this.maxRating = maxRating;
    }

    public void setMaxPrice(float maxPrice) {
        this.maxPrice = maxPrice;
    }

    public void setMinRating(float minRating) {
        this.minRating = minRating;
    }



}
