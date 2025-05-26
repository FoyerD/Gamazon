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

public class AndConditionTest {
    
    @Mock
    private ShoppingBasket basket;
    
    @Mock
    private Condition condition1;
    
    @Mock
    private Condition condition2;
    
    @Mock
    private Condition condition3;
    
    private AndCondition andCondition;
    
    @Before
    public void setUp() {
        andCondition = new AndCondition(Set.of(condition1, condition2));
    }
    
    @Test
    public void testIsSatisfiedWhenAllConditionsTrue() {
        when(condition1.isSatisfied(basket)).thenReturn(true);
        when(condition2.isSatisfied(basket)).thenReturn(true);
        
        assertTrue(andCondition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenOneConditionFalse() {
        when(condition1.isSatisfied(basket)).thenReturn(true);
        when(condition2.isSatisfied(basket)).thenReturn(false);
        
        assertFalse(andCondition.isSatisfied(basket));
    }
    
    @Test
    public void testIsSatisfiedWhenAllConditionsFalse() {
        when(condition1.isSatisfied(basket)).thenReturn(false);
        when(condition2.isSatisfied(basket)).thenReturn(false);
        
        assertFalse(andCondition.isSatisfied(basket));
    }
    
    @Test
    public void testConstructorWithTwoConditions() {
        AndCondition condition = new AndCondition(condition1, condition2);
        assertNotNull(condition.getId());
    }
    
    @Test
    public void testConstructorWithExistingUUID() {
        UUID existingId = UUID.randomUUID();
        AndCondition condition = new AndCondition(existingId, Set.of(condition1, condition2));
        
        assertEquals(existingId, condition.getId());
    }
    
    @Test
    public void testWithThreeConditions() {
        AndCondition threeConditions = new AndCondition(Set.of(condition1, condition2, condition3));
        
        when(condition1.isSatisfied(basket)).thenReturn(true);
        when(condition2.isSatisfied(basket)).thenReturn(true);
        when(condition3.isSatisfied(basket)).thenReturn(true);
        
        assertTrue(threeConditions.isSatisfied(basket));
        
        // If any one is false, result should be false
        when(condition2.isSatisfied(basket)).thenReturn(false);
        assertFalse(threeConditions.isSatisfied(basket));
    }
    
    @Test
    public void testEmptyConditionSet() {
        AndCondition emptyCondition = new AndCondition(Set.of());
        assertTrue(emptyCondition.isSatisfied(basket)); // Vacuous truth
    }
    
    @Test
    public void testHasUniqueId() {
        assertNotNull(andCondition.getId());
        
        AndCondition anotherCondition = new AndCondition(Set.of(condition1, condition2));
        assertNotEquals(andCondition.getId(), anotherCondition.getId());
    }
}