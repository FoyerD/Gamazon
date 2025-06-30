package Application.DTOs;

import java.util.List;
import java.util.stream.Collectors;

import Domain.Store.Discounts.Conditions.AndCondition;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Conditions.MaxPriceCondition;
import Domain.Store.Discounts.Conditions.MaxQuantityCondition;
import Domain.Store.Discounts.Conditions.MinPriceCondition;
import Domain.Store.Discounts.Conditions.MinQuantityCondition;
import Domain.Store.Discounts.Conditions.OrCondition;
import Domain.Store.Discounts.Conditions.TrueCondition;

public class ConditionDTO {
        public enum ConditionType {
        TRUE,
        AND,
        OR,
        XOR,
        MAX_QUANTITY,
        MIN_QUANTITY,
        MAX_PRICE,
        MIN_PRICE
    }
    
    private String id;
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
    public ConditionDTO(ConditionType type) {
        id = "";
        this.type = type;
    }

    public ConditionDTO(String id, ConditionType type) {
        this.id = id;
        this.type = type;
    }
    
    public static ConditionDTO fromCondition(Condition condition) {
        if (condition == null) {
            return null;
        }
        if (condition instanceof MinPriceCondition) {
            return fromMinPriceCondition((MinPriceCondition) condition);
        } else if (condition instanceof MaxPriceCondition) {
            return fromMaxPriceCondition((MaxPriceCondition) condition);
        } else if (condition instanceof MinQuantityCondition) {
            return fromMinQuantityCondition((MinQuantityCondition) condition);
        } else if (condition instanceof MaxQuantityCondition) {
            return fromMaxQuantityCondition((MaxQuantityCondition) condition);
        } else if (condition instanceof AndCondition) {
            return fromAndCondition((AndCondition) condition);
        } else if (condition instanceof OrCondition) {
            return fromOrCondition((OrCondition) condition);
        } else if (condition instanceof TrueCondition) {
            return fromTrueCondition((TrueCondition) condition);
        } else {
            throw new IllegalArgumentException("Unknown condition type: " + condition.getClass().getSimpleName());
        }
    }

    private static ConditionDTO fromMinPriceCondition(MinPriceCondition condition) {
        ConditionDTO dto = new ConditionDTO();
        dto.id = condition.getId();
        dto.type = ConditionType.MIN_PRICE;
        dto.minPrice = condition.getMinPrice();
        return dto;
    }

    private static ConditionDTO fromMaxPriceCondition(MaxPriceCondition condition) {
        ConditionDTO dto = new ConditionDTO();
        dto.id = condition.getId();
        dto.type = ConditionType.MAX_PRICE;
        dto.maxPrice = condition.getMaxPrice();
        return dto;
    }

    private static ConditionDTO fromMinQuantityCondition(MinQuantityCondition condition) {
        ConditionDTO dto = new ConditionDTO();
        dto.id = condition.getId();
        dto.type = ConditionType.MIN_QUANTITY;
        dto.productId = condition.getProductId();
        dto.minQuantity = condition.getMinQuantity();
        return dto;
    }

    private static ConditionDTO fromMaxQuantityCondition(MaxQuantityCondition condition) {
        ConditionDTO dto = new ConditionDTO();
        dto.id = condition.getId();
        dto.type = ConditionType.MAX_QUANTITY;
        dto.productId = condition.getProductId();
        dto.maxQuantity = condition.getMaxQuantity();
        return dto;
    }

    private static ConditionDTO fromAndCondition(AndCondition condition) {
        ConditionDTO dto = new ConditionDTO();
        dto.id = condition.getId();
        dto.type = ConditionType.AND;
        dto.subConditions = condition.getConditions().stream()
            .map(ConditionDTO::fromCondition)
            .collect(Collectors.toList());
        return dto;
    }

    private static ConditionDTO fromOrCondition(OrCondition condition) {
        ConditionDTO dto = new ConditionDTO();
        dto.id = condition.getId();
        dto.type = ConditionType.OR;
        dto.subConditions = condition.getConditions().stream()
            .map(ConditionDTO::fromCondition)
            .collect(Collectors.toList());
        return dto;
    }

    private static ConditionDTO fromTrueCondition(TrueCondition condition) {
        ConditionDTO dto = new ConditionDTO();
        dto.id = condition.getId();
        dto.type = ConditionType.TRUE;
        return dto;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConditionDTO that = (ConditionDTO) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }
}