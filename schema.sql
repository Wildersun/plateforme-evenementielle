CREATE DATABASE IF NOT EXISTS plateforme_evenementielle;
USE plateforme_evenementielle;

-- Table parente Personne
CREATE TABLE personne (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL
);

-- Table Organisateur (hérite de Personne)
CREATE TABLE organisateur (
    id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES personne(id) ON DELETE CASCADE
);

-- Table Client (hérite de Personne)
CREATE TABLE client (
    id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES personne(id) ON DELETE CASCADE
);

-- Table Gerant (hérite de Personne)
CREATE TABLE gerant (
    id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES personne(id) ON DELETE CASCADE
);

-- Table Evenement
CREATE TABLE evenement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(200) NOT NULL,
    date DATE NOT NULL,
    lieu VARCHAR(255) NOT NULL,
    organisateur_id INT NOT NULL,
    FOREIGN KEY (organisateur_id) REFERENCES organisateur(id) ON DELETE CASCADE
);

-- Table Billet
CREATE TABLE billet (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prix DECIMAL(10, 2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    evenement_id INT NOT NULL,
    client_id INT,
    FOREIGN KEY (evenement_id) REFERENCES evenement(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE SET NULL
);
