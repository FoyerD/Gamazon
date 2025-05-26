package Application.DTOs.PoliciesDTOs;

import java.lang.reflect.Member;

import Domain.Store.Item;

public abstract class IPolicyDTO 
{
    private final String policyId;
    private final String storeId;

    public IPolicyDTO(String policyId, String storeId) {
        this.policyId = policyId;
        this.storeId = storeId;
    }

    public String getPolicyId() {
        return policyId;
    }
    public String getStoreId() {
        return storeId;
    }

}
