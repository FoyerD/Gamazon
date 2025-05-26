package Domain.Store.Discounts.Conditions;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.Set;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;

public class OrConditionTest {
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Condition condition1;
    
    @Mock
    private Condition condition2;
    
    @Mock
    private Condition condition3;
    
    private OrCondition orCondition;
    
    @Before
    public void setUp() {
        orCondition = new OrCondition(Set.of(condition1, condition2));
    }
    
    @Test
    public void testIsSatisfiedWhenAllConditionsTrue() {
        when(condition1.isSatisfied(basket)).thenReturn(true);
        when(condition2.isSatisfied(basket)).thenReturn(true);
        
        assertTrue(orCondition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenOneConditionTrue() {
        when(condition1.isSatisfied(basket)).thenReturn(true);
        when(condition2.isSatisfied(basket)).thenReturn(false);
        
        assertTrue(orCondition.isSatisfied(basket));
        
        // Test the other way around
        when(condition1.isSatisfied(basket)).thenReturn(false);
        when(condition2.isSatisfied(basket)).thenReturn(true);
        
        assertTrue(orCondition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenAllConditionsFalse() {
        when(condition1.isSatisfied(basket)).thenReturn(false);
        when(condition2.isSatisfied(basket)).thenReturn(false);
        
        assertFalse(orCondition.isSatisfied(basket));
    }
    
    @Test
    public void testConstructorWithTwoConditions() {
        OrCondition condition = new OrCondition(condition1, condition2);
        assertNotNull(condition.getId());
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        OrCondition condition = new OrCondition(existingId, Set.of(condition1, condition2));
        
        assertEquals(existingId, condition.getId());
    }
    
    @Test
    public void testWithThreeConditions() {
        OrCondition threeConditions = new OrCondition(Set.of(condition1, condition2, condition3));
        
        when(condition1.isSatisfied(basket)).thenReturn(false);
        when(condition2.isSatisfied(basket)).thenReturn(false);
        when(condition3.isSatisfied(basket)).thenReturn(true);
        
        assertTrue(threeConditions.isSatisfied(basket));
    }
    
    @Test
    public void testEmptyConditionSet() {
        OrCondition emptyCondition = new OrCondition(Set.of());
        assertFalse(emptyCondition.isSatisfied(basket)); // No conditions to satisfy
    }
    
    @Test
    public void testHasUniqueId() {
        assertNotNull(orCondition.getId());
        
        OrCondition anotherCondition = new OrCondition(Set.of(condition1, condition2));
        assertNotEquals(orCondition.getId(), anotherCondition.getId());
    }
}