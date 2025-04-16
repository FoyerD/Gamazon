import Domain.User.Member;
import Domain.User.User;

public class TestSetsups {
    public static Member register(String username, String password) {
        return new Member();
    }

    public static Member login(String username, String password) {
        return new Member();
    }
    
}
