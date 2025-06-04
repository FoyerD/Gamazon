package Infrastructure.JpaSpringRepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Domain.Notification.Notification;

public interface IJpaNotificationRepository extends JpaRepository<Notification, String> {

    /**
     * Retrieves all notifications for a specific user.
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId")
    List<Notification> getNotificationsByUserId(@Param("userId") String userId);
} 