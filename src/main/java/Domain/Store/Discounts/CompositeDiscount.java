package Domain.Store.Discounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;
import jakarta.persistence.*;


@Entity
@Table(name = "composite_discount")
public abstract class CompositeDiscount extends Discount {

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "composite_discount_children",
               joinColumns = @JoinColumn(name = "parent_id"),
               inverseJoinColumns = @JoinColumn(name = "child_id"))
    protected List<Discount> discounts;
    protected MergeType mergeType;

    // Constructor for loading from repository with existing ID
    public CompositeDiscount(String id, String storeId, List<Discount> discounts, Condition condition, MergeType mergeType, String description) {
        super(id, storeId, condition, description);
        if(mergeType == null) {
            throw new IllegalArgumentException("MergeType cannot be null");
        }
        if(discounts == null || discounts.isEmpty()) {
            throw new IllegalArgumentException("Discounts list cannot be null or empty");
        }
        this.mergeType = mergeType;
        this.discounts = discounts;
    }

    public CompositeDiscount(String id, String storeId, List<Discount> discounts, Condition condition, MergeType mergeType) {
        this(id, storeId, discounts, condition, mergeType, "Default Composite Discount Description");
    }

    protected List<Map<String, ItemPriceBreakdown>> calculateAllSubDiscounts(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter) {
        List<Map<String, ItemPriceBreakdown>> allSubDiscounts = new ArrayList<>(discounts.size());
        for (Discount discount : discounts) {
            allSubDiscounts.add(discount.calculatePrice(basket, itemGetter));
        }
        return allSubDiscounts;
    }

    // Getter for repository serialization
    public List<Discount> getDiscounts() {
        if (discounts == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(discounts);
        }
    }


    public MergeType getMergeType() {
        return mergeType;
    }
}