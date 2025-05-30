package UI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(
    scanBasePackages = {
        "Infrastructure",
        "Application",
        "Domain",
        "UI"
    }
)
public class GamazonApplication {
    public static void main(String[] args) {
        SpringApplication.run(GamazonApplication.class, args);
    }
}