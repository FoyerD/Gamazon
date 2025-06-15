package UI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ProfileLogger {
    
    @Autowired
    private Environment env;

    @PostConstruct
    public void logActiveProfiles(){
        String[] profiles = env.getActiveProfiles();
        System.out.println("Active Profiles: " + String.join(", ", profiles));
    }
}
