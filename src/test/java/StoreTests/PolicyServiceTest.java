package StoreTests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import Application.PolicyService;
import Application.TokenService;
import Application.DTOs.PolicyDTO;
import Application.utils.Response;
import Domain.Store.Policy;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;
import Domain.management.PolicyFacade;

/**
 * Unit tests for Application.PolicyService.
 * Verifies token validation, permission checks, and delegation to PolicyFacade.
 */
public class PolicyServiceTest {

    private PolicyService      service;
    private PolicyFacade       facadeMock;
    private PermissionManager  permMock;
    private TokenService       tokenServiceMock;

    @Before
    public void setUp() {
        facadeMock       = mock(PolicyFacade.class);
        permMock         = mock(PermissionManager.class);
        tokenServiceMock = mock(TokenService.class);

        // “goodToken” → valid
        when(tokenServiceMock.validateToken("goodToken")).thenReturn(true);
        when(tokenServiceMock.extractId("goodToken")).thenReturn("user1");

        // “badToken” → validation throws IllegalArgumentException
        when(tokenServiceMock.validateToken("badToken"))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        service = new PolicyService(facadeMock, tokenServiceMock, permMock);
    }

    @Test
    public void getAllStorePolicies_validTokenAndPermission_returnsDTOList() {
        // Arrange: facadeMock.getAllStorePolicies("storeA") returns two Domain policies
        Policy p1 = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("P1")
                .storeId("storeA")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(5)
                .build();

        Policy p2 = new Policy.Builder(Policy.Type.MIN_QUANTITY_ALL)
                .policyId("P2")
                .storeId("storeA")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .minItemsAll(2)
                .build();

        when(facadeMock.getAllStorePolicies("storeA")).thenReturn(Arrays.asList(p1, p2));
        doNothing().when(permMock)
                .checkPermission("user1", "storeA", PermissionType.EDIT_STORE_POLICIES);

        // Act
        Response<List<PolicyDTO>> resp = service.getAllStorePolicies("goodToken", "storeA");

        // Assert
        assertFalse("Response should not have an error", resp.errorOccurred());
        List<PolicyDTO> dtos = resp.getValue();
        assertEquals("Should return 2 PolicyDTOs", 2, dtos.size());

        assertEquals("First PolicyDTO policyId should be P1", "P1", dtos.get(0).getPolicyId());
        assertEquals("First PolicyDTO type should be MAX_QUANTITY_ALL",
                     Policy.Type.MAX_QUANTITY_ALL, dtos.get(0).getType());

        assertEquals("Second PolicyDTO policyId should be P2", "P2", dtos.get(1).getPolicyId());
        assertEquals("Second PolicyDTO type should be MIN_QUANTITY_ALL",
                     Policy.Type.MIN_QUANTITY_ALL, dtos.get(1).getType());
    }

