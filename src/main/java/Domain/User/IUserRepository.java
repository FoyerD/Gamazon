package Domain.User;

public interface IUserRepository {
    public User getUser(String id);
    public Guest createGuest();
    public boolean remove(String id);
    
}
