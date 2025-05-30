package Infrastructure.MemoryRepositories;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Repos.IUserRepository;
import Domain.User.Guest;
import Domain.User.Member;
import Domain.User.User;

@Repository
@Profile("dev")
public class MemoryUserRepository extends IUserRepository {
    Map<String, User> users;
    public MemoryUserRepository() {
        this.users = new ConcurrentHashMap<String, User>();
    }
    @Override
    public boolean add(String id, User user) 
    {
        if (users.containsKey(id)) {
            return false; // User already exists
        }
        users.put(id, user);
        return true; // User added successfully
    }
    @Override
    public User remove(String id) {
        if (users.containsKey(id)) {
            return users.remove(id); // User removed successfully
        }
        return null; // User not found
    }
    @Override
    public User get(String id) {
        return users.get(id); // Returns null if user not found
    }

    @Override
    public Member getMember(String id) {
        User user = users.get(id);
        if (user instanceof Member)
            return (Member) user;
        return null;
    }

    @Override
    public Guest getGuest(String id) {
        User user = users.get(id);
        if (user instanceof Guest)
            return (Guest) user;
        return null;
    }

    @Override
    public User update(String id, User user) {
        if (users.containsKey(id)) {
            users.put(id, user); 
            return user;
        }
        return null;
    }
    
    @Override
    public Member getMemberByUsername(String username) {
        if (username.equals(Guest.NAME)) {
            return null;
        }
        for (User user : users.values()) {
            if (user instanceof Member && user.getName().equals(username)) {
                return (Member)user; // User found
            }
        }
        
        return null; // User not found
    }
    @Override
    public String getMemberUsername(String id) {
        User user = users.get(id);
        if (user != null) {
            return user.getName(); // Returns the username of the member
        }
        return null; // User not found
    }
    @Override
    public boolean userIsMember(String id) {
        return users.containsKey(id);
    }
    @Override
    public List<Member> getAllMembers() {
        return users.values().stream()
                .filter(user -> user instanceof Member)
                .map(user -> (Member) user)
                .toList(); // Returns a list of all members
    }
    
    @Override
    public void deleteAll() {
        users.clear(); // Clears all users from the repository
        this.deleteAllLocks(); // Deletes all locks associated with users
    }
    
}
