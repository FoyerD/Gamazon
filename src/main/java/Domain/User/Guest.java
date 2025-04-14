package Domain.User;

public class Guest extends User{
    private Guest() {
        super("Guest");
    }
    public static Guest createGuest() {
        return new Guest();
    }


}
