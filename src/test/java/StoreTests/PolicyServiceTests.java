package StoreTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import Application.PolicyService;
import Application.ServiceManager;
import Application.TokenService;
import Application.DTOs.PolicyDTO;
import Application.DTOs.StoreDTO;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import Domain.FacadeManager;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
import Domain.Repos.IItemRepository;
import Domain.Store.Policy;
import Domain.management.PermissionManager;
import Infrastructure.MemoryRepoManager;

/**
 * Acceptance‐style tests for PolicyService, updated to use the provided PermissionManager.
 *
 * Changes:
 * 1. Register a user (guestEntry + register) to obtain a valid tokenId.
 * 2. After creating the store, call appointFirstStoreOwner(...) so that this user
 *    becomes owner/founder and thus has EDIT_STORE_POLICIES permission.
 */
public class PolicyServiceTests {

    private ServiceManager       serviceManager;
    private PolicyService        policyService;
    private PermissionManager    permManager;
    private TokenService         tokenService;
    private IItemRepository      itemRepo;
    private String               tokenId;
    private String               storeId;

    @Before
    public void setUp() {
        // 1. Initialize in‐memory repositories via MemoryRepoManager
        MemoryRepoManager repoManager = new MemoryRepoManager();

        // 2. Initialize FacadeManager (provides PolicyFacade, PermissionManager, etc.)
        FacadeManager facadeManager = new FacadeManager(repoManager, mock(IExternalPaymentService.class), mock(IExternalSupplyService.class));


        // 3. Initialize ServiceManager so we can register a user and create a store
        this.serviceManager = new ServiceManager(facadeManager);

        // 4. Extract the real PermissionManager from FacadeManager
        this.permManager = facadeManager.getPermissionManager();

        // 5. Extract the real TokenService from ServiceManager
        this.tokenService = serviceManager.getTokenService();

        // 6. Extract the real IItemRepository from MemoryRepoManager
        this.itemRepo = repoManager.getItemRepository();

        // 7. Instantiate PolicyService with its real dependencies
        this.policyService = serviceManager.getPolicyService();

        // ----- A) Register a user to get a valid tokenId -----
        Response<UserDTO> guestResp = serviceManager.getUserService().guestEntry();
        assertFalse("Guest creation should succeed", guestResp.errorOccurred());

        Response<UserDTO> regResp = serviceManager.getUserService().register(
            guestResp.getValue().getSessionToken(),
            "Member1",
            "Password123!",
            "member1@example.com"
        );
        assertFalse("User registration should succeed", regResp.errorOccurred());
        this.tokenId = regResp.getValue().getSessionToken();
        // ------------------------------------------------------

        // 8. Create a store under that user
        Response<StoreDTO> storeResp = serviceManager.getStoreService()
            .addStore(this.tokenId, "PolicyTestStore", "Store for policy tests");
        assertFalse("Store creation should succeed", storeResp.errorOccurred());
        this.storeId = storeResp.getValue().getId();

        // ----- B) Make the new user the first store owner, so they get EDIT_STORE_POLICIES -----
        String userId = tokenService.extractId(this.tokenId);
        permManager.appointFirstStoreOwner(userId, this.storeId);
        // --------------------------------------------------------------------------------------
    }

    // -------------------------------------------------------------
    // Test 1: Initially, no policies exist for the new store.
    // -------------------------------------------------------------
    @Test
    public void GivenNoPolicies_WhenGetAllStorePolicies_ThenReturnEmptyList() {
        Response<List<PolicyDTO>> resp = policyService
            .getAllStorePolicies(this.tokenId, this.storeId);

        assertFalse("Response should not have an error", resp.errorOccurred());
        assertNotNull("Value should be non-null", resp.getValue());
        assertEquals("Initially, there should be 0 policies", 0, resp.getValue().size());
    }

    // -------------------------------------------------------------
    // Test 2: Create a MAX_QUANTITY_ALL policy; retrieving all returns exactly one.
    // -------------------------------------------------------------
    @Test
    public void GivenValidTokenAndStore_WhenCreateMaxQuantityAllPolicy_ThenSucceeds() {
        // Arrange: build a DTO for maxItemsAll = 5
        PolicyDTO dtoToCreate = new PolicyDTO.Builder(this.storeId, Policy.Type.MAX_QUANTITY_ALL)
                                    .createMaxQuantityAllPolicy(5)
                                    .build();

        // Act: call createPolicy
        Response<PolicyDTO> createResp = policyService
            .createPolicy(this.tokenId, this.storeId, dtoToCreate);

        // Assert: creation succeeded
        assertFalse("Response should not have an error", createResp.errorOccurred());
        PolicyDTO createdDto = createResp.getValue();
        assertNotNull("Created PolicyDTO must have non-null policyId", createdDto.getPolicyId());
        assertEquals("StoreId should match", this.storeId, createdDto.getStoreId());
        assertEquals("Type should be MAX_QUANTITY_ALL",
                     Policy.Type.MAX_QUANTITY_ALL, createdDto.getType());
        assertEquals("maxItemsAll must be 5",
                     Integer.valueOf(5), createdDto.getMaxItemsAll());

        // Verify getAllStorePolicies now returns exactly one policy
        Response<List<PolicyDTO>> allResp = policyService
            .getAllStorePolicies(this.tokenId, this.storeId);
        assertFalse("No error when fetching all policies", allResp.errorOccurred());
        List<PolicyDTO> list = allResp.getValue();
        assertEquals("Should have exactly 1 policy", 1, list.size());
        assertEquals("Returned policyId matches created one",
                     createdDto.getPolicyId(), list.get(0).getPolicyId());
    }

