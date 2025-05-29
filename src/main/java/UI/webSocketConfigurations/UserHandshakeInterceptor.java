package UI.webSocketConfigurations;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Intercepts the WebSocket handshake to extract userId and store it in attributes
 * for later binding as Principal.
 */
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest servlet = servletRequest.getServletRequest();
            String userId = servlet.getParameter("userId");
            if (userId != null && !userId.isBlank()) {
                attributes.put("user", new StompPrincipal(userId));
                System.out.println("✅ Interceptor stored userId: " + userId);
            } else {
                System.out.println("⚠️ No userId found in request parameters");
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        // No-op
    }
}
