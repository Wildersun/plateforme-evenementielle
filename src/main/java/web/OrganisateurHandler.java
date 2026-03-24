package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.BilletDAO;
import dao.EvenementDAO;
import dao.PersonneDAO;
import model.Evenement;
import model.Personne;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganisateurHandler implements HttpHandler {

    private EvenementDAO evenementDAO = new EvenementDAO();
    private BilletDAO billetDAO = new BilletDAO();
    private PersonneDAO personneDAO = new PersonneDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Personne session = SessionManager.getSession(exchange);
        boolean isLoggedIn = session != null;
        String role = isLoggedIn ? personneDAO.getRole(session.getId()) : null;

        // Protection : seul un organisateur peut y accéder
        if (!isLoggedIn || !"organisateur".equals(role)) {
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        String nomUser = session.getPrenom();
        String method = exchange.getRequestMethod();
        String response = "";

        if ("GET".equalsIgnoreCase(method)) {
            response = generateHtmlForm(nomUser, session.getId(), "");
            sendResponse(exchange, response, 200);

        } else if ("POST".equalsIgnoreCase(method)) {
            try {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                Map<String, String> params = parseFormData(formData);
                
                String nom = params.get("nom");
                String lieu = params.get("lieu");
                String dateStr = params.get("date");

                if (nom == null || lieu == null || dateStr == null || nom.isEmpty() || lieu.isEmpty() || dateStr.isEmpty()) {
                    response = generateHtmlForm(nomUser, session.getId(), "<div class='alert alert-error'>Veuillez remplir tous les champs.</div>");
                    sendResponse(exchange, response, 400);
                    return;
                }

                Date dateSql = Date.valueOf(dateStr);
                
                Evenement ev = new Evenement(0, nom, dateSql, lieu, session.getId());
                evenementDAO.creerEvenement(ev);

                response = generateHtmlForm(nomUser, session.getId(), "<div class='alert alert-success'>Événement '" + nom + "' publié avec succès !</div>");
                sendResponse(exchange, response, 200);

            } catch (IllegalArgumentException e) {
                response = generateHtmlForm(nomUser, session.getId(), "<div class='alert alert-error'>Format de date invalide. Utilisez AAAA-MM-JJ.</div>");
                sendResponse(exchange, response, 400);
            } catch (Exception e) {
                e.printStackTrace();
                response = generateHtmlForm(nomUser, session.getId(), "<div class='alert alert-error'>Erreur interne du serveur.</div>");
                sendResponse(exchange, response, 500);
            }
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

    private Map<String, String> parseFormData(String formData) throws Exception {
        Map<String, String> map = new HashMap<>();
        if (formData != null && !formData.isEmpty()) {
            String[] pairs = formData.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    map.put(URLDecoder.decode(keyValue[0], "UTF-8"), URLDecoder.decode(keyValue[1], "UTF-8"));
                }
            }
        }
        return map;
    }

    private String generateHtmlForm(String nomUser, int organisateurId, String messageHtml) {
        int totalEvents = evenementDAO.getNbEvenements(organisateurId);
        int totalTickets = billetDAO.getTicketsVendus(organisateurId, null);
        double totalRevenue = billetDAO.getRevenus(organisateurId, null);
        List<Evenement> myEvents = evenementDAO.getEvenementsByOrganisateur(organisateurId);

        DecimalFormat df = new DecimalFormat("#,##0.00");

        StringBuilder sb = new StringBuilder();
        sb.append(HtmlTemplate.getHeader("Tableau de Bord Organisateur"));
        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;\">\n");
        sb.append("    <div>\n");
        sb.append("        <h1 class=\"page-title\" style=\"margin-bottom: 0.5rem;\">Dashboard Organisateur</h1>\n");
        sb.append("        <p class=\"page-subtitle\" style=\"margin: 0;\">Bienvenue, ").append(nomUser).append("! Voici un résumé de vos événements.</p>\n");
        sb.append("    </div>\n");
        sb.append("    <div>\n");
        sb.append("        <a href=\"/create-gerant\" class=\"btn btn-secondary\" style=\"margin-right: 15px; padding: 0.75rem 1.5rem;\">Créer un Gérant</a>\n");
        sb.append("        <a href=\"#nouvel-evenement\" class=\"btn btn-primary\" style=\"padding: 0.75rem 1.5rem;\">Nouvel Événement</a>\n");
        sb.append("    </div>\n");
        sb.append("</div>\n");
        if (messageHtml != null && !messageHtml.isEmpty()) {
            sb.append(messageHtml).append("\n");
        }

        // Stats Cards
        sb.append("<div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1.5rem; margin-bottom: 2.5rem;\">\n");
        
        sb.append("    <div class=\"card\" style=\"text-align: center;\">\n");
        sb.append("        <div class=\"card-body\">\n");
        sb.append("            <div style=\"font-size: 2.5rem; font-weight: 800; color: var(--primary);\">").append(totalEvents).append("</div>\n");
        sb.append("            <div style=\"color: var(--gray); font-weight: 600; text-transform: uppercase; font-size: 0.875rem;\">Mes Événements</div>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");

        sb.append("    <div class=\"card\" style=\"text-align: center;\">\n");
        sb.append("        <div class=\"card-body\">\n");
        sb.append("            <div style=\"font-size: 2.5rem; font-weight: 800; color: var(--secondary);\">").append(totalTickets).append("</div>\n");
        sb.append("            <div style=\"color: var(--gray); font-weight: 600; text-transform: uppercase; font-size: 0.875rem;\">Billets Vendus</div>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");

        sb.append("    <div class=\"card\" style=\"text-align: center;\">\n");
        sb.append("        <div class=\"card-body\">\n");
        sb.append("            <div style=\"font-size: 2.5rem; font-weight: 800; color: var(--dark);\">").append(df.format(totalRevenue)).append(" FCFA</div>\n");
        sb.append("            <div style=\"color: var(--gray); font-weight: 600; text-transform: uppercase; font-size: 0.875rem;\">Mes Revenus</div>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");

        sb.append("</div>\n");

        // Layout Split: Evenements list (Left) and Create Form (Right)
        sb.append("<div style=\"display: flex; flex-wrap: wrap; gap: 2rem;\">\n");
        
        // Left Column : Event list
        sb.append("    <div style=\"flex: 1 1 600px;\">\n");
        sb.append("        <h2>Mes événements</h2>\n");
        if (myEvents.isEmpty()) {
            sb.append("        <div class=\"alert alert-error\">Vous n'avez créé aucun événement.</div>\n");
        } else {
            sb.append("        <div style=\"display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1.5rem;\">\n");
            for (Evenement ev : myEvents) {
                int evTickets = billetDAO.getTicketsVendus(null, ev.getId());
                double evRev = billetDAO.getRevenus(null, ev.getId());
                
                sb.append("            <div class=\"card\">\n");
                sb.append("                <div class=\"card-body\">\n");
                sb.append("                    <h3 class=\"card-title\">").append(ev.getNom()).append("</h3>\n");
                sb.append("                    <p class=\"card-text\"><strong>Lieu:</strong> ").append(ev.getLieu()).append("<br>\n");
                sb.append("                    <strong>Date:</strong> ").append(ev.getDate()).append("</p>\n");
                sb.append("                    <hr style=\"border: none; border-top: 1px solid #E5E7EB; margin: 1rem 0;\">\n");
                sb.append("                    <div style=\"display: flex; justify-content: space-between; font-size: 0.875rem;\">\n");
                sb.append("                        <span>Billets: <strong>").append(evTickets).append("</strong></span>\n");
                sb.append("                        <span>Revenus: <strong>").append(df.format(evRev)).append(" FCFA</strong></span>\n");
                sb.append("                    </div>\n");
                sb.append("                </div>\n");
                sb.append("            </div>\n");
            }
            sb.append("        </div>\n");
        }
        sb.append("    </div>\n");

        // Right Column : Create form
        sb.append("    <div style=\"flex: 0 1 400px;\">\n");
        sb.append("        <div class=\"card\">\n");
        sb.append("            <div class=\"card-body\">\n");
        sb.append("                <h2 style=\"margin-top:0;\">Créer un événement</h2>\n");
        sb.append("                <form method=\"POST\" action=\"/organisateur\">\n");
        sb.append("                    <div class=\"form-group\">\n");
        sb.append("                        <label for=\"nom\">Nom de l'événement</label>\n");
        sb.append("                        <input type=\"text\" id=\"nom\" name=\"nom\" required placeholder=\"Ex: Concert Symphonique\">\n");
        sb.append("                    </div>\n");
        sb.append("                    <div class=\"form-group\">\n");
        sb.append("                        <label for=\"lieu\">Lieu</label>\n");
        sb.append("                        <input type=\"text\" id=\"lieu\" name=\"lieu\" required placeholder=\"Ex: Palais des Sports\">\n");
        sb.append("                    </div>\n");
        sb.append("                    <div class=\"form-group\">\n");
        sb.append("                        <label for=\"date\">Date</label>\n");
        sb.append("                        <input type=\"date\" id=\"date\" name=\"date\" required>\n");
        sb.append("                    </div>\n");
        sb.append("                    <button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%; font-size: 1.125rem; padding: 0.75rem;\">Publier</button>\n");
        sb.append("                </form>\n");
        sb.append("            </div>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");
        
        sb.append("</div>\n");

        sb.append(HtmlTemplate.getFooter());

        return sb.toString();
    }
}
