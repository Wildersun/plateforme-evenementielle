package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Modifier les informations de connexion avec vos identifiants MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/plateforme_evenementielle";
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
                // Établir la connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
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
