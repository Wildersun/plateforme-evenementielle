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
import java.text.DecimalFormat;
import java.util.List;

public class GerantHandler implements HttpHandler {

    private EvenementDAO evenementDAO = new EvenementDAO();
    private BilletDAO billetDAO = new BilletDAO();
    private PersonneDAO personneDAO = new PersonneDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Personne session = SessionManager.getSession(exchange);
        boolean isLoggedIn = session != null;
        String role = isLoggedIn ? personneDAO.getRole(session.getId()) : null;

        if (!isLoggedIn || !"gerant".equals(role)) {
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        String nomUser = session.getPrenom();
        String method = exchange.getRequestMethod();
        String response = "";

        if ("GET".equalsIgnoreCase(method)) {
            response = generateDashboardHtml(nomUser);
            sendResponse(exchange, response, 200);
        } else {
            sendResponse(exchange, "Method Not Allowed", 405);
        }
    }

    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private String generateDashboardHtml(String nomUser) {
        int totalEvents = evenementDAO.getNbEvenements(null);
        int totalTickets = billetDAO.getTicketsVendus(null, null);
        double totalRevenue = billetDAO.getRevenus(null, null);
        List<Evenement> allEvents = evenementDAO.getAllEvenements();

        DecimalFormat df = new DecimalFormat("#,##0.00");

        StringBuilder sb = new StringBuilder();
        sb.append(HtmlTemplate.getHeader("Tableau de Bord Gérant"));
        sb.append(HtmlTemplate.getNavbar(true, "gerant", nomUser));

        sb.append("<div class=\"page-header\">\n");
        sb.append("    <h1 class=\"page-title\">Tableau de Bord Global</h1>\n");
        sb.append("    <p class=\"page-subtitle\">Vue d'ensemble de l'activité sur la plateforme.</p>\n");
        sb.append("</div>\n");

        // Stats Cards
        sb.append(
                "<div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1.5rem; margin-bottom: 2.5rem;\">\n");

        sb.append("    <div class=\"card\" style=\"text-align: center;\">\n");
        sb.append("        <div class=\"card-body\">\n");
        sb.append("            <div style=\"font-size: 2.5rem; font-weight: 800; color: var(--primary);\">")
                .append(totalEvents).append("</div>\n");
        sb.append(
                "            <div style=\"color: var(--gray); font-weight: 600; text-transform: uppercase; font-size: 0.875rem;\">Événements Totaux</div>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");

        sb.append("    <div class=\"card\" style=\"text-align: center;\">\n");
        sb.append("        <div class=\"card-body\">\n");
        sb.append("            <div style=\"font-size: 2.5rem; font-weight: 800; color: var(--secondary);\">")
                .append(totalTickets).append("</div>\n");
        sb.append(
                "            <div style=\"color: var(--gray); font-weight: 600; text-transform: uppercase; font-size: 0.875rem;\">Billets Vendus</div>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");

        sb.append("    <div class=\"card\" style=\"text-align: center;\">\n");
        sb.append("        <div class=\"card-body\">\n");
        sb.append("            <div style=\"font-size: 2.5rem; font-weight: 800; color: var(--dark);\">")
                .append(df.format(totalRevenue)).append(" FCFA</div>\n");
        sb.append(
                "            <div style=\"color: var(--gray); font-weight: 600; text-transform: uppercase; font-size: 0.875rem;\">Revenus Globaux</div>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");

        sb.append("</div>\n");

        // Liste des événements
        sb.append("<h2>Tous les événements</h2>\n");
        if (allEvents.isEmpty()) {
            sb.append("<div class=\"alert alert-error\">Aucun événement sur la plateforme.</div>\n");
        } else {
            sb.append(
                    "<div style=\"display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem;\">\n");
            for (Evenement ev : allEvents) {
                int evTickets = billetDAO.getTicketsVendus(null, ev.getId());
                double evRev = billetDAO.getRevenus(null, ev.getId());

                sb.append("    <div class=\"card\">\n");
                sb.append("        <div class=\"card-body\">\n");
                sb.append("            <h3 class=\"card-title\">").append(ev.getNom()).append("</h3>\n");
                sb.append("            <p class=\"card-text\"><strong>Lieu:</strong> ").append(ev.getLieu())
                        .append("<br>\n");
                sb.append("            <strong>Date:</strong> ").append(ev.getDate()).append("</p>\n");
                sb.append("            <hr style=\"border: none; border-top: 1px solid #E5E7EB; margin: 1rem 0;\">\n");
                sb.append(
                        "            <div style=\"display: flex; justify-content: space-between; font-size: 0.875rem;\">\n");
                sb.append("                <span>Billets: <strong>").append(evTickets).append("</strong></span>\n");
                sb.append("                <span>Revenus: <strong>").append(df.format(evRev))
                        .append(" FCFA</strong></span>\n");
                sb.append("            </div>\n");
                sb.append("        </div>\n");
                sb.append("    </div>\n");
            }
            sb.append("</div>\n");
        }

        sb.append(HtmlTemplate.getFooter());
        return sb.toString();
    }
}
