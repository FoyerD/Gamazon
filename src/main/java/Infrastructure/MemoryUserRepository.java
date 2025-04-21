package Infrastructure;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Domain.User.IUserRepository;
import Domain.User.User;

class MemoryItemRepository implements IUserRepository {
    Map<String, User> users;
    public MemoryItemRepository() {
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
    public User update(String id, User user) {
        if (users.containsKey(id)) {
            users.put(id, user); // User updated successfully
            return user;
        }
        return null; // User not found
    }
    @Override
    public User getUserByUsername(String username) {
        for (User user : users.values()) {
            if (user.getName().equals(username)) {
                return user; // User found
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
        return users.containsKey(id); // Returns true if user is a member
    }
    
}
