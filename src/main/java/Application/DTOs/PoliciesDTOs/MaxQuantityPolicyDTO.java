package Application.DTOs.PoliciesDTOs;

public class MaxQuantityPolicyDTO extends IPolicyDTO {
    private final String itemId;
    private final int maxQuantity;
    private final int amountGot; 

    public MaxQuantityPolicyDTO(String policyId, String storeId, String itemId, int maxQuantity, int amountGot) {
        super(policyId, storeId);
        this.itemId = itemId;
        this.maxQuantity = maxQuantity;
        this.amountGot = amountGot;
    }

    public String getItemId() {
        return itemId;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }
    public int getAmountGot() {
        return amountGot;
    }

}
