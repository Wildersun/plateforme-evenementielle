package main;

import web.WebServer;

public class MainApp {

    public static void main(String[] args) {
        System.out.println("Démarrage de l'application Plateforme Événementielle (Java Web)...");

        // Lancement du serveur HTTP Java
        WebServer webServer = new WebServer();
        webServer.startServer();
    }
}
