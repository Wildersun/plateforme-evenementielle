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

public class RegisterHandler implements HttpHandler {

    private PersonneDAO personneDAO = new PersonneDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String response = "";

        if ("GET".equalsIgnoreCase(method)) {
            response = generateRegisterForm("");
            sendResponse(exchange, response, 200);

        } else if ("POST".equalsIgnoreCase(method)) {
            try {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                Map<String, String> params = parseFormData(formData);

                String prenom = params.get("prenom");
                String nom = params.get("nom");
                String email = params.get("email");
                String password = params.get("password");
                String type = params.get("type"); // client ou organisateur

                if (nom == null || prenom == null || email == null || password == null || type == null) {
                    response = generateRegisterForm("<div class='alert alert-error'>Veuillez remplir tous les champs.</div>");
                    sendResponse(exchange, response, 400);
                    return;
                }

                Personne p = new Personne() {};
                p.setNom(nom);
                p.setPrenom(prenom);
                p.setEmail(email);
                p.setMotDePasse(password);
                
                try {
                    personneDAO.creerPersonne(p, type);
                    
                    // Connecter automatiquement l'utilisateur
                    SessionManager.createSession(exchange, p);
                    
                    // Rediriger vers l'espace adéquat
                    exchange.getResponseHeaders().set("Location", "/" + type);
                    exchange.sendResponseHeaders(302, -1);
                    return;

                } catch (Exception dbError) {
                    response = generateRegisterForm("<div class='alert alert-error'>Erreur: cet email est peut-être déjà utilisé.</div>");
                    sendResponse(exchange, response, 400);
                }

            } catch (Exception e) {
                e.printStackTrace();
                response = generateRegisterForm("<div class='alert alert-error'>Erreur serveur.</div>");
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

    private String generateRegisterForm(String alert) {
        StringBuilder html = new StringBuilder()
                .append(HtmlTemplate.getHeader("Inscription"))
                .append(HtmlTemplate.getNavbar(false, null, null))
                .append("<div class=\"card auth-card\">\n")
                .append("    <div class=\"page-header\">\n")
                .append("        <h1 class=\"page-title\">Créer un compte</h1>\n")
                .append("        <p class=\"page-subtitle\">Rejoignez-nous pour gérer vos événements</p>\n")
                .append("    </div>\n")
                .append(alert)
                .append("    <form method=\"POST\" action=\"/register\">\n")
                .append("        <div class=\"form-group\">\n")
                .append("            <label for=\"prenom\">Prénom</label>\n")
                .append("            <input type=\"text\" id=\"prenom\" name=\"prenom\" required>\n")
                .append("        </div>\n")
                .append("        <div class=\"form-group\">\n")
                .append("            <label for=\"nom\">Nom</label>\n")
                .append("            <input type=\"text\" id=\"nom\" name=\"nom\" required>\n")
                .append("        </div>\n")
                .append("        <div class=\"form-group\">\n")
                .append("            <label for=\"email\">Adresse e-mail</label>\n")
                .append("            <input type=\"email\" id=\"email\" name=\"email\" required>\n")
                .append("        </div>\n")
                .append("        <div class=\"form-group\">\n")
                .append("            <label for=\"password\">Mot de passe</label>\n")
                .append("            <input type=\"password\" id=\"password\" name=\"password\" required>\n")
                .append("        </div>\n")
                .append("        <div class=\"form-group\">\n")
                .append("            <label for=\"type\">Je souhaite m'inscrire en tant que</label>\n")
                .append("            <select id=\"type\" name=\"type\" required>\n")
                .append("                <option value=\"client\">Client (Acheter des billets)</option>\n")
                .append("                <option value=\"organisateur\">Organisateur (Créer des événements)</option>\n")
                .append("            </select>\n")
                .append("        </div>\n")
                .append("        <button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%; font-size: 1.125rem; padding: 0.75rem;\">S'inscrire</button>\n")
                .append("    </form>\n")
                .append("    <p style=\"text-align: center; margin-top: 1.5rem; color: var(--gray);\">\n")
                .append("        Déjà un compte ? <a href=\"/login\" style=\"color: var(--primary); text-decoration: none; font-weight: 600;\">Se connecter</a>\n")
                .append("    </p>\n")
                .append("</div>\n")
                .append(HtmlTemplate.getFooter());
        return html.toString();
    }
}
