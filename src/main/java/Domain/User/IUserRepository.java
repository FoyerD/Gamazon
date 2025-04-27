package Domain.User;

import Domain.ILockbasedRepository;

public abstract class IUserRepository extends ILockbasedRepository<User, String> {
    abstract public boolean add(String id, User user);
    abstract public User remove(String id);
    abstract public User get(String id);
    abstract public Guest getGuest(String id);
    abstract public Member getMember(String id);
    abstract public User update(String id, User user);

    abstract public Member getMemberByUsername(String username);

    abstract public String getMemberUsername(String id);
    abstract public boolean userIsMember(String id);
}
