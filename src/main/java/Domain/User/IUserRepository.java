package Domain.User;


public interface IUserRepository {
    public User getUser(String id);
    public Guest createGuest();
    public boolean remove(String id);
    


    public String getMemberUserName(String id);
    public boolean userIsMember(String id);
    public User getMarketManager();
}
