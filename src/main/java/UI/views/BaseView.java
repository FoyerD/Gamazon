package UI.views;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.utils.TradingLogger;
import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;
import UI.presenters.INotificationPresenter;
import UI.presenters.IUserSessionPresenter;

@JsModule("./ws-client.js")
public abstract class BaseView extends VerticalLayout {

    protected final DbHealthStatus dbHealthStatus;
    protected final GlobalLogoutManager logoutManager;
    protected final IUserSessionPresenter sessionPresenter;
    protected final INotificationPresenter notificationPresenter;
    protected String sessionToken = null;

    protected BaseView(@Autowired(required=false) DbHealthStatus dbHealthStatus, @Autowired(required=false) GlobalLogoutManager logoutManager, IUserSessionPresenter sessionPresenter, INotificationPresenter notificationPresenter) {
        this.notificationPresenter = notificationPresenter;
        this.dbHealthStatus = dbHealthStatus;
        this.logoutManager = logoutManager;
        this.sessionPresenter = sessionPresenter;

        this.sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");


        if (sessionToken != null) {
            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: sessionToken is not null. Attempting to extract userId and inject into JS.");

            String userId = sessionPresenter.extractUserIdFromToken(sessionToken);

            // Inject userId to JavaScript for WebSocket
            
            UI.getCurrent().getPage().executeJs("window.currentUserId = $0;", userId);
            UI.getCurrent().getPage().executeJs("sessionStorage.setItem('currentUserId', $0); window.connectWebSocket && window.connectWebSocket($0);", userId);

            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: Injected userId to JS: " + userId);

            // Flush pending messages
            List<String> messages = notificationPresenter.getNotifications(userId);
            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: Consumed " + messages.size() + " pending messages for userId=" + userId);

            for (String msg : messages) {
                Notification.show("🔔 " + msg, 4000, Notification.Position.TOP_CENTER);
            }

            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: Displayed all pending messages for userId=" + userId);
            }
        else {
            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: sessionToken is null. Skipping userId injection and pending message handling.");
        }

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

