package Domain.Store.Discounts;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import Application.DTOs.DiscountDTO;
import Domain.Store.ItemFacade;
import Domain.Store.Item;
import Domain.Store.Product;
import Domain.Store.Discounts.Discount.MergeType;
import Domain.Store.Discounts.Conditions.TrueCondition;
import Domain.Store.Discounts.Conditions.MinPriceCondition;
import Domain.Store.Discounts.Qualifiers.ProductQualifier;
import Domain.Store.Discounts.Qualifiers.StoreQualifier;

public class DiscountFacadeTest {

    private DiscountFacade discountFacade;
    
    @Mock
    private IDiscountRepository mockDiscountRepository;
    
    @Mock
    private ItemFacade mockItemFacade;
    
    @Mock
    private Product mockProduct;
    
    @Mock
    private Item mockItem;
    
    private String testStoreId;
    private String testProductId;
    private TrueCondition testCondition;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        testStoreId = "store123";
        testProductId = "product456";
        testCondition = new TrueCondition();
        
        // Setup mock behavior
        when(mockItemFacade.getProduct(testProductId)).thenReturn(mockProduct);
        when(mockProduct.getProductId()).thenReturn(testProductId);
        when(mockItem.getProductId()).thenReturn(testProductId);
        when(mockItem.getPrice()).thenReturn(10.0);
        