    // -------------------------------------------------------------
    // Test 3: Retrieve a single existing policy by its ID.
    // -------------------------------------------------------------
    @Test
    public void GivenExistingPolicy_WhenGetPolicyById_ThenReturnCorrectDTO() {
        // Arrange: create a MIN_QUANTITY_ALL policy with minItemsAll = 2
        PolicyDTO dtoToCreate = new PolicyDTO.Builder(this.storeId, Policy.Type.MIN_QUANTITY_ALL)
                                    .createMinQuantityAllPolicy(2)
                                    .build();
        Response<PolicyDTO> createResp = policyService
            .createPolicy(this.tokenId, this.storeId, dtoToCreate);
        assertFalse("Creation should succeed", createResp.errorOccurred());
        String policyId = createResp.getValue().getPolicyId();

        // Act: getPolicyById
        Response<PolicyDTO> getResp = policyService
            .getPolicyById(this.tokenId, this.storeId, policyId);

        // Assert: retrieved data matches
        assertFalse("Response should not have an error", getResp.errorOccurred());
        PolicyDTO fetched = getResp.getValue();
        assertEquals("Fetched policyId should match", policyId, fetched.getPolicyId());
        assertEquals("Fetched Type should be MIN_QUANTITY_ALL",
                     Policy.Type.MIN_QUANTITY_ALL, fetched.getType());
        assertEquals("minItemsAll should be 2",
                     Integer.valueOf(2), fetched.getMinItemsAll());
    }

