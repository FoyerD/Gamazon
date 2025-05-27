package Application.DTOs;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import Domain.Store.Discounts.Conditions.*;

public class ConditionDTO {
    
    public enum ConditionType {
        MIN_PRICE,
        MAX_PRICE,
        MIN_QUANTITY,
        MAX_QUANTITY,
        AND,
        OR,
        TRUE
    }
    
    private UUID id;
    private ConditionType type;
    
    // Fields for simple conditions
    private Double minPrice;
    private Double maxPrice;
    private String productId;
    private Integer minQuantity;
    private Integer maxQuantity;
    
    // Fields for composite conditions
    private List<ConditionDTO> subConditions;
    
    // Default constructor for JSON serialization
    public ConditionDTO() {}
    
    // Constructor for simple conditions
    public ConditionDTO(UUID id, ConditionType type) {
        this.id = id;
        this.type = type;
    }
    
    // Factory methods for creating from domain objects
    public static ConditionDTO fromCondition(Condition condition) {
        if (condition == null) {
            return null;
        }
        
        ConditionDTO dto = new ConditionDTO();
        dto.id = condition.getId();
        
        if (condition instanceof MinPriceCondition) {
            MinPriceCondition minPriceCondition = (MinPriceCondition) condition;
            dto.type = ConditionType.MIN_PRICE;
            dto.minPrice = minPriceCondition.getMinPrice();
            
        } else if (condition instanceof MaxPriceCondition) {
            MaxPriceCondition maxPriceCondition = (MaxPriceCondition) condition;
            dto.type = ConditionType.MAX_PRICE;
            dto.maxPrice = maxPriceCondition.getMaxPrice();
            
        } else if (condition instanceof MinQuantityCondition) {
            MinQuantityCondition minQuantityCondition = (MinQuantityCondition) condition;
            dto.type = ConditionType.MIN_QUANTITY;
            dto.productId = minQuantityCondition.getProductId();
            dto.minQuantity = minQuantityCondition.getMinQuantity();
            
        } else if (condition instanceof MaxQuantityCondition) {
            MaxQuantityCondition maxQuantityCondition = (MaxQuantityCondition) condition;
            dto.type = ConditionType.MAX_QUANTITY;
            dto.productId = maxQuantityCondition.getProductId();
            dto.maxQuantity = maxQuantityCondition.getMaxQuantity();
            
        } else if (condition instanceof AndCondition) {
            AndCondition andCondition = (AndCondition) condition;
            dto.type = ConditionType.AND;
            dto.subConditions = andCondition.getConditions().stream()
                .map(ConditionDTO::fromCondition)
                .collect(Collectors.toList());
                
        } else if (condition instanceof OrCondition) {
            OrCondition orCondition = (OrCondition) condition;
            dto.type = ConditionType.OR;
            dto.subConditions = orCondition.getConditions().stream()
                .map(ConditionDTO::fromCondition)
                .collect(Collectors.toList());
                
        } else if (condition instanceof TrueCondition) {
            dto.type = ConditionType.TRUE;
            
        } else {
            throw new IllegalArgumentException("Unknown condition type: " + condition.getClass().getSimpleName());
        }
        
        return dto;
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public ConditionType getType() {
        return type;
    }
    
    public void setType(ConditionType type) {
        this.type = type;
    }
    
    public Double getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }
    
    public Double getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public Integer getMinQuantity() {
        return minQuantity;
    }
    
    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }
    
    public Integer getMaxQuantity() {
        return maxQuantity;
    }
    
    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }
    
    public List<ConditionDTO> getSubConditions() {
        return subConditions;
    }
    
    public void setSubConditions(List<ConditionDTO> subConditions) {
        this.subConditions = subConditions;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConditionDTO{");
        sb.append("id=").append(id);
        sb.append(", type=").append(type);
        
        switch (type) {
            case MIN_PRICE:
                sb.append(", minPrice=").append(minPrice);
                break;
            case MAX_PRICE:
                sb.append(", maxPrice=").append(maxPrice);
                break;
            case MIN_QUANTITY:
                sb.append(", productId='").append(productId).append('\'');
                sb.append(", minQuantity=").append(minQuantity);
                break;
            case MAX_QUANTITY:
                sb.append(", productId='").append(productId).append('\'');
                sb.append(", maxQuantity=").append(maxQuantity);
                break;
            case AND:
            case OR:
                sb.append(", subConditions=").append(subConditions != null ? subConditions.size() : 0);
                break;
            case TRUE:
                // No additional fields
                break;
        }
        
        sb.append('}');
        return sb.toString();
    }
}