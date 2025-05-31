package UI.DatabaseRelated;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DbHealthChecker {

    private final JdbcTemplate jdbcTemplate;
    private final DbHealthStatus dbHealthStatus;

    public DbHealthChecker(JdbcTemplate jdbcTemplate, DbHealthStatus dbHealthStatus) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbHealthStatus = dbHealthStatus;
    }

    @Scheduled(fixedDelay = 10000)
    public void checkDbConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbHealthStatus.setDbAvailable(true);
            System.out.println("[DB CHECK] ✅ Connection OK");
        } catch (Exception e) {
            dbHealthStatus.setDbAvailable(false);
            System.out.println("[DB CHECK] ❌ Connection FAILED: " + e.getMessage());
        }
    }

}
