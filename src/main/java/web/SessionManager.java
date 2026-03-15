package web;

import com.sun.net.httpserver.HttpExchange;
import java.util.HashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import model.Personne;

public class SessionManager {

    // Stockage en mémoire des sessions : SessionID -> Personne
    private static final Map<String, Personne> sessions = new HashMap<>();

    public static void createSession(HttpExchange exchange, Personne user) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, user);
        
        // Ajouter un cookie de session
        exchange.getResponseHeaders().add("Set-Cookie", "SESSION_ID=" + sessionId + "; Path=/; HttpOnly");
    }

    public static Personne getSession(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split("; ");
            for (String cookie : cookies) {
                if (cookie.startsWith("SESSION_ID=")) {
                    String sessionId = cookie.substring(11); // longueur de "SESSION_ID="
                    return sessions.get(sessionId);
                }
            }
        }
        return null; // Non authentifié
    }

    public static void destroySession(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split("; ");
            for (String cookie : cookies) {
                if (cookie.startsWith("SESSION_ID=")) {
                    String sessionId = cookie.substring(11);
                    sessions.remove(sessionId);
                    // Effacer le cookie côté client
                    exchange.getResponseHeaders().add("Set-Cookie", "SESSION_ID=; Path=/; Max-Age=0; HttpOnly");
                    return;
                }
            }
        }
    }
}
