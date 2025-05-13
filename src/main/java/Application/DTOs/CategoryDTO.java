package Application.DTOs;

import Domain.Store.Category;

public class CategoryDTO {

    private final String name;
    private final String description;

    public CategoryDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static CategoryDTO fromCategory(Category category) {
        return new CategoryDTO(category.getName(), category.getDescription());
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}