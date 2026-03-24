package web;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {

    private HttpServer server;

    public void startServer() {
        try {
            // Création du serveur HTTP sur le port 8080
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            
            System.out.println("Initialisation des routes (Handlers)...");
            
            // Mapping des routes HTTP vers les classes Handler spécifiques
            server.createContext("/", new HomeHandler());
            server.createContext("/client", new ClientHandler());
            server.createContext("/organisateur", new OrganisateurHandler());
            server.createContext("/gerant", new GerantHandler());
            
            // Nouvelles routes d'Authentification & Achat
            server.createContext("/login", new LoginHandler());
            server.createContext("/register", new RegisterHandler());
            server.createContext("/logout", new LogoutHandler());
            server.createContext("/event", new EventDetailsHandler());
            server.createContext("/buy-ticket", new BuyTicketHandler());
            server.createContext("/profile", new ProfileHandler());
            server.createContext("/create-gerant", new CreateGerantHandler());

            // Utilise l'exécuteur par défaut
            server.setExecutor(null);
            
            // Démarrage du serveur
            server.start();
            System.out.println("=================================================");
            System.out.println(" Serveur Web Java démarré avec succès !");
            System.out.println(" Ouvrez votre navigateur sur: http://localhost:8080/");
            System.out.println("=================================================");
            
        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur Web : " + e.getMessage());
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("Serveur Web arrêté.");
        }
    }
}
