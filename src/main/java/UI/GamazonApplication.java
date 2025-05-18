package UI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import(MockAppConfig.class)
public class GamazonApplication {
    public static void main(String[] args) {
        SpringApplication.run(GamazonApplication.class, args);
    }
}
