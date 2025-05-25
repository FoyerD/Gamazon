package Application.DTOs.PoliciesDTOs;

import java.util.List;

import Domain.Store.Policies.IPolicy;

public class AndPolicyDTO extends IPolicyDTO {
    private final List<IPolicy> policies;

    public AndPolicyDTO(String policyId, String storeId, List<IPolicy> policies) {
        super(policyId, storeId);
        this.policies = policies;
    }

    public List<IPolicy> getPolicies() {
        return policies;
    }
    

}
