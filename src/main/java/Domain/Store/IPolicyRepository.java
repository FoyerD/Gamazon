
package Domain.Store;


import java.util.List;

import Domain.ILockbasedRepository;
import Domain.Store.Policy;

public abstract class IPolicyRepository extends ILockbasedRepository<Policy, String> 
{
    abstract public List<Policy> getAllStorePolicies(String StoreId);
}
