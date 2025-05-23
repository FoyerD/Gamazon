package Domain.Store.Discounts;

import java.util.LinkedList;
import java.util.List;

// the price breakdown class is used to store the price breakdown of a product
// along the calculation of the discounts

public class PriceBreakDown {
    // Class implementation

    private List<String> descriptions;



    public List<String> getDescriptions() {
        return new LinkedList<>(descriptions);
    }

    

}
