package Domain.Store.Discounts.Conditions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import Application.DTOs.ConditionDTO;

public class ConditionBuilderTest {

    private ConditionBuilder conditionBuilder;
    
    @Before
    public void setUp() {
        conditionBuilder = new ConditionBuilder();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildConditionWithNullDTO() {
        conditionBuilder.buildCondition(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildConditionWithNullType() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(null);
        conditionBuilder.buildCondition(dto);
    }
    
    @Test
    public void testBuildMinPriceCondition() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_PRICE);
        dto.setId("min-price-id");
        dto.setMinPrice(50.0);
        
        Condition condition = conditionBuilder.buildCondition(dto);
        
        assertTrue("Should return MinPriceCondition", condition instanceof MinPriceCondition);
        MinPriceCondition minPriceCondition = (MinPriceCondition) condition;
        assertEquals("Should have correct min price", 50.0, minPriceCondition.getMinPrice(), 0.001);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMinPriceConditionWithNullPrice() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_PRICE);
        dto.setMinPrice(null);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMinPriceConditionWithNegativePrice() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_PRICE);
        dto.setMinPrice(-10.0);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test
    public void testBuildMaxPriceCondition() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MAX_PRICE);
        dto.setId("max-price-id");
        dto.setMaxPrice(100.0);
        
        Condition condition = conditionBuilder.buildCondition(dto);
        
        assertTrue("Should return MaxPriceCondition", condition instanceof MaxPriceCondition);
        MaxPriceCondition maxPriceCondition = (MaxPriceCondition) condition;
        assertEquals("Should have correct max price", 100.0, maxPriceCondition.getMaxPrice(), 0.001);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMaxPriceConditionWithNullPrice() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MAX_PRICE);
        dto.setMaxPrice(null);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMaxPriceConditionWithNegativePrice() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MAX_PRICE);
        dto.setMaxPrice(-10.0);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test
    public void testBuildMinQuantityCondition() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_QUANTITY);
        dto.setId("min-qty-id");
        dto.setProductId("product123");
        dto.setMinQuantity(5);
        
        Condition condition = conditionBuilder.buildCondition(dto);
        
        assertTrue("Should return MinQuantityCondition", condition instanceof MinQuantityCondition);
        MinQuantityCondition minQuantityCondition = (MinQuantityCondition) condition;
        assertEquals("Should have correct product ID", "product123", minQuantityCondition.getProductId());
        assertEquals("Should have correct min quantity", 5, minQuantityCondition.getMinQuantity());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMinQuantityConditionWithNullProductId() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_QUANTITY);
        dto.setProductId(null);
        dto.setMinQuantity(5);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMinQuantityConditionWithEmptyProductId() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_QUANTITY);
        dto.setProductId("");
        dto.setMinQuantity(5);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMinQuantityConditionWithNullQuantity() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_QUANTITY);
        dto.setProductId("product123");
        dto.setMinQuantity(null);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMinQuantityConditionWithNegativeQuantity() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_QUANTITY);
        dto.setProductId("product123");
        dto.setMinQuantity(-1);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test
    public void testBuildMaxQuantityCondition() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MAX_QUANTITY);
        dto.setId("max-qty-id");
        dto.setProductId("product456");
        dto.setMaxQuantity(10);
        
        Condition condition = conditionBuilder.buildCondition(dto);
        
        assertTrue("Should return MaxQuantityCondition", condition instanceof MaxQuantityCondition);
        MaxQuantityCondition maxQuantityCondition = (MaxQuantityCondition) condition;
        assertEquals("Should have correct product ID", "product456", maxQuantityCondition.getProductId());
        assertEquals("Should have correct max quantity", 10, maxQuantityCondition.getMaxQuantity());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMaxQuantityConditionWithNullProductId() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MAX_QUANTITY);
        dto.setProductId(null);
        dto.setMaxQuantity(10);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMaxQuantityConditionWithEmptyProductId() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MAX_QUANTITY);
        dto.setProductId("");
        dto.setMaxQuantity(10);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMaxQuantityConditionWithNullQuantity() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MAX_QUANTITY);
        dto.setProductId("product456");
        dto.setMaxQuantity(null);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMaxQuantityConditionWithNegativeQuantity() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MAX_QUANTITY);
        dto.setProductId("product456");
        dto.setMaxQuantity(-1);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test
    public void testBuildTrueCondition() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.TRUE);
        dto.setId("true-condition-id");
        
        Condition condition = conditionBuilder.buildCondition(dto);
        
        assertTrue("Should return TrueCondition", condition instanceof TrueCondition);
    }
    
    @Test
    public void testBuildAndCondition() {
        // Create sub-condition DTOs
        ConditionDTO subCondition1 = new ConditionDTO();
        subCondition1.setType(ConditionDTO.ConditionType.TRUE);
        subCondition1.setId("sub1");
        
        ConditionDTO subCondition2 = new ConditionDTO();
        subCondition2.setType(ConditionDTO.ConditionType.TRUE);
        subCondition2.setId("sub2");
        
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.AND);
        dto.setId("and-id");
        dto.setSubConditions(Arrays.asList(subCondition1, subCondition2));
        
        Condition condition = conditionBuilder.buildCondition(dto);
        
        assertTrue("Should return AndCondition", condition instanceof AndCondition);
        AndCondition andCondition = (AndCondition) condition;
        assertEquals("Should have correct number of sub-conditions", 2, andCondition.getConditions().size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildAndConditionWithNullSubConditions() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.AND);
        dto.setSubConditions(null);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildAndConditionWithEmptySubConditions() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.AND);
        dto.setSubConditions(Arrays.asList());
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test
    public void testBuildOrCondition() {
        // Create sub-condition DTOs
        ConditionDTO subCondition1 = new ConditionDTO();
        subCondition1.setType(ConditionDTO.ConditionType.TRUE);
        subCondition1.setId("sub1");
        
        ConditionDTO subCondition2 = new ConditionDTO();
        subCondition2.setType(ConditionDTO.ConditionType.TRUE);
        subCondition2.setId("sub2");
        
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.OR);
        dto.setId("or-id");
        dto.setSubConditions(Arrays.asList(subCondition1, subCondition2));
        
        Condition condition = conditionBuilder.buildCondition(dto);
        
        assertTrue("Should return OrCondition", condition instanceof OrCondition);
        OrCondition orCondition = (OrCondition) condition;
        assertEquals("Should have correct number of sub-conditions", 2, orCondition.getConditions().size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildOrConditionWithNullSubConditions() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.OR);
        dto.setSubConditions(null);
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildOrConditionWithEmptySubConditions() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.OR);
        dto.setSubConditions(Arrays.asList());
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildConditionWithUnknownType() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.XOR); // XOR is not implemented in ConditionBuilder
        
        conditionBuilder.buildCondition(dto);
    }
    
    @Test
    public void testBuildConditionWithId() {
        String existingId = "existing-id";
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_PRICE);
        dto.setMinPrice(75.0);
        
        Condition condition = conditionBuilder.buildConditionWithId(dto, existingId);
        
        assertEquals("Should use existing ID", existingId, condition.getId());
        assertTrue("Should return MinPriceCondition", condition instanceof MinPriceCondition);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildConditionWithIdNullDTO() {
        conditionBuilder.buildConditionWithId(null, "existing-id");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildConditionWithIdNullExistingId() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.TRUE);
        conditionBuilder.buildConditionWithId(dto, null);
    }
    
    @Test
    public void testValidateConditionDTO() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_PRICE);
        dto.setMinPrice(50.0);
        
        // Should not throw exception
        conditionBuilder.validateConditionDTO(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateConditionDTOWithNullDTO() {
        conditionBuilder.validateConditionDTO(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateConditionDTOWithNullType() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(null);
        
        conditionBuilder.validateConditionDTO(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateMinPriceConditionWithInvalidPrice() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MIN_PRICE);
        dto.setMinPrice(-10.0);
        
        conditionBuilder.validateConditionDTO(dto);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateMaxQuantityConditionWithMissingProductId() {
        ConditionDTO dto = new ConditionDTO();
        dto.setType(ConditionDTO.ConditionType.MAX_QUANTITY);
        dto.setProductId(null);
        dto.setMaxQuantity(10);
        
        conditionBuilder.validateConditionDTO(dto);
    }
    
    @Test
    public void testValidateConditionDTOs() {
        ConditionDTO dto1 = new ConditionDTO();
        dto1.setType(ConditionDTO.ConditionType.TRUE);
        
        ConditionDTO dto2 = new ConditionDTO();
        dto2.setType(ConditionDTO.ConditionType.TRUE);
        
        List<ConditionDTO> dtos = Arrays.asList(dto1, dto2);
        
        // Should not throw exception
        conditionBuilder.validateConditionDTOs(dtos);
    }
    
    @Test
    public void testValidateConditionDTOsWithNull() {
        // Should not throw exception (allows null lists)
        conditionBuilder.validateConditionDTOs(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidateConditionDTOsWithInvalidDTO() {
        ConditionDTO validDto = new ConditionDTO();
        validDto.setType(ConditionDTO.ConditionType.TRUE);
        
        ConditionDTO invalidDto = new ConditionDTO();
        invalidDto.setType(ConditionDTO.ConditionType.MIN_PRICE);
        invalidDto.setMinPrice(-10.0); // Invalid negative price
        
        List<ConditionDTO> dtos = Arrays.asList(validDto, invalidDto);
        
        conditionBuilder.validateConditionDTOs(dtos);
    }
}