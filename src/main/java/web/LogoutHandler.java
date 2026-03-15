package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.PersonneDAO;
import java.io.IOException;
import java.io.OutputStream;

public class LogoutHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        SessionManager.destroySession(exchange);
        
        exchange.getResponseHeaders().set("Location", "/");
        exchange.sendResponseHeaders(302, -1);
    }
}
