import com.avaje.ebean.Ebean;
import com.avaje.ebean.common.BeanList;
import models.*;
import controllers.Utils.Utils;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Logger;
import play.test.FakeApplication;
import play.test.Helpers;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

/**
 * Created by Guillaume on 01/02/2016.
 */
public class ModelBeanTest {

    public static FakeApplication app;
    public static String createDdl = "";
    public static String dropDdl = "";

    @BeforeClass
    public static void startApp() throws IOException {
        app = Helpers.fakeApplication(Helpers.inMemoryDatabase());
        Helpers.start(app);

        // Reading the evolution file
        String evolutionContent = FileUtils.readFileToString(
                app.getWrappedApplication().getFile("conf/evolutions/default/1.sql"));
        String[] splittedEvolutionContent = evolutionContent.split("# --- !Ups");
        String[] upsDowns = splittedEvolutionContent[1].split("# --- !Downs");
        createDdl = upsDowns[0];
        dropDdl = upsDowns[1];
    }

    @Before
    public void beforeEachTest() {
        Ebean.execute(Ebean.createCallableSql(dropDdl));
        Ebean.execute(Ebean.createCallableSql(createDdl));
    }

    @AfterClass
    public static void stopApp() {
        Helpers.stop(app);
    }

    @Test
    public void testPersistAdresse() {
            Adresse a1 = new Adresse("3 Street Cloude","645123","Cupertinoss","America");
            Logger.debug(a1.toString());
            a1.save();
            Logger.debug(a1.toString());
            assertNotNull(a1.id);
            Adresse a2 = Adresse.find.byId(a1.id);
            assertEquals(a1,a2);

    }

    @Test
    public void testFindAdress() {
            Adresse a1 = new Adresse("3 Street Cloude","645123","Cupertinoss","America");
            Adresse a2 = new Adresse("9 rue nuage","123456","paris","France");
            a1.save();
            a2.save();
            assertNotNull(a1.id);
            assertNotNull(a2.id);
            Adresse a3 = Adresse.find.where().eq("adresse","3 Street Cloude").findList().get(0);
            Adresse a4 = Adresse.find.where().eq("adresse","9 rue nuage").findList().get(0);
            assertEquals(a1,a3);
            assertEquals(a2,a4);
            Adresse a5 = Adresse.find.where().eq("zipCode","645123").findList().get(0);
            Adresse a6 = Adresse.find.where().eq("zipCode","123456").findList().get(0);
            assertEquals(a1,a5);
            assertEquals(a2,a6);
            Adresse a7 = Adresse.find.where().eq("ville","Cupertinoss").findList().get(0);
            Adresse a8 = Adresse.find.where().eq("ville","paris").findList().get(0);
            assertEquals(a1,a7);
            assertEquals(a2,a8);
            Adresse a9 = Adresse.find.where().eq("pays","America").findList().get(0);
            Adresse a10 = Adresse.find.where().eq("pays","France").findList().get(0);
            assertEquals(a1,a9);
            assertEquals(a2,a10);
    }


    @Test
    public void testPersistClient(){
            Adresse a1 = new Adresse("30 Street Cloudz","645019","Cupertinoooo","USAAA");
            a1.save();
            List<Contact> listContacts = new BeanList<>();
            Contact c1 = new Contact("Jobs","Steve","s.j@apple.com","0215465978");
            Contact c2 = new Contact("James","Frank","f.j@apple.com","0215465979");
            listContacts.add(c1);
            listContacts.add(c2);

            Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
            utilisateur.save();

            Projet pr = new Projet("Site Apple","Développement du nouveau site d'Apple", utilisateur,
                    Utils.getDateFrom(2016,2,2),Utils.getDateFrom(2016,2,10),Utils.getDateFrom(2016,2,3),
                    Utils.getDateFrom(2016,2,9),Utils.getDateFrom(2016,2,9),24D, UniteProjetEnum.SEMAINE,new Byte("0"),false,false,null,3,null,null);
            List<Projet> listProjet = new BeanList<>();
            listProjet.add(pr);
            Client cl = new Client("Applee",2,true, a1,listContacts, listProjet);
            cl.save();

            Logger.debug("c1 id="+c1.id);
            Logger.debug(cl.toString());
            assertNotNull(cl.id);
            Client cl2 = Client.find.byId(cl.id);
            Logger.debug(cl2.toString());
            assertEquals(cl,cl2);
    }

    @Test
    public void testPersistContact() {
            Client client = new Client();
            client.nom = "Apple";
            client.save();

            Contact contact = new Contact();
            contact.nom = "Jobs";
            contact.prenom = "Steve";
            contact.email = "s.j@apple.com";
            contact.telephone = "0215465978";
            contact.client = client;
            contact.save();

            client.ajouterContact(contact);

            Logger.debug(contact.toString());
            assertNotNull(contact.id);
            Contact c2 = Contact.find.byId(contact.id);
            Logger.debug(c2.toString());
            assertEquals(contact,c2);
    }

