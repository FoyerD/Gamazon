package UI.views;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import UI.presenters.INotificationPresenter;
import UI.presenters.IUserSessionPresenter;

@Route("register")
public class RegisterView extends BaseView {

    private final ILoginPresenter loginPresenter;

    public RegisterView(ILoginPresenter loginPresenter, @Autowired(required = false) DbHealthStatus dbHealthStatus, 
                        @Autowired(required = false) GlobalLogoutManager logoutManager, IUserSessionPresenter sessionPresenter, INotificationPresenter notificationPresenter) {
        super(dbHealthStatus, logoutManager, sessionPresenter, notificationPresenter);
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

        DatePicker birthDatePicker = new DatePicker("Birth Date");
        birthDatePicker.setWidth("300px");

        // Set range: user must be born at least 1 year ago
        LocalDate today = LocalDate.now();
        birthDatePicker.setMax(today.minusYears(1));  // latest selectable birth date is 1 year ago
        birthDatePicker.setInitialPosition(LocalDate.of(2000, 1, 1));


        Button registerButton = new Button("Register", e -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();
            String email = emailField.getValue();
            LocalDate birthDate = birthDatePicker.getValue();
            
            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || birthDate.equals(birthDatePicker.getEmptyValue())) {
                Notification.show("Please fill in all fields", 3000, Notification.Position.MIDDLE);
                return;
            }

            String sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
            Response<UserDTO> response = this.loginPresenter.registerUser(sessionToken, username, password, email, birthDate);
            
            if (!response.errorOccurred()) {
                Notification.show("Registration successful!", 3000, Notification.Position.MIDDLE);
                this.loginPresenter.logout(sessionToken);
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

        add(title, usernameField, passwordField, emailField, birthDatePicker, registerButton, backButton);
    }
} 