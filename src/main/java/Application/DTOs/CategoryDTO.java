package Application.DTOs;

import Domain.Store.Category;

public class CategoryDTO {

    private String name;
    private String description;

    public CategoryDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public CategoryDTO(Category category) {
        this.name = category.getName();
        this.description = category.getDescription();
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
}
