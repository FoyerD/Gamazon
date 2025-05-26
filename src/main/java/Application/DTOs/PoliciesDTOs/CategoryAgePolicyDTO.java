package Application.DTOs.PoliciesDTOs;

public class CategoryAgePolicyDTO extends IPolicyDTO {
    private final String category;
    private final int minAge;

    public CategoryAgePolicyDTO(String policyId, String storeId, String category, int minAge) {
        super(policyId, storeId);
        this.category = category;
        this.minAge = minAge;
    }

    public String getCategory() {
        return category;
    }
    public int getMinAge() {
        return minAge;
    }
    
}
