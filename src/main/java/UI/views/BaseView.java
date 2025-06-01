package UI.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import UI.DatabaseRelated.DbHealthStatus;

public abstract class BaseView extends VerticalLayout {

    protected final DbHealthStatus dbHealthStatus;

    protected BaseView(DbHealthStatus dbHealthStatus) {
        this.dbHealthStatus = dbHealthStatus;
        setupDbHealthCheck();
    }

    private void setupDbHealthCheck() {
        if (dbHealthStatus != null) {
            UI.getCurrent().addPollListener(event -> {
                if (!dbHealthStatus.isDbAvailable()) {
                    Notification.show("⚠️ DB connection lost. You will be logged out.", 3000, Notification.Position.TOP_CENTER);
                    UI.getCurrent().getSession().close(); // Optional: end session
                    UI.getCurrent().getPage().setLocation("/");
                }
            });
            UI.getCurrent().setPollInterval(4000);
        }
    }
}
