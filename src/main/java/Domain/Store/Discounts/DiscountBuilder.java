package Domain.Store.Discounts;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import Application.DTOs.DiscountDTO;
import Domain.Store.ItemFacade;
import Domain.Store.Category;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Conditions.ConditionBuilder;
import Domain.Store.Discounts.Qualifiers.*;

/**
 * Builder class for creating Discount domain objects from DTOs.
 * Handles the conversion between UI DTOs and domain objects.
 */
@Component
public class DiscountBuilder {
    
    private final ItemFacade itemFacade;
    private final ConditionBuilder conditionBuilder;
    
    public DiscountBuilder(ItemFacade itemFacade, ConditionBuilder conditionBuilder) {
        this.itemFacade = itemFacade;
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
        if (discountDTO == null) {
            throw new IllegalArgumentException("DiscountDTO cannot be null");
        }
        
        if (discountDTO.getType() == null) {
            throw new IllegalArgumentException("Discount type cannot be null");
        }
        
        switch (discountDTO.getType()) {
            case SIMPLE:
                return buildSimpleDiscount(discountDTO);
                
            case AND:
                return buildAndDiscount(discountDTO);
                
            case OR:
                return buildOrDiscount(discountDTO);
                
            case XOR:
                return buildXorDiscount(discountDTO);
                
            case DOUBLE:
                return buildDoubleDiscount(discountDTO);
                
            case MAX:
                return buildMaxDiscount(discountDTO);
                
            default:
                throw new IllegalArgumentException("Unknown discount type: " + discountDTO.getType());
        }
    }
    
    /**
     * Builds a Discount with existing UUID (for updates).
     */
    public Discount buildDiscount(DiscountDTO discountDTO, String id) {
        if (discountDTO == null) {
            throw new IllegalArgumentException("DiscountDTO cannot be null");
        }
        
        if (id == null) {
            throw new IllegalArgumentException("Existing ID cannot be null");
        }
        
        Condition cond = conditionBuilder.buildCondition(discountDTO.getCondition());
        switch(discountDTO.getType()) {
            case SIMPLE:
                return new SimpleDiscount(UUID.fromString(id), itemFacade, discountDTO.getDiscountPercentage(), buildQualifier(discountDTO), cond);
                
            case AND:
                return new AndDiscount(UUID.fromString(id), itemFacade, discountDTO.getSubDiscounts().stream()
                    .map(this::buildDiscount)
                    .collect(Collectors.toSet()));
                    
            case OR:
                return new OrDiscount(UUID.fromString(id), itemFacade, buildDiscount(discountDTO.getSubDiscounts().get(0)), 
                    discountDTO.getSubDiscounts().subList(1, discountDTO.getSubDiscounts().size())
                        .stream()
                        .map(subDto -> conditionBuilder.buildCondition(subDto.getCondition()))
                        .collect(Collectors.toSet()));
                        
            case XOR:
                if (discountDTO.getSubDiscounts().size() != 2) {
                    throw new IllegalArgumentException("XorDiscount requires exactly 2 sub-discounts");
                }
                return new XorDiscount(UUID.fromString(id), itemFacade, buildDiscount(discountDTO.getSubDiscounts().get(0)), 
                    buildDiscount(discountDTO.getSubDiscounts().get(1)));
                    
            case DOUBLE:
                return new DoubleDiscount(UUID.fromString(id), itemFacade, discountDTO.getSubDiscounts().stream()
                    .map(this::buildDiscount)
                    .collect(Collectors.toSet()));
                    
            case MAX:
                return new MaxDiscount(UUID.fromString(id), itemFacade, discountDTO.getSubDiscounts().stream()
                    .map(this::buildDiscount)
                    .collect(Collectors.toSet()));
                    
            default:
                throw new IllegalArgumentException("Unknown discount type: " + discountDTO.getType());
        }
    }
    
    /**
     * Builds a SimpleDiscount with an existing condition ID.
     * 
     * @param discountDTO The discount DTO
     * @param existingCondition The existing condition to use
     * @return The built SimpleDiscount
     */
    public SimpleDiscount buildSimpleDiscountWithCondition(DiscountDTO discountDTO, Condition existingCondition) {
        validateSimpleDiscountDTO(discountDTO);
        
        if (existingCondition == null) {
            throw new IllegalArgumentException("Existing condition cannot be null");
        }
        
        DiscountQualifier qualifier = buildQualifier(discountDTO);
        
        return new SimpleDiscount(itemFacade, discountDTO.getDiscountPercentage(), qualifier, existingCondition);
    }
    
