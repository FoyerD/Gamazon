package Application.DTOs;

import java.util.List;

public class ReceiptDTO {
    private final String receiptId;
    private final String clientName;
    private final String storeName;
    private final List<OrderedItemDTO> items;

    public ReceiptDTO(String receiptId, String clientName, String storeName, List<OrderedItemDTO> items) {
        this.receiptId = receiptId;
        this.clientName = clientName;
        this.storeName = storeName;
        this.items = items;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getStoreName() {
        return storeName;
    }

    public List<OrderedItemDTO> getItems() {
        return items;
    }


    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(OrderedItemDTO::getPrice)
                .sum();
    }

}
