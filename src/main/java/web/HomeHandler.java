package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.PersonneDAO;
import model.Personne;

import java.io.IOException;
import java.io.OutputStream;

public class HomeHandler implements HttpHandler {

    private PersonneDAO dao = new PersonneDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Personne session = SessionManager.getSession(exchange);
        boolean isLoggedIn = session != null;
        String role = isLoggedIn ? dao.getRole(session.getId()) : null;
        String nom = isLoggedIn ? session.getPrenom() : null;

        StringBuilder html = new StringBuilder()
                .append(HtmlTemplate.getHeader("Accueil"))
                .append(HtmlTemplate.getNavbar(isLoggedIn, role, nom))
                .append("<div style=\"text-align: center; margin-top: 50px;\">\n")
                .append("  <h1 style=\"font-size: 3rem; color: var(--primary); margin-bottom: 1rem;\">La billeterie réinventée.</h1>\n")
                .append("  <p style=\"font-size: 1.25rem; color: var(--gray); max-width: 600px; margin: 0 auto 30px;\">Découvrez une plateforme intuitive et premium pour organiser vos événements ou réserver vos billets en quelques clics.</p>\n");

        if (!isLoggedIn) {
            html.append("  <a href=\"/register\" class=\"btn btn-primary\" style=\"font-size: 1.2rem; padding: 15px 30px;\">Commencer maintenant</a>\n");
        } else {
            if ("client".equals(role)) {
                html.append("  <a href=\"/client\" class=\"btn btn-primary\" style=\"font-size: 1.2rem; padding: 15px 30px;\">Parcourir les événements</a>\n");
            } else if ("organisateur".equals(role)) {
                html.append("  <a href=\"/organisateur\" class=\"btn btn-primary\" style=\"font-size: 1.2rem; padding: 15px 30px;\">Mon Espace Organisateur</a>\n");
            }
        }

        html.append("</div>\n")
            .append(HtmlTemplate.getFooter());

        byte[] responseBytes = html.toString().getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
