package Domain;

public interface IRepository<V, K> {
    // Generic methods for repository
    // T - the type of the item
    // K - the type of the key (ID)
    boolean add(K key, V item);
    V remove(K id);
    V get(K id);
}