package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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
import java.util.HashMap;
import java.util.Map;

public class OrganisateurHandler implements HttpHandler {

    private EvenementDAO evenementDAO = new EvenementDAO();
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
            response = generateHtmlForm(nomUser, "");
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
                    response = generateHtmlForm(nomUser, "<div class='alert alert-error'>Veuillez remplir tous les champs.</div>");
                    sendResponse(exchange, response, 400);
                    return;
                }

                Date dateSql = Date.valueOf(dateStr);
                
                Evenement ev = new Evenement(0, nom, dateSql, lieu, session.getId());
                evenementDAO.creerEvenement(ev);

                response = generateHtmlForm(nomUser, "<div class='alert alert-success'>Événement '" + nom + "' publié avec succès !</div>");
                sendResponse(exchange, response, 200);

            } catch (IllegalArgumentException e) {
                response = generateHtmlForm(nomUser, "<div class='alert alert-error'>Format de date invalide. Utilisez AAAA-MM-JJ.</div>");
                sendResponse(exchange, response, 400);
            } catch (Exception e) {
                e.printStackTrace();
                response = generateHtmlForm(nomUser, "<div class='alert alert-error'>Erreur interne du serveur.</div>");
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

    private String generateHtmlForm(String nomUser, String messageHtml) {
        return HtmlTemplate.getHeader("Espace Organisateur") +
               HtmlTemplate.getNavbar(true, "organisateur", nomUser) +
               "<div class=\"card auth-card\" style=\"max-width: 600px;\">\n" +
               "    <div class=\"page-header\">\n" +
               "        <h1 class=\"page-title\">Créer un Événement</h1>\n" +
               "        <p class=\"page-subtitle\">Publiez votre nouvel événement et commencez à vendre des billets.</p>\n" +
               "    </div>\n" +
               "    " + messageHtml + "\n" +
               "    <form method=\"POST\" action=\"/organisateur\">\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"nom\">Nom de l'événement</label>\n" +
               "            <input type=\"text\" id=\"nom\" name=\"nom\" required placeholder=\"Ex: Concert Symphonique\">\n" +
               "        </div>\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"lieu\">Lieu</label>\n" +
               "            <input type=\"text\" id=\"lieu\" name=\"lieu\" required placeholder=\"Ex: Palais des Sports\">\n" +
               "        </div>\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"date\">Date</label>\n" +
               "            <input type=\"date\" id=\"date\" name=\"date\" required>\n" +
               "        </div>\n" +
               "        <button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%; font-size: 1.125rem; padding: 0.75rem;\">Publier l'événement</button>\n" +
               "    </form>\n" +
               "</div>\n" +
               HtmlTemplate.getFooter();
    }
}
