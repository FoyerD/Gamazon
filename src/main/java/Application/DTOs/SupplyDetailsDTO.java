package Application.DTOs;

import java.time.LocalDate;

import Domain.Shopping.PaymentDetails;
import Domain.Shopping.SupplyDetails;

public class SupplyDetailsDTO {
    private final String deliveryAddress;
    private final String city;
    private final String country;
    private final String zipCode;

    public SupplyDetailsDTO(String deliveryAddress, String city, String country, String zipCode, String holder) {
        this.deliveryAddress = deliveryAddress;
        this.city = city;
        this.country = country;
        this.zipCode = zipCode;
    }

    public SupplyDetailsDTO(SupplyDetails supplyDetails) {
        this.deliveryAddress = supplyDetails.getDeliveryAddress();
        this.city = supplyDetails.getCity();
        this.country = supplyDetails.getCountry();
        this.zipCode = supplyDetails.getZipCode();
    }

    public static SupplyDetailsDTO from(SupplyDetails supplyDetails) {
        return new SupplyDetailsDTO(supplyDetails);
    }

    public SupplyDetails toSupplyDetails() {
        return new SupplyDetails(this.deliveryAddress, this.city, this.country, this.zipCode);
    }

    public String getDeliveryAddress() { return deliveryAddress; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getZipCode() { return zipCode; }
}
