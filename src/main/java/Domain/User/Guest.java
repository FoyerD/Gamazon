package Domain.User;

public class Guest extends User{
    private Guest() {
        super("Guest");
    }
    public static Guest createGuest() {
        return new Guest();
    }

    @Override
    public void visitExit(LoginManager loginManager) {
        super.visitExit(loginManager);
        loginManager.exit(this);
    }

}
