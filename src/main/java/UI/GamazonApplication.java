package UI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(
    scanBasePackages = {
        "Application",
        "Domain",
        "UI",
        "Infrastructure"
    }
)
@EnableJpaRepositories(basePackages = "Infrastructure.JpaSpringRepositories")
@EntityScan(basePackages = "Domain.Store")
public class GamazonApplication {
    public static void main(String[] args) {
        SpringApplication.run(GamazonApplication.class, args);
    }
}