package UI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(
    scanBasePackages = {
        "Application",
        "Domain",
        "UI",
        "Infrastructure",
    },
    exclude = {DataSourceAutoConfiguration.class}
)
public class GamazonApplication {
    public static void main(String[] args) {
        SpringApplication.run(GamazonApplication.class, args);
    }
}