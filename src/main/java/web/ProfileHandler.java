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

public class ProfileHandler implements HttpHandler {

    private PersonneDAO personneDAO = new PersonneDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Personne session = SessionManager.getSession(exchange);
        boolean isLoggedIn = session != null;
        
        if (!isLoggedIn) {
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        String response = "";
        
        // Fetch fresh details from DB to always display correct profile info
        Personne currentUser = personneDAO.getPersonneById(session.getId());

        if ("GET".equalsIgnoreCase(method)) {
            response = generateHtmlForm(currentUser, "");
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
                    response = generateHtmlForm(currentUser, "<div class='alert alert-error'>Veuillez remplir tous les champs.</div>");
                    sendResponse(exchange, response, 400);
                    return;
                }

                // Update user details
                currentUser.setNom(nom);
                currentUser.setPrenom(prenom);
                currentUser.setEmail(email);
                currentUser.setMotDePasse(motDePasse);
                
                personneDAO.updatePersonne(currentUser);
                
                // Update session
                session.setNom(nom);
                session.setPrenom(prenom);
                session.setEmail(email);
                // mot de passe n'est pas utilisé dans session
                
                response = generateHtmlForm(currentUser, "<div class='alert alert-success'>Profil mis à jour avec succès !</div>");
                sendResponse(exchange, response, 200);

            } catch (Exception e) {
                e.printStackTrace();
                response = generateHtmlForm(currentUser, "<div class='alert alert-error'>Erreur interne du serveur lors de la mise à jour.</div>");
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

    private String generateHtmlForm(Personne user, String messageHtml) {
        String role = personneDAO.getRole(user.getId());
        return HtmlTemplate.getHeader("Mon Profil") +
               HtmlTemplate.getNavbar(true, role, user.getPrenom()) +
               "<div class=\"card auth-card\" style=\"max-width: 500px;\">\n" +
               "    <div class=\"page-header\">\n" +
               "        <h1 class=\"page-title\">Mon Profil</h1>\n" +
               "        <p class=\"page-subtitle\">Consultez et modifiez vos informations personnelles.</p>\n" +
               "    </div>\n" +
               "    " + messageHtml + "\n" +
               "    <form method=\"POST\" action=\"/profile\">\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"nom\">Nom</label>\n" +
               "            <input type=\"text\" id=\"nom\" name=\"nom\" value=\"" + escapeHtml(user.getNom()) + "\" required>\n" +
               "        </div>\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"prenom\">Prénom</label>\n" +
               "            <input type=\"text\" id=\"prenom\" name=\"prenom\" value=\"" + escapeHtml(user.getPrenom()) + "\" required>\n" +
               "        </div>\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"email\">Adresse Email</label>\n" +
               "            <input type=\"email\" id=\"email\" name=\"email\" value=\"" + escapeHtml(user.getEmail()) + "\" required>\n" +
               "        </div>\n" +
               "        <div class=\"form-group\">\n" +
               "            <label for=\"mot_de_passe\">Nouveau mot de passe (ou actuel)</label>\n" +
               "            <input type=\"password\" id=\"mot_de_passe\" name=\"mot_de_passe\" value=\"" + escapeHtml(user.getMotDePasse() != null ? user.getMotDePasse() : "") + "\" required>\n" +
               "        </div>\n" +
               "        <button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%; font-size: 1.125rem; padding: 0.75rem;\">Sauvegarder les modifications</button>\n" +
               "    </form>\n" +
               "</div>\n" +
               HtmlTemplate.getFooter();
    }
    
    // Quick helper to escape quotes in HTML input values
    private String escapeHtml(String in) {
        if (in == null) return "";
        return in.replace("\"", "&quot;");
    }
}
