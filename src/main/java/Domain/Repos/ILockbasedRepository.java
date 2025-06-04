package Domain.Repos;

import java.util.Map;

public abstract class ILockbasedRepository<V, K> implements IRepository<V, K> {
    private Map<K, Object> locks = new java.util.concurrent.ConcurrentHashMap<>();
    
    public ILockbasedRepository() {
        this.locks = new java.util.concurrent.ConcurrentHashMap<>();
    }

    public Object getLock(K id) {
        return locks.get(id);
    }
    
    public boolean addLock(K id) {
        return locks.putIfAbsent(id, new Object()) == null;
    }
    
    protected void removeLock(K id) {
        locks.remove(id);
    }
    
    protected boolean isIdValid(K id) {
        return id != null && !id.toString().trim().isEmpty();
    }

    protected void deleteAllLocks() {
        locks.clear();
    }
}