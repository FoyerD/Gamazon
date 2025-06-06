
package Domain.Repos;


import java.util.List;

import Domain.Store.Policy;

public abstract class IPolicyRepository extends ILockbasedRepository<Policy, String> 
{
    abstract public List<Policy> getAllStorePolicies(String StoreId);
}
