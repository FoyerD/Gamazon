package Domain.Store;

import java.util.LinkedHashSet;
import java.util.Set;


public class Product {

    private String productId;
    private String name;
    private Set<Category> categories;

    public Product(String productId, String name, Set<Category> categories){
        this.productId = productId;
        this.name = name;
        this.categories = categories;
    }
    
    public Product(String productId, String name){
        this.productId = productId;
        this.name = name;
        this.categories = new LinkedHashSet<>();
    }

    public boolean addCategory(Category c){
        return categories.add(c);
    }
    
    public boolean removeCategory(Category c){
        return categories.remove(c);
    }
    
    public String getProductId(){
        return productId;
    }

    public String getName(){
        return name;
    }

    public Set<Category> getCategories(){
        return categories;
    }
}
