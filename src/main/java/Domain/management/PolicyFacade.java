package Domain.management;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Component;

import Domain.Repos.IPolicyRepository;
import Domain.Repos.IUserRepository;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Policy;
import Domain.Store.ProductFacade;
import Domain.User.Member;

@Component
public class PolicyFacade {

    private final IPolicyRepository policyRepository;
    private final IUserRepository userRepository;
    private final ItemFacade itemFacade;
    private final ProductFacade productFacade;

    public PolicyFacade(IPolicyRepository policyRepository,
                        IUserRepository userRepoMock,
                        ItemFacade itemFacadeMock,
                        ProductFacade productFacadeMock) {
        this.policyRepository = policyRepository;
        this.userRepository   = userRepoMock;
        this.itemFacade       = itemFacadeMock;
        this.productFacade    = productFacadeMock;
    }
    

    private void validateIds(String policyId, String storeId) {
        if (policyId == null || policyId.isBlank() ||
            storeId  == null || storeId.isBlank()) {
            throw new IllegalArgumentException("Policy ID and Store ID cannot be empty");
        }
    }

    private void injectLookups(Policy policy, String storeId) {
        policy.injectLookups(
            productFacade::getProduct,
            id -> itemFacade.getItem(storeId, id)
        );
    }
    private void injectLookups(List<Policy> policies, String storeId) {
        for (Policy p : policies) {
            injectLookups(p, storeId);
        }
    }


    public Policy createMinQuantityAllPolicy(String storeId, int minQuantity) 
    {
        String policyId = UUID.randomUUID().toString();
        validateIds(policyId, storeId);
        if (minQuantity < 1) {
            throw new IllegalArgumentException("minQuantity must be ≥ 1");
        }

        Policy policy = new Policy.Builder(Policy.Type.MIN_QUANTITY_ALL)
            .policyId(policyId)
            .storeId(storeId)
            .productLookup(productFacade::getProduct)
            .itemLookup(id -> itemFacade.getItem(storeId, id))
            .minItemsAll(minQuantity)
            .build();

        if (!policyRepository.add(policyId, policy)) {
            throw new IllegalStateException("Policy already exists: " + policyId);
        }
        return policy;
    }

    public Policy createMaxQuantityAllPolicy(String storeId,
                                             int maxQuantity) {
        String policyId = UUID.randomUUID().toString();
        validateIds(policyId, storeId);
        if (maxQuantity < 1) {
            throw new IllegalArgumentException("maxQuantity must be ≥ 1");
        }

        Policy policy = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
            .policyId(policyId)
            .storeId(storeId)
            .productLookup(productFacade::getProduct)
            .itemLookup(id -> itemFacade.getItem(storeId, id))
            .maxItemsAll(maxQuantity)
            .build();

        if (!policyRepository.add(policyId, policy)) {
            throw new IllegalStateException("Policy already exists: " + policyId);
        }
        return policy;
    }

    public Policy createMinQuantityProductPolicy(String storeId,
                                                 String productId,
                                                 int minQuantity) {
        String policyId = UUID.randomUUID().toString();
        validateIds(policyId, storeId);
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be empty");
        }
        if (minQuantity < 1) {
            throw new IllegalArgumentException("minQuantity must be ≥ 1");
        }

        Policy policy = new Policy.Builder(Policy.Type.MIN_QUANTITY_PRODUCT)
            .policyId(policyId)
            .storeId(storeId)
            .productLookup(productFacade::getProduct)
            .itemLookup(id -> itemFacade.getItem(storeId, id))
            .targetProductId(productId)
            .minItemsProduct(minQuantity)
            .build();

