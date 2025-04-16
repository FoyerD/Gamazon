package Domain.Store;

public class Category {
    private String name;
    private String description;

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public boolean isEquals(Category other) {
        if (other == null) {
            return false;
        }
        return this.name.equals(other.name);
    }

    public String getDescription() {
        return description;
    }
    
}