    @Test
    public void testFindContact() {
            Contact c1 = new Contact("Jobs","Steve","s.j@apple.com","0245651229");
            Contact c2 = new Contact("Ibra","Zlatan","z.i@zlatanino.com","0123456789");
            c1.save();
            c2.save();
            assertNotNull(c1.id);
            assertNotNull(c2.id);
            Contact a3 = Contact.find.where().eq("nom","Jobs").findList().get(0);
            Contact a4 = Contact.find.where().eq("nom","Ibra").findList().get(0);
            assertEquals(c1,a3);
            assertEquals(c2,a4);
            Contact a5 = Contact.find.where().eq("prenom","Steve").findList().get(0);
            Contact a6 = Contact.find.where().eq("prenom","Zlatan").findList().get(0);
            assertEquals(c1,a5);
            assertEquals(c2,a6);
            Contact a7 = Contact.find.where().eq("email","s.j@apple.com").findList().get(0);
            Contact a8 = Contact.find.where().eq("email","z.i@zlatanino.com").findList().get(0);
            assertEquals(c1,a7);
            assertEquals(c2,a8);
            Contact a9 = Contact.find.where().eq("telephone","0245651229").findList().get(0);
            Contact a10 = Contact.find.where().eq("telephone","0123456789").findList().get(0);
            assertEquals(c1,a9);
            assertEquals(c2,a10);
    }
/*
    @Test
    public void testPersistNotification() {
            Notification n1 = new Notification();
            n1.title = "3 Street Cloud";
            n1.contentNotification = "64500";
            n1.dateEnvoi = Utils.getDateFrom(2016,12,1);
            //n1.link = "http://localhost:9000/notif";
            n1.etatLecture = false;
            n1.archiver = false;
            n1.utilisateur = new Utilisateur("Jean","De la fontaine","jlf@vieux.com","0247563598",false,"azertY1");
            Logger.debug(n1.toString());
            n1.save();
            Logger.debug(n1.toString());
            assertNotNull(n1.id);
            Notification n2 = Notification.find.byId(n1.id);
            assertEquals(n1,n2);
    }

    @Test
    public void testFindNotification() {
            Notification n1 = new Notification("title1","content1",Utils.getDateFrom(2016,10,10),true,false,new Utilisateur(), null, new Projet());
            Notification n2 = new Notification("title2","content2",Utils.getDateFrom(2016,10,12),false,true,new Utilisateur(), new Tache(), null);
            n1.save();
            n2.save();
            Logger.debug(n1.toString());
            Logger.debug(n2.toString());
            assertNotNull(n1.id);
            assertNotNull(n2.id);
            Notification a3 = Notification.find.where().eq("title","title1").findList().get(0);
            Notification a4 = Notification.find.where().eq("title","title2").findList().get(0);
            assertEquals(n1,a3);
            assertEquals(n2,a4);
            Notification a5 = Notification.find.where().eq("contentNotification","content1").findList().get(0);
            Notification a6 = Notification.find.where().eq("contentNotification","content2").findList().get(0);
            assertEquals(n1,a5);
            assertEquals(n2,a6);
            Notification a9 = Notification.find.where().eq("link","http://notif1.com").findList().get(0);
            Notification a10 = Notification.find.where().eq("link","http://notif2.com").findList().get(0);
            assertEquals(n1,a9);
            assertEquals(n2,a10);
            Notification a11 = Notification.find.where().eq("etatLecture",true).findList().get(0);
            Notification a12 = Notification.find.where().eq("etatLecture",false).findList().get(0);
            assertEquals(n1,a11);
            assertEquals(n2,a12);
            Notification a13 = Notification.find.where().eq("archiver",false).findList().get(0);
            Notification a14 = Notification.find.where().eq("archiver",true).findList().get(0);
            assertEquals(n1,a13);
            assertEquals(n2,a14);
    }
*/
    @Test
    public void testPersistProjet() {
            Client cl = new Client();
            cl.nom = "Apple";
            cl.save();

            Projet projet = new Projet();
            projet.nom = "Site Apple";
            projet.description = "Développement du nouveau site d'Apple";
            projet.dateDebutTheorique = Utils.getDateFrom(2016,2,2);
            projet.dateFinTheorique = Utils.getDateFrom(2016,2,10);
            projet.dateDebutReel = Utils.getDateFrom(2016,2,3);
            projet.dateFinReelTot = Utils.getDateFrom(2016,2,9);
            projet.dateFinReelTard = Utils.getDateFrom(2016,2,9);
            projet.chargeInitiale = 24D;
            projet.unite = UniteProjetEnum.SEMAINE;
            projet.avancementGlobal = new Byte("0");
            projet.enCours = true;
            projet.archive = false;
            projet.client = cl;
            projet.priorite = 1;
            projet.save();

            Logger.debug(projet.toString());
            assertNotNull(projet.id);
            Projet pr2 = Projet.find.byId(projet.id);
            Logger.debug(pr2.toString());

            assertEquals(projet,pr2);
    }

