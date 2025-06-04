package Domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "guest")
public class Guest extends User{
    public static final String NAME = "Guest";
    protected  Guest() {
        super(NAME);
    }
    public static synchronized Guest createGuest() {
        return new Guest();
    }

    public Member register(String username, String password, String email) {
        return new Member(this.id, username, password, email);
    }


    /***
     * Logs out the guest user from the system.
     * @param loginManager The LoginManager instance to handle the logout process.
     * * This method overrides the logout method in the User class to ensure that the guest user is properly removed from the system.
     */
    @Override
    public void logout(LoginManager loginManager) {
        super.logout(loginManager);
        loginManager.exit(this);
    }


}
