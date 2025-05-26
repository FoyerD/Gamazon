package Application.DTOs.PoliciesDTOs;

public class CategoryPolicyDTO extends IPolicyDTO 
{
    private final String category;

    public CategoryPolicyDTO(String policyId, String storeId, String category) {
        super(policyId, storeId);
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
    
}
