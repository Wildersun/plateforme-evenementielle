package dao;

import model.Evenement;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EvenementDAO {

    public void creerEvenement(Evenement evenement) {
        String sql = "INSERT INTO evenement (nom, date, lieu, organisateur_id) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, evenement.getNom());
            pstmt.setDate(2, evenement.getDate());
            pstmt.setString(3, evenement.getLieu());
            pstmt.setInt(4, evenement.getOrganisateurId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        evenement.setId(generatedKeys.getInt(1));
                        System.out.println("Événement créé avec succès avec l'ID : " + evenement.getId());
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'événement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public java.util.List<Evenement> getAllEvenements() {
        java.util.List<Evenement> evenements = new java.util.ArrayList<>();
        String sql = "SELECT * FROM evenement";
        Connection conn = DatabaseConnection.getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Evenement ev = new Evenement();
                ev.setId(rs.getInt("id"));
                ev.setNom(rs.getString("nom"));
                ev.setDate(rs.getDate("date"));
                ev.setLieu(rs.getString("lieu"));
                ev.setOrganisateurId(rs.getInt("organisateur_id"));
                evenements.add(ev);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evenements;
    }

    public Evenement getEvenementById(int id) {
        String sql = "SELECT * FROM evenement WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Evenement ev = new Evenement();
                    ev.setId(rs.getInt("id"));
                    ev.setNom(rs.getString("nom"));
                    ev.setDate(rs.getDate("date"));
                    ev.setLieu(rs.getString("lieu"));
                    ev.setOrganisateurId(rs.getInt("organisateur_id"));
                    return ev;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.List<Evenement> getEvenementsByOrganisateur(int organisateurId) {
        java.util.List<Evenement> evenements = new java.util.ArrayList<>();
        String sql = "SELECT * FROM evenement WHERE organisateur_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, organisateurId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Evenement ev = new Evenement();
                    ev.setId(rs.getInt("id"));
                    ev.setNom(rs.getString("nom"));
                    ev.setDate(rs.getDate("date"));
                    ev.setLieu(rs.getString("lieu"));
                    ev.setOrganisateurId(rs.getInt("organisateur_id"));
                    evenements.add(ev);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evenements;
    }

    public int getNbEvenements(Integer organisateurId) {
        String sql = (organisateurId == null) 
            ? "SELECT COUNT(*) FROM evenement" 
            : "SELECT COUNT(*) FROM evenement WHERE organisateur_id = ?";
            
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (organisateurId != null) {
                pstmt.setInt(1, organisateurId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public java.util.List<Evenement> searchEvenementsByName(String keyword) {
        java.util.List<Evenement> evenements = new java.util.ArrayList<>();
        String sql = "SELECT * FROM evenement WHERE LOWER(nom) LIKE ?";
        Connection conn = DatabaseConnection.getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Evenement ev = new Evenement();
                    ev.setId(rs.getInt("id"));
                    ev.setNom(rs.getString("nom"));
                    ev.setDate(rs.getDate("date"));
                    ev.setLieu(rs.getString("lieu"));
                    ev.setOrganisateurId(rs.getInt("organisateur_id"));
                    evenements.add(ev);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evenements;
    }
}
