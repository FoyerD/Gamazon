package Application.DTOs;

import java.util.List;
public class ClientOrderDTO {
    String receiptId;
    String clientName;
    List<ClientItemDTO> items;

    public ClientOrderDTO(String receiptId, String clientName, List<ClientItemDTO> items) {
        this.receiptId = receiptId;
        this.clientName = clientName;
        this.items = items;
    }

    public String getClientName() {
        return clientName;
    }

    public List<ClientItemDTO> getItems() {
        return items;
    }   

    public Double getTotalPrice() {
        return items.stream()
                    .mapToDouble(i -> i.getPrice() * i.getQuantity())
                    .sum();
    }


}