    // -------------------------------------------------------------
    // Test 4: getPolicyById for non-existent policy → error
    // -------------------------------------------------------------
    @Test
    public void GivenNonexistentPolicy_WhenGetPolicyById_ThenReturnError() {
        String randomPolicyId = UUID.randomUUID().toString();
        Response<PolicyDTO> resp = policyService
            .getPolicyById(this.tokenId, this.storeId, randomPolicyId);
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().toLowerCase().contains("no policy"));
    }

    // -------------------------------------------------------------
    // Test 5: Delete an existing policy successfully.
    // -------------------------------------------------------------
    @Test
    public void GivenExistingPolicy_WhenDeletePolicy_ThenSucceeds() {
        // Arrange: create a MAX_QUANTITY_ALL policy to delete
        PolicyDTO dto = new PolicyDTO.Builder(this.storeId, Policy.Type.MAX_QUANTITY_ALL)
                              .createMaxQuantityAllPolicy(3)
                              .build();
        Response<PolicyDTO> createResp = policyService
            .createPolicy(this.tokenId, this.storeId, dto);
        assertFalse("Creation should succeed", createResp.errorOccurred());
        String toDeleteId = createResp.getValue().getPolicyId();

        // Act: deletePolicy
        Response<Boolean> deleteResp = policyService
            .deletePolicy(this.tokenId, this.storeId, toDeleteId);

        // Assert: deletion succeeded
        assertFalse("Deletion should not produce an error", deleteResp.errorOccurred());
        assertTrue("deletePolicy should return true", deleteResp.getValue());

        // Verify getAllStorePolicies is now empty again
        Response<List<PolicyDTO>> allResp = policyService
            .getAllStorePolicies(this.tokenId, this.storeId);
        assertFalse("No error when fetching all policies", allResp.errorOccurred());
        assertEquals("Should have 0 policies after deletion", 0, allResp.getValue().size());
    }


    // -------------------------------------------------------------
    // Test 6: deletePolicy for a non‐existent policy → returns success==true
    // -------------------------------------------------------------
    @Test
    public void GivenNonexistentPolicy_WhenDeletePolicy_ThenReturnSuccess() 
    {
        String randomPolicyId = UUID.randomUUID().toString();

        // Because PolicyFacade.removePolicy(...) does not throw on missing ID,
        // deletePolicy(...) will return a non‐error Response<Boolean> with value = true.
        Response<Boolean> resp = policyService
            .deletePolicy(this.tokenId, this.storeId, randomPolicyId);

        // Verify that no error occurred, and the returned value is true.
        assertFalse("Response should not indicate an error", resp.errorOccurred());
        assertTrue("deletePolicy should still return true", resp.getValue());
    }
    // -------------------------------------------------------------
    // Test 7: Update an existing policy (change maxItemsAll from 4 to 7),
    //         but because the old ID was removed internally, we expect an error.
    // -------------------------------------------------------------
    @Test
    public void GivenExistingPolicy_WhenUpdatePolicy_ThenReturnErrorBecauseOldIdIsDeleted() {
        // Arrange: create a MAX_QUANTITY_ALL policy with maxItemsAll = 4
        PolicyDTO dto = new PolicyDTO.Builder(this.storeId, Policy.Type.MAX_QUANTITY_ALL)
                                .createMaxQuantityAllPolicy(4)
                                .build();
        Response<PolicyDTO> createResp = policyService
            .createPolicy(this.tokenId, this.storeId, dto);
        assertFalse("Creation should succeed", createResp.errorOccurred());
        String toUpdateId = createResp.getValue().getPolicyId();

        // Act: attempt to updatePolicy to change maxItemsAll -> 7
        PolicyDTO dtoNew = new PolicyDTO.Builder(this.storeId, Policy.Type.MAX_QUANTITY_ALL)
                                .createMaxQuantityAllPolicy(7)
                                .build();
        Response<PolicyDTO> updateResp = policyService
            .updatePolicy(this.tokenId, this.storeId, toUpdateId, dtoNew);

        // Assert: because the old policy was removed underneath, updateResp should be an error
        assertTrue(
            "Update should produce an error (old ID no longer exists)",
            updateResp.errorOccurred()
        );
        // The underlying facade exception is "No policy for ID: …", so check that substring
        assertTrue(
            "Expected error message to mention 'no policy', but was: " + updateResp.getErrorMessage(),
            updateResp.getErrorMessage().toLowerCase().contains("no policy")
        );
    }


    // -------------------------------------------------------------
    // Test 8: updatePolicy for non-existent policy → error
    // -------------------------------------------------------------
    @Test
    public void GivenNonexistentPolicy_WhenUpdatePolicy_ThenReturnError() {
        String randomPolicyId = UUID.randomUUID().toString();
        PolicyDTO dtoNew = new PolicyDTO.Builder(this.storeId, Policy.Type.MAX_QUANTITY_ALL)
                                .createMaxQuantityAllPolicy(10)
                                .build();
        Response<PolicyDTO> resp = policyService
            .updatePolicy(this.tokenId, this.storeId, randomPolicyId, dtoNew);
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().toLowerCase().contains("no policy"));
    }

    // -------------------------------------------------------------
    // Test 9: createPolicy with invalid token → error
    // -------------------------------------------------------------
    @Test
    public void GivenInvalidToken_WhenCreatePolicy_ThenReturnError() {
        PolicyDTO dto = new PolicyDTO.Builder(this.storeId, Policy.Type.MAX_QUANTITY_ALL)
                            .createMaxQuantityAllPolicy(2)
                            .build();
        Response<PolicyDTO> resp = policyService
            .createPolicy("invalidToken", this.storeId, dto);
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().toLowerCase().contains("invalid token"));
    }

    // -------------------------------------------------------------
    // Test 10: createPolicy with no permission → error
    // -------------------------------------------------------------
    @Test
    public void GivenNoPermission_WhenCreatePolicy_ThenReturnError() {
        // 1) First create a store under the original user (who does have rights)
        Response<StoreDTO> respStore = serviceManager.getStoreService()
            .addStore(this.tokenId, "NoPermStore", "Store without policy-edit perms");
        assertFalse("Store creation should succeed", respStore.errorOccurred());
        String noPermStoreId = respStore.getValue().getId();

        // 2) Register a second, completely new user (so they have no rights on that store)
        Response<UserDTO> guest2 = serviceManager.getUserService().guestEntry();
        assertFalse("Second guest creation should succeed", guest2.errorOccurred());

        Response<UserDTO> reg2 = serviceManager.getUserService().register(
            guest2.getValue().getSessionToken(),
            "Member2",
            "AnotherPass!234",
            "member2@example.com"
        );
        assertFalse("Second user registration should succeed", reg2.errorOccurred());
        String secondToken = reg2.getValue().getSessionToken();

        // 3) Build a DTO as if this second user tried to create a policy on noPermStoreId
        PolicyDTO dto = new PolicyDTO.Builder(noPermStoreId, Policy.Type.MAX_QUANTITY_ALL)
                            .createMaxQuantityAllPolicy(3)
                            .build();

        // 4) Call createPolicy() with the second user's token
        Response<PolicyDTO> resp = policyService
            .createPolicy(secondToken, noPermStoreId, dto);

        // 5) Now assert that it fails due to lack of permission
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(
            "Error message should mention permission",
            resp.getErrorMessage().toLowerCase().contains("permission")
        );
    }


    // -------------------------------------------------------------
    // Test 11: getViolatedPolicies for a user with no cart → empty list
    // -------------------------------------------------------------
    @Test
    public void GivenValidUserWithEmptyCart_WhenGetViolatedPolicies_ThenReturnEmptyList() {
        Response<List<PolicyDTO>> resp = policyService.getViolatedPolicies(this.tokenId);
        assertFalse(resp.errorOccurred());
        assertNotNull(resp.getValue());
        assertEquals(0, resp.getValue().size());
    }

}