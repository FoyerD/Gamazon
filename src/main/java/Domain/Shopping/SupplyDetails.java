package Domain.Shopping;

import java.time.LocalDate;

import jakarta.persistence.Embeddable;

@Embeddable
public class SupplyDetails {

    private String deliveryAddress;
    private String city;
    private String country;
    private String zipCode;

    protected SupplyDetails() {} // Required by JPA

    public SupplyDetails(String deliveryAddress, String city, String country, String zipCode) {
        this.deliveryAddress = deliveryAddress;
        this.city = city;
        this.country = country;
        this.zipCode = zipCode;
    }

    public String getDeliveryAddress() { return deliveryAddress; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getZipCode() { return zipCode; }
}
