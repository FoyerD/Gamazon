package Domain;

public interface IRepository<V, K> {
    // Generic methods for repository
    // V - the type of the value
    // K - the type of the key (ID)
    boolean add(K key, V value);
    V remove(K id);
    V get(K id);
    V update(K id, V value);
}