package UI.DatabaseRelated;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
@Component
@Profile("prod")
public class DbHealthStatus {
    private volatile boolean dbAvailable = true;

    public boolean isDbAvailable() {
        return dbAvailable;
    }

    public void setDbAvailable(boolean available) {
        this.dbAvailable = available;
    }
}
