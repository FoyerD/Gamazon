package UI.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;


public abstract class BaseView extends VerticalLayout {

    protected final DbHealthStatus dbHealthStatus;
    protected final GlobalLogoutManager logoutManager;

    protected BaseView(@Autowired(required=false) DbHealthStatus dbHealthStatus, @Autowired(required=false) GlobalLogoutManager logoutManager) {
        this.dbHealthStatus = dbHealthStatus;
        this.logoutManager = logoutManager;
        setupDbHealthCheck();
    }

    private void setupDbHealthCheck() {
        if (dbHealthStatus != null && logoutManager != null) {
            UI.getCurrent().addPollListener(event -> {
                if (!dbHealthStatus.isDbAvailable()) {
                    Notification.show("⚠️ DB connection lost. You will be logged out.", 3000, Notification.Position.TOP_CENTER);
                    logoutManager.markForceLogoutNeeded();
                    UI.getCurrent().getSession().close();
                    UI.getCurrent().getPage().setLocation("/");
                } else if (logoutManager.shouldForceLogout()) {
                    Notification.show("🔁 DB is back. Please log in again.", 3000, Notification.Position.TOP_CENTER);
                    logoutManager.confirmLogoutProcessed();
                    UI.getCurrent().getSession().close();
                    UI.getCurrent().getPage().setLocation("/");
                }
            });
            UI.getCurrent().setPollInterval(4000);
        }
    }
}

