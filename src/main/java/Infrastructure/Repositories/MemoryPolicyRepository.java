package Infrastructure.Repositories;

import Domain.Store.Store;
import Domain.Store.Policies.IPolicy;

import java.security.Policy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryPolicyRepository extends Domain.Store.IPolicyRepository {
    private final Map<String, IPolicy> policies = new ConcurrentHashMap<>();

    @Override
    public List<IPolicy> getAllStorePolicies(String storeId) 
    {
        return policies.values().stream()
                       .filter(p -> storeId.equals(p.getStoreId()))
                       .collect(Collectors.toList());
    }

    @Override
    public boolean add(String id, IPolicy value) 
    {
        return policies.putIfAbsent(id, value) == null;
    }

    @Override
    public IPolicy remove(String id) 
    {
        return policies.remove(id);
    }

    @Override
    public IPolicy get(String id) 
    {
        return policies.get(id);
    }

    @Override
    public IPolicy update(String id, IPolicy value) 
    {
        if (!policies.containsKey(id)) 
        {
            return null;
        }
        policies.put(id, value);
        return value;
    }
}
