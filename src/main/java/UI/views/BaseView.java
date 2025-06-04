package UI.views;

import java.util.Timer;
import java.util.TimerTask;

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
            UI ui = UI.getCurrent();  // Capture UI safely now

            ui.addPollListener(event -> {
                if (!dbHealthStatus.isDbAvailable()) {
                    logoutManager.markForceLogoutNeeded();

                    Notification.show("⚠️ DB connection lost. You will be logged out.", 3000, Notification.Position.TOP_CENTER);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ui.access(() -> {
                                ui.getSession().close();
                                ui.getPage().setLocation("/");
                            });
                        }
                    }, 500); // Delay before redirect

                } else if (logoutManager.shouldForceLogout()) {
                    Notification.show("🔁 DB is back. Please log in again.", 3000, Notification.Position.TOP_CENTER);

                    logoutManager.confirmLogoutProcessed();

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ui.access(() -> {
                                ui.getSession().close();
                                ui.getPage().setLocation("/");
                            });
                        }
                    }, 500); // Delay before redirect
                }
            });

            ui.setPollInterval(4000);
        }
    }


}

