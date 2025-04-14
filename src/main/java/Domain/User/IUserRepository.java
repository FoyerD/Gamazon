package Domain.User;

public interface IUserRepository {
    public User getUser(String username);
    public Guest createGuest();
    
}
