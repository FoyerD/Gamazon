package Infrastructure.JpaSpringRepositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Repos.IUserRepository;
import Domain.User.Guest;
import Domain.User.Member;
import Domain.User.User;

@Repository
@Profile("prod")
public class JpaUserSpringRepository extends IUserRepository {

    private final IJpaUserSpringRepository jpaRepo;

    public JpaUserSpringRepository(IJpaUserSpringRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public void deleteAll() {
        jpaRepo.deleteAll();
    }

    @Override
    public boolean add(String id, User user) {
        if (user == null) return false;
        jpaRepo.save(user);
        return true;
    }

    @Override
    public User remove(String id) {
        UUID uuid = UUID.fromString(id);
        Optional<User> user = jpaRepo.findById(uuid);
        user.ifPresent(jpaRepo::delete);
        return user.orElse(null);
    }

    @Override
    public User get(String id) {
        UUID uuid = UUID.fromString(id);
        return jpaRepo.findById(uuid).orElse(null);
    }

    @Override
    public Guest getGuest(String id) {
        UUID uuid = UUID.fromString(id);
        return jpaRepo.findGuestById(uuid).orElse(null);
    }

    @Override
    public Member getMember(String id) {
        UUID uuid = UUID.fromString(id);
        return jpaRepo.findMemberById(uuid).orElse(null);
    }

    @Override
    public User update(String id, User user) {
        if (user == null) return null;
        return jpaRepo.save(user);
    }

    @Override
    public Member getMemberByUsername(String username) {
        return jpaRepo.findMemberByUsername(username).orElse(null);
    }

    @Override
    public String getMemberUsername(String id) {
        UUID uuid = UUID.fromString(id);
        return jpaRepo.findMemberUsernameById(uuid).orElse(null);
    }

    @Override
    public boolean userIsMember(String id) {
        UUID uuid = UUID.fromString(id);
        return jpaRepo.existsMemberById(uuid);
    }

    @Override
    public List<Member> getAllMembers() {
        return jpaRepo.findAllMembers();
    }

    @Override
    public List<User> getAllUsers() {
        return jpaRepo.findAll();
    }
}