        if (!policyRepository.add(policyId, policy)) {
            throw new IllegalStateException("Policy already exists: " + policyId);
        }
        return policy;
    }

    public Policy createMaxQuantityProductPolicy(String storeId,
                                                 String productId,
                                                 int maxQuantity) {
        String policyId = UUID.randomUUID().toString();
        validateIds(policyId, storeId);
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be empty");
        }
        if (maxQuantity < 1) {
            throw new IllegalArgumentException("maxQuantity must be ≥ 1");
        }

        Policy policy = new Policy.Builder(Policy.Type.MAX_QUANTITY_PRODUCT)
            .policyId(policyId)
            .storeId(storeId)
            .productLookup(productFacade::getProduct)
            .itemLookup(id -> itemFacade.getItem(storeId, id))
            .targetProductId(productId)
            .maxItemsProduct(maxQuantity)
            .build();

        if (!policyRepository.add(policyId, policy)) {
            throw new IllegalStateException("Policy already exists: " + policyId);
        }
        return policy;
    }

    public Policy createMinQuantityCategoryPolicy(String storeId,
                                                  String category,
                                                  int minQuantity) {
        String policyId = UUID.randomUUID().toString();
        validateIds(policyId, storeId);
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category cannot be empty");
        }
        if (minQuantity < 1) {
            throw new IllegalArgumentException("minQuantity must be ≥ 1");
        }

        Policy policy = new Policy.Builder(Policy.Type.MIN_QUANTITY_CATEGORY)
            .policyId(policyId)
            .storeId(storeId)
            .productLookup(productFacade::getProduct)
            .itemLookup(id -> itemFacade.getItem(storeId, id))
            .targetCategory(category)
            .minItemsCategory(minQuantity)
            .build();

        if (!policyRepository.add(policyId, policy)) {
            throw new IllegalStateException("Policy already exists: " + policyId);
        }
        return policy;
    }

    public Policy createMaxQuantityCategoryPolicy(String storeId,
                                                  String category,
                                                  int maxQuantity) {
        String policyId = UUID.randomUUID().toString();
        validateIds(policyId, storeId);
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category cannot be empty");
        }
        if (maxQuantity < 1) {
            throw new IllegalArgumentException("maxQuantity must be ≥ 1");
        }

        Policy policy = new Policy.Builder(Policy.Type.MAX_QUANTITY_CATEGORY)
            .policyId(policyId)
            .storeId(storeId)
            .productLookup(productFacade::getProduct)
            .itemLookup(id -> itemFacade.getItem(storeId, id))
            .targetCategory(category)
            .maxItemsCategory(maxQuantity)
            .build();

        if (!policyRepository.add(policyId, policy)) {
            throw new IllegalStateException("Policy already exists: " + policyId);
        }
        return policy;
    }

    public Policy createCategoryDisallowPolicy(String storeId,
                                               String category) {
        String policyId = UUID.randomUUID().toString();
        validateIds(policyId, storeId);
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category cannot be empty");
        }

        Policy policy = new Policy.Builder(Policy.Type.CATEGORY_DISALLOW)
            .policyId(policyId)
            .storeId(storeId)
            .productLookup(productFacade::getProduct)
            .itemLookup(id -> itemFacade.getItem(storeId, id))
            .disallowedCategory(category)
            .build();

        if (!policyRepository.add(policyId, policy)) {
            throw new IllegalStateException("Policy already exists: " + policyId);
        }
        return policy;
    }

    public Policy createCategoryAgePolicy(String storeId,
                                          String category,
                                          int minAge) {
        String policyId = UUID.randomUUID().toString();
        validateIds(policyId, storeId);
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category cannot be empty");
        }
        if (minAge < 0) {
            throw new IllegalArgumentException("minAge must be ≥ 0");
        }

        Policy policy = new Policy.Builder(Policy.Type.CATEGORY_AGE)
            .policyId(policyId)
            .storeId(storeId)
            .productLookup(productFacade::getProduct)
            .itemLookup(id -> itemFacade.getItem(storeId, id))
            .ageCategory(category)
            .minAge(minAge)
            .build();

        if (!policyRepository.add(policyId, policy)) {
            throw new IllegalStateException("Policy already exists: " + policyId);
        }
        return policy;
    }

    public void removePolicy(String policyId) {
        policyRepository.remove(policyId);
    }

    public Policy getPolicy(String policyId) {
        Policy p = policyRepository.get(policyId);
        if (p == null) {
            throw new NoSuchElementException("No policy for ID: " + policyId);
        }
        injectLookups(p, p.getStoreId());
        return p;
    }

    public List<Policy> getAllStorePolicies(String storeId) {
        if (storeId == null || storeId.isBlank()) {
            throw new IllegalArgumentException("storeId cannot be empty");
        }
        List<Policy> policies = policyRepository.getAllStorePolicies(storeId);
        injectLookups(policies, storeId);
        return policies;
    }

    public boolean isApplicable(String basketId,
                                String memberId,
                                String policyId,
                                ShoppingBasket basket) {
        if (basketId == null || memberId == null || policyId == null ||
            basketId.isBlank() || memberId.isBlank() || policyId.isBlank()) {
            throw new IllegalArgumentException("IDs cannot be empty");
        }

        Policy policy = getPolicy(policyId);
        if (basket == null) {
            throw new NoSuchElementException("No basket: " + basketId);
        }
        Member member = userRepository.getMember(memberId);
        if (member == null) {
            throw new NoSuchElementException("No member: " + memberId);
        }

        return policy.isApplicable(basket, member);
    }
}