    @Test
    public void getAllStorePolicies_invalidToken_returnsErrorResponse() {
        // Act
        Response<List<PolicyDTO>> resp = service.getAllStorePolicies("badToken", "storeA");

        // Assert
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().toLowerCase().contains("invalid token"));
    }

    @Test
    public void getPolicy_validTokenAndPermission_returnsSingleDTO() {
        // Arrange: facadeMock.getPolicy("P10") returns a Domain policy
        Policy p = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("P10")
                .storeId("storeX")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(10)
                .build();

        when(facadeMock.getPolicy("P10")).thenReturn(p);
        doNothing().when(permMock)
                .checkPermission("user1", "storeX", PermissionType.EDIT_STORE_POLICIES);

        // Act
        Response<PolicyDTO> resp = service.getPolicyById("goodToken", "storeX", "P10");

        // Assert
        assertFalse("Response should not have an error", resp.errorOccurred());
        PolicyDTO dto = resp.getValue();
        assertEquals("PolicyDTO policyId should be P10", "P10", dto.getPolicyId());
        assertEquals("PolicyDTO type should be MAX_QUANTITY_ALL",
                     Policy.Type.MAX_QUANTITY_ALL, dto.getType());
    }

    @Test
    public void getPolicy_nonexistentPolicy_returnsErrorResponse() {
        // Arrange: facadeMock.getPolicy("noSuch") throws NoSuchElementException
        when(facadeMock.getPolicy("noSuch"))
                .thenThrow(new NoSuchElementException("Policy not found"));
        doNothing().when(permMock)
                .checkPermission("user1", "storeZ", PermissionType.EDIT_STORE_POLICIES);

        // Act
        Response<PolicyDTO> resp = service.getPolicyById("goodToken", "storeZ", "noSuch");

        // Assert
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().toLowerCase().contains("not found"));
    }

    @Test
    public void deletePolicy_validInput_succeeds() {
        // Arrange: permission succeeds, facadeMock.removePolicy does not throw
        doNothing().when(permMock)
                .checkPermission("user1", "storeB", PermissionType.EDIT_STORE_POLICIES);
        doNothing().when(facadeMock).removePolicy("Pdelete");

        // Act
        Response<Boolean> resp = service.deletePolicy("goodToken", "storeB", "Pdelete");

        // Assert
        assertFalse("Response should not have an error", resp.errorOccurred());
        assertTrue("Response value should be true", resp.getValue());
        verify(facadeMock, times(1)).removePolicy("Pdelete");
    }

    @Test
    public void deletePolicy_noPermission_returnsErrorResponse() {
        // Arrange: permission manager throws SecurityException
        doThrow(new SecurityException("No rights"))
                .when(permMock).checkPermission("user1", "storeB", PermissionType.EDIT_STORE_POLICIES);

        // Act
        Response<Boolean> resp = service.deletePolicy("goodToken", "storeB", "Pdelete");

        // Assert
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().contains("No rights"));
    }

    @Test
    public void deletePolicy_invalidToken_returnsErrorResponse() {
        // Act
        Response<Boolean> resp = service.deletePolicy("badToken", "storeB", "Px");

        // Assert
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().toLowerCase().contains("invalid token"));
    }

    @Test
    public void createPolicy_maxQuantityAll_valid_succeeds() {
        // Arrange: build a Domain policy for DTO
        Policy pDomain = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("storeC")
                .storeId("storeC")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(4)
                .build();

        PolicyDTO dtoToSend = new PolicyDTO(pDomain);

        // When facadeMock.createMaxQuantityAllPolicy("storeC", 4) is called, return a new Domain policy
        Policy created = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("new-policy-id")
                .storeId("storeC")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(4)
                .build();

        when(facadeMock.createMaxQuantityAllPolicy("storeC", 4)).thenReturn(created);
        doNothing().when(permMock).checkPermission("user1", "storeC", PermissionType.EDIT_STORE_POLICIES);

        // Act
        Response<PolicyDTO> resp = service.createPolicy("goodToken", "storeC", dtoToSend);

        // Assert
        assertFalse("Response should not have an error", resp.errorOccurred());
        PolicyDTO returned = resp.getValue();
        assertEquals("PolicyDTO policyId should be new-policy-id", "new-policy-id", returned.getPolicyId());
        assertEquals("PolicyDTO storeId should be storeC", "storeC", returned.getStoreId());
        assertEquals("PolicyDTO type should be MAX_QUANTITY_ALL",
                     Policy.Type.MAX_QUANTITY_ALL, returned.getType());
    }

    @Test
    public void createPolicy_invalidPermission_returnsErrorResponse() {
        // Arrange: build a DTO for MAX_QUANTITY_ALL
        Policy pDom = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("storeD")
                .storeId("storeD")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(1)
                .build();
        PolicyDTO dto = new PolicyDTO(pDom);

        doThrow(new SecurityException("Forbidden"))
                .when(permMock).checkPermission("user1", "storeD", PermissionType.EDIT_STORE_POLICIES);

        // Act
        Response<PolicyDTO> resp = service.createPolicy("goodToken", "storeD", dto);

        // Assert
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().contains("Forbidden"));
    }

    @Test
    public void createPolicy_invalidToken_returnsErrorResponse() {
        // Arrange: build a DTO (details don’t matter since token is invalid)
        Policy pDom = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("storeX")
                .storeId("storeX")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(1)
                .build();
        PolicyDTO dto = new PolicyDTO(pDom);

        // Act
        Response<PolicyDTO> resp = service.createPolicy("badToken", "storeX", dto);

        // Assert
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().toLowerCase().contains("invalid token"));
    }

    @Test
    public void updatePolicy_valid_succeeds() {
        // Arrange: update an existing policy from maxItemsAll=2 to maxItemsAll=5
        PolicyDTO dtoOrig = new PolicyDTO(
                new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                    .policyId("storeU")
                    .storeId("storeU")
                    .productLookup(id -> null)
                    .itemLookup(id -> null)
                    .maxItemsAll(2)
                    .build()
        );

        PolicyDTO dtoNew = new PolicyDTO(
                new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                    .policyId("storeU")
                    .storeId("storeU")
                    .productLookup(id -> null)
                    .itemLookup(id -> null)
                    .maxItemsAll(5)
                    .build()
        );

        doNothing().when(facadeMock).removePolicy("storeU");

        Policy updatedDomain = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("storeU")
                .storeId("storeU")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(5)
                .build();
        when(facadeMock.createMaxQuantityAllPolicy("storeU", 5)).thenReturn(updatedDomain);

        doNothing().when(permMock).checkPermission("user1", "storeU", PermissionType.EDIT_STORE_POLICIES);
        when(facadeMock.getPolicy("storeU")).thenReturn(updatedDomain);

        // Act
        Response<PolicyDTO> resp = service.updatePolicy("goodToken", "storeU", "storeU", dtoNew);

        // Assert
        assertFalse("Response should not have an error", resp.errorOccurred());
        PolicyDTO ret = resp.getValue();
        assertEquals("maxItemsAll should have been updated to 5", Integer.valueOf(5), ret.getMaxItemsAll());
    }

    @Test
    public void updatePolicy_invalidPermission_returnsErrorResponse() {
        PolicyDTO dtoNew = new PolicyDTO(
                new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                    .policyId("storeU")
                    .storeId("storeU")
                    .productLookup(id -> null)
                    .itemLookup(id -> null)
                    .maxItemsAll(5)
                    .build()
        );

        doThrow(new SecurityException("NoRights"))
                .when(permMock).checkPermission("user1", "storeU", PermissionType.EDIT_STORE_POLICIES);

        // Act
        Response<PolicyDTO> resp = service.updatePolicy("goodToken", "storeU", "storeU", dtoNew);

        // Assert
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().contains("NoRights"));
    }

    @Test
    public void updatePolicy_invalidToken_returnsErrorResponse() {
        // Arrange: build any DTO (token is invalid, so no further behavior matters)
        PolicyDTO dto = new PolicyDTO(
                new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                    .policyId("storeX")
                    .storeId("storeX")
                    .productLookup(id -> null)
                    .itemLookup(id -> null)
                    .maxItemsAll(1)
                    .build()
        );

        // Act
        Response<PolicyDTO> resp = service.updatePolicy("badToken", "storeX", "storeX", dto);

        // Assert
        assertTrue("Response should indicate an error", resp.errorOccurred());
        assertTrue(resp.getErrorMessage().toLowerCase().contains("invalid token"));
    }
}
