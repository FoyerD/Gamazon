package Domain.Store.Discounts;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import Application.DTOs.DiscountDTO;
import Application.DTOs.DiscountDTO.QualifierType;
import Domain.Store.Category;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Conditions.ConditionBuilder;
import Domain.Store.Discounts.Qualifiers.CategoryQualifier;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;
import Domain.Store.Discounts.Qualifiers.ProductQualifier;
import Domain.Store.Discounts.Qualifiers.StoreQualifier;

/**
 * Builder class for creating Discount domain objects from DTOs.
 * Handles the conversion between UI DTOs and domain objects.
 */
@Component
public class DiscountBuilder {
    
    private final ConditionBuilder conditionBuilder;
    
    public DiscountBuilder(ConditionBuilder conditionBuilder) {
        this.conditionBuilder = conditionBuilder;
    }
    
    /**
     * Builds a Discount domain object from a DiscountDTO.
     * 
     * @param discountDTO The DTO containing discount data
     * @return The corresponding Discount domain object
     * @throws IllegalArgumentException if the DTO is invalid or has unknown type
     */
    public Discount buildDiscount(DiscountDTO discountDTO) {
        return buildDiscount(discountDTO, UUID.randomUUID().toString());
    }
    
    /**
     * Builds a Discount with existing ID (for updates).
     */
    public Discount buildDiscount(DiscountDTO discountDTO, String id) {
        if (discountDTO == null) {
            throw new IllegalArgumentException("DiscountDTO cannot be null");
        }
        
        if (id == null) {
            throw new IllegalArgumentException("Existing ID cannot be null");
        }
        
        Condition cond = conditionBuilder.buildCondition(discountDTO.getCondition());
        List<Discount> subDiscounts = discountDTO.getSubDiscounts().stream().map((ddto) -> buildDiscount(ddto)).collect(Collectors.toList());
        switch(discountDTO.getType()) {
            case SIMPLE:
                return new SimpleDiscount(id,discountDTO.getDiscountPercentage(), makeQualifier(discountDTO.getQualifierType(), discountDTO.getQualifierValue()), cond);
                
            case AND:
                return new AndDiscount(id,
                    subDiscounts,
                    cond,
                    discountDTO.getMergeType());
                    
            case OR:
                return new OrDiscount(id,
                    subDiscounts,
                    cond,
                    discountDTO.getMergeType());
                        
            case XOR:
                return new XorDiscount(id,
                    subDiscounts.get(0),
                    subDiscounts.get(1),
                    cond,
                    discountDTO.getMergeType());
                    
            default:
                throw new IllegalArgumentException("Unknown discount type: " + discountDTO.getType());
        }
    }
    

    public DiscountQualifier makeQualifier(QualifierType type, String value) {
        if (type == null) {
            throw new IllegalArgumentException("Qualifier type cannot be null");
        }
        
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Qualifier value cannot be null or empty");
        }
        
        switch (type) {
            case PRODUCT:
                return new ProductQualifier(value);
                
            case CATEGORY:
                return new CategoryQualifier(makeCategory(value));
                
            case STORE:
                return new StoreQualifier(value);
                
            default:
                throw new IllegalArgumentException("Unknown qualifier type: " + type);
        }

    }

    private Category makeCategory(String category) {
        if (category == null || category.isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        return new Category(category, "Containing all products in category " + category);
    }

    /**
     * Validates a DiscountDTO for SimpleDiscount creation.
     */
    private void validateSimpleDiscountDTO(DiscountDTO dto) {
        if (dto.getType() != DiscountDTO.DiscountType.SIMPLE) {
            throw new IllegalArgumentException("Expected SIMPLE discount type");
        }
        
        if (dto.getDiscountPercentage() > 1 || dto.getDiscountPercentage() < 0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 1");
        }
        
        if (dto.getQualifierType() == null) {
            throw new IllegalArgumentException("Qualifier type cannot be null");
        }
        
        if (dto.getCondition() == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        
        // Validate condition
        conditionBuilder.validateConditionDTO(dto.getCondition());
    }
    
    /**
     * Validates a DiscountDTO for composite discount creation.
     */
    private void validateCompositeDiscountDTO(DiscountDTO dto) {
        if (dto.getSubDiscounts() == null || dto.getSubDiscounts().isEmpty()) {
            throw new IllegalArgumentException("Sub-discounts cannot be null or empty for composite discount");
        }
        
        // Recursively validate sub-discounts
        for (DiscountDTO subDto : dto.getSubDiscounts()) {
            validateDiscountDTO(subDto);
        }
    }
    
    /**
     * Validates a DiscountDTO.
     * 
     * @param discountDTO DTO to validate
     * @throws IllegalArgumentException if the DTO is invalid
     */
    public void validateDiscountDTO(DiscountDTO discountDTO) {
        if (discountDTO == null) {
            throw new IllegalArgumentException("DiscountDTO cannot be null");
        }
        
        if (discountDTO.getType() == null) {
            throw new IllegalArgumentException("Discount type cannot be null");
        }
        
        switch (discountDTO.getType()) {
            case SIMPLE:
                validateSimpleDiscountDTO(discountDTO);
                break;
                
            case AND:
            case OR:
            case XOR:
                validateCompositeDiscountDTO(discountDTO);
                break;
                
            default:
                throw new IllegalArgumentException("Unknown discount type: " + discountDTO.getType());
        }
    }
    
    /**
     * Validates a list of DiscountDTOs.
     * 
     * @param discountDTOs List of DTOs to validate
     * @throws IllegalArgumentException if any DTO is invalid
     */
    public void validateDiscountDTOs(List<DiscountDTO> discountDTOs) {
        if (discountDTOs == null) {
            return; // Allow null lists
        }
        
        for (DiscountDTO dto : discountDTOs) {
            validateDiscountDTO(dto);
        }
    }
}