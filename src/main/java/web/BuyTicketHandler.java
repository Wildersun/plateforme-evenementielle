package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.BilletDAO;
import dao.EvenementDAO;
import dao.PersonneDAO;
import model.Billet;
import model.Evenement;
import model.Personne;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class BuyTicketHandler implements HttpHandler {

    private BilletDAO billetDAO = new BilletDAO();
    private EvenementDAO evenementDAO = new EvenementDAO();
    private PersonneDAO personneDAO = new PersonneDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (!"POST".equalsIgnoreCase(method)) {
            exchange.getResponseHeaders().set("Location", "/client");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        Personne session = SessionManager.getSession(exchange);
        if (session == null || !"client".equals(personneDAO.getRole(session.getId()))) {
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String formData = br.readLine();
            Map<String, String> params = parseFormData(formData);

            int eventId = Integer.parseInt(params.get("eventId"));
            String typeBillet = params.get("typeBillet");

            Evenement ev = evenementDAO.getEvenementById(eventId);
            if (ev == null) throw new IllegalArgumentException("Événement invalide");

            // Calcul du prix
            double basePrix = billetDAO.getPrixMinPourEvenement(eventId);
            if(basePrix == 0) basePrix = 5000;
            
            double prixFinal = basePrix;
            if ("VIP".equals(typeBillet)) prixFinal = basePrix * 2;
            else if ("Premium".equals(typeBillet)) prixFinal = basePrix * 5;

            // Enregistrement du billet pour le client
            Billet billet = new Billet();
            billet.setPrix(prixFinal);
            billet.setType(typeBillet);
            billet.setEvenementId(eventId);
            billet.setClientId(session.getId());
            
            billetDAO.creerBillet(billet);

            // Re-afficher la page avec succès (on pourrait rediriger vers un dashboard client)
            String html = HtmlTemplate.getHeader("Succès") +
                          HtmlTemplate.getNavbar(true, "client", session.getPrenom()) +
                          "<div class=\"container text-center\" style=\"text-align:center; padding: 3rem; background: white; border-radius: 1rem; max-width: 600px; margin: 40px auto;\">" +
                          "  <h1 style=\"color: var(--secondary);\">Achat Réussi !</h1>" +
                          "  <p>Votre billet <strong>" + typeBillet + "</strong> pour <em>" + ev.getNom() + "</em> a bien été réservé au prix de " + prixFinal + " FCFA.</p>" +
                          "  <a href=\"/client\" class=\"btn btn-primary\" style=\"margin-top:20px;\">Retour aux événements</a>" +
                          "</div>" +
                          HtmlTemplate.getFooter();

            byte[] responseBytes = html.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            exchange.getResponseHeaders().set("Location", "/client");
            exchange.sendResponseHeaders(302, -1);
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
}
