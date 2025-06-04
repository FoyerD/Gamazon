package Infrastructure.JpaSpringRepositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Domain.User.Guest;
import Domain.User.Member;
import Domain.User.User;

public interface IJpaUserSpringRepository extends JpaRepository<User, UUID> {

    // Generic user operations
    Optional<User> findById(UUID id);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> getUserById(@Param("id") UUID id);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> updateUser(@Param("id") UUID id);  // Note: semantic is weird – should be handled via save()

    // Member-specific queries
    @Query("SELECT m FROM Member m WHERE m.id = :id")
    Optional<Member> findMemberById(@Param("id") UUID id);

    @Query("SELECT m FROM Member m WHERE m.username = :username")
    Optional<Member> findMemberByUsername(@Param("username") String username);

    @Query("SELECT m.username FROM Member m WHERE m.id = :id")
    Optional<String> findMemberUsernameById(@Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.id = :id")
    boolean existsMemberById(@Param("id") UUID id);

    @Query("SELECT m FROM Member m")
    List<Member> findAllMembers();

    // Guest-specific queries
    @Query("SELECT g FROM Guest g WHERE g.id = :id")
    Optional<Guest> findGuestById(@Param("id") UUID id);
}
