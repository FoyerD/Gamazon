package Application;

import Application.utils.Response;
import Application.DTOs.PolicyDTO;
import Application.utils.Error;
import Application.utils.TradingLogger;
import Domain.Store.Policy;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;
import Domain.management.PolicyFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class PolicyService {
    private static final String CLASS_NAME = PolicyService.class.getSimpleName();

    private final PolicyFacade policyFacade;
    private final TokenService tokenService;
    private final PermissionManager permissionManager;

    public PolicyService(PolicyFacade policyFacade,
                         TokenService tokenService,
                         PermissionManager permissionManager) {
        this.policyFacade      = policyFacade;
        this.tokenService      = tokenService;
        this.permissionManager = permissionManager;
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
                                           .map(PolicyDTO::new)
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
            return new Response<>(new PolicyDTO(policy));

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
                case AND:
                    if (details.getSubPolicies() == null || details.getSubPolicies().isEmpty()) {
                        throw new IllegalArgumentException("AND policy needs children");
                    }
                    created = policyFacade.createAndPolicy(
                            details.getSubPolicies(),
                            details.getPolicyId(),
                            storeId);
                    break;
                case MIN_QUANTITY_ALL:
                    created = policyFacade.createMinQuantityAllPolicy(
                            details.getPolicyId(),
                            storeId,
                            details.getMinItemsAll());
                    break;
                case MAX_QUANTITY_ALL:
                    created = policyFacade.createMaxQuantityAllPolicy(
                            details.getPolicyId(),
                            storeId,
                            details.getMaxItemsAll());
                    break;
                case MIN_QUANTITY_PRODUCT:
                    created = policyFacade.createMinQuantityProductPolicy(
                            details.getPolicyId(),
                            storeId,
                            details.getTargetProductId(),
                            details.getMinItemsProduct());
                    break;
                case MAX_QUANTITY_PRODUCT:
                    created = policyFacade.createMaxQuantityProductPolicy(
                            details.getPolicyId(),
                            storeId,
                            details.getTargetProductId(),
                            details.getMaxItemsProduct());
                    break;
                case MIN_QUANTITY_CATEGORY:
                    created = policyFacade.createMinQuantityCategoryPolicy(
                            details.getPolicyId(),
                            storeId,
                            details.getTargetCategory(),
                            details.getMinItemsCategory());
                    break;
                case MAX_QUANTITY_CATEGORY:
                    created = policyFacade.createMaxQuantityCategoryPolicy(
                            details.getPolicyId(),
                            storeId,
                            details.getTargetCategory(),
                            details.getMaxItemsCategory());
                    break;
                case CATEGORY_DISALLOW:
                    created = policyFacade.createCategoryDisallowPolicy(
                            details.getPolicyId(),
                            storeId,
                            details.getDisallowedCategory());
                    break;
                case CATEGORY_AGE:
                    created = policyFacade.createCategoryAgePolicy(
                            details.getPolicyId(),
                            storeId,
                            details.getAgeCategory(),
                            details.getMinAge());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported policy type");
            }

            TradingLogger.logEvent(CLASS_NAME, method,
                    "Created policy " + created.getPolicyId() +
                    " (" + created.getType() + ") for store " + storeId);
            return new Response<>(new PolicyDTO(created));

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
            return new Response<>(new PolicyDTO(updated));

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
