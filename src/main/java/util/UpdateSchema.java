package util;

import java.sql.Connection;
import java.sql.Statement;

public class UpdateSchema {
    public static void main(String[] args) {
        System.out.println("Mise à jour du schéma de base de données...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            String sql = "ALTER TABLE personne ADD COLUMN mot_de_passe VARCHAR(255) NOT NULL DEFAULT '1234'";
            stmt.executeUpdate(sql);
            System.out.println("Colonne 'mot_de_passe' ajoutée avec succès !");
            
        } catch (Exception e) {
            System.out.println("Info: " + e.getMessage() + " (La colonne existe peut-être déjà).");
        }
    }
}
