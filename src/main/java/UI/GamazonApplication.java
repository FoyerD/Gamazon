package UI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(
    scanBasePackages = {
        "Infrastructure",
        "Application",
        "Domain",
        "UI"
    }
)
@EnableScheduling
public class GamazonApplication {
    public static void main(String[] args) {
        SpringApplication.run(GamazonApplication.class, args);
    }    
}