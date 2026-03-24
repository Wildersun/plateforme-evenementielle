package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.BilletDAO;
import dao.EvenementDAO;
import dao.PersonneDAO;
import model.Evenement;
import model.Personne;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ClientHandler implements HttpHandler {

    private EvenementDAO evenementDAO = new EvenementDAO();
    private BilletDAO billetDAO = new BilletDAO();
    private PersonneDAO personneDAO = new PersonneDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Personne session = SessionManager.getSession(exchange);
        boolean isLoggedIn = session != null;
        String role = isLoggedIn ? personneDAO.getRole(session.getId()) : null;
        String nom = isLoggedIn ? session.getPrenom() : null;

        // Protection : seul un client peut voir la liste s'il est loggé (ou accès
        // public)
        if (isLoggedIn && !"client".equals(role)) {
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        StringBuilder html = new StringBuilder()
                .append(HtmlTemplate.getHeader("Espace Client - Événements"))
                .append(HtmlTemplate.getNavbar(isLoggedIn, role, nom))
                .append("<div class=\"page-header\">\n")
                .append("    <h1 class=\"page-title\">Événements à Venir</h1>\n")
                .append("    <p class=\"page-subtitle\">Réservez vos places pour les meilleurs événements du moment</p>\n")
                .append("</div>\n")
                .append("<div style=\"display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 2rem;\">\n");

        List<Evenement> evenements = evenementDAO.getAllEvenements();

        if (evenements.isEmpty()) {
            html.append(
                    "<div style=\"grid-column: 1 / -1; text-align: center; color: var(--gray); padding: 3rem; background: white; border-radius: 1rem;\">")
                    .append("  <h3>Aucun événement pour le moment.</h3>")
                    .append("  <p>Revenez plus tard !</p>")
                    .append("</div>");
        } else {
            for (Evenement ev : evenements) {
                double minPrix = billetDAO.getPrixMinPourEvenement(ev.getId());
                String prixStr = minPrix > 0 ? String.format("%.0f FCFA", minPrix) : "Gratuit / TBD";
                String dateStr = ev.getDate() != null ? ev.getDate().toString() : "Date à confirmer";

                html.append("    <div class=\"card\">\n")
                        .append("        <div class=\"card-body\">\n")
                        .append("            <div style=\"display: flex; justify-content: space-between; align-items: start; margin-bottom: 1rem;\">\n")
                        .append("                <span class=\"badge\">").append(dateStr).append("</span>\n")
                        .append("            </div>\n")
                        .append("            <h2 class=\"card-title\">").append(ev.getNom()).append("</h2>\n")
                        .append("            <p class=\"card-text\">📍 ").append(ev.getLieu()).append("</p>\n")
                        .append("            <div style=\"margin-top: auto; padding-top: 1rem; border-top: 1px solid #E5E7EB; display: flex; justify-content: space-between; align-items: center;\">\n")
                        .append("                <span style=\"font-size: 1.25rem; font-weight: bold; color: var(--primary);\">")
                        .append(prixStr).append("</span>\n")
                        .append("                <a href=\"/event?id=").append(ev.getId())
                        .append("\" class=\"btn btn-primary\">Voir Détails</a>\n")
                        .append("            </div>\n")
                        .append("        </div>\n")
                        .append("    </div>\n");
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
