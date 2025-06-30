package UI.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import Application.DTOs.UserDTO;
import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;
import UI.presenters.INotificationPresenter;
import UI.presenters.IUserSessionPresenter;

@Route("gif-view")
public class GIFView extends BaseView implements BeforeEnterObserver {

    private final String sessionToken;
    private final String currentUsername;
    private final UserDTO user;

    public GIFView(@Autowired(required = false) DbHealthStatus dbHealthStatus,
                   @Autowired(required = false) GlobalLogoutManager globalLogoutManager,
                   IUserSessionPresenter userSessionPresenter,
                   INotificationPresenter notificationPresenter) {

        super(dbHealthStatus, globalLogoutManager, userSessionPresenter, notificationPresenter);

        this.sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");
        this.user = (UserDTO) UI.getCurrent().getSession().getAttribute("user");
        this.currentUsername = (String) UI.getCurrent().getSession().getAttribute("username");

        setSizeFull();
        getStyle().set("background", "linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)");

        Button backButton = new Button("\ud83c\udfe0 Back to Home", e -> UI.getCurrent().navigate("home"));
        backButton.getStyle()
                .set("position", "fixed")
                .set("top", "20px")
                .set("left", "20px")
                .set("z-index", "1001")
                .set("background-color", "#38a169")
                .set("color", "white")
                .set("font-weight", "bold");
        add(backButton);

        String[] gifUrls = {
            "https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExcmI2dWVmbmZtcGl3OGtrMHR2NDY5b2R0aWIzaDJmazVub2dvaXA5ZyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/uP781D6zVOdG0/giphy.gif",
            "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExZjVnZ2J0eG4zeDN1M3kxMnFubjMzeWh5Z3k1a2F4NHkyYzVnZDR2ZyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/WQJ2DORvilpEk/giphy.gif",
            "https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExNXlqYm01Z2c0Mmpwam9mdGNjMTh6MXVhNTUwN282N2ZhMjZ1dTI0biZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/C7g1iJFwqXCk8/giphy.gif",
            "https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExeGRubHFycWFuNTlhZXE5dnRjcmlpcGJ3NTZyeHV4Y3VkbHNmaTJidiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/GXqLGxgpE2tfq/giphy.gif",
            "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExZWRqNjRkNzRkZGYxODIycDNiZXh1Z3hlNDQ5b25laG13Z2Z3Y29uNyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/1nCfZ1mDXGcyk/giphy.gif",
            "https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExaTd1NzJrZXhmZTZ5Z3Q4MzR5dGl4eWp4dmNhdm5rbHJnYW94cm01ayZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/UNcCOA9CGVpdu/giphy.gif",
            "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExN2JxZXdtdnozOHJsczJpenA2bzMzOXpodTJpYW5uaHQ3Zmd0aHFyNiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/e0tTzm60eHbZ6/giphy.gif",
            "https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExNzl0YXI2dm13ZmkzdHM1dnVidG14dDNiZTQxZXd0eWtteDhyYzU3diZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/4dAqzSvxV15Is/giphy.gif",
            "https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExODk0eWluYXJ6bXFpb2k2bWQxZ3lmcDd1b2NlYnE5ZXAwcGNnemd1ayZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/GGPa1hbSpHpcUWXagb/giphy.gif",
            "https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExMmtuYjl3aGVxOW96Z2Z4cm9jZHp6NzF0OTdvOTdrenlmdDlodGFkbyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/PbnBee5MuIaS4/giphy.gif",
            "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExcmp3dnAyenFxN3ltdTQ5cmcyanp5Nm5yaGdtbzB4MmZ6OGRzbWhrMSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l41YkFIiBxQdRlMnC/giphy.gif",
            "https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExeG81azNkeXpuYWY0aXN2dG5sdjlra3BnaHhsdzAzMTF3eTdidHFoaSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/rfZCdXOgS5aDK/giphy.gif",
            "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExZTB6Y3pwMjV2a2R5Nmxhemw3cHo2NDd1bnNscGZoZHdvZWwwcWx3ZSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o85xvFbS3XcoNlhV6/giphy.gif",
            "https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExemR2Y2o3cnhjZzFob2FnMmNqZHFwMGRlcjFpenIyMnN4dmdibGY3ayZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/99WRPEyvToq5i/giphy.gif",
            "https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExZ2p3NTk5ZnhkbGI5aWlidzZpbjJ6aTNieWg0MDZ2Nm4yOGtzeHFxdSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/yE72eDy7lj3JS/giphy.gif",
            "https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExMzJ0bXFiNHM5eXAycHNyNmFwOWxvbWtqaHF6ZjZpeGRxc2tldXIwOCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/UAmdO9mDnFJCllRisF/giphy.gif",
            "https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExeGVtMXRneW52YnBvbmp6cHdlZTVrNzB2MWFlZHc2NDR5amxvd3IzZSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3otPoGt5vb0KNPPu2k/giphy.gif"
        };

        for (int i = 0; i < gifUrls.length; i++) {
            String left = (5 + (i * 17) % 90) + "%";
            String bottom = (10 + (i * 23) % 80) + "%";
            addGif(gifUrls[i], "150px", "150px", "fixed", bottom, left, null, null);
        }

        addGif("https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExdGdhbTAxbTlwcjdpdDFlcmpiMHB6eWE2MWdnZWtzaWVkcmthdzR2MyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/JmPenP1svctdfDCEHi/giphy.gif",
            "215px", "300px", "fixed", "70%", "25%", null, null);
        addGif("https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExb3BucmY3bjV0a2ExM3Q4aDBpb2xidHBuNWRtb3ZlbnAya3A1cXczOCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/XEg38j4mGddszAddiT/giphy.gif",
            "300px", "100px", "fixed", "40%", "70%", null, null);
        addGif("https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExbXh4NWppZXhpdmxmeXh3bzA3OThoYTMzOHVnb3M5Yzg5c3M2dG83ZCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/ZtusrBPGWbqlW/giphy.gif",
            "250px", "200px", "fixed", "25%", "40%", null, null);
        addGif("https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExajM5Y2cweTNxcGNndWhjdnhmaHFhYWk0NTVneml1dGx3NGR5c2NqcyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/L3bj6t3opdeNddYCyl/giphy.gif",
            "300px", "220px", "fixed", "55%", "55%", null, null);
    }

    private void addGif(String url, String width, String height, String position, String bottom,
                        String left, String right, String transform) {
        Image gif = new Image(url, "GIF");
        gif.setWidth(width);
        gif.setHeight(height);

        Div wrapper = new Div(gif);
        wrapper.getStyle()
            .set("position", position)
            .set("z-index", "1000");

        if (bottom != null) wrapper.getStyle().set("bottom", bottom);
        if (left != null) wrapper.getStyle().set("left", left);
        if (right != null) wrapper.getStyle().set("right", right);
        if (transform != null) wrapper.getStyle().set("transform", transform);

        add(wrapper);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (sessionToken == null || user == null) {
            event.forwardTo("");
        }
    }
}