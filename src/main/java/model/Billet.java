package model;

public class Billet {
    private int id;
    private double prix;
    private String type;
    private int evenementId;
    private Integer clientId;

    public Billet() {
    }

    public Billet(int id, double prix, String type, int evenementId, Integer clientId) {
        this.id = id;
        this.prix = prix;
        this.type = type;
        this.evenementId = evenementId;
        this.clientId = clientId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getEvenementId() {
        return evenementId;
    }

    public void setEvenementId(int evenementId) {
        this.evenementId = evenementId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
}
