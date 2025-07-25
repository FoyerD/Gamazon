package Infrastructure.JpaSpringRepositories;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("prod")
@EnableJpaRepositories("Infrastructure.JpaSpringRepositories")
@EntityScan("Domain")
public class JpaProdConfig {}
