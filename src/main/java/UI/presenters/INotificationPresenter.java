package UI.presenters;

import java.util.List;

public interface INotificationPresenter {
    /**
     * Returns and removes all pending notifications for the current user.
     * 
     * @param sessionToken the session token of the user
     * @return list of notification messages for the user
     */
    List<String> getNotifications(String sessionToken);
}
