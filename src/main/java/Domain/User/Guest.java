package Domain.User;

public class Guest extends User{
    public static final String NAME = "Guest";
    private Guest() {
        super(NAME);
    }
    public static Guest createGuest() {
        return new Guest();
    }

    public Member register(String username, String password, String email) {
        return new Member(this.id, username, password, email);
    }

    @Override
    public void logout(LoginManager loginManager) {
        super.logout(loginManager);
        loginManager.exit(this);
    }


}
