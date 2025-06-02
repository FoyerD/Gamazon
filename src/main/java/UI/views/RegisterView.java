package UI.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import Application.DTOs.UserDTO;
import Application.utils.Response;
import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;
import UI.presenters.ILoginPresenter;

@Route("register")
public class RegisterView extends BaseView {

    private final ILoginPresenter loginPresenter;

    public RegisterView(ILoginPresenter loginPresenter, @Autowired(required = false) DbHealthStatus dbHealthStatus, 
                        @Autowired(required = false) GlobalLogoutManager logoutManager) {
        super(dbHealthStatus, logoutManager);
        this.loginPresenter = loginPresenter;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "linear-gradient(to right, #edf2f7, #e2e8f0)");

        H1 title = new H1("Register New Account");
        title.getStyle().set("color", "#1a202c");

        TextField usernameField = new TextField("Username");
        usernameField.setWidth("300px");
        
        PasswordField passwordField = new PasswordField("Password");
        passwordField.setWidth("300px");
        
        EmailField emailField = new EmailField("Email");
        emailField.setWidth("300px");

        Button registerButton = new Button("Register", e -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();
            String email = emailField.getValue();
            
            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Notification.show("Please fill in all fields", 3000, Notification.Position.MIDDLE);
                return;
            }

            String sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
            Response<UserDTO> response = loginPresenter.registerUser(sessionToken, username, password, email);
            
            if (!response.errorOccurred()) {
                Notification.show("Registration successful!", 3000, Notification.Position.MIDDLE);
                loginPresenter.logout(sessionToken);
                UI.getCurrent().navigate("");
            } else {
                Notification.show("Registration failed: " + response.getErrorMessage(), 
                                3000, Notification.Position.MIDDLE);
            }
        });
        registerButton.getStyle()
            .set("background-color", "#38a169")
            .set("color", "white")
            .set("margin-top", "20px");

        Button backButton = new Button("Back to Home", e -> UI.getCurrent().navigate("home"));
        backButton.getStyle()
            .set("background-color", "#4299e1")
            .set("color", "white")
            .set("margin-top", "10px");

        add(title, usernameField, passwordField, emailField, registerButton, backButton);
    }
} 