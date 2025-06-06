package Domain.Store;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import Domain.Repos.IPolicyRepository;
import Domain.Repos.IUserRepository;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Policy;
import Domain.Store.ProductFacade;
import Domain.User.Member;
import Domain.management.PolicyFacade;

/**
 * Unit tests for Domain.management.PolicyFacade.
 * Verifies creation, retrieval, applicability, and removal of policies (no date logic).
 */
public class PolicyFacadeTest {

    private PolicyFacade       facade;
    private IPolicyRepository  repoMock;
    private IUserRepository    userRepoMock;
    private ItemFacade         itemFacadeMock;
    private ProductFacade      productFacadeMock;

    @Before
    public void setUp() {
        repoMock          = mock(IPolicyRepository.class);
        userRepoMock      = mock(IUserRepository.class);
        itemFacadeMock    = mock(ItemFacade.class);
        productFacadeMock = mock(ProductFacade.class);

        facade = new PolicyFacade(repoMock, userRepoMock, itemFacadeMock, productFacadeMock);
    }

    @Test
    public void createMinQuantityAllPolicy_validInput_succeedsAndStoresPolicy() {
        // Arrange: repoMock.add(...) returns true → policy was added successfully
        when(repoMock.add(anyString(), any(Policy.class))).thenReturn(true);

        // Act
        Policy p = facade.createMinQuantityAllPolicy("storeA", 3);

        // Assert
        assertNotNull("A new Policy should be returned", p);
        assertEquals("storeId should be storeA", "storeA", p.getStoreId());
        assertEquals("minItemsAll should be 3", 3, p.getMinItemsAll());
        verify(repoMock, times(1)).add(anyString(), any(Policy.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMinQuantityAllPolicy_invalidThreshold_throwsException() {
        // minItemsAll < 1 should cause IllegalArgumentException
        facade.createMinQuantityAllPolicy("storeA", 0);
    }

    @Test
    public void createMaxQuantityProductPolicy_validInput_succeedsAndStoresPolicy() {
        // Arrange: productId is non-empty, maxQuantity ≥ 1
        when(repoMock.add(anyString(), any(Policy.class))).thenReturn(true);

        // Act
        Policy p = facade.createMaxQuantityProductPolicy("storeA", "prodX", 5);

        // Assert
        assertNotNull("Policy should be created", p);
        assertEquals("storeId should be storeA", "storeA", p.getStoreId());
        assertEquals("targetProductId should be prodX", "prodX", p.getTargetProductId());
        assertEquals("maxItemsProduct should be 5", 5, p.getMaxItemsProduct());
        verify(repoMock, times(1)).add(anyString(), any(Policy.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMaxQuantityProductPolicy_emptyProductId_throwsException() {
        // Blank or null productId should cause IllegalArgumentException
        facade.createMaxQuantityProductPolicy("storeA", "   ", 2);
    }

    @Test
    public void getPolicy_existingId_returnsThatPolicy() {
        // Arrange: repoMock.get("p123") returns a simple MAX_QUANTITY_ALL policy
        Policy stored = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("p123")
                .storeId("storeZ")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(7)
                .build();

        when(repoMock.get("p123")).thenReturn(stored);

        // Act
        Policy actual = facade.getPolicy("p123");

        // Assert
        assertNotNull("Should retrieve the Policy from the repository", actual);
        assertEquals("policyId should be p123", "p123", actual.getPolicyId());
    }

    @Test(expected = NoSuchElementException.class)
    public void getPolicy_nonexistentId_throwsNoSuchElement() {
        // repoMock.get("badId") returns null → facade should throw NoSuchElementException
        when(repoMock.get("badId")).thenReturn(null);
        facade.getPolicy("badId");
    }

    @Test
    public void isApplicable_validInputsAndPolicyFound_returnsTrue() {
        // Arrange: build a simple MAX_QUANTITY_ALL policy
        Policy stored = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("pVal")
                .storeId("storeV")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(10)
                .build();

        when(repoMock.get("pVal")).thenReturn(stored);

        // Build basket and member mocks
        ShoppingBasket basketMock = mock(ShoppingBasket.class);
        Member memberMock       = mock(Member.class);

        when(basketMock.getOrders()).thenReturn(Map.of("x", 1));
        when(userRepoMock.getMember("mM")).thenReturn(memberMock);

        // Act
        boolean actual = facade.isApplicable("someBasketId", "mM", "pVal", basketMock);

        // Assert
        assertTrue("1 ≤ 10 → policy should be applicable", actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isApplicable_nullOrBlankIds_throwsIllegalArgument() {
        // Any null or blank basketId/memberId/policyId should trigger IllegalArgumentException
        facade.isApplicable(null, "m", "p", mock(ShoppingBasket.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void isApplicable_nonexistentPolicy_throwsNoSuchElement() {
        when(repoMock.get("nope")).thenReturn(null);
        facade.isApplicable("b1", "m1", "nope", mock(ShoppingBasket.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void isApplicable_nullBasket_throwsNoSuchElement() {
        // Policy exists, but null basket → NoSuchElementException
        Policy stored = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("p200")
                .storeId("storeX")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(2)
                .build();

        when(repoMock.get("p200")).thenReturn(stored);
        facade.isApplicable("b2", "m2", "p200", null);
    }

    @Test
    public void removePolicy_existingId_invokesRepository() {
        // Arrange: repoMock.remove("pDel") returns a policy
        when(repoMock.remove("pDel")).thenReturn(
            new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("pDel")
                .storeId("storeY")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(1)
                .build()
        );

        // Act
        facade.removePolicy("pDel");

        // Assert
        verify(repoMock, times(1)).remove("pDel");
    }
}