    private SimpleDiscount buildSimpleDiscount(DiscountDTO dto) {
        validateSimpleDiscountDTO(dto);
        
        DiscountQualifier qualifier = buildQualifier(dto);
        Condition condition = conditionBuilder.buildCondition(dto.getCondition());
        
        return new SimpleDiscount(itemFacade, dto.getDiscountPercentage(), qualifier, condition);
    }
    
    private AndDiscount buildAndDiscount(DiscountDTO dto) {
        validateCompositeDiscountDTO(dto);
        
        Set<Discount> discounts = dto.getSubDiscounts().stream()
            .map(this::buildDiscount)
            .collect(Collectors.toSet());
            
        return new AndDiscount(itemFacade, discounts);
    }
    
    private OrDiscount buildOrDiscount(DiscountDTO dto) {
        validateCompositeDiscountDTO(dto);
        
        // OrDiscount needs a base discount and conditions
        // Based on the first sub-discount and remaining conditions
        if (dto.getSubDiscounts().size() < 1) {
            throw new IllegalArgumentException("OrDiscount requires at least one sub-discount");
        }
        
        Discount baseDiscount = buildDiscount(dto.getSubDiscounts().get(0));
        
        // Convert remaining discounts to conditions (this might need domain model adjustment)
        Set<Condition> conditions = dto.getSubDiscounts().subList(1, dto.getSubDiscounts().size())
            .stream()
            .map(subDto -> conditionBuilder.buildCondition(subDto.getCondition()))
            .collect(Collectors.toSet());
            
        return new OrDiscount(itemFacade, baseDiscount, conditions);
    }
    
    private XorDiscount buildXorDiscount(DiscountDTO dto) {
        validateCompositeDiscountDTO(dto);
        
        if (dto.getSubDiscounts().size() != 2) {
            throw new IllegalArgumentException("XorDiscount requires exactly 2 sub-discounts");
        }
        
        Discount discount1 = buildDiscount(dto.getSubDiscounts().get(0));
        Discount discount2 = buildDiscount(dto.getSubDiscounts().get(1));
        
        return new XorDiscount(itemFacade, discount1, discount2);
    }
    
    private DoubleDiscount buildDoubleDiscount(DiscountDTO dto) {
        validateCompositeDiscountDTO(dto);
        
        Set<Discount> discounts = dto.getSubDiscounts().stream()
            .map(this::buildDiscount)
            .collect(Collectors.toSet());
            
        return new DoubleDiscount(itemFacade, discounts);
    }
    
    private MaxDiscount buildMaxDiscount(DiscountDTO dto) {
        validateCompositeDiscountDTO(dto);
        
        Set<Discount> discounts = dto.getSubDiscounts().stream()
            .map(this::buildDiscount)
            .collect(Collectors.toSet());
            
        return new MaxDiscount(itemFacade, discounts);
    }
    
    private DiscountQualifier buildQualifier(DiscountDTO dto) {
        if (dto.getQualifierType() == null) {
            throw new IllegalArgumentException("Qualifier type cannot be null for SimpleDiscount");
        }
        
        switch (dto.getQualifierType()) {
            case PRODUCT:
                if (dto.getQualifierValue() == null || dto.getQualifierValue().trim().isEmpty()) {
                    throw new IllegalArgumentException("Product ID required for ProductQualifier");
                }
                return new ProductQualifier(dto.getQualifierValue());
                
            case CATEGORY:
                if (dto.getQualifierValue() == null || dto.getQualifierValue().trim().isEmpty()) {
                    throw new IllegalArgumentException("Category name required for CategoryQualifier");
                }
                // This would need a way to get Category by name - might need CategoryRepository
                Category category = createCategoryFromName(dto.getQualifierValue());
                return new CategoryQualifier(category);
                
            case STORE:
                return new StoreQualifier();
                
            default:
                throw new IllegalArgumentException("Unknown qualifier type: " + dto.getQualifierType());
        }
    }
    
    private Category createCategoryFromName(String categoryName) {
        // This is a placeholder - would need proper Category creation/lookup
        // Might need to inject a CategoryRepository or CategoryFacade
        return new Category(categoryName, "Default description for " + categoryName);
    }
    
    /**
     * Validates a DiscountDTO for SimpleDiscount creation.
     */
    private void validateSimpleDiscountDTO(DiscountDTO dto) {
        if (dto.getType() != DiscountDTO.DiscountType.SIMPLE) {
            throw new IllegalArgumentException("Expected SIMPLE discount type");
        }
        
        if (dto.getDiscountPercentage() == null) {
            throw new IllegalArgumentException("Discount percentage cannot be null");
        }
        
        if (dto.getDiscountPercentage() < 0 || dto.getDiscountPercentage() > 1) {
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
            case DOUBLE:
            case MAX:
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