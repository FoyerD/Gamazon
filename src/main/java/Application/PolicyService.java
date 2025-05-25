package Application;

import Application.utils.Response;
import Application.DTOs.PoliciesDTOs.AndPolicyDTO;
import Application.DTOs.PoliciesDTOs.CategoryAgePolicyDTO;
import Application.DTOs.PoliciesDTOs.CategoryPolicyDTO;
import Application.DTOs.PoliciesDTOs.IPolicyDTO;
import Application.DTOs.PoliciesDTOs.MaxQuantityPolicyDTO;
import Application.DTOs.PoliciesDTOs.MinQuantityPolicyDTO;
import Application.utils.Error;
import Application.utils.TradingLogger;
import Domain.Store.Policies.IPolicy;
import Domain.Store.IPolicyRepository;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;
import Domain.management.PolicyFacade;

import org.aspectj.weaver.ast.And;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class PolicyService 
{
    private static final String CLASS_NAME = PolicyService.class.getSimpleName();

    private final PolicyFacade policyFacade;
    private final TokenService tokenService;
    private final PermissionManager permissionManager;

    public PolicyService(PolicyFacade policyFacade,
                         TokenService tokenService,
                         PermissionManager permissionManager) {
        this.policyFacade = policyFacade;
        this.tokenService = tokenService;
        this.permissionManager = permissionManager;
    }

    public Response<List<IPolicy>> getAllStorePolicies(String sessionToken, String storeId) {
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
            permissionManager.checkPermission(userId, storeId, PermissionType.EDIT_STORE_POLICIES);

            List<IPolicy> policies = policyFacade.getAllStorPolicies(storeId);
            if (policies.isEmpty()) {
                TradingLogger.logEvent(CLASS_NAME, method,
                                      "No policies found for store " + storeId);
                return new Response<>(List.of());
            }
            else
            {
                TradingLogger.logEvent(CLASS_NAME, method,
                                  "Fetched " + policies.size() + " policies for store " + storeId);
            }
            return new Response<>(policies);

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<IPolicy> getPolicyById(String sessionToken, String storeId, String policyId) {
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
            permissionManager.checkPermission(userId, storeId, PermissionType.EDIT_STORE_POLICIES);

            IPolicy policy = policyFacade.getPolicy(policyId);

            TradingLogger.logEvent(CLASS_NAME, method,
                                  "Fetched policy " + policyId + " for store " + storeId);
            return new Response<>(policy);

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<IPolicy> createPolicy(String sessionToken, String storeId, IPolicyDTO details) {
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
            permissionManager.checkPermission(userId, storeId, PermissionType.EDIT_STORE_POLICIES);

            if (details instanceof AndPolicyDTO)
            {
                AndPolicyDTO andPolicyDTO = (AndPolicyDTO) details;
                List<IPolicy> policies = andPolicyDTO.getPolicies();
                if (policies == null || policies.isEmpty()) {
                    throw new Exception("AndPolicy must contain at least one policy");
                }
                policyFacade.createAndPolicy(policies, andPolicyDTO.getPolicyId(), storeId);
                TradingLogger.logEvent(CLASS_NAME, method,
                                      "Created AndPolicy with ID " + andPolicyDTO.getPolicyId() +
                                      " for store " + storeId);
                return new Response<>(policyFacade.getPolicy(andPolicyDTO.getPolicyId()));
            } 
            else if (details instanceof CategoryPolicyDTO) 
            {
                CategoryPolicyDTO categoryPolicyDTO = (CategoryPolicyDTO) details;
                String disallowedCategories = categoryPolicyDTO.getCategory();
                if (disallowedCategories == null || disallowedCategories.isEmpty()) {
                    throw new Exception("CategoryPolicy must have at least one disallowed category");
                }
                policyFacade.createCategoryPolicy(disallowedCategories, categoryPolicyDTO.getPolicyId(), storeId);
                TradingLogger.logEvent(CLASS_NAME, method,
                                      "Created CategoryPolicy with ID " + categoryPolicyDTO.getPolicyId() +
                                      " for store " + storeId);
                return new Response<>(policyFacade.getPolicy(categoryPolicyDTO.getPolicyId()));
            } 
            else if (details instanceof CategoryAgePolicyDTO) 
            {
                CategoryAgePolicyDTO agePolicyDTO = (CategoryAgePolicyDTO) details;
                String category = agePolicyDTO.getCategory();
                if (category == null || category.isEmpty()) {
                    throw new Exception("CategoryAgePolicy must have a valid category");
                }
                if (agePolicyDTO.getMinAge() <= 0) {
                    throw new Exception("CategoryAgePolicy must have a valid minimum age");
                }
                policyFacade.createCategoryAgePolicy(agePolicyDTO.getPolicyId(), storeId, 
                                                     agePolicyDTO.getMinAge(), category);
                TradingLogger.logEvent(CLASS_NAME, method,
                                      "Created CategoryAgePolicy with ID " + agePolicyDTO.getPolicyId() +
                                      " for store " + storeId);
                return new Response<>(policyFacade.getPolicy(agePolicyDTO.getPolicyId()));
            }
            else if (details instanceof MaxQuantityPolicyDTO) 
            {
                MaxQuantityPolicyDTO maxQuantityPolicyDTO = (MaxQuantityPolicyDTO) details;
                if (maxQuantityPolicyDTO.getMaxQuantity() <= 0) {
                    throw new Exception("MaxQuantityPolicy must have a valid maximum quantity");
                }
                policyFacade.createMaxQuantitPolicy(maxQuantityPolicyDTO.getPolicyId(), storeId, 
                                                     maxQuantityPolicyDTO.getMaxQuantity(), maxQuantityPolicyDTO.getAmountGot());
                TradingLogger.logEvent(CLASS_NAME, method,
                                      "Created MaxQuantityPolicy with ID " + maxQuantityPolicyDTO.getPolicyId() +
                                      " for store " + storeId);
                return new Response<>(policyFacade.getPolicy(maxQuantityPolicyDTO.getPolicyId()));
            }
            else if (details instanceof MinQuantityPolicyDTO) 
            {
                MinQuantityPolicyDTO minAgePolicyDTO = (MinQuantityPolicyDTO) details;
                if (minAgePolicyDTO.getMinQuantity() <= 0) {
                    throw new Exception("MinQuantityPolicy must have a valid minimum quantity");
                }
                policyFacade.createMinQuantityPolicy(minAgePolicyDTO.getPolicyId(), storeId, 
                                                     minAgePolicyDTO.getMinQuantity(), minAgePolicyDTO.getAmountGot());
                TradingLogger.logEvent(CLASS_NAME, method,
                                      "Created MinQuantityPolicy with ID " + minAgePolicyDTO.getPolicyId() +
                                      " for store " + storeId);
                return new Response<>(policyFacade.getPolicy(minAgePolicyDTO.getPolicyId()));
            }
            else 
            {
                TradingLogger.logError(CLASS_NAME, method, "Unsupported policy type");
                throw new Exception("Unsupported policy type");
                }   
        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<IPolicy> updatePolicy(String sessionToken, String storeId, Long policyId, IPolicy updates) {
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
            permissionManager.checkPermission(userId, storeId, PermissionType.EDIT_STORE_POLICIES);

            IPolicy updatedPolicy = policyFacade.updatePolicy(policyId, updates);
            TradingLogger.logEvent(CLASS_NAME, method,
                                  "Updated policy " + policyId + " for store " + storeId);
            return new Response<>(updatedPolicy);

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> deletePolicy(String sessionToken, String storeId, Long policyId) 
    {
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
            permissionManager.checkPermission(userId, storeId, PermissionType.EDIT_STORE_POLICIES);

            boolean deleted = policyFacade.deletePolicy(policyId);
            TradingLogger.logEvent(CLASS_NAME, method,
                                  "Deleted policy " + policyId + " for store " + storeId);
            return new Response<>(deleted);

        } catch (Exception ex) {
            TradingLogger.logError(CLASS_NAME, method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}
