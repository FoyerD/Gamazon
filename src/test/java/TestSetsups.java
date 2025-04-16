import Domain.User.Member;


public class TestSetsups {
    public static Member register(String username, String password) {
        return new Member(username);
    }

    public static Member login(String username, String password) {
        return new Member(username);
    }
    
}
