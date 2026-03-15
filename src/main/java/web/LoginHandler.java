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

public class LoginHandler implements HttpHandler {

    private PersonneDAO personneDAO = new PersonneDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String response = "";

        if ("GET".equalsIgnoreCase(method)) {
            response = generateLoginForm("");
            sendResponse(exchange, response, 200);

        } else if ("POST".equalsIgnoreCase(method)) {
            try {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                Map<String, String> params = parseFormData(formData);

                String email = params.get("email");
                String password = params.get("password");

                Personne user = personneDAO.authentifier(email, password);

                if (user != null) {
                    // Authentification réussie
                    SessionManager.createSession(exchange, user);
                    String role = personneDAO.getRole(user.getId());

                    // Redirection
                    exchange.getResponseHeaders().set("Location", "/".equals(role) ? "/" : ("/" + role));
                    exchange.sendResponseHeaders(302, -1);
                    return;
                } else {
                    response = generateLoginForm("<div class='alert alert-error'>Email ou mot de passe incorrect.</div>");
                    sendResponse(exchange, response, 401);
                }

            } catch (Exception e) {
                e.printStackTrace();
                response = generateLoginForm("<div class='alert alert-error'>Erreur serveur.</div>");
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

    private String generateLoginForm(String alert) {
        StringBuilder html = new StringBuilder()
                .append(HtmlTemplate.getHeader("Connexion"))
                .append(HtmlTemplate.getNavbar(false, null, null))
                .append("<div class=\"card auth-card\">\n")
                .append("    <div class=\"page-header\">\n")
                .append("        <h1 class=\"page-title\">Bon retour</h1>\n")
                .append("        <p class=\"page-subtitle\">Connectez-vous à votre compte</p>\n")
                .append("    </div>\n")
                .append(alert)
                .append("    <form method=\"POST\" action=\"/login\">\n")
                .append("        <div class=\"form-group\">\n")
                .append("            <label for=\"email\">Adresse e-mail</label>\n")
                .append("            <input type=\"email\" id=\"email\" name=\"email\" required placeholder=\"vous@exemple.com\">\n")
                .append("        </div>\n")
                .append("        <div class=\"form-group\">\n")
                .append("            <label for=\"password\">Mot de passe</label>\n")
                .append("            <input type=\"password\" id=\"password\" name=\"password\" required>\n")
                .append("        </div>\n")
                .append("        <button type=\"submit\" class=\"btn btn-primary\" style=\"width: 100%; font-size: 1.125rem; padding: 0.75rem;\">Se connecter</button>\n")
                .append("    </form>\n")
                .append("    <p style=\"text-align: center; margin-top: 1.5rem; color: var(--gray);\">\n")
                .append("        Pas encore de compte ? <a href=\"/register\" style=\"color: var(--primary); text-decoration: none; font-weight: 600;\">S'inscrire</a>\n")
                .append("    </p>\n")
                .append("</div>\n")
                .append(HtmlTemplate.getFooter());
        return html.toString();
    }
}
