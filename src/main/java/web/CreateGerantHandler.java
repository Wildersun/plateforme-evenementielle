package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.PersonneDAO;
import model.Personne;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class CreateGerantHandler implements HttpHandler {

    private PersonneDAO personneDAO = new PersonneDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Personne session = SessionManager.getSession(exchange);
        boolean isLoggedIn = session != null;
        String role = isLoggedIn ? personneDAO.getRole(session.getId()) : null;

        // Only an organizer can create a manager
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
                String prenom = params.get("prenom");
                String email = params.get("email");
                String motDePasse = params.get("mot_de_passe");

                if (nom == null || prenom == null || email == null || motDePasse == null || 
                    nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || motDePasse.isEmpty()) {
                    response = generateHtmlForm(nomUser, "<div class='alert alert-error'>Veuillez remplir tous les champs.</div>");
                    sendResponse(exchange, response, 400);
                    return;
                }

                Personne newGerant = new Personne() {};
                newGerant.setNom(nom);
                newGerant.setPrenom(prenom);
                newGerant.setEmail(email);
                newGerant.setMotDePasse(motDePasse);
                
                personneDAO.creerPersonne(newGerant, "gerant");

                response = generateHtmlForm(nomUser, "<div class='alert alert-success'>Compte gérant '" + prenom + " " + nom + "' créé avec succès !</div>");
                sendResponse(exchange, response, 200);

            } catch (Exception e) {
                e.printStackTrace();
                response = generateHtmlForm(nomUser, "<div class='alert alert-error'>Erreur interne. Cet email est peut-être déjà utilisé.</div>");
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
        return HtmlTemplate.getHeader("Créer Gérant") +
               HtmlTemplate.getNavbar(true, "organisateur", nomUser) +
               "<div class=\"card auth-card\" style=\"max-width: 500px;\">\n" +
               "    <div class=\"page-header\">\n" +
               "        <h1 class=\"page-title\">Nouveau Gérant</h1>\n" +
               "        <p class=\"page-subtitle\">Invitez un nouveau gérant à gérer la plateforme.</p>\n" +
               "    </div>\n" +
               "    " + messageHtml + "\n" +
               "    <form method=\"POST\" action=\"/create-gerant\">\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"nom\">Nom</label>\n" +
               "            <input type=\"text\" id=\"nom\" name=\"nom\" required>\n" +
               "        </div>\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"prenom\">Prénom</label>\n" +
               "            <input type=\"text\" id=\"prenom\" name=\"prenom\" required>\n" +
               "        </div>\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"email\">Email</label>\n" +
               "            <input type=\"email\" id=\"email\" name=\"email\" required>\n" +
               "        </div>\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"mot_de_passe\">Mot de passe provisoire</label>\n" +
               "            <input type=\"password\" id=\"mot_de_passe\" name=\"mot_de_passe\" required>\n" +
               "        </div>\n" +
               "        <button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%; font-size: 1.125rem; padding: 0.75rem;\">Créer le Gérant</button>\n" +
               "    </form>\n" +
               "    <div style=\"text-align:center; margin-top:20px;\"><a href=\"/organisateur\">Retour au Dashboard</a></div>\n" +
               "</div>\n" +
               HtmlTemplate.getFooter();
    }
}
