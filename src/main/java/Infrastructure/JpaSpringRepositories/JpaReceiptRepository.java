package Infrastructure.JpaSpringRepositories;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Pair;
import Domain.Repos.IReceiptRepository;
import Domain.Shopping.Receipt;
import Domain.Store.Product;

@Repository
@Profile("prod")
public class JpaReceiptRepository extends IReceiptRepository {

    private final IJpaReceiptRepository jpaReceiptRepository;

    public JpaReceiptRepository(IJpaReceiptRepository jpaReceiptRepository) {
        this.jpaReceiptRepository = jpaReceiptRepository;
    }

    @Override
    public String saveReceipt(Receipt receipt) {
        jpaReceiptRepository.save(receipt);
        return receipt.getReceiptId();
    }

    @Override
    public String savePurchase(String clientId, String storeId, Map<Product, Pair<Integer, Double>> products,
            double totalPrice, String paymentDetails) {
        Receipt receipt = new Receipt(clientId, storeId, products, totalPrice, paymentDetails);
        return saveReceipt(receipt);
    }

    @Override
    public Receipt getReceipt(String receiptId) {
        return jpaReceiptRepository.findById(receiptId).orElse(null);
    }

    @Override
    public List<Receipt> getClientReceipts(String clientId) {
        return jpaReceiptRepository.getByClientId(clientId);
    }

    @Override
    public List<Receipt> getStoreReceipts(String storeId) {
        return jpaReceiptRepository.getByStoreId(storeId);
    }

    @Override
    public List<Receipt> getClientStoreReceipts(String clientId, String storeId) {
        return jpaReceiptRepository.getClientStoreReceipts(clientId, storeId);
    }

    @Override
    public void clear() {
        jpaReceiptRepository.deleteAll();
    }

    @Override
    public void deleteAll() {
        jpaReceiptRepository.deleteAll();
    }

    @Override
    public boolean add(String id, Receipt value) {
        if (jpaReceiptRepository.existsById(id)) {
            return false;
        }
        jpaReceiptRepository.save(value);
        return true;
    }

    @Override
    public Receipt remove(String id) {
        Receipt existing = jpaReceiptRepository.findById(id).orElse(null);
        if (existing != null) {
            jpaReceiptRepository.deleteById(id);
        }
        return existing;
    }

    @Override
    public Receipt get(String id) {
        return jpaReceiptRepository.findById(id).orElse(null);
    }

    @Override
    public Receipt update(String id, Receipt value) {
        if (!jpaReceiptRepository.existsById(id)) {
            return null;
        }
        return jpaReceiptRepository.save(value);
    }
} 