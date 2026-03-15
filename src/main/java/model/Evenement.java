package model;

import java.sql.Date;

public class Evenement {
    private int id;
    private String nom;
    private Date date;
    private String lieu;
    private int organisateurId;

    public Evenement() {
    }

    public Evenement(int id, String nom, Date date, String lieu, int organisateurId) {
        this.id = id;
        this.nom = nom;
        this.date = date;
        this.lieu = lieu;
        this.organisateurId = organisateurId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public int getOrganisateurId() {
        return organisateurId;
    }

    public void setOrganisateurId(int organisateurId) {
        this.organisateurId = organisateurId;
    }
}
