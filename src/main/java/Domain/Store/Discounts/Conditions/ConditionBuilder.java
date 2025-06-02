package Domain.Store.Discounts.Conditions;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import Application.DTOs.ConditionDTO;


/**
 * Builder class for creating Condition domain objects from DTOs.
 * Handles the conversion between UI DTOs and domain objects.
 */
@Component
public class ConditionBuilder {
    
    /**
     * Builds a Condition domain object from a ConditionDTO.
     * 
     * @param conditionDTO The DTO containing condition data
     * @return The corresponding Condition domain object
     * @throws IllegalArgumentException if the DTO is invalid or has unknown type
     */
    public Condition buildCondition(ConditionDTO conditionDTO) {
        if (conditionDTO == null) {
            throw new IllegalArgumentException("ConditionDTO cannot be null");
        }
        
        if (conditionDTO.getType() == null) {
            throw new IllegalArgumentException("Condition type cannot be null");
        }
        
        switch (conditionDTO.getType()) {
            case MIN_PRICE:
                return buildMinPriceCondition(conditionDTO);
                
            case MAX_PRICE:
                return buildMaxPriceCondition(conditionDTO);
                
            case MIN_QUANTITY:
                return buildMinQuantityCondition(conditionDTO);
                
            case MAX_QUANTITY:
                return buildMaxQuantityCondition(conditionDTO);
                
            case AND:
                return buildAndCondition(conditionDTO);
                
            case OR:
                return buildOrCondition(conditionDTO);
                
            case TRUE:
                return buildTrueCondition(conditionDTO);
                
            default:
                throw new IllegalArgumentException("Unknown condition type: " + conditionDTO.getType());
        }
    }
    
    /**
     * Builds a Condition with existing UUID (for updates).
     */
    public Condition buildConditionWithId(ConditionDTO conditionDTO, String existingId) {
        if (conditionDTO == null) {
            throw new IllegalArgumentException("ConditionDTO cannot be null");
        }
        
        if (existingId == null) {
            throw new IllegalArgumentException("Existing ID cannot be null");
        }
        
        switch (conditionDTO.getType()) {
            case MIN_PRICE:
                return new MinPriceCondition(existingId, conditionDTO.getMinPrice());
                
            case MAX_PRICE:
                return new MaxPriceCondition(existingId, conditionDTO.getMaxPrice());
                
            case MIN_QUANTITY:
                return new MinQuantityCondition(existingId, conditionDTO.getProductId(), conditionDTO.getMinQuantity());
                
            case MAX_QUANTITY:
                return new MaxQuantityCondition(existingId, conditionDTO.getProductId(), conditionDTO.getMaxQuantity());
                
            case AND:
                List<Condition> andConditions = conditionDTO.getSubConditions().stream()
                    .map(this::buildCondition)
                    .collect(Collectors.toList());
                return new AndCondition(existingId, andConditions);
                
            case OR:
                List<Condition> orConditions = conditionDTO.getSubConditions().stream()
                    .map(this::buildCondition)
                    .collect(Collectors.toList());
                return new OrCondition(existingId, orConditions);
                
            case TRUE:
                return new TrueCondition(existingId);
                
            default:
                throw new IllegalArgumentException("Unknown condition type: " + conditionDTO.getType());
        }
    }
    
    private MinPriceCondition buildMinPriceCondition(ConditionDTO dto) {
        if (dto.getMinPrice() == null) {
            throw new IllegalArgumentException("Min price cannot be null for MinPriceCondition");
        }
        if (dto.getMinPrice() < 0) {
            throw new IllegalArgumentException("Min price cannot be negative");
        }
        
        return new MinPriceCondition(dto.getMinPrice());
    }
    
    private MaxPriceCondition buildMaxPriceCondition(ConditionDTO dto) {
        if (dto.getMaxPrice() == null) {
            throw new IllegalArgumentException("Max price cannot be null for MaxPriceCondition");
        }
        if (dto.getMaxPrice() < 0) {
            throw new IllegalArgumentException("Max price cannot be negative");
        }
        
        return new MaxPriceCondition(dto.getMaxPrice());
    }
    
    private MinQuantityCondition buildMinQuantityCondition(ConditionDTO dto) {
        if (dto.getProductId() == null || dto.getProductId().trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for MinQuantityCondition");
        }
        if (dto.getMinQuantity() == null) {
            throw new IllegalArgumentException("Min quantity cannot be null for MinQuantityCondition");
        }
        if (dto.getMinQuantity() < 0) {
            throw new IllegalArgumentException("Min quantity cannot be negative");
        }
        
        return new MinQuantityCondition(dto.getProductId(), dto.getMinQuantity());
    }
    
