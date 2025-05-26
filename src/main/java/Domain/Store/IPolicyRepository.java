
package Domain.Store;


import java.util.List;

import Domain.ILockbasedRepository;
import Domain.Store.Policies.IPolicy;

public abstract class IPolicyRepository extends ILockbasedRepository<IPolicy, String> 
{
    abstract public List<IPolicy> getAllStorePolicies(String StoreId);
}
