package UI;

import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.component.page.AppShellConfigurator;

@Push
@PWA(name = "Gamazon", shortName = "Gamazon")
// @Theme("your-theme") // Only if you're using a custom theme
public class AppShellConfig implements AppShellConfigurator {
}
