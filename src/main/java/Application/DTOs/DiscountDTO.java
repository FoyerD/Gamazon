package Application.DTOs;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import Domain.Store.Discounts.*;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;
import Domain.Store.Discounts.Qualifiers.ProductQualifier;
import Domain.Store.Discounts.Qualifiers.CategoryQualifier;
import Domain.Store.Discounts.Qualifiers.StoreQualifier;

public class DiscountDTO {
    
    public enum DiscountType {
        SIMPLE,
        AND,
        OR,
        XOR,
        DOUBLE,
        MAX
    }
    
    public enum QualifierType {
        PRODUCT,
        CATEGORY,
        STORE
    }
    
    private UUID id;
    private DiscountType type;
    private ConditionDTO condition;
    
    // Fields for simple discounts
    private Float discountPercentage;
    private QualifierType qualifierType;
    private String qualifierValue; // Product ID, Category name, or null for store
    
    // Fields for composite discounts
    private List<DiscountDTO> subDiscounts;
    
    // Default constructor for JSON serialization
    public DiscountDTO() {}
    
    // Constructor for basic initialization
    public DiscountDTO(UUID id, DiscountType type, ConditionDTO condition) {
        this.id = id;
        this.type = type;
        this.condition = condition;
    }
    
    // Factory method to create from domain object
    public static DiscountDTO fromDiscount(Discount discount) {
        if (discount == null) {
            return null;
        }
        
        DiscountDTO dto = new DiscountDTO();
        dto.id = discount.getId();
        dto.condition = ConditionDTO.fromCondition(discount.getCondition());
        
        if (discount instanceof SimpleDiscount) {
            SimpleDiscount simpleDiscount = (SimpleDiscount) discount;
            dto.type = DiscountType.SIMPLE;
            dto.discountPercentage = simpleDiscount.getDiscountPercentage();
            
            // Handle qualifier type
            DiscountQualifier qualifier = simpleDiscount.getQualifier();
            if (qualifier instanceof ProductQualifier) {
                dto.qualifierType = QualifierType.PRODUCT;
                // Note: ProductQualifier doesn't expose productId, might need to be added
                dto.qualifierValue = getProductIdFromQualifier((ProductQualifier) qualifier);
            } else if (qualifier instanceof CategoryQualifier) {
                dto.qualifierType = QualifierType.CATEGORY;
                // Note: CategoryQualifier doesn't expose category, might need to be added
                dto.qualifierValue = getCategoryFromQualifier((CategoryQualifier) qualifier);
            } else if (qualifier instanceof StoreQualifier) {
                dto.qualifierType = QualifierType.STORE;
                dto.qualifierValue = null; // Store qualifier applies to all products
            } else {
                throw new IllegalArgumentException("Unknown qualifier type: " + qualifier.getClass().getSimpleName());
            }
            
        } else if (discount instanceof AndDiscount) {
            AndDiscount andDiscount = (AndDiscount) discount;
            dto.type = DiscountType.AND;
            dto.subDiscounts = andDiscount.getDiscounts().stream()
                .map(DiscountDTO::fromDiscount)
                .collect(Collectors.toList());
                
        } else if (discount instanceof OrDiscount) {
            OrDiscount orDiscount = (OrDiscount) discount;
            dto.type = DiscountType.OR;
            dto.subDiscounts = orDiscount.getDiscounts().stream()
                .map(DiscountDTO::fromDiscount)
                .collect(Collectors.toList());
                
        } else if (discount instanceof XorDiscount) {
            XorDiscount xorDiscount = (XorDiscount) discount;
            dto.type = DiscountType.XOR;
            dto.subDiscounts = xorDiscount.getDiscounts().stream()
                .map(DiscountDTO::fromDiscount)
                .collect(Collectors.toList());
                
        } else if (discount instanceof DoubleDiscount) {
            DoubleDiscount doubleDiscount = (DoubleDiscount) discount;
            dto.type = DiscountType.DOUBLE;
            dto.subDiscounts = doubleDiscount.getDiscounts().stream()
                .map(DiscountDTO::fromDiscount)
                .collect(Collectors.toList());
                
        } else if (discount instanceof MaxDiscount) {
            MaxDiscount maxDiscount = (MaxDiscount) discount;
            dto.type = DiscountType.MAX;
            dto.subDiscounts = maxDiscount.getDiscounts().stream()
                .map(DiscountDTO::fromDiscount)
                .collect(Collectors.toList());
                
        } else {
            throw new IllegalArgumentException("Unknown discount type: " + discount.getClass().getSimpleName());
        }
        
        return dto;
    }
    
    // Helper methods to extract qualifier information
    // Note: These methods may need to be implemented based on the actual qualifier implementations
    private static String getProductIdFromQualifier(ProductQualifier qualifier) {
        // This would require ProductQualifier to expose its productId field
        // For now, return null or implement reflection-based access
        return null; // TODO: Implement based on ProductQualifier structure
    }
    
    private static String getCategoryFromQualifier(CategoryQualifier qualifier) {
        // This would require CategoryQualifier to expose its category field
        // For now, return null or implement reflection-based access
        return null; // TODO: Implement based on CategoryQualifier structure
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public DiscountType getType() {
        return type;
    }
    
    public void setType(DiscountType type) {
        this.type = type;
    }
    
    public ConditionDTO getCondition() {
        return condition;
    }
    
    public void setCondition(ConditionDTO condition) {
        this.condition = condition;
    }
    
    public Float getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Float discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public QualifierType getQualifierType() {
        return qualifierType;
    }
    
    public void setQualifierType(QualifierType qualifierType) {
        this.qualifierType = qualifierType;
    }
    
    public String getQualifierValue() {
        return qualifierValue;
    }
    
    public void setQualifierValue(String qualifierValue) {
        this.qualifierValue = qualifierValue;
    }
    
    public List<DiscountDTO> getSubDiscounts() {
        return subDiscounts;
    }
    
    public void setSubDiscounts(List<DiscountDTO> subDiscounts) {
        this.subDiscounts = subDiscounts;
    }
    
    // Utility methods
    public boolean isSimpleDiscount() {
        return type == DiscountType.SIMPLE;
    }
    
    public boolean isCompositeDiscount() {
        return type != DiscountType.SIMPLE;
    }
    
    public int getSubDiscountCount() {
        return subDiscounts != null ? subDiscounts.size() : 0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DiscountDTO{");
        sb.append("id=").append(id);
        sb.append(", type=").append(type);
        
        if (condition != null) {
            sb.append(", condition=").append(condition.getType());
        }
        
        switch (type) {
            case SIMPLE:
                sb.append(", discountPercentage=").append(discountPercentage);
                sb.append(", qualifierType=").append(qualifierType);
                if (qualifierValue != null) {
                    sb.append(", qualifierValue='").append(qualifierValue).append('\'');
                }
                break;
            case AND:
            case OR:
            case XOR:
            case DOUBLE:
            case MAX:
                sb.append(", subDiscounts=").append(getSubDiscountCount());
                break;
        }
        
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DiscountDTO that = (DiscountDTO) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}