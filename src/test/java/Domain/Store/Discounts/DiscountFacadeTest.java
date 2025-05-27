package Domain.Store.Discounts;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.*;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class DiscountFacadeTest {

    @Mock
    private IDiscountRepository mockDiscountRepository;
    
    @Mock
    private IConditionRepository mockConditionRepository;
    
    @Mock
    private ItemFacade mockItemFacade;
    
    @Mock
    private DiscountQualifier mockQualifier;
    
    @Mock
    private Condition mockCondition;
    
    @Mock
    private Discount mockDiscount1;
    
    @Mock
    private Discount mockDiscount2;

    private DiscountFacade discountFacade;
    
    // Store created objects for invariant checking
    private Object lastCreatedObject;
    private String testStoreId = "testStore123";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        discountFacade = new DiscountFacade(mockDiscountRepository, mockConditionRepository);
        lastCreatedObject = null;
    }
    
    @After
    public void checkInvariants() {
        if (lastCreatedObject != null) {
            checkObjectInvariants(lastCreatedObject);
        }
        checkRepositoryInvariants();
        checkSystemInvariants();
    }
    
    /**
     * Checks invariants related to repository state and consistency
     */
    private void checkRepositoryInvariants() {
        // Repository state invariants
        checkRepositoryStateInvariants();
        
        // Cross-repository consistency
        checkRepositoryConsistency();
    }
    
    /**
     * Checks repository state invariants
     */
    private void checkRepositoryStateInvariants() {
        // Verify repositories are not null
        assertNotNull("Discount repository should not be null", mockDiscountRepository);
        assertNotNull("Condition repository should not be null", mockConditionRepository);
        
        // Check that facade maintains repository references
        assertNotNull("DiscountFacade should maintain repository references", discountFacade);
    }
    
    /**
     * Checks consistency between repositories
     */
    private void checkRepositoryConsistency() {
        // For creation tests, verify save was called exactly once for the created object with store ID
        if (lastCreatedObject != null) {
            if (lastCreatedObject instanceof Condition) {
                // Condition should be saved exactly once with store ID
                verify(mockConditionRepository, times(1)).save(eq(testStoreId), eq((Condition) lastCreatedObject));
                // Should not be saved to discount repository
                verify(mockDiscountRepository, never()).save(anyString(), any(Discount.class));
            } else if (lastCreatedObject instanceof Discount) {
                // Discount should be saved exactly once with store ID
                verify(mockDiscountRepository, times(1)).save(eq(testStoreId), eq((Discount) lastCreatedObject));
                // Should not be saved to condition repository
                verify(mockConditionRepository, never()).save(anyString(), any(Condition.class));
            }
        }
    }
    
    /**
     * Checks system-wide invariants
     */
    private void checkSystemInvariants() {
        checkFacadeInvariants();
        checkBusinessRuleInvariants();
    }
    
    /**
     * Checks facade-specific invariants
     */
    private void checkFacadeInvariants() {
        // Facade should always be in a consistent state
        assertNotNull("DiscountFacade should not be null", discountFacade);
        
        // Facade should not create objects without saving them
        // (This is implicitly checked by repository verification above)
    }
    
    /**
     * Checks business rule invariants across the system
     */
    private void checkBusinessRuleInvariants() {
        if (lastCreatedObject instanceof CompositeDiscount) {
            CompositeDiscount composite = (CompositeDiscount) lastCreatedObject;
            
            // Business rule: Composite discounts should have consistent sub-discount conditions
            for (Discount subDiscount : composite.getDiscounts()) {
                assertNotNull("Sub-discounts should have conditions", subDiscount.getCondition());
                
                // Business rule: All sub-discounts should be properly formed
                if (subDiscount instanceof SimpleDiscount) {
                    SimpleDiscount simple = (SimpleDiscount) subDiscount;
                    assertTrue("Sub-discount percentage should be valid", 
                              simple.getDiscountPercentage() >= 0 && simple.getDiscountPercentage() <= 1);
                }
            }
        }
        
        if (lastCreatedObject instanceof CompositeCondition) {
            CompositeCondition composite = (CompositeCondition) lastCreatedObject;
            
            // Business rule: Composite conditions should not have circular references
            checkNoCircularReferences(composite);
        }
    }
    
    /**
     * Checks for circular references in composite conditions
     */
    private void checkNoCircularReferences(CompositeCondition composite) {
        Set<UUID> visitedIds = new HashSet<>();
        checkCircularHelper(composite, visitedIds);
    }
    
    private void checkCircularHelper(Condition condition, Set<UUID> visitedIds) {
        UUID conditionId = condition.getId();
        
        assertFalse("Circular reference detected in condition: " + conditionId, 
                   visitedIds.contains(conditionId));
        
        visitedIds.add(conditionId);
        
        if (condition instanceof CompositeCondition) {
            CompositeCondition composite = (CompositeCondition) condition;
            for (Condition subCondition : composite.getConditions()) {
                checkCircularHelper(subCondition, new HashSet<>(visitedIds));
            }
        }
    }
    
    /**
     * Checks invariants specific to Discount objects
     */
    private void checkDiscountInvariants(Discount discount) {
        assertNotNull("Discount ID should not be null", discount.getId());
        assertNotNull("Condition should not be null", discount.getCondition());
        
        if (discount instanceof SimpleDiscount) {
            SimpleDiscount simple = (SimpleDiscount) discount;
            assertTrue("Discount percentage should be between 0 and 1", 
                      simple.getDiscountPercentage() >= 0 && simple.getDiscountPercentage() <= 1);
            assertNotNull("Qualifier should not be null", simple.getQualifier());
        } else if (discount instanceof CompositeDiscount) {
            CompositeDiscount composite = (CompositeDiscount) discount;
            assertNotNull("Discounts set should not be null", composite.getDiscounts());
            assertFalse("Discounts set should not be empty", composite.getDiscounts().isEmpty());
            for (Discount subDiscount : composite.getDiscounts()) {
                assertNotNull("Each sub-discount should not be null", subDiscount);
            }
            
            // Check specific condition types for different discount types
            if (discount instanceof AndDiscount) {
                assertTrue("AndDiscount should have AndCondition", 
                          discount.getCondition() instanceof AndCondition);
            } else if (discount instanceof OrDiscount) {
                assertTrue("OrDiscount should have OrCondition", 
                          discount.getCondition() instanceof OrCondition);
            } else if (discount instanceof DoubleDiscount || discount instanceof MaxDiscount) {
                assertTrue("DoubleDiscount/MaxDiscount should have TrueCondition", 
                          discount.getCondition() instanceof TrueCondition);
            }
        }
    }

    /**
     * Checks common invariants for all created objects
     */
    private void checkObjectInvariants(Object obj) {
        assertNotNull("Created object should not be null", obj);
        
        if (obj instanceof Condition) {
            checkConditionInvariants((Condition) obj);
        } else if (obj instanceof Discount) {
            checkDiscountInvariants((Discount) obj);
        }
    }
    
    /**
     * Checks invariants specific to Condition objects
     */
    private void checkConditionInvariants(Condition condition) {
        assertNotNull("Condition ID should not be null", condition.getId());
        
        if (condition instanceof MinPriceCondition) {
            MinPriceCondition minPrice = (MinPriceCondition) condition;
            assertTrue("Min price should be non-negative", minPrice.getMinPrice() >= 0);
        } else if (condition instanceof MaxPriceCondition) {
            MaxPriceCondition maxPrice = (MaxPriceCondition) condition;
            assertTrue("Max price should be non-negative", maxPrice.getMaxPrice() >= 0);
        } else if (condition instanceof MinQuantityCondition) {
            MinQuantityCondition minQty = (MinQuantityCondition) condition;
            assertNotNull("Product ID should not be null", minQty.getProductId());
            assertFalse("Product ID should not be empty", minQty.getProductId().trim().isEmpty());
            assertTrue("Min quantity should be non-negative", minQty.getMinQuantity() >= 0);
        } else if (condition instanceof MaxQuantityCondition) {
            MaxQuantityCondition maxQty = (MaxQuantityCondition) condition;
            assertNotNull("Product ID should not be null", maxQty.getProductId());
            assertFalse("Product ID should not be empty", maxQty.getProductId().trim().isEmpty());
            assertTrue("Max quantity should be non-negative", maxQty.getMaxQuantity() >= 0);
        } else if (condition instanceof CompositeCondition) {
            CompositeCondition composite = (CompositeCondition) condition;
            assertNotNull("Conditions set should not be null", composite.getConditions());
            assertFalse("Conditions set should not be empty", composite.getConditions().isEmpty());
            for (Condition subCondition : composite.getConditions()) {
                assertNotNull("Each sub-condition should not be null", subCondition);
            }
        }
    }

    // ===========================================
    // CONSTRUCTOR TESTS
    // ===========================================

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDiscountRepository() {
        new DiscountFacade(null, mockConditionRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullConditionRepository() {
        new DiscountFacade(mockDiscountRepository, null);
    }

    @Test
    public void testConstructorWithValidRepositories() {
        DiscountFacade facade = new DiscountFacade(mockDiscountRepository, mockConditionRepository);
        assertNotNull("DiscountFacade should be created successfully", facade);
    }

    // ===========================================
    // STORE ID VALIDATION TESTS
    // ===========================================

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMinPriceConditionWithNullStoreId() {
        discountFacade.createMinPriceCondition(null, mockItemFacade, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMinPriceConditionWithEmptyStoreId() {
        discountFacade.createMinPriceCondition("   ", mockItemFacade, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSimpleDiscountWithNullStoreId() {
        discountFacade.createSimpleDiscount(null, mockItemFacade, 0.2f, mockQualifier, mockCondition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSimpleDiscountWithEmptyStoreId() {
        discountFacade.createSimpleDiscount("", mockItemFacade, 0.2f, mockQualifier, mockCondition);
    }

    // ===========================================
    // CONDITION CREATION TESTS
    // ===========================================

    @Test
    public void testCreateMinPriceCondition() {
        MinPriceCondition condition = discountFacade.createMinPriceCondition(testStoreId, mockItemFacade, 100.0);
        
        assertNotNull("MinPriceCondition should be created", condition);
        assertEquals("Min price should be set correctly", 100.0, condition.getMinPrice(), 0.001);
        verify(mockConditionRepository).save(eq(testStoreId), eq(condition));
        
        lastCreatedObject = condition;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMinPriceConditionWithNullItemFacade() {
        discountFacade.createMinPriceCondition(testStoreId, null, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMinPriceConditionWithNegativePrice() {
        discountFacade.createMinPriceCondition(testStoreId, mockItemFacade, -50.0);
    }

    @Test
    public void testCreateMaxPriceCondition() {
        MaxPriceCondition condition = discountFacade.createMaxPriceCondition(testStoreId, mockItemFacade, 200.0);
        
        assertNotNull("MaxPriceCondition should be created", condition);
        assertEquals("Max price should be set correctly", 200.0, condition.getMaxPrice(), 0.001);
        verify(mockConditionRepository).save(eq(testStoreId), eq(condition));
        
        lastCreatedObject = condition;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMaxPriceConditionWithNullItemFacade() {
        discountFacade.createMaxPriceCondition(testStoreId, null, 200.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMaxPriceConditionWithNegativePrice() {
        discountFacade.createMaxPriceCondition(testStoreId, mockItemFacade, -100.0);
    }

    @Test
    public void testCreateMinQuantityCondition() {
        MinQuantityCondition condition = discountFacade.createMinQuantityCondition(testStoreId, mockItemFacade, "product1", 5);
        
        assertNotNull("MinQuantityCondition should be created", condition);
        assertEquals("Product ID should be set correctly", "product1", condition.getProductId());
        assertEquals("Min quantity should be set correctly", 5, condition.getMinQuantity());
        verify(mockConditionRepository).save(eq(testStoreId), eq(condition));
        
        lastCreatedObject = condition;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMinQuantityConditionWithNullProductId() {
        discountFacade.createMinQuantityCondition(testStoreId, mockItemFacade, null, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMinQuantityConditionWithEmptyProductId() {
        discountFacade.createMinQuantityCondition(testStoreId, mockItemFacade, "   ", 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMinQuantityConditionWithNegativeQuantity() {
        discountFacade.createMinQuantityCondition(testStoreId, mockItemFacade, "product1", -3);
    }

    @Test
    public void testCreateMaxQuantityCondition() {
        MaxQuantityCondition condition = discountFacade.createMaxQuantityCondition(testStoreId, mockItemFacade, "product2", 10);
        
        assertNotNull("MaxQuantityCondition should be created", condition);
        assertEquals("Product ID should be set correctly", "product2", condition.getProductId());
        assertEquals("Max quantity should be set correctly", 10, condition.getMaxQuantity());
        verify(mockConditionRepository).save(eq(testStoreId), eq(condition));
        
        lastCreatedObject = condition;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMaxQuantityConditionWithNullItemFacade() {
        discountFacade.createMaxQuantityCondition(testStoreId, null, "product2", 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMaxQuantityConditionWithNegativeQuantity() {
        discountFacade.createMaxQuantityCondition(testStoreId, mockItemFacade, "product2", -5);
    }

    @Test
    public void testCreateAndConditionWithSet() {
        Set<Condition> conditions = new HashSet<>();
        conditions.add(mockCondition);
        
        AndCondition condition = discountFacade.createAndCondition(testStoreId, conditions);
        
        assertNotNull("AndCondition should be created", condition);
        verify(mockConditionRepository).save(eq(testStoreId), eq(condition));
        
        lastCreatedObject = condition;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAndConditionWithNullSet() {
        discountFacade.createAndCondition(testStoreId, (Set<Condition>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAndConditionWithEmptySet() {
        discountFacade.createAndCondition(testStoreId, new HashSet<>());
    }

    @Test
    public void testCreateAndConditionWithTwoConditions() {
        Condition condition1 = mock(Condition.class);
        Condition condition2 = mock(Condition.class);
        
        AndCondition condition = discountFacade.createAndCondition(testStoreId, condition1, condition2);
        
        assertNotNull("AndCondition should be created", condition);
        verify(mockConditionRepository).save(eq(testStoreId), eq(condition));
        
        lastCreatedObject = condition;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAndConditionWithNullCondition1() {
        discountFacade.createAndCondition(testStoreId, null, mockCondition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAndConditionWithNullCondition2() {
        discountFacade.createAndCondition(testStoreId, mockCondition, null);
    }

    @Test
    public void testCreateOrConditionWithSet() {
        Set<Condition> conditions = new HashSet<>();
        conditions.add(mockCondition);
        
        OrCondition condition = discountFacade.createOrCondition(testStoreId, conditions);
        
        assertNotNull("OrCondition should be created", condition);
        verify(mockConditionRepository).save(eq(testStoreId), eq(condition));
        
        lastCreatedObject = condition;
    }

    @Test
    public void testCreateOrConditionWithTwoConditions() {
        Condition condition1 = mock(Condition.class);
        Condition condition2 = mock(Condition.class);
        
        OrCondition condition = discountFacade.createOrCondition(testStoreId, condition1, condition2);
        
        assertNotNull("OrCondition should be created", condition);
        verify(mockConditionRepository).save(eq(testStoreId), eq(condition));
        
        lastCreatedObject = condition;
    }

    @Test
    public void testCreateTrueCondition() {
        TrueCondition condition = discountFacade.createTrueCondition(testStoreId);
        
        assertNotNull("TrueCondition should be created", condition);
        verify(mockConditionRepository).save(eq(testStoreId), eq(condition));
        
        lastCreatedObject = condition;
    }

    // ===========================================
    // DISCOUNT CREATION TESTS
    // ===========================================

    @Test
    public void testCreateSimpleDiscountWithCondition() {
        SimpleDiscount discount = discountFacade.createSimpleDiscount(testStoreId, mockItemFacade, 0.2f, mockQualifier, mockCondition);
        
        assertNotNull("SimpleDiscount should be created", discount);
        assertEquals("Discount percentage should be set correctly", 0.2, discount.getDiscountPercentage(), 0.001);
        assertEquals("Qualifier should be set correctly", mockQualifier, discount.getQualifier());
        verify(mockDiscountRepository).save(eq(testStoreId), eq(discount));
        
        lastCreatedObject = discount;
    }

    @Test
    public void testCreateSimpleDiscountWithoutCondition() {
        SimpleDiscount discount = discountFacade.createSimpleDiscount(testStoreId, mockItemFacade, 0.15f, mockQualifier);
        
        assertNotNull("SimpleDiscount should be created", discount);
        assertEquals("Discount percentage should be set correctly", 0.15, discount.getDiscountPercentage(), 0.001);
        assertEquals("Qualifier should be set correctly", mockQualifier, discount.getQualifier());
        verify(mockDiscountRepository).save(eq(testStoreId), eq(discount));
        
        lastCreatedObject = discount;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSimpleDiscountWithNullItemFacade() {
        discountFacade.createSimpleDiscount(testStoreId, null, 0.2f, mockQualifier, mockCondition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSimpleDiscountWithInvalidPercentageNegative() {
        discountFacade.createSimpleDiscount(testStoreId, mockItemFacade, -0.1f, mockQualifier, mockCondition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSimpleDiscountWithInvalidPercentageAboveOne() {
        discountFacade.createSimpleDiscount(testStoreId, mockItemFacade, 1.5f, mockQualifier, mockCondition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSimpleDiscountWithNullQualifier() {
        discountFacade.createSimpleDiscount(testStoreId, mockItemFacade, 0.2f, null, mockCondition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSimpleDiscountWithNullCondition() {
        discountFacade.createSimpleDiscount(testStoreId, mockItemFacade, 0.2f, mockQualifier, null);
    }

    @Test
    public void testCreateAndDiscountWithSet() {
        Set<Discount> discounts = new HashSet<>();
        discounts.add(mockDiscount1);
        discounts.add(mockDiscount2);
        
        when(mockDiscount1.getCondition()).thenReturn(mockCondition);
        when(mockDiscount2.getCondition()).thenReturn(mockCondition);
        
        AndDiscount discount = discountFacade.createAndDiscount(testStoreId, mockItemFacade, discounts);
        
        assertNotNull("AndDiscount should be created", discount);
        verify(mockDiscountRepository).save(eq(testStoreId), eq(discount));
        
        lastCreatedObject = discount;
    }

    @Test
    public void testCreateAndDiscountWithTwoDiscounts() {
        Condition condition1 = mock(Condition.class);
        Condition condition2 = mock(Condition.class);
        
        when(mockDiscount1.getCondition()).thenReturn(condition1);
        when(mockDiscount2.getCondition()).thenReturn(condition2);
        
        AndDiscount discount = discountFacade.createAndDiscount(testStoreId, mockItemFacade, mockDiscount1, mockDiscount2);
        
        assertNotNull("AndDiscount should be created", discount);
        verify(mockDiscountRepository).save(eq(testStoreId), eq(discount));
        
        lastCreatedObject = discount;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAndDiscountWithNullDiscountSet() {
        discountFacade.createAndDiscount(testStoreId, mockItemFacade, (Set<Discount>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAndDiscountWithEmptyDiscountSet() {
        discountFacade.createAndDiscount(testStoreId, mockItemFacade, new HashSet<>());
    }

    @Test
    public void testCreateOrDiscount() {
        Set<Condition> conditions = new HashSet<>();
        conditions.add(mockCondition);
        
        // Mock the condition for mockDiscount1
        when(mockDiscount1.getCondition()).thenReturn(mockCondition);
        
        OrDiscount discount = discountFacade.createOrDiscount(testStoreId, mockItemFacade, mockDiscount1, conditions);
        
        assertNotNull("OrDiscount should be created", discount);
        verify(mockDiscountRepository).save(eq(testStoreId), eq(discount));
        
        lastCreatedObject = discount;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOrDiscountWithNullDiscount() {
        Set<Condition> conditions = new HashSet<>();
        conditions.add(mockCondition);
        
        discountFacade.createOrDiscount(testStoreId, mockItemFacade, null, conditions);
    }

    @Test
    public void testCreateXorDiscount() {
        // Mock conditions for both discounts
        when(mockDiscount1.getCondition()).thenReturn(mockCondition);
        when(mockDiscount2.getCondition()).thenReturn(mockCondition);
        
        XorDiscount discount = discountFacade.createXorDiscount(testStoreId, mockItemFacade, mockDiscount1, mockDiscount2);
        
        assertNotNull("XorDiscount should be created", discount);
        verify(mockDiscountRepository).save(eq(testStoreId), eq(discount));
        
        lastCreatedObject = discount;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateXorDiscountWithNullDiscount1() {
        discountFacade.createXorDiscount(testStoreId, mockItemFacade, null, mockDiscount2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateXorDiscountWithNullDiscount2() {
        discountFacade.createXorDiscount(testStoreId, mockItemFacade, mockDiscount1, null);
    }

    @Test
    public void testCreateDoubleDiscount() {
        Set<Discount> discounts = new HashSet<>();
        discounts.add(mockDiscount1);
        discounts.add(mockDiscount2);
        
        // Mock conditions for both discounts
        when(mockDiscount1.getCondition()).thenReturn(mockCondition);
        when(mockDiscount2.getCondition()).thenReturn(mockCondition);
        
        DoubleDiscount discount = discountFacade.createDoubleDiscount(testStoreId, mockItemFacade, discounts);
        
        assertNotNull("DoubleDiscount should be created", discount);
        verify(mockDiscountRepository).save(eq(testStoreId), eq(discount));
        
        lastCreatedObject = discount;
    }

    @Test
    public void testCreateMaxDiscountWithSet() {
        Set<Discount> discounts = new HashSet<>();
        discounts.add(mockDiscount1);
        discounts.add(mockDiscount2);
        
        // Mock conditions for both discounts
        when(mockDiscount1.getCondition()).thenReturn(mockCondition);
        when(mockDiscount2.getCondition()).thenReturn(mockCondition);
        
        MaxDiscount discount = discountFacade.createMaxDiscount(testStoreId, mockItemFacade, discounts);
        
        assertNotNull("MaxDiscount should be created", discount);
        verify(mockDiscountRepository).save(eq(testStoreId), eq(discount));
        
        lastCreatedObject = discount;
    }

    @Test
    public void testCreateMaxDiscountWithTwoDiscounts() {
        // Mock conditions for both discounts
        when(mockDiscount1.getCondition()).thenReturn(mockCondition);
        when(mockDiscount2.getCondition()).thenReturn(mockCondition);
        
        MaxDiscount discount = discountFacade.createMaxDiscount(testStoreId, mockItemFacade, mockDiscount1, mockDiscount2);
        
        assertNotNull("MaxDiscount should be created", discount);
        verify(mockDiscountRepository).save(eq(testStoreId), eq(discount));
        
        lastCreatedObject = discount;
    }

    // ===========================================
    // STORE-SPECIFIC REPOSITORY OPERATION TESTS
    // ===========================================

    @Test
    public void testAddDiscount() {
        discountFacade.addDiscount(testStoreId, mockDiscount1);
        
        verify(mockDiscountRepository).save(eq(testStoreId), eq(mockDiscount1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDiscountWithNullStoreId() {
        discountFacade.addDiscount(null, mockDiscount1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDiscountWithNullDiscount() {
        discountFacade.addDiscount(testStoreId, null);
    }

    @Test
    public void testAddCondition() {
        discountFacade.addCondition(testStoreId, mockCondition);
        
        verify(mockConditionRepository).save(eq(testStoreId), eq(mockCondition));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddConditionWithNullStoreId() {
        discountFacade.addCondition(null, mockCondition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddConditionWithNullCondition() {
        discountFacade.addCondition(testStoreId, null);
    }

    @Test
    public void testGetStoreDiscounts() {
        Set<Discount> expectedDiscounts = new HashSet<>();
        expectedDiscounts.add(mockDiscount1);
        expectedDiscounts.add(mockDiscount2);
        
        when(mockDiscountRepository.getStoreDiscounts(testStoreId)).thenReturn(expectedDiscounts);
        
        Set<Discount> result = discountFacade.getStoreDiscounts(testStoreId);
        
        assertEquals("Should return store discounts", expectedDiscounts, result);
        verify(mockDiscountRepository).getStoreDiscounts(testStoreId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStoreDiscountsWithNullStoreId() {
        discountFacade.getStoreDiscounts(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStoreDiscountsWithEmptyStoreId() {
        discountFacade.getStoreDiscounts("   ");
    }

    @Test
    public void testGetStoreDiscountsList() {
        List<Discount> expectedDiscounts = Arrays.asList(mockDiscount1, mockDiscount2);
        
        when(mockDiscountRepository.findByStoreId(testStoreId)).thenReturn(expectedDiscounts);
        
        List<Discount> result = discountFacade.getStoreDiscountsList(testStoreId);
        
        assertEquals("Should return store discounts as list", expectedDiscounts, result);
        verify(mockDiscountRepository).findByStoreId(testStoreId);
    }

    @Test
    public void testUpdateDiscount() {
        UUID discountId = UUID.randomUUID();
        when(mockDiscount1.getId()).thenReturn(discountId);
        when(mockDiscountRepository.existsById(discountId)).thenReturn(true);
        
        discountFacade.updateDiscount(testStoreId, mockDiscount1);
        
        verify(mockDiscountRepository).existsById(discountId);
        verify(mockDiscountRepository).save(eq(testStoreId), eq(mockDiscount1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDiscountWithNonExistentId() {
        UUID discountId = UUID.randomUUID();
        when(mockDiscount1.getId()).thenReturn(discountId);
        when(mockDiscountRepository.existsById(discountId)).thenReturn(false);
        
        discountFacade.updateDiscount(testStoreId, mockDiscount1);
    }

    @Test
    public void testRemoveDiscountFromStore() {
        UUID discountId = UUID.randomUUID();
        Set<Discount> storeDiscounts = new HashSet<>();
        storeDiscounts.add(mockDiscount1);
        
        when(mockDiscount1.getId()).thenReturn(discountId);
        when(mockDiscountRepository.getStoreDiscounts(testStoreId)).thenReturn(storeDiscounts);
        
        discountFacade.removeDiscount(testStoreId, discountId);
        
        verify(mockDiscountRepository).getStoreDiscounts(testStoreId);
        verify(mockDiscountRepository).deleteById(discountId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDiscountNotInStore() {
        UUID discountId = UUID.randomUUID();
        UUID otherDiscountId = UUID.randomUUID();
        Set<Discount> storeDiscounts = new HashSet<>();
        storeDiscounts.add(mockDiscount1);
        
        when(mockDiscount1.getId()).thenReturn(otherDiscountId);
        when(mockDiscountRepository.getStoreDiscounts(testStoreId)).thenReturn(storeDiscounts);
        
        discountFacade.removeDiscount(testStoreId, discountId);
    }

    @Test
    public void testGetStoreDiscountCount() {
        Set<Discount> storeDiscounts = new HashSet<>();
        storeDiscounts.add(mockDiscount1);
        storeDiscounts.add(mockDiscount2);
        
        when(mockDiscountRepository.getStoreDiscounts(testStoreId)).thenReturn(storeDiscounts);
        
        int result = discountFacade.getStoreDiscountCount(testStoreId);
        
        assertEquals("Should return correct discount count", 2, result);
        verify(mockDiscountRepository).getStoreDiscounts(testStoreId);
    }

    // ===========================================
    // GLOBAL REPOSITORY OPERATION TESTS
    // ===========================================

    @Test
    public void testFindDiscountById() {
        UUID discountId = UUID.randomUUID();
        
        when(mockDiscountRepository.findById(discountId)).thenReturn(mockDiscount1);
        
        Discount result = discountFacade.findDiscountById(discountId);
        
        assertEquals("Should return the discount from repository", mockDiscount1, result);
        verify(mockDiscountRepository).findById(discountId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindDiscountByIdWithNullId() {
        discountFacade.findDiscountById(null);
    }

    @Test
    public void testGetAllDiscounts() {
        Map<UUID, Discount> expectedDiscounts = new HashMap<>();
        expectedDiscounts.put(UUID.randomUUID(), mockDiscount1);
        expectedDiscounts.put(UUID.randomUUID(), mockDiscount2);
        
        when(mockDiscountRepository.findAll()).thenReturn(expectedDiscounts);
        
        Map<UUID, Discount> result = discountFacade.getAllDiscounts();
        
        assertEquals("Should return all discounts from repository", expectedDiscounts, result);
        verify(mockDiscountRepository).findAll();
    }

    @Test
    public void testRemoveDiscountGlobal() {
        UUID discountId = UUID.randomUUID();
        
        discountFacade.removeDiscount(discountId);
        
        verify(mockDiscountRepository).deleteById(discountId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDiscountGlobalWithNullId() {
        discountFacade.removeDiscount(null);
    }

    @Test
    public void testDiscountExists() {
        UUID discountId = UUID.randomUUID();
        
        when(mockDiscountRepository.existsById(discountId)).thenReturn(true);
        
        boolean result = discountFacade.discountExists(discountId);
        
        assertTrue("Should return true when discount exists", result);
        verify(mockDiscountRepository).existsById(discountId);
    }

    @Test
    public void testGetDiscountCount() {
        when(mockDiscountRepository.size()).thenReturn(5);
        
        int result = discountFacade.getDiscountCount();
        
        assertEquals("Should return correct discount count", 5, result);
        verify(mockDiscountRepository).size();
    }

    @Test
    public void testFindConditionById() {
        UUID conditionId = UUID.randomUUID();
        
        when(mockConditionRepository.findById(conditionId)).thenReturn(mockCondition);
        
        Condition result = discountFacade.findConditionById(conditionId);
        
        assertEquals("Should return the condition from repository", mockCondition, result);
        verify(mockConditionRepository).findById(conditionId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindConditionByIdWithNullId() {
        discountFacade.findConditionById(null);
    }

    @Test
    public void testGetAllConditions() {
        Map<UUID, Condition> expectedConditions = new HashMap<>();
        expectedConditions.put(UUID.randomUUID(), mockCondition);
        
        when(mockConditionRepository.findAll()).thenReturn(expectedConditions);
        
        Map<UUID, Condition> result = discountFacade.getAllConditions();
        
        assertEquals("Should return all conditions from repository", expectedConditions, result);
        verify(mockConditionRepository).findAll();
    }

    @Test
    public void testRemoveConditionGlobal() {
        UUID conditionId = UUID.randomUUID();
        
        discountFacade.removeCondition(conditionId);
        
        verify(mockConditionRepository).deleteById(conditionId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveConditionGlobalWithNullId() {
        discountFacade.removeCondition(null);
    }

    @Test
    public void testConditionExists() {
        UUID conditionId = UUID.randomUUID();
        
        when(mockConditionRepository.existsById(conditionId)).thenReturn(false);
        
        boolean result = discountFacade.conditionExists(conditionId);
        
        assertFalse("Should return false when condition doesn't exist", result);
        verify(mockConditionRepository).existsById(conditionId);
    }

    @Test
    public void testGetConditionCount() {
        when(mockConditionRepository.size()).thenReturn(3);
        
        int result = discountFacade.getConditionCount();
        
        assertEquals("Should return correct condition count", 3, result);
        verify(mockConditionRepository).size();
    }

    @Test
    public void testClearAll() {
        discountFacade.clearAll();
        
        verify(mockDiscountRepository).clear();
        verify(mockConditionRepository).clear();
    }

    // ===========================================
    // EDGE CASE AND BOUNDARY TESTS
    // ===========================================

    @Test
    public void testCreateMinPriceConditionWithZeroPrice() {
        MinPriceCondition condition = discountFacade.createMinPriceCondition(testStoreId, mockItemFacade, 0.0);
        
        assertNotNull("Should allow zero price", condition);
        assertEquals("Zero price should be set correctly", 0.0, condition.getMinPrice(), 0.001);
        
        lastCreatedObject = condition;
    }

    @Test
    public void testCreateSimpleDiscountWithZeroPercentage() {
        SimpleDiscount discount = discountFacade.createSimpleDiscount(testStoreId, mockItemFacade, 0.0f, mockQualifier);
        
        assertNotNull("Should allow zero percentage", discount);
        assertEquals("Zero percentage should be set correctly", 0.0, discount.getDiscountPercentage(), 0.001);
        
        lastCreatedObject = discount;
    }

    @Test
    public void testCreateSimpleDiscountWithOneHundredPercentDiscount() {
        SimpleDiscount discount = discountFacade.createSimpleDiscount(testStoreId, mockItemFacade, 1.0f, mockQualifier);
        
        assertNotNull("Should allow 100% discount", discount);
        assertEquals("100% discount should be set correctly", 1.0, discount.getDiscountPercentage(), 0.001);
        
        lastCreatedObject = discount;
    }

    @Test
    public void testCreateMinQuantityConditionWithZeroQuantity() {
        MinQuantityCondition condition = discountFacade.createMinQuantityCondition(testStoreId, mockItemFacade, "product1", 0);
        
        assertNotNull("Should allow zero quantity", condition);
        assertEquals("Zero quantity should be set correctly", 0, condition.getMinQuantity());
        
        lastCreatedObject = condition;
    }

    // ===========================================
    // STORE-SPECIFIC CONDITION TESTS
    // ===========================================

    @Test
    public void testGetStoreConditionsWithMemoryRepository() {
        // Create a real memory repository for this test
        Infrastructure.Repositories.MemoryConditionRepository memoryConditionRepo = 
            new Infrastructure.Repositories.MemoryConditionRepository();
        
        DiscountFacade testFacade = new DiscountFacade(mockDiscountRepository, memoryConditionRepo);
        
        // Add a condition to the store
        MinPriceCondition condition = testFacade.createMinPriceCondition(testStoreId, mockItemFacade, 100.0);
        
        // Retrieve store conditions
        Map<UUID, Condition> storeConditions = testFacade.getStoreConditions(testStoreId);
        
        assertNotNull("Store conditions should not be null", storeConditions);
        assertTrue("Store should have conditions", storeConditions.containsKey(condition.getId()));
        assertEquals("Should return the correct condition", condition, storeConditions.get(condition.getId()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetStoreConditionsWithNonMemoryRepository() {
        discountFacade.getStoreConditions(testStoreId);
    }

    @Test
    public void testRemoveConditionFromStore() {
        // Create a real memory repository for this test
        Infrastructure.Repositories.MemoryConditionRepository memoryConditionRepo = 
            new Infrastructure.Repositories.MemoryConditionRepository();
        
        DiscountFacade testFacade = new DiscountFacade(mockDiscountRepository, memoryConditionRepo);
        
        // Add a condition to the store
        MinPriceCondition condition = testFacade.createMinPriceCondition(testStoreId, mockItemFacade, 100.0);
        UUID conditionId = condition.getId();
        
        // Remove the condition from the store
        testFacade.removeCondition(testStoreId, conditionId);
        
        // Verify it's removed
        Map<UUID, Condition> storeConditions = testFacade.getStoreConditions(testStoreId);
        assertFalse("Condition should be removed from store", storeConditions.containsKey(conditionId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveConditionNotInStore() {
        // Create a real memory repository for this test
        Infrastructure.Repositories.MemoryConditionRepository memoryConditionRepo = 
            new Infrastructure.Repositories.MemoryConditionRepository();
        
        DiscountFacade testFacade = new DiscountFacade(mockDiscountRepository, memoryConditionRepo);
        
        // Try to remove a condition that doesn't exist in the store
        UUID nonExistentConditionId = UUID.randomUUID();
        testFacade.removeCondition(testStoreId, nonExistentConditionId);
    }

    @Test
    public void testGetStoreConditionCount() {
        // Create a real memory repository for this test
        Infrastructure.Repositories.MemoryConditionRepository memoryConditionRepo = 
            new Infrastructure.Repositories.MemoryConditionRepository();
        
        DiscountFacade testFacade = new DiscountFacade(mockDiscountRepository, memoryConditionRepo);
        
        // Add conditions to the store
        testFacade.createMinPriceCondition(testStoreId, mockItemFacade, 100.0);
        testFacade.createMaxPriceCondition(testStoreId, mockItemFacade, 200.0);
        
        int count = testFacade.getStoreConditionCount(testStoreId);
        
        assertEquals("Should return correct condition count for store", 2, count);
    }

    @Test
    public void testClearStoreDiscountsAndConditions() {
        // Create real memory repositories for this test
        Infrastructure.Repositories.MemoryDiscountRepository memoryDiscountRepo = 
            new Infrastructure.Repositories.MemoryDiscountRepository();
        Infrastructure.Repositories.MemoryConditionRepository memoryConditionRepo = 
            new Infrastructure.Repositories.MemoryConditionRepository();
        
        DiscountFacade testFacade = new DiscountFacade(memoryDiscountRepo, memoryConditionRepo);
        
        // Add discount and condition to the store
        testFacade.createMinPriceCondition(testStoreId, mockItemFacade, 100.0);
        testFacade.createSimpleDiscount(testStoreId, mockItemFacade, 0.2f, mockQualifier);
        
        // Verify they exist
        assertTrue("Store should have conditions", testFacade.getStoreConditionCount(testStoreId) > 0);
        assertTrue("Store should have discounts", testFacade.getStoreDiscountCount(testStoreId) > 0);
        
        // Clear store data
        testFacade.clearStoreDiscountsAndConditions(testStoreId);
        
        // Verify they're cleared
        assertEquals("Store should have no conditions", 0, testFacade.getStoreConditionCount(testStoreId));
        assertEquals("Store should have no discounts", 0, testFacade.getStoreDiscountCount(testStoreId));
    }

    // ===========================================
    // INTEGRATION TESTS
    // ===========================================

    @Test
    public void testCreateAndRetrieveDiscountWorkflow() {
        Set<Discount> storeDiscounts = new HashSet<>();
        SimpleDiscount createdDiscount = discountFacade.createSimpleDiscount(testStoreId, mockItemFacade, 0.15f, mockQualifier);
        storeDiscounts.add(createdDiscount);
        
        when(mockDiscountRepository.getStoreDiscounts(testStoreId)).thenReturn(storeDiscounts);
        
        Set<Discount> retrievedDiscounts = discountFacade.getStoreDiscounts(testStoreId);
        
        assertTrue("Should contain the created discount", retrievedDiscounts.contains(createdDiscount));
        verify(mockDiscountRepository).save(eq(testStoreId), eq(createdDiscount));
        verify(mockDiscountRepository).getStoreDiscounts(testStoreId);
        
        lastCreatedObject = createdDiscount;
    }

    @Test
    public void testMultipleStoreIsolation() {
        String store1 = "store1";
        String store2 = "store2";
        
        Set<Discount> store1Discounts = new HashSet<>();
        Set<Discount> store2Discounts = new HashSet<>();
        
        SimpleDiscount discount1 = discountFacade.createSimpleDiscount(store1, mockItemFacade, 0.1f, mockQualifier);
        SimpleDiscount discount2 = discountFacade.createSimpleDiscount(store2, mockItemFacade, 0.2f, mockQualifier);
        
        store1Discounts.add(discount1);
        store2Discounts.add(discount2);
        
        when(mockDiscountRepository.getStoreDiscounts(store1)).thenReturn(store1Discounts);
        when(mockDiscountRepository.getStoreDiscounts(store2)).thenReturn(store2Discounts);
        
        Set<Discount> store1Retrieved = discountFacade.getStoreDiscounts(store1);
        Set<Discount> store2Retrieved = discountFacade.getStoreDiscounts(store2);
        
        assertTrue("Store1 should contain discount1", store1Retrieved.contains(discount1));
        assertFalse("Store1 should not contain discount2", store1Retrieved.contains(discount2));
        assertTrue("Store2 should contain discount2", store2Retrieved.contains(discount2));
        assertFalse("Store2 should not contain discount1", store2Retrieved.contains(discount1));
        
        verify(mockDiscountRepository).save(eq(store1), eq(discount1));
        verify(mockDiscountRepository).save(eq(store2), eq(discount2));
    }
}