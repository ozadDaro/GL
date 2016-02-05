package models;

import com.avaje.ebean.Model;
import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Guillaume on 25/01/2016.
 */
@Entity
@Table
public class Projet extends Model {

    @Id
    @GeneratedValue
    public Long id;
    public String nom;
    public String description;
    public Utilisateur responsable;
    @Formats.DateTime(pattern = "dd/MM/yyyy HH:mm")
    public LocalDate dateDebutTheorique;
    @Formats.DateTime(pattern = "dd/MM/yyyy HH:mm")
    public LocalDate dateFinTheorique;
    @Formats.DateTime(pattern = "dd/MM/yyyy HH:mm")
    public LocalDate dateDebutReel;
    @Formats.DateTime(pattern = "dd/MM/yyyy HH:mm")
    public LocalDate dateFinReel;
    public Integer chargeInitiale;
    public Byte avancementGlobal;
    public Boolean enCours;
    public Boolean archive;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn
    public Client client;

    @Constraints.Min(1)
    @Constraints.Max(3)
    public Integer priorite;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name="Tache")
    public List<Tache> listTaches;

    public UniteProjetEnum unite;

    public static Model.Finder<Long, Projet> find = new Model.Finder<>(Projet.class);

    public Projet(String nom, String description, Utilisateur responsable, LocalDate dateDebutTheorique, LocalDate dateFinTheorique,
                  LocalDate dateDebutReel, LocalDate dateFinReel, Integer chargeInitiale, UniteProjetEnum unite,
                  Byte avancementGlobal, Boolean enCours, Boolean archive, Client client, Integer priorite, List<Tache> listTaches) {
        this.nom = nom;
        this.description = description;
        this.responsable = responsable;
        this.dateDebutTheorique = dateDebutTheorique;
        this.dateFinTheorique = dateFinTheorique;
        this.dateDebutReel = dateDebutReel;
        this.dateFinReel = dateFinReel;
        this.chargeInitiale = chargeInitiale;
        this.unite = unite;
        this.avancementGlobal = avancementGlobal;
        this.enCours = enCours;
        this.archive = archive;
        this.client = client;
        this.priorite = priorite;
        this.listTaches = listTaches;
    }

    public Projet() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        try {
            Projet projet = (Projet) obj;
            return (projet.id.equals(this.id) && projet.nom.equals(this.nom) &&
                    projet.description.equals(this.description) &&
                    projet.dateDebutTheorique.equals(this.dateDebutTheorique) &&
                    projet.dateFinTheorique.equals(this.dateFinTheorique) &&
                    projet.dateDebutReel.equals(this.dateDebutReel) &&
                    projet.dateFinTheorique.equals(this.dateFinTheorique) &&
                    projet.chargeInitiale.equals(this.chargeInitiale) &&
                    projet.avancementGlobal.equals(this.avancementGlobal) &&
                    projet.enCours.equals(this.enCours) &&
                    projet.archive.equals(this.archive) &&
                    projet.priorite.equals(this.priorite) &&
                    projet.client.id.equals(this.client.id) );
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Projet : ").append(id).append("] : ").append(nom).append(", ").append(description);
        sb.append("\nDebutTH : ").append(dateDebutTheorique).append(", FinTH : ").append(dateFinTheorique);
        sb.append(", DebutRE : ").append(dateDebutReel).append(", FinRE : ").append(dateFinReel);
        sb.append("\nChargeInitiale : ").append(chargeInitiale).append(", Avancement (%) : ").append(avancementGlobal);
        sb.append(", En cours : ").append(enCours).append(", archive : ").append(archive);
        sb.append(", Priorite :").append(priorite).append("\n");
        if(client != null){
            sb.append("Client : ").append(client.nom);
        }
        if(listTaches != null) {
            for (Tache tache : listTaches) {
                sb.append("\n").append(tache.nom).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * TODO testme
     * Ajouter la tache en parametre a la liste des taches du projet
     * @param tache
     */
    public void ajouterTache(Tache tache)throws IllegalArgumentException{
        if(listTaches.contains(tache)){
            throw new IllegalArgumentException("Le projet "+this.nom+", contient deja la tache "+tache.nom+
                    ", creation impossible");
        }
        listTaches.add(tache);
    }


    /**
     * TODO testme
     * Modifie la tache en parametre
     * @param tache
     * @throws IllegalArgumentException
     */
    public void modifierTache(Tache tache)throws IllegalArgumentException{
        if(!listTaches.contains(tache)){
            throw new IllegalArgumentException("Le projet "+this.nom+", ne contient pas la tache "+tache.nom+
                    ", modification impossible");
        }

        //TODO modifier tâche
    }

    /**
     * TODO testme
     * Supprimer la tâche du systeme
     * @param tache
     * @throws IllegalArgumentException
     */
    public void supprimerTache(Tache tache) throws IllegalArgumentException{
        if(!listTaches.contains(tache)){
            throw new IllegalArgumentException("Le projet "+this.nom+", ne contient pas la tache "+tache.nom+
                    ", suppression impossible");
        }
        listTaches.remove(tache);
    }

    /**
     * TODO testme
     * Affecte l'utilisateur en parametre en tant que responsable du projet
     * @param responsable
     * @throws IllegalStateException
     */
    public void associerResponsable(Utilisateur responsable) throws IllegalStateException{
        if(this.responsable != null){
            throw new IllegalStateException("Il y a deja un responsable pour ce projet");
        }
        this.responsable = responsable;
    }

    /**
     * TODO testme
     * Modifie le responsable de projet par l'utilisateur en parametre
     * @param responsable
     * @throws IllegalArgumentException
     */
    public void modifierResponsable(Utilisateur responsable) throws IllegalArgumentException{
        if(this.responsable == responsable){
            throw new IllegalArgumentException("Remplacement du responsable de projet par le même responsable");
        }
        this.responsable = responsable;
    }

    /**
     * TODO testme
     * Associe le projet courant au client passé en parametre
     * @param client
     * @throws IllegalStateException
     */
    public void associerClient(Client client) throws IllegalStateException{
        if(this.client != null){
            throw new IllegalStateException("Il y a deja un client pour ce projet");
        }
        this.client = client;
    }
}