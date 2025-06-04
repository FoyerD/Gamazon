package Domain.Store;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import Domain.Shopping.ShoppingBasket;
import Domain.User.Member;

/**
 * Unit tests for Domain.Store.Policy.
 * Verifies isApplicable() logic for several policy types, without any date checks.
 */
public class PolicyTest {

    private ShoppingBasket mockBasket;
    private Member         mockMember;

    @Before
    public void setUp() {
        mockBasket = mock(ShoppingBasket.class);
        mockMember = mock(Member.class);
    }

    @Test
    public void minQuantityAllPolicy_whenBasketBelowThreshold_isNotApplicable() {
        // Arrange: create a MIN_QUANTITY_ALL policy with minItemsAll = 5
        Policy policy = new Policy.Builder(Policy.Type.MIN_QUANTITY_ALL)
                .policyId("policy1")
                .storeId("storeA")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .minItemsAll(5)
                .build();

        // Basket has total quantity 3 for some product → should be not applicable
        when(mockBasket.getOrders()).thenReturn(Map.of("prodX", 3));

        // Act
        boolean result = policy.isApplicable(mockBasket, mockMember);

        // Assert
        assertFalse("MIN_QUANTITY_ALL with minItemsAll=5 should not apply when any quantity < 5", result);
    }

    @Test
    public void minQuantityAllPolicy_whenAllProductsMeetThreshold_isApplicable() {
        // Arrange: MIN_QUANTITY_ALL with threshold 5 → require each product quantity ≥ 5
        Policy policy = new Policy.Builder(Policy.Type.MIN_QUANTITY_ALL)
                .policyId("policy2")
                .storeId("storeA")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .minItemsAll(5)
                .build();

        // First simulate failing scenario
        when(mockBasket.getOrders()).thenReturn(Map.of("prodA", 2, "prodB", 3));
        boolean initial = policy.isApplicable(mockBasket, mockMember);
        assertFalse("2 < 5 or 3 < 5 → should not apply", initial);

        // Now simulate both quantities ≥ 5
        when(mockBasket.getOrders()).thenReturn(Map.of("prodA", 5, "prodB", 5));
        boolean result = policy.isApplicable(mockBasket, mockMember);
        assertTrue("MIN_QUANTITY_ALL should apply when all product quantities ≥ minItemsAll", result);
    }

    @Test
    public void maxQuantityAllPolicy_whenAnyProductExceedsThreshold_isNotApplicable() {
        // Arrange: MAX_QUANTITY_ALL with threshold 10, basket has quantities 12 and 8 → false
        Policy policy = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("policy3")
                .storeId("storeB")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(10)
                .build();

        when(mockBasket.getOrders()).thenReturn(Map.of("prodA", 12, "prodB", 8));
        boolean result = policy.isApplicable(mockBasket, mockMember);
        assertFalse("MAX_QUANTITY_ALL with maxItemsAll=10 should not apply when any quantity > 10", result);
    }

    @Test
    public void maxQuantityAllPolicy_whenAllProductsWithinThreshold_isApplicable() {
        // Arrange: MAX_QUANTITY_ALL with threshold 10, quantities 7 and 5 → true
        Policy policy = new Policy.Builder(Policy.Type.MAX_QUANTITY_ALL)
                .policyId("policy4")
                .storeId("storeB")
                .productLookup(id -> null)
                .itemLookup(id -> null)
                .maxItemsAll(10)
                .build();

        when(mockBasket.getOrders()).thenReturn(Map.of("prodA", 7, "prodB", 5));
        boolean result = policy.isApplicable(mockBasket, mockMember);
        assertTrue("MAX_QUANTITY_ALL should apply when all quantities ≤ maxItemsAll", result);
    }

}
