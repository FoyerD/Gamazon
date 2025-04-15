package Domain;

public interface IRepository<T, K> {
    // Generic methods for repository
    // T - the type of the item
    // K - the type of the key (ID)
    void add(T item);
    void remove(K id);
    T get(K id);
}