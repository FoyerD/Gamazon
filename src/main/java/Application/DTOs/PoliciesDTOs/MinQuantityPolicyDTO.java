package Application.DTOs.PoliciesDTOs;

public class MinQuantityPolicyDTO extends IPolicyDTO {
    private final String itemId;
    private final int minQuantity;
    private final int amountGot; 

    public MinQuantityPolicyDTO(String policyId, String storeId, String itemId, int minQuantity, int amountGot) {
        super(policyId, storeId);
        this.itemId = itemId;
        this.minQuantity = minQuantity;
        this.amountGot = amountGot;
    }

    public String getItemId() {
        return itemId;
    }

    public int getMinQuantity() {
        return minQuantity;
    }
    public int getAmountGot() {
        return amountGot;
    }
    
    
}