    private MaxQuantityCondition buildMaxQuantityCondition(ConditionDTO dto) {
        if (dto.getProductId() == null || dto.getProductId().trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for MaxQuantityCondition");
        }
        if (dto.getMaxQuantity() == null) {
            throw new IllegalArgumentException("Max quantity cannot be null for MaxQuantityCondition");
        }
        if (dto.getMaxQuantity() < 0) {
            throw new IllegalArgumentException("Max quantity cannot be negative");
        }
        
        return new MaxQuantityCondition(dto.getProductId(), dto.getMaxQuantity());
    }
    
    private AndCondition buildAndCondition(ConditionDTO dto) {
        if (dto.getSubConditions() == null || dto.getSubConditions().isEmpty()) {
            throw new IllegalArgumentException("Sub-conditions cannot be null or empty for AndCondition");
        }
        
        List<Condition> conditions = dto.getSubConditions().stream()
            .map(this::buildCondition)
            .collect(Collectors.toList());
            
        return new AndCondition(conditions);
    }
    
    private OrCondition buildOrCondition(ConditionDTO dto) {
        if (dto.getSubConditions() == null || dto.getSubConditions().isEmpty()) {
            throw new IllegalArgumentException("Sub-conditions cannot be null or empty for OrCondition");
        }
        
        List<Condition> conditions = dto.getSubConditions().stream()
            .map(this::buildCondition)
            .collect(Collectors.toList());
            
        return new OrCondition(conditions);
    }
    
    private TrueCondition buildTrueCondition(ConditionDTO dto) {
        return new TrueCondition();
    }
    
    /**
     * Validates a list of ConditionDTOs.
     * 
     * @param conditionDTOs List of DTOs to validate
     * @throws IllegalArgumentException if any DTO is invalid
     */
    public void validateConditionDTOs(List<ConditionDTO> conditionDTOs) {
        if (conditionDTOs == null) {
            return; // Allow null lists
        }
        
        for (ConditionDTO dto : conditionDTOs) {
            validateConditionDTO(dto);
        }
    }
    
    /**
     * Validates a single ConditionDTO.
     * 
     * @param conditionDTO DTO to validate
     * @throws IllegalArgumentException if the DTO is invalid
     */
    public void validateConditionDTO(ConditionDTO conditionDTO) {
        if (conditionDTO == null) {
            throw new IllegalArgumentException("ConditionDTO cannot be null");
        }
        
        if (conditionDTO.getType() == null) {
            throw new IllegalArgumentException("Condition type cannot be null");
        }
        
        // Validate type-specific fields
        switch (conditionDTO.getType()) {
            case MIN_PRICE:
                if (conditionDTO.getMinPrice() == null || conditionDTO.getMinPrice() < 0) {
                    throw new IllegalArgumentException("Valid min price required for MinPriceCondition");
                }
                break;
                
            case MAX_PRICE:
                if (conditionDTO.getMaxPrice() == null || conditionDTO.getMaxPrice() < 0) {
                    throw new IllegalArgumentException("Valid max price required for MaxPriceCondition");
                }
                break;
                
            case MIN_QUANTITY:
                if (conditionDTO.getProductId() == null || conditionDTO.getProductId().trim().isEmpty()) {
                    throw new IllegalArgumentException("Product ID required for MinQuantityCondition");
                }
                if (conditionDTO.getMinQuantity() == null || conditionDTO.getMinQuantity() < 0) {
                    throw new IllegalArgumentException("Valid min quantity required for MinQuantityCondition");
                }
                break;
                
            case MAX_QUANTITY:
                if (conditionDTO.getProductId() == null || conditionDTO.getProductId().trim().isEmpty()) {
                    throw new IllegalArgumentException("Product ID required for MaxQuantityCondition");
                }
                if (conditionDTO.getMaxQuantity() == null || conditionDTO.getMaxQuantity() < 0) {
                    throw new IllegalArgumentException("Valid max quantity required for MaxQuantityCondition");
                }
                break;
                
            case AND:
            case OR:
                if (conditionDTO.getSubConditions() == null || conditionDTO.getSubConditions().isEmpty()) {
                    throw new IllegalArgumentException("Sub-conditions required for composite conditions");
                }
                // Recursively validate sub-conditions
                validateConditionDTOs(conditionDTO.getSubConditions());
                break;
                
            case TRUE:
                // No validation needed for TrueCondition
                break;
                
            default:
                throw new IllegalArgumentException("Unknown condition type: " + conditionDTO.getType());
        }
    }
}