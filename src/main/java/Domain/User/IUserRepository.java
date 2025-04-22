package Domain.User;

import Domain.IRepository;

public interface IUserRepository extends IRepository<User, String> {
    public boolean add(String id, User user);
    public User remove(String id);
    public User get(String id);
    public Guest getGuest(String id);
    public Member getMember(String id);
    public User update(String id, User user);

    public Member getMemberByUsername(String username);

    public String getMemberUsername(String id);
    public boolean userIsMember(String id);
}
