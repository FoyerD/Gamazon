package Infrastructure.JpaSpringRepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Domain.Shopping.Receipt;

public interface IJpaReceiptRepository extends JpaRepository<Receipt, String> {

    /**
     * Retrieves all receipts for a specific client.
     */
    List<Receipt> getByClientId(String clientId);

    /**
     * Retrieves all receipts for a specific store.
     */
    List<Receipt> getByStoreId(String storeId);

    /**
     * Retrieves all receipts for a specific client at a specific store.
     */
    @Query("SELECT r FROM Receipt r WHERE r.clientId = :clientId AND r.storeId = :storeId")
    List<Receipt> getClientStoreReceipts(@Param("clientId") String clientId, @Param("storeId") String storeId);
} 