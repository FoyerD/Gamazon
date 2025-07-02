package Domain.Store.Discounts;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import Application.DTOs.DiscountDTO;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Conditions.ConditionBuilder;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class DiscountBuilderTest {

    private DiscountBuilder discountBuilder;
    
    @Mock
    private ConditionBuilder mockConditionBuilder;
    
    @Mock
    private Condition mockCondition;
    
    @Mock
    private DiscountQualifier mockQualifier;
    
    private final String TEST_STORE_ID = "store123";
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        discountBuilder = new DiscountBuilder(mockConditionBuilder);
    }
    
    @Test(expected = NullPointerException.class)
    public void testBuildDiscountWithNullDTO() {
        discountBuilder.buildDiscount(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildDiscountWithNullId() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.SIMPLE);
        dto.setStoreId(TEST_STORE_ID);
        discountBuilder.buildDiscount(dto, null, TEST_STORE_ID);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildDiscountWithNullStoreId() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.SIMPLE);
        discountBuilder.buildDiscount(dto, "test-id", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildDiscountWithEmptyStoreId() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.SIMPLE);
        discountBuilder.buildDiscount(dto, "test-id", "");
    }
    
    @Test
    public void testBuildSimpleDiscount() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.SIMPLE);
        dto.setStoreId(TEST_STORE_ID);
        dto.setDiscountPercentage(25.0f);
        dto.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        dto.setQualifierValue("product123");
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        when(mockConditionBuilder.buildConditionWithId(eq(conditionDTO), anyString())).thenReturn(mockCondition);
        
        Discount discount = discountBuilder.buildDiscount(dto, "test-id", TEST_STORE_ID);
        
        assertTrue("Should return SimpleDiscount", discount instanceof SimpleDiscount);
        assertEquals("Should have correct ID", "test-id", discount.getId());
        assertEquals("Should have correct store ID", TEST_STORE_ID, discount.getStoreId());
        SimpleDiscount simpleDiscount = (SimpleDiscount) discount;
        assertEquals("Should have correct discount percentage", 25.0, simpleDiscount.getDiscountPercentage(), 0.001);
    }
    
    @Test
    public void testBuildSimpleDiscountWithDefaultMethod() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.SIMPLE);
        dto.setStoreId(TEST_STORE_ID);
        dto.setDiscountPercentage(15.0f);
        dto.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        dto.setQualifierValue("product456");
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        when(mockConditionBuilder.buildConditionWithId(eq(conditionDTO), anyString())).thenReturn(mockCondition);
        
        // Test the single-parameter method that generates UUID
        Discount discount = discountBuilder.buildDiscount(dto);
        
        assertTrue("Should return SimpleDiscount", discount instanceof SimpleDiscount);
        assertNotNull("Should have generated ID", discount.getId());
        assertEquals("Should have correct store ID", TEST_STORE_ID, discount.getStoreId());
        SimpleDiscount simpleDiscount = (SimpleDiscount) discount;
        assertEquals("Should have correct discount percentage", 15.0, simpleDiscount.getDiscountPercentage(), 0.001);
    }
    
    @Test
    public void testBuildAndDiscount() {
        DiscountDTO subDto1 = new DiscountDTO();
        subDto1.setType(DiscountDTO.DiscountType.SIMPLE);
        subDto1.setStoreId(TEST_STORE_ID);
        subDto1.setDiscountPercentage(10.0f);
        subDto1.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        subDto1.setQualifierValue("product1");
        subDto1.setCondition(new Application.DTOs.ConditionDTO());
        
        DiscountDTO subDto2 = new DiscountDTO();
        subDto2.setType(DiscountDTO.DiscountType.SIMPLE);
        subDto2.setStoreId(TEST_STORE_ID);
        subDto2.setDiscountPercentage(15.0f);
        subDto2.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        subDto2.setQualifierValue("product2");
        subDto2.setCondition(new Application.DTOs.ConditionDTO());
        
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.AND);
        dto.setStoreId(TEST_STORE_ID);
        dto.setSubDiscounts(Arrays.asList(subDto1, subDto2));
        dto.setMergeType(Discount.MergeType.MAX);
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        when(mockConditionBuilder.buildConditionWithId(any(), anyString())).thenReturn(mockCondition);
        
        Discount discount = discountBuilder.buildDiscount(dto, "and-test-id", TEST_STORE_ID);
        
        assertTrue("Should return AndDiscount", discount instanceof AndDiscount);
        assertEquals("Should have correct ID", "and-test-id", discount.getId());
        assertEquals("Should have correct store ID", TEST_STORE_ID, discount.getStoreId());
        AndDiscount andDiscount = (AndDiscount) discount;
        assertEquals("Should have correct number of sub-discounts", 2, andDiscount.getDiscounts().size());
        assertEquals("Should have correct merge type", Discount.MergeType.MAX, andDiscount.getMergeType());
    }
    
    @Test
    public void testBuildOrDiscount() {
        DiscountDTO subDto = new DiscountDTO();
        subDto.setType(DiscountDTO.DiscountType.SIMPLE);
        subDto.setStoreId(TEST_STORE_ID);
        subDto.setDiscountPercentage(20.0f);
        subDto.setQualifierType(DiscountDTO.QualifierType.STORE);
        subDto.setQualifierValue(TEST_STORE_ID);
        subDto.setCondition(new Application.DTOs.ConditionDTO());
        
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.OR);
        dto.setStoreId(TEST_STORE_ID);
        dto.setSubDiscounts(Arrays.asList(subDto));
        dto.setMergeType(Discount.MergeType.MUL);
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        when(mockConditionBuilder.buildConditionWithId(any(), anyString())).thenReturn(mockCondition);
        
        Discount discount = discountBuilder.buildDiscount(dto, "or-test-id", TEST_STORE_ID);
        
        assertTrue("Should return OrDiscount", discount instanceof OrDiscount);
        assertEquals("Should have correct ID", "or-test-id", discount.getId());
        assertEquals("Should have correct store ID", TEST_STORE_ID, discount.getStoreId());
        OrDiscount orDiscount = (OrDiscount) discount;
        assertEquals("Should have correct merge type", Discount.MergeType.MUL, orDiscount.getMergeType());
    }
    
    @Test
    public void testBuildXorDiscount() {
        DiscountDTO subDto1 = new DiscountDTO();
        subDto1.setType(DiscountDTO.DiscountType.SIMPLE);
        subDto1.setStoreId(TEST_STORE_ID);
        subDto1.setDiscountPercentage(10.0f);
        subDto1.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        subDto1.setQualifierValue("product1");
        subDto1.setCondition(new Application.DTOs.ConditionDTO());
        
        DiscountDTO subDto2 = new DiscountDTO();
        subDto2.setType(DiscountDTO.DiscountType.SIMPLE);
        subDto2.setStoreId(TEST_STORE_ID);
        subDto2.setDiscountPercentage(15.0f);
        subDto2.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        subDto2.setQualifierValue("product2");
        subDto2.setCondition(new Application.DTOs.ConditionDTO());
        
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.XOR);
        dto.setStoreId(TEST_STORE_ID);
        dto.setSubDiscounts(Arrays.asList(subDto1, subDto2));
        dto.setMergeType(Discount.MergeType.MAX);
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        when(mockConditionBuilder.buildConditionWithId(eq(conditionDTO), anyString())).thenReturn(mockCondition);
        
        Discount discount = discountBuilder.buildDiscount(dto, "xor-test-id", TEST_STORE_ID);
        
        assertTrue("Should return XorDiscount", discount instanceof XorDiscount);
        assertEquals("Should have correct ID", "xor-test-id", discount.getId());
        assertEquals("Should have correct store ID", TEST_STORE_ID, discount.getStoreId());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildXorDiscountWithInsufficientSubDiscounts() {
        DiscountDTO subDto1 = new DiscountDTO();
        subDto1.setType(DiscountDTO.DiscountType.SIMPLE);
        subDto1.setStoreId(TEST_STORE_ID);
        subDto1.setDiscountPercentage(10.0f);
        subDto1.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        subDto1.setQualifierValue("product1");
        subDto1.setCondition(new Application.DTOs.ConditionDTO());
        
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.XOR);
        dto.setStoreId(TEST_STORE_ID);
        dto.setSubDiscounts(Arrays.asList(subDto1)); // Only one sub-discount, XOR needs 2
        dto.setMergeType(Discount.MergeType.MAX);
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        when(mockConditionBuilder.buildCondition(any())).thenReturn(mockCondition);
        
        discountBuilder.buildDiscount(dto, "xor-test-id", TEST_STORE_ID);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildDiscountWithNullSubDiscountsForComposite() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.AND);
        dto.setStoreId(TEST_STORE_ID);
        dto.setSubDiscounts(null); // This should cause an error
        dto.setMergeType(Discount.MergeType.MAX);
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        when(mockConditionBuilder.buildCondition(any())).thenReturn(mockCondition);
        
        discountBuilder.buildDiscount(dto, "test-id", TEST_STORE_ID);
    }
    
    @Test
    public void testStoreIdPropagationToSubDiscounts() {
        DiscountDTO subDto = new DiscountDTO();
        subDto.setType(DiscountDTO.DiscountType.SIMPLE);
        subDto.setStoreId("original-store"); // This should be overridden
        subDto.setDiscountPercentage(10.0f);
        subDto.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        subDto.setQualifierValue("product1");
        subDto.setCondition(new Application.DTOs.ConditionDTO());
        
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.OR);
        dto.setStoreId("original-parent-store");
        dto.setSubDiscounts(Arrays.asList(subDto));
        dto.setMergeType(Discount.MergeType.MAX);
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        when(mockConditionBuilder.buildConditionWithId(any(), anyString())).thenReturn(mockCondition);
        
        String newStoreId = "new-store-123";
        Discount discount = discountBuilder.buildDiscount(dto, "test-id", newStoreId);
        
        assertEquals("Parent discount should have new store ID", newStoreId, discount.getStoreId());
        OrDiscount orDiscount = (OrDiscount) discount;
        for (Discount subDiscount : orDiscount.getDiscounts()) {
            assertEquals("Sub-discount should have new store ID", newStoreId, subDiscount.getStoreId());
        }
    }
    
    @Test
    public void testMakeProductQualifier() {
        DiscountQualifier qualifier = discountBuilder.makeQualifier(
            DiscountDTO.QualifierType.PRODUCT, "product123");
        
        assertTrue("Should return ProductQualifier", 
                  qualifier instanceof Domain.Store.Discounts.Qualifiers.ProductQualifier);
    }
    
    @Test
    public void testMakeCategoryQualifier() {
        DiscountQualifier qualifier = discountBuilder.makeQualifier(
            DiscountDTO.QualifierType.CATEGORY, "Electronics");
        
        assertTrue("Should return CategoryQualifier", 
                  qualifier instanceof Domain.Store.Discounts.Qualifiers.CategoryQualifier);
    }
    
    @Test
    public void testMakeStoreQualifier() {
        DiscountQualifier qualifier = discountBuilder.makeQualifier(
            DiscountDTO.QualifierType.STORE, "store123");
        
        assertTrue("Should return StoreQualifier", 
                  qualifier instanceof Domain.Store.Discounts.Qualifiers.StoreQualifier);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMakeQualifierWithNullType() {
        discountBuilder.makeQualifier(null, "value");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMakeQualifierWithNullValue() {
        discountBuilder.makeQualifier(DiscountDTO.QualifierType.PRODUCT, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMakeQualifierWithEmptyValue() {
        discountBuilder.makeQualifier(DiscountDTO.QualifierType.PRODUCT, "");
    }
    
    @Test
    public void testMakeCategoryCreatesProperCategory() {
        DiscountQualifier qualifier = discountBuilder.makeQualifier(
            DiscountDTO.QualifierType.CATEGORY, "Books");
        
        assertTrue("Should return CategoryQualifier", 
                  qualifier instanceof Domain.Store.Discounts.Qualifiers.CategoryQualifier);
        
        Domain.Store.Discounts.Qualifiers.CategoryQualifier categoryQualifier = 
            (Domain.Store.Discounts.Qualifiers.CategoryQualifier) qualifier;
        assertEquals("Should have correct category name", "Books", categoryQualifier.getCategory());
    }
    
    @Test
    public void testValidateDiscountDTO() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.SIMPLE);
        dto.setDiscountPercentage(0.5f);
        dto.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        // Should not throw exception
        discountBuilder.validateDiscountDTO(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateDiscountDTOWithNullDTO() {
        discountBuilder.validateDiscountDTO(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateDiscountDTOWithNullType() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(null);
        
        discountBuilder.validateDiscountDTO(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateSimpleDiscountDTOWithInvalidPercentage() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.SIMPLE);
        dto.setDiscountPercentage(1.5f); // Invalid - greater than 1
        dto.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        discountBuilder.validateDiscountDTO(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateSimpleDiscountDTOWithNegativePercentage() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.SIMPLE);
        dto.setDiscountPercentage(-0.1f); // Invalid - negative
        dto.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        discountBuilder.validateDiscountDTO(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateSimpleDiscountDTOWithNullQualifierType() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.SIMPLE);
        dto.setDiscountPercentage(0.2f);
        dto.setQualifierType(null); // Invalid
        
        Application.DTOs.ConditionDTO conditionDTO = new Application.DTOs.ConditionDTO();
        dto.setCondition(conditionDTO);
        
        discountBuilder.validateDiscountDTO(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateSimpleDiscountDTOWithNullCondition() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.SIMPLE);
        dto.setDiscountPercentage(0.2f);
        dto.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        dto.setCondition(null); // Invalid
        
        discountBuilder.validateDiscountDTO(dto);
    }
    
    @Test
    public void testValidateDiscountDTOs() {
        DiscountDTO dto1 = new DiscountDTO();
        dto1.setType(DiscountDTO.DiscountType.SIMPLE);
        dto1.setDiscountPercentage(0.2f);
        dto1.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        dto1.setCondition(new Application.DTOs.ConditionDTO());
        
        DiscountDTO dto2 = new DiscountDTO();
        dto2.setType(DiscountDTO.DiscountType.SIMPLE);
        dto2.setDiscountPercentage(0.3f);
        dto2.setQualifierType(DiscountDTO.QualifierType.STORE);
        dto2.setCondition(new Application.DTOs.ConditionDTO());
        
        List<DiscountDTO> dtos = Arrays.asList(dto1, dto2);
        
        // Should not throw exception
        discountBuilder.validateDiscountDTOs(dtos);
    }
    
    @Test
    public void testValidateDiscountDTOsWithNull() {
        // Should not throw exception (allows null lists)
        discountBuilder.validateDiscountDTOs(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateCompositeDiscountDTOWithNullSubDiscounts() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.AND);
        dto.setSubDiscounts(null);
        
        discountBuilder.validateDiscountDTO(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateCompositeDiscountDTOWithEmptySubDiscounts() {
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.OR);
        dto.setSubDiscounts(Arrays.asList());
        
        discountBuilder.validateDiscountDTO(dto);
    }
    
    @Test
    public void testValidateCompositeDiscountDTOWithValidSubDiscounts() {
        DiscountDTO subDto = new DiscountDTO();
        subDto.setType(DiscountDTO.DiscountType.SIMPLE);
        subDto.setDiscountPercentage(0.1f);
        subDto.setQualifierType(DiscountDTO.QualifierType.PRODUCT);
        subDto.setCondition(new Application.DTOs.ConditionDTO());
        
        DiscountDTO dto = new DiscountDTO();
        dto.setType(DiscountDTO.DiscountType.AND);
        dto.setSubDiscounts(Arrays.asList(subDto));
        
        // Should not throw exception
        discountBuilder.validateDiscountDTO(dto);
    }
}