        discountFacade = new DiscountFacade(mockDiscountRepository, mockItemFacade);
    }
    
    // ===========================================
    // CONSTRUCTOR TESTS
    // ===========================================
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NullDiscountRepository_ThrowsException() {
        new DiscountFacade(null, mockItemFacade);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NullItemFacade_ThrowsException() {
        new DiscountFacade(mockDiscountRepository, null);
    }
    
    @Test
    public void testConstructor_ValidParameters_CreatesInstance() {
        DiscountFacade facade = new DiscountFacade(mockDiscountRepository, mockItemFacade);
        assertNotNull(facade);
    }
    
    // ===========================================
    // SIMPLE DISCOUNT CREATION TESTS
    // ===========================================
    
    @Test
    public void testCreateSimpleDiscount_ValidParameters_Success() {
        // Arrange
        float discountPercentage = 0.2f;
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        when(mockDiscountRepository.add(any(String.class), any(SimpleDiscount.class))).thenReturn(true);
        
        // Act
        SimpleDiscount result = discountFacade.createSimpleDiscount(testStoreId, discountPercentage, qualifier, testCondition);
        
        // Assert
        assertNotNull(result);
        assertEquals(testStoreId, result.getStoreId());
        assertEquals(discountPercentage, result.getDiscountPercentage(), 0.001);
        assertEquals(qualifier, result.getQualifier());
        verify(mockDiscountRepository).add(eq(result.getId()), eq(result));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSimpleDiscount_InvalidDiscountPercentage_ThrowsException() {
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        discountFacade.createSimpleDiscount(testStoreId, 1.5f, qualifier, testCondition);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSimpleDiscount_NegativeDiscountPercentage_ThrowsException() {
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        discountFacade.createSimpleDiscount(testStoreId, -0.1f, qualifier, testCondition);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSimpleDiscount_NullQualifier_ThrowsException() {
        discountFacade.createSimpleDiscount(testStoreId, 0.2f, null, testCondition);
    }
    
    // ===========================================
    // AND DISCOUNT CREATION TESTS
    // ===========================================
    
    @Test
    public void testCreateAndDiscount_ValidParameters_Success() {
        // Arrange
        ProductQualifier qualifier1 = new ProductQualifier(testProductId);
        ProductQualifier qualifier2 = new ProductQualifier("product789");
        SimpleDiscount discount1 = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.1f, qualifier1, testCondition);
        SimpleDiscount discount2 = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.2f, qualifier2, testCondition);
        List<Discount> discounts = Arrays.asList(discount1, discount2);
        
        when(mockDiscountRepository.add(any(String.class), any(AndDiscount.class))).thenReturn(true);
        
        // Act
        AndDiscount result = discountFacade.createAndDiscount(testStoreId, discounts, testCondition, MergeType.MAX);
        
        // Assert
        assertNotNull(result);
        assertEquals(testStoreId, result.getStoreId());
        assertEquals(MergeType.MAX, result.getMergeType());
        assertEquals(2, result.getDiscounts().size());
        verify(mockDiscountRepository).add(eq(result.getId()), eq(result));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAndDiscount_EmptyDiscountsList_ThrowsException() {
        discountFacade.createAndDiscount(testStoreId, Arrays.asList(), testCondition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAndDiscount_NullDiscountsList_ThrowsException() {
        discountFacade.createAndDiscount(testStoreId, null, testCondition, MergeType.MAX);
    }
    
    // ===========================================
    // OR DISCOUNT CREATION TESTS
    // ===========================================
    
    @Test
    public void testCreateOrDiscount_ValidParameters_Success() {
        // Arrange
        ProductQualifier qualifier1 = new ProductQualifier(testProductId);
        SimpleDiscount discount1 = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.1f, qualifier1, testCondition);
        List<Discount> discounts = Arrays.asList(discount1);
        
        when(mockDiscountRepository.add(any(String.class), any(OrDiscount.class))).thenReturn(true);
        
        // Act
        OrDiscount result = discountFacade.createOrDiscount(testStoreId, discounts, testCondition, MergeType.MUL);
        
        // Assert
        assertNotNull(result);
        assertEquals(testStoreId, result.getStoreId());
        assertEquals(MergeType.MUL, result.getMergeType());
        verify(mockDiscountRepository).add(eq(result.getId()), eq(result));
    }
    
    // ===========================================
    // XOR DISCOUNT CREATION TESTS
    // ===========================================
    
    @Test
    public void testCreateXorDiscount_ValidParameters_Success() {
        // Arrange
        ProductQualifier qualifier1 = new ProductQualifier(testProductId);
        ProductQualifier qualifier2 = new ProductQualifier("product789");
        SimpleDiscount discount1 = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.1f, qualifier1, testCondition);
        SimpleDiscount discount2 = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.2f, qualifier2, testCondition);
        
        when(mockDiscountRepository.add(any(String.class), any(XorDiscount.class))).thenReturn(true);
        
        // Act
        XorDiscount result = discountFacade.createXorDiscount(testStoreId, discount1, discount2, testCondition, MergeType.MAX);
        
        // Assert
        assertNotNull(result);
        assertEquals(testStoreId, result.getStoreId());
        assertEquals(MergeType.MAX, result.getMergeType());
        assertEquals(2, result.getDiscounts().size());
        verify(mockDiscountRepository).add(eq(result.getId()), eq(result));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateXorDiscount_NullFirstDiscount_ThrowsException() {
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        SimpleDiscount discount = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.1f, qualifier, testCondition);
        discountFacade.createXorDiscount(testStoreId, null, discount, testCondition, MergeType.MAX);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateXorDiscount_NullSecondDiscount_ThrowsException() {
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        SimpleDiscount discount = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.1f, qualifier, testCondition);
        discountFacade.createXorDiscount(testStoreId, discount, null, testCondition, MergeType.MAX);
    }
    
    // ===========================================
    // ADD DISCOUNT TESTS
    // ===========================================
    
    @Test
    public void testAddDiscount_ValidDiscount_Success() {
        // Arrange
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        SimpleDiscount discount = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.2f, qualifier, testCondition);
        when(mockDiscountRepository.add(discount.getId(), discount)).thenReturn(true);
        
        // Act
        boolean result = discountFacade.addDiscount(testStoreId, discount);
        
        // Assert
        assertTrue(result);
        verify(mockDiscountRepository).add(discount.getId(), discount);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddDiscount_NullDiscount_ThrowsException() {
        Discount nullDiscount = null;
        discountFacade.addDiscount(testStoreId, nullDiscount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDiscount_NullDiscountDTO_ThrowsException() {
        DiscountDTO nullDiscountDTO = null;
        discountFacade.addDiscount(testStoreId, nullDiscountDTO);
    }
    
    @Test
    public void testAddDiscountDTO_ValidDTO_Success() throws Exception {
        // Arrange
        DiscountDTO discountDTO = mock(DiscountDTO.class);
        when(discountDTO.getStoreId()).thenReturn(testStoreId);
        when(discountDTO.getType()).thenReturn(DiscountDTO.DiscountType.SIMPLE);
        when(discountDTO.getDiscountPercentage()).thenReturn(0.2);
        when(discountDTO.getQualifierType()).thenReturn(DiscountDTO.QualifierType.PRODUCT);
        when(discountDTO.getQualifierValue()).thenReturn(testProductId);
        when(discountDTO.getCondition()).thenReturn(mock(Application.DTOs.ConditionDTO.class));
        when(discountDTO.getSubDiscounts()).thenReturn(Arrays.asList());
        
        when(mockDiscountRepository.add(any(String.class), any(Discount.class))).thenReturn(true);
        
        // Act
        Discount result = discountFacade.addDiscount(testStoreId, discountDTO);
        
        // Assert
        assertNotNull(result);
        assertEquals(testStoreId, result.getStoreId());
    }
    
    @Test(expected = Exception.class)
    public void testAddDiscountDTO_RepositoryFails_ThrowsException() throws Exception {
        // Arrange
        DiscountDTO discountDTO = mock(DiscountDTO.class);
        when(discountDTO.getStoreId()).thenReturn(testStoreId);
        when(discountDTO.getType()).thenReturn(DiscountDTO.DiscountType.SIMPLE);
        when(discountDTO.getDiscountPercentage()).thenReturn(0.2);
        when(discountDTO.getQualifierType()).thenReturn(DiscountDTO.QualifierType.PRODUCT);
        when(discountDTO.getQualifierValue()).thenReturn(testProductId);
        when(discountDTO.getCondition()).thenReturn(mock(Application.DTOs.ConditionDTO.class));
        when(discountDTO.getSubDiscounts()).thenReturn(Arrays.asList());
        
        when(mockDiscountRepository.add(any(String.class), any(Discount.class))).thenReturn(false);
        
        // Act & Assert
        discountFacade.addDiscount(testStoreId, discountDTO);
    }
    
    // ===========================================
    // GET STORE DISCOUNTS TESTS
    // ===========================================
    
    @Test
    public void testGetStoreDiscounts_ValidStoreId_ReturnsDiscounts() {
        // Arrange
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        SimpleDiscount discount = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.2f, qualifier, testCondition);
        List<Discount> expectedDiscounts = Arrays.asList(discount);
        when(mockDiscountRepository.getStoreDiscounts(testStoreId)).thenReturn(expectedDiscounts);
        
        // Act
        List<Discount> result = discountFacade.getStoreDiscounts(testStoreId);
        
        // Assert
        assertEquals(expectedDiscounts, result);
        verify(mockDiscountRepository).getStoreDiscounts(testStoreId);
    }
    
    // ===========================================
    // UPDATE DISCOUNT TESTS
    // ===========================================
    
    @Test
    public void testUpdateDiscount_ExistingDiscount_Success() {
        // Arrange
        String discountId = UUID.randomUUID().toString();
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        SimpleDiscount discount = new SimpleDiscount(discountId, testStoreId, 0.3f, qualifier, testCondition);
        
        when(mockDiscountRepository.exists(discountId)).thenReturn(true);
        when(mockDiscountRepository.add(discountId, discount)).thenReturn(true);
        
        // Act
        discountFacade.updateDiscount(testStoreId, discount);
        
        // Assert
        verify(mockDiscountRepository).exists(discountId);
        verify(mockDiscountRepository).add(discountId, discount);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDiscount_NonExistingDiscount_ThrowsException() {
        // Arrange
        String discountId = UUID.randomUUID().toString();
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        SimpleDiscount discount = new SimpleDiscount(discountId, testStoreId, 0.3f, qualifier, testCondition);
        
        when(mockDiscountRepository.exists(discountId)).thenReturn(false);
        
        // Act & Assert
        discountFacade.updateDiscount(testStoreId, discount);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDiscount_NullDiscount_ThrowsException() {
        discountFacade.updateDiscount(testStoreId, null);
    }
    
    // ===========================================
    // REMOVE DISCOUNT TESTS
    // ===========================================
    
    @Test
    public void testRemoveDiscount_ExistingDiscount_Success() {
        // Arrange
        String discountId = UUID.randomUUID().toString();
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        SimpleDiscount discount = new SimpleDiscount(discountId, testStoreId, 0.2f, qualifier, testCondition);
        List<Discount> storeDiscounts = Arrays.asList(discount);
        
        when(mockDiscountRepository.getStoreDiscounts(testStoreId)).thenReturn(storeDiscounts);
        when(mockDiscountRepository.remove(discountId)).thenReturn(discount);
        
        // Act
        boolean result = discountFacade.removeDiscount(testStoreId, discountId);
        
        // Assert
        assertTrue(result);
        verify(mockDiscountRepository).getStoreDiscounts(testStoreId);
        verify(mockDiscountRepository).remove(discountId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDiscount_DiscountNotInStore_ThrowsException() {
        // Arrange
        String discountId = UUID.randomUUID().toString();
        when(mockDiscountRepository.getStoreDiscounts(testStoreId)).thenReturn(Arrays.asList());
        
        // Act & Assert
        discountFacade.removeDiscount(testStoreId, discountId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDiscount_NullDiscountId_ThrowsException() {
        discountFacade.removeDiscount(testStoreId, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDiscount_EmptyDiscountId_ThrowsException() {
        discountFacade.removeDiscount(testStoreId, "");
    }
    
    // ===========================================
    // GET DISCOUNT TESTS
    // ===========================================
    
    @Test
    public void testGetDiscount_ValidId_ReturnsDiscount() {
        // Arrange
        String discountId = UUID.randomUUID().toString();
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        SimpleDiscount expectedDiscount = new SimpleDiscount(discountId, testStoreId, 0.2f, qualifier, testCondition);
        when(mockDiscountRepository.get(discountId)).thenReturn(expectedDiscount);
        
        // Act
        Discount result = discountFacade.getDiscount(discountId);
        
        // Assert
        assertEquals(expectedDiscount, result);
        verify(mockDiscountRepository).get(discountId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetDiscount_NullId_ThrowsException() {
        discountFacade.getDiscount(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetDiscount_EmptyId_ThrowsException() {
        discountFacade.getDiscount("");
    }
    
    // ===========================================
    // REMOVE DISCOUNT BY ID TESTS
    // ===========================================
    
    @Test
    public void testRemoveDiscountById_ValidId_Success() {
        // Arrange
        String discountId = UUID.randomUUID().toString();
        when(mockDiscountRepository.remove(discountId)).thenReturn(mock(Discount.class));
        
        // Act
        discountFacade.removeDiscount(discountId);
        
        // Assert
        verify(mockDiscountRepository).remove(discountId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveDiscountById_NullId_ThrowsException() {
        discountFacade.removeDiscount((String) null);
    }
    
    // ===========================================
    // DISCOUNT EXISTS TESTS
    // ===========================================
    
    @Test
    public void testDiscountExists_ExistingId_ReturnsTrue() {
        // Arrange
        String discountId = UUID.randomUUID().toString();
        when(mockDiscountRepository.exists(discountId)).thenReturn(true);
        
        // Act
        boolean result = discountFacade.discountExists(discountId);
        
        // Assert
        assertTrue(result);
        verify(mockDiscountRepository).exists(discountId);
    }
    
    @Test
    public void testDiscountExists_NonExistingId_ReturnsFalse() {
        // Arrange
        String discountId = UUID.randomUUID().toString();
        when(mockDiscountRepository.exists(discountId)).thenReturn(false);
        
        // Act
        boolean result = discountFacade.discountExists(discountId);
        
        // Assert
        assertFalse(result);
        verify(mockDiscountRepository).exists(discountId);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDiscountExists_NullId_ThrowsException() {
        discountFacade.discountExists(null);
    }
    
    // ===========================================
    // VALIDATION HELPER TESTS
    // ===========================================
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateDiscountPercentage_GreaterThanOne_ThrowsException() {
        // This test indirectly tests the validation by trying to create a discount with invalid percentage
        ProductQualifier qualifier = new ProductQualifier(testProductId);
        discountFacade.createSimpleDiscount(testStoreId, 1.1f, qualifier, testCondition);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateProductId_NonExistingProduct_ThrowsException() {
        // Arrange
        String invalidProductId = "nonexistent";
        when(mockItemFacade.getProduct(invalidProductId)).thenReturn(null);
        ProductQualifier qualifier = new ProductQualifier(invalidProductId);
        
        // Act & Assert - This should trigger validation during createSimpleDiscount
        discountFacade.createSimpleDiscount(testStoreId, 0.2f, qualifier, testCondition);
    }
    
    // ===========================================
    // INTEGRATION TESTS WITH COMPOSITE DISCOUNTS
    // ===========================================
    
    @Test
    public void testCreateComplexDiscountHierarchy_Success() {
        // Arrange
        ProductQualifier qualifier1 = new ProductQualifier(testProductId);
        ProductQualifier qualifier2 = new ProductQualifier("product789");
        
        SimpleDiscount discount1 = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.1f, qualifier1, testCondition);
        SimpleDiscount discount2 = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.2f, qualifier2, testCondition);
        
        when(mockDiscountRepository.add(any(String.class), any(Discount.class))).thenReturn(true);
        
        // Act - Create an AND discount containing simple discounts
        AndDiscount andDiscount = discountFacade.createAndDiscount(testStoreId, Arrays.asList(discount1, discount2), testCondition, MergeType.MAX);
        
        // Create an OR discount containing the AND discount and another simple discount
        SimpleDiscount discount3 = new SimpleDiscount(UUID.randomUUID().toString(), testStoreId, 0.15f, qualifier1, testCondition);
        OrDiscount orDiscount = discountFacade.createOrDiscount(testStoreId, Arrays.asList(andDiscount, discount3), testCondition, MergeType.MUL);
        
        // Assert
        assertNotNull(andDiscount);
        assertNotNull(orDiscount);
        assertEquals(2, andDiscount.getDiscounts().size());
        assertEquals(2, orDiscount.getDiscounts().size());
        
        // Verify repository interactions
        verify(mockDiscountRepository, times(4)).add(any(String.class), any(Discount.class));
    }
}