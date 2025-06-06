package Application;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Application.DTOs.CategoryDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.PolicyDTO;
import Application.DTOs.PolicyDTO.Builder;
import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.Repos.IItemRepository;
import Domain.Store.Item;
import Domain.Store.Policy;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;
import Domain.management.PolicyFacade;

@Service
public class PolicyService {
    private static final String CLASS_NAME = PolicyService.class.getSimpleName();

    private final PolicyFacade policyFacade;
    private final TokenService tokenService;
    private final PermissionManager permissionManager;
    private final IItemRepository itemRepository;

    public PolicyService(PolicyFacade policyFacade,
                         TokenService tokenService,
                         PermissionManager permissionManager,
                         IItemRepository itemRepository) {
        this.policyFacade      = policyFacade;
        this.tokenService      = tokenService;
        this.permissionManager = permissionManager;
        this.itemRepository = itemRepository;
    }

    private PolicyDTO convertPolicyToDTO(Policy policy) {
        Builder builder = new PolicyDTO.Builder(policy.getStoreId(), policy.getType());
        Item prod = null;

        switch (policy.getType()) {

            case MIN_QUANTITY_ALL:
                return builder.createMinQuantityAllPolicy(policy.getMinItemsAll()).build(policy.getPolicyId());
            case MAX_QUANTITY_ALL:
                return builder.createMaxQuantityAllPolicy(policy.getMaxItemsAll()).build(policy.getPolicyId());
            case MIN_QUANTITY_PRODUCT:
                prod = itemRepository.getItem(policy.getStoreId(), policy.getTargetProductId());
                if (prod == null) {
                    throw new NoSuchElementException("Couldn't find product " + policy.getTargetProductId());
                }
                return builder.createMinQuantityProductPolicy(ItemDTO.fromItem(prod), policy.getMinItemsProduct()).build(policy.getPolicyId());
            case MAX_QUANTITY_PRODUCT:
                prod = itemRepository.getItem(policy.getStoreId(), policy.getTargetProductId());
                if (prod == null) {
                    throw new NoSuchElementException("Couldn't find product " + policy.getTargetProductId());
                }
                return builder.createMaxQuantityProductPolicy(ItemDTO.fromItem(prod), policy.getMaxItemsProduct()).build(policy.getPolicyId());
            case MIN_QUANTITY_CATEGORY:  
                return builder.createMinQuantityCategoryPolicy(
                    new CategoryDTO(policy.getTargetCategory(), "Description Unavailable"),
                    policy.getMinItemsCategory()
                ).build();
            case MAX_QUANTITY_CATEGORY:
                return builder.createMaxQuantityCategoryPolicy(
                    new CategoryDTO(policy.getTargetCategory(), "Description Unavailable"),
                    policy.getMaxItemsCategory()
                ).build();
            case CATEGORY_DISALLOW:
                return builder.createCategoryDisallowPolicy(new CategoryDTO(policy.getTargetCategory(), "Description Unavailable")).build(policy.getPolicyId());
            case CATEGORY_AGE:
                return builder.createCategoryAgePolicy(new CategoryDTO(policy.getTargetCategory(), "Description Unavailable"), policy.getMinAge()).build(policy.getPolicyId());
            default:
                throw new IllegalStateException("Unsupported policy type " + policy.getType().name());
        }
    }

