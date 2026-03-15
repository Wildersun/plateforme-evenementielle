package util;
import java.sql.Connection;
import java.sql.Statement;

public class CleanDB {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            
            // Delete the clients, gérants and organisateurs the bot previously created
            int deletedPersonne = stmt.executeUpdate("DELETE FROM personne WHERE email IN ('jean.dupont@test.com', 'alice.martin@test.com', 'paul.lefebvre@test.com')");
            // Delete the event
            int deletedEvents = stmt.executeUpdate("DELETE FROM evenement WHERE nom = 'Concert Rock'");
            
            System.out.println("Cleaned up bot data. Deleted " + deletedPersonne + " test users and " + deletedEvents + " test events.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
