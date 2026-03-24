package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.EvenementDAO;
import dao.PersonneDAO;
import model.Evenement;
import model.Personne;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.List;

public class HomeHandler implements HttpHandler {

    private PersonneDAO dao = new PersonneDAO();
    private EvenementDAO evenementDAO = new EvenementDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Personne session = SessionManager.getSession(exchange);
        boolean isLoggedIn = session != null;
        String role = isLoggedIn ? dao.getRole(session.getId()) : null;
        String nom = isLoggedIn ? session.getPrenom() : null;

        String query = exchange.getRequestURI().getQuery();
        String searchKeyword = null;
        if (query != null && query.startsWith("q=")) {
            searchKeyword = URLDecoder.decode(query.substring(2), "UTF-8");
        }

        StringBuilder html = new StringBuilder()
                .append(HtmlTemplate.getHeader("Accueil"))
                .append(HtmlTemplate.getNavbar(isLoggedIn, role, nom))
                .append("<div style=\"position: relative; background: url('https://images.unsplash.com/photo-1540039155732-6809dbdd5262?auto=format&fit=crop&q=80&w=2000') center/cover no-repeat; padding: 100px 20px; text-align: center; color: white;\">\n")
                .append("  <div style=\"position: absolute; top:0; left:0; right:0; bottom:0; background: rgba(0,0,0,0.6); z-index: 1;\"></div>\n")
                .append("  <div style=\"position: relative; z-index: 2;\">\n")
                .append("    <h1 style=\"font-size: 4rem; font-weight: 900; margin-bottom: 1rem; text-shadow: 0 4px 6px rgba(0,0,0,0.3);\">La billetterie réinventée.</h1>\n")
                .append("    <p style=\"font-size: 1.25rem; max-width: 600px; margin: 0 auto 30px; text-shadow: 0 2px 4px rgba(0,0,0,0.3);\">Découvrez des événements inoubliables et réservez vos billets en quelques clics.</p>\n")
                .append("    <form action=\"/\" method=\"GET\" style=\"max-width: 600px; margin: 0 auto 40px; display: flex; gap: 10px;\">\n")
                .append("        <input type=\"text\" name=\"q\" placeholder=\"Rechercher un artiste, concert, lieu...\" style=\"flex: 1; padding: 15px 20px; font-size: 1.1rem; border: none; border-radius: 50px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\" value=\"").append(searchKeyword != null ? searchKeyword : "").append("\">\n")
                .append("        <button type=\"submit\" class=\"btn btn-primary\" style=\"border-radius: 50px; padding: 15px 30px; font-size: 1.1rem;\">Rechercher</button>\n")
                .append("    </form>\n");

        if (!isLoggedIn) {
            html.append("    <a href=\"/register\" class=\"btn btn-secondary\" style=\"font-size: 1.1rem; padding: 12px 25px; border-radius: 50px;\">Créer un compte</a>\n");
        } else {
            if ("client".equals(role)) {
                html.append("    <a href=\"/client\" class=\"btn btn-secondary\" style=\"font-size: 1.1rem; padding: 12px 25px; border-radius: 50px;\">Mon Espace Client</a>\n");
            } else if ("organisateur".equals(role)) {
                html.append("    <a href=\"/organisateur\" class=\"btn btn-secondary\" style=\"font-size: 1.1rem; padding: 12px 25px; border-radius: 50px;\">Espace Organisateur</a>\n");
            }
        }
        html.append("  </div>\n");
        html.append("</div>\n");
        html.append("<div class=\"container\">\n");
        
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            List<Evenement> results = evenementDAO.searchEvenementsByName(searchKeyword);
            html.append("<h2 style=\"margin-top: 2rem;\">Résultats de recherche pour \"").append(searchKeyword).append("\"</h2>\n");
            
            if (results.isEmpty()) {
                html.append("<div class=\"alert alert-error\">Aucun événement trouvé pour votre recherche.</div>\n");
            } else {
                html.append("<div style=\"display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; margin-top: 1.5rem;\">\n");
                for (Evenement ev : results) {
                    html.append("    <div class=\"card\">\n");
                    html.append("        <div class=\"card-body\">\n");
                    html.append("            <h3 class=\"card-title\">").append(ev.getNom()).append("</h3>\n");
                    html.append("            <p class=\"card-text\"><strong>Lieu:</strong> ").append(ev.getLieu()).append("<br>\n");
                    html.append("            <strong>Date:</strong> ").append(ev.getDate()).append("</p>\n");
                    html.append("            <a href=\"/event?id=").append(ev.getId()).append("\" class=\"btn btn-primary\" style=\"width: 100%; margin-top: auto;\">Voir les détails</a>\n");
                    html.append("        </div>\n");
                    html.append("    </div>\n");
                }
                html.append("</div>\n");
            }
        } else {
            html.append("<div style=\"text-align: center; padding: 4rem 2rem; color: var(--gray);\">\n");
            html.append("    <h2>Les meilleurs événements vous attendent</h2>\n");
            html.append("    <p>Utilisez la barre de recherche ci-dessus pour explorer les concerts, spectacles et plus encore.</p>\n");
            html.append("</div>\n");
        }

        html.append("</div>\n") // end of extra container
            .append(HtmlTemplate.getFooter());

        byte[] responseBytes = html.toString().getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
