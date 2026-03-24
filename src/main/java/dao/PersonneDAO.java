package dao;

import model.Personne;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PersonneDAO {
    
    // type peut être 'client', 'organisateur' ou 'gerant'
    public void creerPersonne(Personne personne, String type) {
        String sqlPersonne = "INSERT INTO personne (nom, prenom, email, mot_de_passe) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();

        try {
            // Désactiver l'auto-commit pour gérer la transaction
            conn.setAutoCommit(false);

            // 1. Insérer dans la table parent 'personne'
            PreparedStatement pstmtPersonne = conn.prepareStatement(sqlPersonne, Statement.RETURN_GENERATED_KEYS);
            pstmtPersonne.setString(1, personne.getNom());
            pstmtPersonne.setString(2, personne.getPrenom());
            pstmtPersonne.setString(3, personne.getEmail());
            // Si pas défini, mettre 1234
            pstmtPersonne.setString(4, (personne.getMotDePasse() != null) ? personne.getMotDePasse() : "1234");
            
            int affectedRows = pstmtPersonne.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Échec de la création de la personne, aucune ligne affectée.");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = pstmtPersonne.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int personneId = generatedKeys.getInt(1);
                    personne.setId(personneId);

                    // 2. Insérer dans la table enfant correspondante
                    String sqlEnfant = "";
                    switch (type.toLowerCase()) {
                        case "client":
                            sqlEnfant = "INSERT INTO client (id) VALUES (?)";
                            break;
                        case "organisateur":
                            sqlEnfant = "INSERT INTO organisateur (id) VALUES (?)";
                            break;
                        case "gerant":
                            sqlEnfant = "INSERT INTO gerant (id) VALUES (?)";
                            break;
                        default:
                            throw new SQLException("Type de personne invalide : " + type);
                    }

                    PreparedStatement pstmtEnfant = conn.prepareStatement(sqlEnfant);
                    pstmtEnfant.setInt(1, personneId);
                    pstmtEnfant.executeUpdate();
                    pstmtEnfant.close();

                    // Valider la transaction
                    conn.commit();
                    System.out.println("Personne (" + type + ") créée avec succès avec l'ID : " + personneId);
                } else {
                    throw new SQLException("Échec de la création de la personne, aucun ID obtenu.");
                }
            }
            pstmtPersonne.close();
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Annuler en cas d'erreur
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Erreur lors de la création de la personne : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true); // Remettre l'auto-commit par défaut
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Personne authentifier(String email, String motDePasse) {
        String sql = "SELECT * FROM personne WHERE email = ? AND mot_de_passe = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, motDePasse);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Pour simplifier, on crée une instance de Personne anonyme (ou on pourrait avoir une factory)
                    // car on ne sait pas encore si c'est un client ou organisateur, mais on renvoie la base
                    Personne p = new Personne() {};
                    p.setId(rs.getInt("id"));
                    p.setNom(rs.getString("nom"));
                    p.setPrenom(rs.getString("prenom"));
                    p.setEmail(rs.getString("email"));
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getRole(int personneId) {
        Connection conn = DatabaseConnection.getConnection();
        try {
            // Vérifier Client
            PreparedStatement psClient = conn.prepareStatement("SELECT id FROM client WHERE id = ?");
            psClient.setInt(1, personneId);
            if (psClient.executeQuery().next()) return "client";

            // Vérifier Organisateur
            PreparedStatement psOrg = conn.prepareStatement("SELECT id FROM organisateur WHERE id = ?");
            psOrg.setInt(1, personneId);
            if (psOrg.executeQuery().next()) return "organisateur";

            // Vérifier Gérant
            PreparedStatement psGerant = conn.prepareStatement("SELECT id FROM gerant WHERE id = ?");
            psGerant.setInt(1, personneId);
            if (psGerant.executeQuery().next()) return "gerant";

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "inconnu";
    }

    public Personne getPersonneById(int id) {
        String sql = "SELECT * FROM personne WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Personne p = new Personne() {};
                    p.setId(rs.getInt("id"));
                    p.setNom(rs.getString("nom"));
                    p.setPrenom(rs.getString("prenom"));
                    p.setEmail(rs.getString("email"));
                    p.setMotDePasse(rs.getString("mot_de_passe"));
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePersonne(Personne personne) {
        String sql = "UPDATE personne SET nom = ?, prenom = ?, email = ?, mot_de_passe = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, personne.getNom());
            pstmt.setString(2, personne.getPrenom());
            pstmt.setString(3, personne.getEmail());
            pstmt.setString(4, personne.getMotDePasse());
            pstmt.setInt(5, personne.getId());
            pstmt.executeUpdate();
            System.out.println("Profil de la personne (ID: " + personne.getId() + ") mis à jour avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la personne : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
