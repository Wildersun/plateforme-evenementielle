package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Modifier les informations de connexion avec vos identifiants MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/plateforme_evenementielle?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root"; // Utilisateur MySQL
    private static final String PASSWORD = ""; // Mot de passe MySQL

    private static Connection connection = null;

    private DatabaseConnection() {
        // Constructeur privé pour empêcher l'instanciation (Singleton)
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Charger le driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Établir la connexion sans base de données d'abord
                Connection tempConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", USER, PASSWORD);
                // Créer la base de données si elle n'existe pas
                try (Statement stmt = tempConn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS plateforme_evenementielle");
                }
                tempConn.close();
                // Maintenant se connecter à la base de données
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                // Initialiser le schéma si nécessaire
                initializeSchema(connection);
                System.out.println("Connexion à la base de données réussie.");
            } catch (ClassNotFoundException e) {
                System.err.println("Driver MySQL introuvable.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la connexion à la base de données : " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }

    private static void initializeSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Vérifier si la table personne existe
            try {
                stmt.executeQuery("SELECT 1 FROM personne LIMIT 1");
            } catch (SQLException e) {
                // La table n'existe pas, exécuter le schéma
                System.out.println("Initialisation du schéma de base de données...");
                try (BufferedReader br = new BufferedReader(new FileReader("schema.sql"))) {
                    StringBuilder sql = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sql.append(line).append("\n");
                    }
                    // Diviser en statements individuels
                    String[] statements = sql.toString().split(";");
                    for (String statement : statements) {
                        if (!statement.trim().isEmpty()) {
                            stmt.execute(statement);
                        }
                    }
                    System.out.println("Schéma initialisé avec succès.");
                } catch (Exception ex) {
                    System.err.println("Erreur lors de l'initialisation du schéma : " + ex.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du schéma : " + e.getMessage());
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Connexion à la base de données fermée.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