    @Test
    public void testPersistTask() {
            Projet pr = new Projet();
            pr.nom = "New project";
            Contact c1 = new Contact("Toto","Tata","toto.tata@tt.tt","0123456789");
            List<Contact> contactList = new BeanList<>();
            contactList.add(c1);
            Utilisateur u1 = Utilisateur.create("Blanchard","Guillaume","g.b@abc.fr","0123456789","azertY1");
            u1.save();
            List<Utilisateur> utilisateursNotifications = null;
            Tache tache = new Tache(
                    "Etude 1",
                    "Cette tâche permet de réaliser l'étude du projet",u1,
                    1,
                    true,
                    Utils.getDateFrom(2016,2,1),
                    Utils.getDateFrom(2016,2,20),
                    Utils.getDateFrom(2016,2,25),
                    20D,
                    0D,
                    20D,
                    contactList,pr,null,null,null);
            //tache.responsableTache = u1;
            tache.idTache = "1";

            c1.save();
            pr.save();
            tache.save();
            u1.affectTache(tache);

            Logger.debug(tache.toString());
            assertNotNull(tache.id);
            Tache t2 = Tache.find.byId(tache.id);
            Logger.debug(t2.id.toString());
            Logger.debug(t2.nom);
            Logger.debug(t2.description);
            Logger.debug(t2.critique.toString());
            //Logger.debug(Double.toString(t2.getChargeConsommee()));
            Logger.debug(t2.chargeInitiale.toString());
            Logger.debug(t2.dateDebut.toString());
            Logger.debug(t2.dateFinTard.toString());
            Logger.debug(t2.dateFinTot.toString());
            Logger.debug(t2.niveau.toString());
            //Logger.debug(t2.interlocuteurs.toString());
            Logger.debug(t2.toString());
            assertEquals(tache,t2);
    }


    @Test
    public void testPersistUtilisateur() {
            Utilisateur u1 = new Utilisateur();
            u1.nom = "Jobss";
            u1.prenom = "Steeve";
            u1.email = "s.ja@apple.com";
            u1.telephone = "0215465948";
            u1.save();
            u1.setPassword("azertY1");
            Logger.debug(u1.toString());
            u1.save();
            Logger.debug(u1.toString());
            assertNotNull(u1.id);
            Utilisateur u2 = Utilisateur.find.byId(u1.id);
            assertEquals(u1,u2);
            assertTrue(u2.checkPassword("azertY1"));
            assertFalse(u2.checkPassword("FALSE_PASSWORD"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPasswordCourtException(){
            Utilisateur u1 = new Utilisateur();
            u1.setPassword("Azer1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPasswordChiffreException(){
            Utilisateur u1 = new Utilisateur();
            u1.setPassword("Azertya");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPasswordMajException(){
            Utilisateur u1 = new Utilisateur();
            u1.setPassword("azerty1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPasswordMinException(){
            Utilisateur u1 = new Utilisateur();
            u1.setPassword("AZERTY1");
    }

    @Test
    public void testFindUtilisateur() {
            Utilisateur u1 = Utilisateur.create("A","C","a.c@apple.com","1236549870","TOTO123a");
            Utilisateur u2 = Utilisateur.create("B","D","b.d@zlatanino.com","0147258369","TATA123a");
            u1.save();
            u2.save();
            assertNotNull(u1.id);
            assertNotNull(u2.id);
            Utilisateur a3 = Utilisateur.find.where().eq("nom","A").findList().get(0);
            Utilisateur a4 = Utilisateur.find.where().eq("nom","B").findList().get(0);
            assertEquals(u1,a3);
            assertEquals(u2,a4);
            Utilisateur a5 = Utilisateur.find.where().eq("prenom","C").findList().get(0);
            Utilisateur a6 = Utilisateur.find.where().eq("prenom","D").findList().get(0);
            assertEquals(u1,a5);
            assertEquals(u2,a6);
            Utilisateur a7 = Utilisateur.find.where().eq("email","a.c@apple.com").findList().get(0);
            Utilisateur a8 = Utilisateur.find.where().eq("email","b.d@zlatanino.com").findList().get(0);
            assertEquals(u1,a7);
            assertEquals(u2,a8);
            Utilisateur a9 = Utilisateur.find.where().eq("telephone","1236549870").findList().get(0);
            Utilisateur a10 = Utilisateur.find.where().eq("telephone","0147258369").findList().get(0);
            assertEquals(u1,a9);
            assertEquals(u2,a10);
            assertEquals(Utilisateur.find.where().eq("password","TOTO").findList().size(),0);
            assertEquals(Utilisateur.find.where().eq("password","TATA").findList().size(),0);
    }
}
