package dao;

import model.Billet;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class BilletDAO {

    public void creerBillet(Billet billet) {
        String sql = "INSERT INTO billet (prix, type, evenement_id, client_id) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setDouble(1, billet.getPrix());
            pstmt.setString(2, billet.getType());
            pstmt.setInt(3, billet.getEvenementId());
            
            if (billet.getClientId() != null) {
                pstmt.setInt(4, billet.getClientId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        billet.setId(generatedKeys.getInt(1));
                        System.out.println("Billet créé avec succès avec l'ID : " + billet.getId());
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du billet : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public double getPrixMinPourEvenement(int evenementId) {
        String sql = "SELECT MIN(prix) AS min_prix FROM billet WHERE evenement_id = ?";
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, evenementId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double prix = rs.getDouble("min_prix");
                    // rs.wasNull() sera vrai si aucun billet n'existe pour cet événement
                    if (!rs.wasNull()) {
                        return prix;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
