package Application.DTOs;

import java.util.List;

import Domain.Shopping.Receipt;

public class ReceiptDTO {
    private final String receiptId;
    private final String clientName;
    private final List<OrderedItemDTO> items;

    public ReceiptDTO(String receiptId, String clientName, List<OrderedItemDTO> items) {
        this.receiptId = receiptId;
        this.clientName = clientName;
        this.items = items;

    }

}
