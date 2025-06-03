package UI.DatabaseRelated;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import UI.presenters.LoginPresenter;

@Component
@Profile("prod")
public class GlobalLogoutManager {

    private final DbHealthStatus dbHealthStatus;
    private final LoginPresenter loginPresenter;
    private volatile boolean shouldForceLogout = false;
    private volatile boolean forceLogoutProcessed = false;

    @Autowired
    public GlobalLogoutManager(DbHealthStatus dbHealthStatus, LoginPresenter loginPresenter) {
        this.dbHealthStatus = dbHealthStatus;
        this.loginPresenter = loginPresenter;
        startWatcherThread();
    }

    public void markForceLogoutNeeded() {
        shouldForceLogout = true;
        forceLogoutProcessed = false;
    }

    public boolean shouldForceLogout() {
        return shouldForceLogout && !forceLogoutProcessed;
    }

    public void confirmLogoutProcessed() {
        forceLogoutProcessed = true;
    }

    private void startWatcherThread() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (shouldForceLogout && dbHealthStatus.isDbAvailable()) {
                        System.out.println("✅ DB is back. Triggering force logout.");
                        loginPresenter.logOutAllUsers();
                        shouldForceLogout = false;
                    }
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }, "DB-Watcher-Thread");
        thread.setDaemon(true);
        thread.start();
    }
}
