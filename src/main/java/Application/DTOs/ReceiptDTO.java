package Application.DTOs;

import java.util.List;

public class ReceiptDTO {
    private final String receiptId;
    private final String clientName;
    private final List<OrderedItemDTO> items;

    public ReceiptDTO(String receiptId, String clientName, List<OrderedItemDTO> items) {
        this.receiptId = receiptId;
        this.clientName = clientName;
        this.items = items;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getClientName() {
        return clientName;
    }

    public List<OrderedItemDTO> getItems() {
        return items;
    }

}
