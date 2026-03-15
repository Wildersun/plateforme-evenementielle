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

public class EventDetailsHandler implements HttpHandler {

    private EvenementDAO evenementDAO = new EvenementDAO();
    private BilletDAO billetDAO = new BilletDAO();
    private PersonneDAO personneDAO = new PersonneDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Personne session = SessionManager.getSession(exchange);
        boolean isLoggedIn = session != null;
        String role = isLoggedIn ? personneDAO.getRole(session.getId()) : null;
        String nomUser = isLoggedIn ? session.getPrenom() : null;

        String query = exchange.getRequestURI().getQuery(); // ex: id=5
        int eventId = -1;
        if (query != null && query.startsWith("id=")) {
            try {
                eventId = Integer.parseInt(query.substring(3));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        Evenement ev = null;
        if (eventId != -1) {
            ev = evenementDAO.getEvenementById(eventId);
        }

        StringBuilder html = new StringBuilder();
        if (ev == null) {
            html.append(HtmlTemplate.getHeader("Erreur"))
                .append(HtmlTemplate.getNavbar(isLoggedIn, role, nomUser))
                .append("<div class=\"container\"><div class=\"alert alert-error\">Événement introuvable.</div></div>")
                .append(HtmlTemplate.getFooter());
        } else {
            double prixBase = billetDAO.getPrixMinPourEvenement(ev.getId());
            if(prixBase == 0) prixBase = 5000; // Prix par défaut

            String dateStr = ev.getDate() != null ? ev.getDate().toString() : "Date tbd";

            html.append(HtmlTemplate.getHeader(ev.getNom()))
                .append(HtmlTemplate.getNavbar(isLoggedIn, role, nomUser))
                .append("<div style=\"max-width: 800px; margin: 0 auto;\">\n")
                .append("    <a href=\"/client\" style=\"color: var(--gray); text-decoration: none; display: inline-block; margin-bottom: 2rem;\">← Retour aux événements</a>\n")
                .append("    <div class=\"card\">\n")
                .append("        <div class=\"card-body\">\n")
                .append("            <span class=\"badge\" style=\"margin-bottom: 1rem;\">Événement Exclusif</span>\n")
                .append("            <h1 style=\"font-size: 2.5rem; color: var(--dark); margin-top: 0; margin-bottom: 0.5rem;\">").append(ev.getNom()).append("</h1>\n")
                .append("            <p style=\"font-size: 1.25rem; color: var(--gray); margin-bottom: 2rem;\">📍 ").append(ev.getLieu()).append(" &nbsp;&nbsp;|&nbsp;&nbsp; 📅 ").append(dateStr).append("</p>\n")
                .append("            <hr style=\"border: none; border-top: 1px solid #E5E7EB; margin-bottom: 2rem;\">\n")
                
                .append("            <h3 style=\"margin-top: 0;\">Réservation de Billets</h3>\n");

            if (!isLoggedIn) {
                html.append("            <div class=\"alert alert-error\" style=\"display: flex; justify-content: space-between; align-items: center;\">\n")
                    .append("                <span>Vous devez être connecté pour acheter un billet.</span>\n")
                    .append("                <a href=\"/login\" class=\"btn btn-primary btn-sm\">Se connecter</a>\n")
                    .append("            </div>\n");
            } else if (!"client".equals(role)) {
                html.append("            <div class=\"alert alert-error\">Seuls les clients peuvent réserver des billets.</div>\n");
            } else {
                html.append("            <form method=\"POST\" action=\"/buy-ticket\">\n")
                    .append("                <input type=\"hidden\" name=\"eventId\" value=\"").append(ev.getId()).append("\">\n")
                    .append("                <div class=\"form-group\">\n")
                    .append("                    <label for=\"typeBillet\">Choisissez votre type de billet :</label>\n")
                    .append("                    <select id=\"typeBillet\" name=\"typeBillet\" required>\n")
                    .append("                        <option value=\"Classique\">Classique - ").append(prixBase).append(" FCFA</option>\n")
                    .append("                        <option value=\"VIP\">VIP (Place Prioritaire + Cadeau) - ").append(prixBase * 2).append(" FCFA</option>\n")
                    .append("                        <option value=\"Premium\">Premium (Accès Backstage) - ").append(prixBase * 5).append(" FCFA</option>\n")
                    .append("                    </select>\n")
                    .append("                </div>\n")
                    .append("                <button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%; font-size: 1.125rem; padding: 1rem;\">Confirmer l'Achat</button>\n")
                    .append("            </form>\n");
            }

            html.append("        </div>\n")
                .append("    </div>\n")
                .append("</div>\n")
                .append(HtmlTemplate.getFooter());
        }

        byte[] responseBytes = html.toString().getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