    @Transactional
    public Response<List<PolicyDTO>> getAllStorePolicies(String sessionToken, String storeId) {
        String method = "getAllStorePolicies";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            String userId = tokenService.extractId(sessionToken);
            if (permissionManager.isBanned(userId)) {
                throw new Exception("User is banned from viewing policies.");
            }
            permissionManager.checkPermission(
                    userId, storeId, PermissionType.EDIT_STORE_POLICIES);

            List<Policy> policies = policyFacade.getAllStorePolicies(storeId);
            List<PolicyDTO> dtos = policies.stream()
                                           .map(this::convertPolicyToDTO)
                                           .collect(Collectors.toList());

            if (dtos.isEmpty()) {
                TradingLogger.logEvent(CLASS_NAME, method,
                        "No policies found for store " + storeId);
            } else {
                TradingLogger.logEvent(CLASS_NAME, method,
                        "Fetched " + dtos.size() + " policies for store " + storeId);
            }
            return new Response<>(dtos);

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    @Transactional
    public Response<PolicyDTO> getPolicyById(String sessionToken,
                                             String storeId,
                                             String policyId) {
        String method = "getPolicyById";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            String userId = tokenService.extractId(sessionToken);
            if (permissionManager.isBanned(userId)) {
                throw new Exception("User is banned from viewing policies.");
            }
            permissionManager.checkPermission(
                    userId, storeId, PermissionType.EDIT_STORE_POLICIES);

            Policy policy = policyFacade.getPolicy(policyId);
            TradingLogger.logEvent(CLASS_NAME, method,
                    "Fetched policy " + policyId + " for store " + storeId);
            
            return new Response<>(convertPolicyToDTO(policy));

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    @Transactional
    public Response<PolicyDTO> createPolicy(String sessionToken,
                                            String storeId,
                                            PolicyDTO details) {
        String method = "createPolicy";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            String userId = tokenService.extractId(sessionToken);
            if (permissionManager.isBanned(userId)) {
                throw new Exception("User is banned from creating policies.");
            }
            permissionManager.checkPermission(
                    userId, storeId, PermissionType.EDIT_STORE_POLICIES);

            Policy created;
            switch (details.getType()) {
                // TODO: Deprecated
                // case AND:
                //     if (details.getSubPolicies() == null || details.getSubPolicies().isEmpty()) {
                //         throw new IllegalArgumentException("AND policy needs children");
                //     }
                //     created = policyFacade.createAndPolicy(
                //             details.getSubPolicies().stream().map(PolicyDTO::toPolicy).toList(),
                //             storeId); // TODO: AND policies willnot work. lookup functions aren't defined. Refctoring needed :(
                //     break;
                case MIN_QUANTITY_ALL:
                    created = policyFacade.createMinQuantityAllPolicy(
                            storeId,
                            details.getMinItemsAll());
                    break;
                case MAX_QUANTITY_ALL:
                    created = policyFacade.createMaxQuantityAllPolicy(
                            storeId,
                            details.getMaxItemsAll());
                    break;
                case MIN_QUANTITY_PRODUCT:
                    created = policyFacade.createMinQuantityProductPolicy(
                            storeId,
                            details.getTargetProduct().getProductId(),
                            details.getMinItemsProduct());
                    break;
                case MAX_QUANTITY_PRODUCT:
                    created = policyFacade.createMaxQuantityProductPolicy(
                            storeId,
                            details.getTargetProduct().getProductId(),
                            details.getMaxItemsProduct());
                    break;
                case MIN_QUANTITY_CATEGORY:
                    created = policyFacade.createMinQuantityCategoryPolicy(
                            storeId,
                            details.getTargetCategory().getName(),
                            details.getMinItemsCategory());
                    break;
                case MAX_QUANTITY_CATEGORY:
                    created = policyFacade.createMaxQuantityCategoryPolicy(
                            storeId,
                            details.getTargetCategory().getName(),
                            details.getMaxItemsCategory());
                    break;
                case CATEGORY_DISALLOW:
                    created = policyFacade.createCategoryDisallowPolicy(
                            storeId,
                            details.getDisallowedCategory().getName());
                    break;
                case CATEGORY_AGE:
                    created = policyFacade.createCategoryAgePolicy(
                            storeId,
                            details.getAgeCategory().getName(),
                            details.getMinAge());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported policy type");
            }

            TradingLogger.logEvent(CLASS_NAME, method,
                    "Created policy " + created.getPolicyId() +
                    " (" + created.getType() + ") for store " + storeId);
            return new Response<>(convertPolicyToDTO(created));

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    @Transactional
    public Response<PolicyDTO> updatePolicy(String sessionToken,
                                            String storeId,
                                            String policyId,
                                            PolicyDTO details) {
        String method = "updatePolicy";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            String userId = tokenService.extractId(sessionToken);
            if (permissionManager.isBanned(userId)) {
                throw new Exception("User is banned from updating policies.");
            }
            permissionManager.checkPermission(
                    userId, storeId, PermissionType.EDIT_STORE_POLICIES);

            // Recreate or modify via facade as needed. Example: delete + create
            policyFacade.removePolicy(policyId);
            createPolicy(sessionToken, storeId, details);

            TradingLogger.logEvent(CLASS_NAME, method,
                    "Updated policy " + policyId + " for store " + storeId);
            Policy updated = policyFacade.getPolicy(policyId);
            return new Response<>(convertPolicyToDTO(updated));

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    @Transactional
    public Response<Boolean> deletePolicy(String sessionToken,
                                          String storeId,
                                          String policyId) {
        String method = "deletePolicy";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                TradingLogger.logError(CLASS_NAME, method, "Invalid token");
                return Response.error("Invalid token");
            }
            String userId = tokenService.extractId(sessionToken);
            if (permissionManager.isBanned(userId)) {
                throw new Exception("User is banned from deleting policies.");
            }
            permissionManager.checkPermission(
                    userId, storeId, PermissionType.EDIT_STORE_POLICIES);

            policyFacade.removePolicy(policyId);
            TradingLogger.logEvent(CLASS_NAME, method,
                    "Deleted policy " + policyId + " for store " + storeId);
            return new Response<>(true);

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}
