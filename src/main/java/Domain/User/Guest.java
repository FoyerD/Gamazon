package Domain.User;

public class Guest extends User{
    private Guest() {
        super("Guest");
    }
    public static Guest createGuest() {
        return new Guest();
    }

    @Override
    public Member register(String username, String password, String email) {
        return new Member(this.id, username, password, email);
    }

    @Override
    public void visitExit(LoginManager loginManager) {
        super.visitExit(loginManager);
        loginManager.exit(this);
    }


}
