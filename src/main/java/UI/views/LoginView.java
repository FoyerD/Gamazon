package UI.views;

import UI.presenters.ILoginPresenter;
import Application.DTOs.UserDTO;
import Application.utils.Response;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.ClientCallable;
import org.springframework.beans.factory.annotation.Autowired;

@JsModule("./ws-client.js")
@Route("")
public class LoginView extends VerticalLayout {

    private final ILoginPresenter loginPresenter;

    private final TextField usernameField = new TextField("Username");
    private final PasswordField passwordField = new PasswordField("Password");
    private final Button loginButton = new Button("Log In");
    private final Button guestButton = new Button("Continue as Guest");

    @Autowired
    public LoginView(ILoginPresenter loginPresenter) {
        this.loginPresenter = loginPresenter;

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        Div card = new Div();
        card.getStyle().set("padding", "2rem")
                      .set("border-radius", "1rem")
                      .set("box-shadow", "0 8px 24px rgba(0,0,0,0.15)")
                      .set("background-color", "#ffffff")
                      .set("width", "350px");

        H2 title = new H2("Welcome Back");
        title.getStyle().set("color", "#2c3e50");
        Paragraph subtitle = new Paragraph("Log in to your account or continue as guest.");
        subtitle.getStyle().set("color", "#7f8c8d");

        usernameField.setWidthFull();
        passwordField.setWidthFull();
        loginButton.setWidthFull();
        guestButton.setWidthFull();

        loginButton.getStyle().set("background-color", "#3498db").set("color", "white");
        guestButton.getStyle().set("background-color", "#2ecc71").set("color", "white");

        loginButton.addClickListener(e -> login());
        guestButton.addClickListener(e -> loginAsGuest());

        VerticalLayout formLayout = new VerticalLayout(title, subtitle, usernameField, passwordField, loginButton, guestButton);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        formLayout.setAlignItems(Alignment.STRETCH);

        card.add(formLayout);
        add(card);
    }

    private void login() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();

        Response<UserDTO> response = loginPresenter.login(username, password);
        if (response.errorOccurred()) {
            Notification.show("Login failed: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
        } else {
            String sessionToken = response.getValue().getSessionToken();
            UI.getCurrent().getSession().setAttribute("sessionToken", sessionToken);
            UI.getCurrent().getSession().setAttribute("username", response.getValue().getUsername());
            
            // Initialize WebSocket connection by setting the userId in both window and sessionStorage
            UI.getCurrent().getPage().executeJs(
                "window.currentUserId = $0; sessionStorage.setItem('currentUserId', $0);",
                response.getValue().getUsername()
            );
            
            Notification.show("Welcome, " + response.getValue().getUsername());
            UI.getCurrent().navigate("home");
        }
    }

    private void loginAsGuest() {
        Response<UserDTO> response = loginPresenter.guestEnter();
        if (response.errorOccurred()) {
            Notification.show("Guest login failed: " + response.getErrorMessage(), 3000, Notification.Position.MIDDLE);
        } else {
            String sessionToken = response.getValue().getSessionToken();
            UI.getCurrent().getSession().setAttribute("sessionToken", sessionToken);
            UI.getCurrent().getSession().setAttribute("username", response.getValue().getUsername());
            
            // Initialize WebSocket connection by setting the userId in both window and sessionStorage
            UI.getCurrent().getPage().executeJs(
                "window.currentUserId = $0; sessionStorage.setItem('currentUserId', $0);",
                response.getValue().getUsername()
            );
            
            Notification.show("Logged in as Guest");
            UI.getCurrent().navigate("home");
        }
    }

    @ClientCallable
    private void showNotification(String message, String type, Integer duration, String position) {
        Notification notification = new Notification(message);
        notification.setDuration(duration > 0 ? duration : 10000);
        notification.setPosition(Notification.Position.valueOf(position.toUpperCase().replace('-', '_')));
        
        if ("error".equals(type)) {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else if ("warning".equals(type)) {
            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
        } else if ("success".equals(type)) {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        
        notification.open();
    }
}
