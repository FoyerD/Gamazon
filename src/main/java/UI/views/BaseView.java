package UI.views;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.utils.TradingLogger;
import UI.DatabaseRelated.DbHealthStatus;
import UI.DatabaseRelated.GlobalLogoutManager;
import UI.presenters.INotificationPresenter;
import UI.presenters.IUserSessionPresenter;

@JsModule("./ws-client.js")

public abstract class BaseView extends VerticalLayout {

    protected final DbHealthStatus dbHealthStatus;
    protected final GlobalLogoutManager logoutManager;
    protected final IUserSessionPresenter sessionPresenter;
    protected final INotificationPresenter notificationPresenter;
    protected String sessionToken = null;

    protected BaseView(@Autowired(required=false) DbHealthStatus dbHealthStatus, @Autowired(required=false) GlobalLogoutManager logoutManager, IUserSessionPresenter sessionPresenter, INotificationPresenter notificationPresenter) {
        this.notificationPresenter = notificationPresenter;
        this.dbHealthStatus = dbHealthStatus;
        this.logoutManager = logoutManager;
        this.sessionPresenter = sessionPresenter;

        this.sessionToken = (String) UI.getCurrent().getSession().getAttribute("sessionToken");


        if (sessionToken != null) {
            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: sessionToken is not null. Attempting to extract userId and inject into JS.");

            String userId = sessionPresenter.extractUserIdFromToken(sessionToken);

            // Inject userId to JavaScript for WebSocket
            
            UI.getCurrent().getPage().executeJs("window.currentUserId = $0;", userId);
            UI.getCurrent().getPage().executeJs("sessionStorage.setItem('currentUserId', $0); window.connectWebSocket && window.connectWebSocket($0);", userId);

            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: Injected userId to JS: " + userId);

            // Flush pending messages
            List<String> messages = notificationPresenter.getNotifications(userId);
            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: Consumed " + messages.size() + " pending messages for userId=" + userId);

            for (String msg : messages) {
                Notification.show("🔔 " + msg, 4000, Notification.Position.TOP_CENTER);
            }

            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: Displayed all pending messages for userId=" + userId);
            }
        else {
            TradingLogger.logEvent("HomePageView", "constructor",
                "DEBUG: sessionToken is null. Skipping userId injection and pending message handling.");
        }

        setupDbHealthCheck();
        
    }

    private void setupDbHealthCheck() {
        if (dbHealthStatus != null && logoutManager != null) {
            UI ui = UI.getCurrent();  // Capture UI safely now

            ui.addPollListener(event -> {
                if (!dbHealthStatus.isDbAvailable()) {
                    logoutManager.markForceLogoutNeeded();

                    Notification.show("⚠️ DB connection lost. You will be logged out.", 3000, Notification.Position.TOP_CENTER);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ui.access(() -> {
                                ui.getSession().close();
                                ui.getPage().setLocation("/");
                            });
                        }
                    }, 500); // Delay before redirect

                } else if (logoutManager.shouldForceLogout()) {
                    Notification.show("🔁 DB is back. Please log in again.", 3000, Notification.Position.TOP_CENTER);

                    logoutManager.confirmLogoutProcessed();

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ui.access(() -> {
                                ui.getSession().close();
                                ui.getPage().setLocation("/");
                            });
                        }
                    }, 500); // Delay before redirect
                }
            });

            ui.setPollInterval(4000);
        }
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        injectTracksToClient();
    }

    protected void injectTracksToClient() {
        try {
            File folder = new File("src/main/resources/static/audio");
            if (!folder.exists()) {
                System.out.println("⚠️ Audio folder not found at src/main/resources/static/audio");
                return;
            }

            String[] tracks = folder.list((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".mp3") || lower.endsWith(".wav");
            });

            if (tracks == null || tracks.length == 0) {
                System.out.println("⚠️ No valid audio files found in /audio folder.");
                return;
            }

            String[] trackPaths = new String[tracks.length];
            for (int i = 0; i < tracks.length; i++) {
                trackPaths[i] = "/audio/" + tracks[i];
            }

            String jsonArray = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(trackPaths);

            UI.getCurrent().getPage().executeJs("""
                window.tracks = %s;
                console.log("[Music Debug] Updated tracks array:", window.tracks);
            """.formatted(jsonArray));

            UI.getCurrent().getPage().executeJs("""
                if (!document.getElementById('backgroundMusic')) {
                    console.log("[Music Debug] Initializing background music system...");

                    const audio = document.createElement('audio');
                    audio.id = 'backgroundMusic';
                    audio.volume = 0.2;
                    document.body.appendChild(audio);

                    function playRandom() {
                        console.log("[Music Debug] playRandom() called");

                        if (!window.tracks || window.tracks.length === 0) {
                            console.error("[Music Debug] No tracks available.");
                            return;
                        }

                        const randomIndex = Math.floor(Math.random() * window.tracks.length);
                        const randomTrack = window.tracks[randomIndex];

                        console.log("[Music Debug] Selected track index:", randomIndex);
                        console.log("[Music Debug] Selected track path:", randomTrack);

                        if (!randomTrack) {
                            console.error("[Music Debug] Selected track is empty or undefined.");
                            return;
                        }

                        audio.pause();
                        audio.src = randomTrack;
                        audio.load();

                        audio.play()
                            .then(() => console.log("[Music Debug] Playback started successfully."))
                            .catch(err => console.error("[Music Debug] Playback failed:", err));
                    }

                    audio.addEventListener('ended', () => {
                        console.log("[Music Debug] Track ended, selecting next...");
                        playRandom();
                    });

                    window.startBackgroundMusic = function() {
                        console.log("[Music Debug] startBackgroundMusic() called");
                        playRandom();
                    };

                    console.log("[Music Debug] Music system ready. Awaiting user interaction.");
                }
            """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

