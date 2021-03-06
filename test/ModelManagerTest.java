import com.avaje.ebean.Ebean;
import com.avaje.ebean.common.BeanList;
import controllers.Utils.Utils;
import models.*;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Logger;
import play.test.FakeApplication;
import play.test.Helpers;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by zzcoolj on 16/2/24.
 */

public class ModelManagerTest {

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
    public void testCreerTacheInitialisationProjet() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        Client client = new Client("Apple", 2, false, null, new BeanList<Contact>(), new BeanList<Projet>());

        // Attention! C'est obligé d'ajouter un responsable dans le constructeur de Projet et Tache.
        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,2,10, dateDebutReel: 2016,2,3, dateFinReelTot: 2016,2,9, dateFinReelTard: 2016,2,9
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 2), Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 9), Utils.getDateFrom(2016, 2, 9), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, client, 3, null, null);
        projet.save();

        //tache dateDebut: (2016,2,3), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tache = new Tache("Tache", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 10D, 20D, null, null, null, null, null);
        tache.save();

        try {
            projet.creerTacheInitialisationProjet(tache);

            Tache tacheSelected = Tache.find.byId(tache.id);
            assertEquals(tacheSelected.idTache, "1");
            assertEquals(tacheSelected.niveau, Integer.valueOf(0));
            assertEquals(tacheSelected.parent, null);

            Projet projetSelected = Projet.find.byId(projet.id);
            assertTrue(projetSelected.listTaches().contains(tacheSelected));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreerTacheAuDessus() {

        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,2,10, dateDebutReel: 2016,2,3, dateFinReelTot: 2016,2,9, dateFinReelTard: 2016,2,9
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 2), Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 9), Utils.getDateFrom(2016, 2, 9), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        //tache dateDebut: (2016,2,3), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tache1 = new Tache("Tache1", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 10D, 20D, null, null, null, null, null);
        tache1.save();

        //tache dateDebut: (2016,2,3), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tache2 = new Tache("Tache1", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 10D, 20D, null, null, null, null, null);
        tache2.save();

        try {
            projet.creerTacheInitialisationProjet(tache1);
            // tache2: idTache=1; tache1: idTache=2
            projet.creerTacheAuDessus(tache2, tache1);

            Tache tache1Selected = Tache.find.byId(tache1.id);
            Tache tache2Selected = Tache.find.byId(tache2.id);
            Projet projetSelected = Projet.find.byId(projet.id);

            assertTrue(projetSelected.listTaches().contains(tache2Selected));
            assertEquals(tache1Selected.idTache, "2");
            assertEquals(tache2Selected.idTache, "1");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreerTacheEnDessous() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,2,10, dateDebutReel: 2016,2,3, dateFinReelTot: 2016,2,9, dateFinReelTard: 2016,2,9
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 2), Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 9), Utils.getDateFrom(2016, 2, 9), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        //tache dateDebut: (2016,2,3), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tache1 = new Tache("Tache1", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 10D, 20D, null, null, null, null, null);
        tache1.save();

        //tache dateDebut: (2016,2,3), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tache2 = new Tache("Tache1", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 10D, 20D, null, null, null, null, null);
        tache2.save();

        try {
            projet.creerTacheInitialisationProjet(tache1);
            // tache2: idTache=2; tache1: idTache=1
            projet.creerTacheEnDessous(tache2, tache1);

            Tache tache1Selected = Tache.find.byId(tache1.id);
            Tache tache2Selected = Tache.find.byId(tache2.id);
            Projet projetSelected = Projet.find.byId(projet.id);

            assertTrue(projetSelected.listTaches().contains(tache2Selected));
            assertEquals(tache1Selected.idTache, "1");
            assertEquals(tache2Selected.idTache, "2");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreerSousTache() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,2,10, dateDebutReel: 2016,2,3, dateFinReelTot: 2016,2,9, dateFinReelTard: 2016,2,9
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 2), Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 9), Utils.getDateFrom(2016, 2, 9), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        //tache dateDebut: (2016,2,3), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tache1 = new Tache("Tache1", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 5), Utils.getDateFrom(2016, 2, 5), 20D, 10D, 20D, null, null, null, null, null);
        tache1.save();

        //tache dateDebut: (2016,2,3), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tache2 = new Tache("Tache2", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 10D, 20D, null, null, null, null, null);
        tache2.save();

        try {
            // 1er test
            projet.creerTacheInitialisationProjet(tache1);
            // tache2: idTache=1.1; tache1: idTache=1
            projet.creerSousTache(tache2, tache1);

            Tache tache1Selected = Tache.find.byId(tache1.id);
            Tache tache2Selected = Tache.find.byId(tache2.id);
            Projet projetSelected = Projet.find.byId(projet.id);

            assertTrue(projetSelected.listTaches().contains(tache1Selected));
            assertTrue(projetSelected.listTaches().contains(tache2Selected));
            assertEquals(tache1Selected.idTache, "1");
            assertEquals(tache2Selected.idTache, "1.1");
            assertTrue(tache1Selected.enfants().contains(tache2Selected));

            // 2eme test
            //tache dateDebut: (2016,2,3), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
            Tache tache3 = new Tache("Tache3", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                    Utils.getDateFrom(2016, 2, 5), Utils.getDateFrom(2016, 2, 5), 20D, 10D, 20D, null, null, tache2, null, null);
            tache3.save();
            projet.creerTacheEnDessous(tache3, tache2);
            assertEquals(tache1Selected.idTache, "1");
            assertEquals(tache2Selected.idTache, "1.1");
            assertEquals(Tache.find.byId(tache3.id).idTache, "1.2");

            Tache tache4 = new Tache("Tache4", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                    Utils.getDateFrom(2016, 2, 5), Utils.getDateFrom(2016, 2, 5), 20D, 10D, 20D, null, null, tache2, null, null);
            tache4.save();
            projet.creerSousTache(tache4, tache3);
            assertEquals(tache1Selected.idTache, "1");
            assertEquals(tache2Selected.idTache, "1.1");
            assertEquals(Tache.find.byId(tache3.id).idTache, "1.2");
            assertEquals(Tache.find.byId(tache4.id).idTache, "1.2.1");
            assertTrue(Tache.find.byId(tache1.id).enfants().contains(Tache.find.byId(tache2.id)));
            assertTrue(Tache.find.byId(tache1.id).enfants().contains(Tache.find.byId(tache3.id)));
            assertTrue(Tache.find.byId(tache3.id).enfants().contains(Tache.find.byId(tache4.id)));

            assertFalse(Tache.find.byId(tache1.id).enfants().contains(Tache.find.byId(tache4.id)));
            assertFalse(Tache.find.byId(tache2.id).enfants().contains(Tache.find.byId(tache3.id)));
            assertFalse(Tache.find.byId(tache2.id).enfants().contains(Tache.find.byId(tache4.id)));

            assertTrue(Tache.find.byId(tache4.id).predecesseur.equals(Tache.find.byId(tache2.id)));
            assertFalse(Tache.find.byId(tache4.id).predecesseur.equals(Tache.find.byId(tache1.id)));
            assertFalse(Tache.find.byId(tache4.id).predecesseur.equals(Tache.find.byId(tache4.id)));
            assertFalse(Tache.find.byId(tache4.id).predecesseur.equals(Tache.find.byId(tache3.id)));
            assertTrue(Tache.find.byId(tache4.id).getSuccesseurs().isEmpty());

            assertTrue(Tache.find.byId(tache2.id).getSuccesseurs().contains(Tache.find.byId(tache3.id)));
            assertFalse(Tache.find.byId(tache2.id).getSuccesseurs().contains(Tache.find.byId(tache1.id)));
            assertFalse(Tache.find.byId(tache2.id).getSuccesseurs().contains(Tache.find.byId(tache2.id)));
            assertTrue(Tache.find.byId(tache2.id).getSuccesseurs().contains(Tache.find.byId(tache4.id)));
            assertTrue(!Tache.find.byId(tache2.id).hasPredecesseur());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIdTache() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,3,4, dateDebutReel: 2016,2,2, dateFinReelTot: 2016,3,3, dateFinReelTard: 2016,3,3
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 2), Utils.getDateFrom(2016, 3, 4), Utils.getDateFrom(2016, 2, 2),
                Utils.getDateFrom(2016, 3, 3), Utils.getDateFrom(2016, 3, 3), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        //tache dateDebut: (2016,2,2), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tacheA = new Tache("TacheA", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 2),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 0D, 20D, null, null, null, null, null);
        tacheA.save();

        //tache dateDebut: (2016,2,4), dateFinTot: (2016,2,18), dateFinTard: (2016,2,18)
        Tache tacheB = new Tache("TacheB", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 18), Utils.getDateFrom(2016, 2, 18), 20D, 0D, 20D, null, null, null, null, null);
        tacheB.save();

        //tache dateDebut: (2016,2,18), dateFinTot: (2016,2,28), dateFinTard: (2016,2,28)
        Tache tacheC = new Tache("TacheC", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 18),
                Utils.getDateFrom(2016, 2, 28), Utils.getDateFrom(2016, 2, 28), 20D, 0D, 20D, null, null, null, null, null);
        tacheC.save();

        //tache dateDebut: (2016,2,28), dateFinTot: (2016,3,3), dateFinTard: (2016,3,3)
        Tache tacheD = new Tache("TacheD", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 28),
                Utils.getDateFrom(2016, 3, 3), Utils.getDateFrom(2016, 3, 3), 20D, 0D, 20D, null, null, null, null, null);
        tacheD.save();

        //tache dateDebut: (2016,2,18), dateFinTot: (2016,2,20), dateFinTard: (2016,2,20)
        Tache tacheE = new Tache("TacheE", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 18),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 20), 20D, 0D, 20D, null, null, null, null, null);
        tacheE.save();

        //tache dateDebut: (2016,2,4), dateFinTot: (2016,2,20), dateFinTard: (2016,2,20)
        Tache tacheF = new Tache("TacheF", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 20), 20D, 0D, 20D, null, null, null, null, null);
        tacheF.save();

        //tache dateDebut: (2016,2,20), dateFinTot: (2016,2,28), dateFinTard: (2016,2,28)
        Tache tacheG = new Tache("TacheG", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 20),
                Utils.getDateFrom(2016, 2, 28), Utils.getDateFrom(2016, 2, 28), 20D, 0D, 20D, null, null, null, null, null);
        tacheG.save();

        //tache dateDebut: (2016,2,4), dateFinTot: (2016,2,5), dateFinTard: (2016,2,5)
        Tache tacheH = new Tache("TacheH", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 5), Utils.getDateFrom(2016, 2, 5), 20D, 0D, 20D, null, null, null, null, null);
        tacheH.save();

        //tache dateDebut: (2016,2,5), dateFinTot: (2016,2,10), dateFinTard: (2016,2,10)
        Tache tacheI = new Tache("TacheI", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 5),
                Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 10), 20D, 0D, 20D, null, null, null, null, null);
        tacheI.save();

        //tache dateDebut: (2016,2,10), dateFinTot: (2016,2,13), dateFinTard: (2016,2,13)
        Tache tacheJ = new Tache("TacheJ", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 10),
                Utils.getDateFrom(2016, 2, 13), Utils.getDateFrom(2016, 2, 13), 20D, 0D, 20D, null, null, null, null, null);
        tacheJ.save();

        //tache dateDebut: (2016,2,13), dateFinTot: (2016,2,18), dateFinTard: (2016,2,18)
        Tache tacheK = new Tache("TacheK", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 13),
                Utils.getDateFrom(2016, 2, 18), Utils.getDateFrom(2016, 2, 18), 20D, 0D, 20D, null, null, null, null, null);
        tacheK.save();

        //tache dateDebut: (2016,2,5), dateFinTot: (2016,2,10), dateFinTard: (2016,2,10)
        Tache tacheL = new Tache("TacheL", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 5),
                Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 10), 20D, 0D, 20D, null, null, null, null, null);
        tacheL.save();

        //tache dateDebut: (2016,2,10), dateFinTot: (2016,2,20), dateFinTard: (2016,2,20)
        Tache tacheM = new Tache("TacheM", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 10),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 20), 20D, 0D, 20D, null, null, null, null, null);
        tacheM.save();

        try {
            projet.creerTacheInitialisationProjet(tacheA);
                /*
                System.out.println("------ L1 -------");
                System.out.println(tacheA.nom + " " + tacheA.idTache);
                System.out.println("------ Fin - L1 -------");
                */

            projet.creerTacheEnDessous(tacheB, tacheA);
                /*
                System.out.println("------ L2 -------");
                System.out.println(tacheA.nom + " " + tacheA.idTache);
                System.out.println(tacheB.nom + " " + tacheB.idTache);
                System.out.println("------ Fin - L2 -------");
                */

            projet.creerTacheEnDessous(tacheC, tacheB);
                /*
                System.out.println("------ L3 -------");
                System.out.println(tacheA.nom + " " + tacheA.idTache);
                System.out.println(tacheB.nom + " " + tacheB.idTache);
                System.out.println(tacheC.nom + " " + tacheC.idTache);
                System.out.println("------ Fin - L3 -------");
                */

            projet.creerTacheEnDessous(tacheD, tacheC);
                /*
                System.out.println("------ L4 -------");
                System.out.println(tacheA.nom + " " + tacheA.idTache);
                System.out.println(tacheB.nom + " " + tacheB.idTache);
                System.out.println(tacheC.nom + " " + tacheC.idTache);
                System.out.println(tacheD.nom + " " + tacheD.idTache);
                System.out.println("------ Fin - L4 -------");
                */

            projet.creerTacheEnDessous(tacheE, tacheB);
                /*
                System.out.println("------ L5 -------");
                System.out.println(tacheA.nom + " " + tacheA.idTache);
                System.out.println(tacheB.nom + " " + tacheB.idTache);
                System.out.println(tacheE.nom + " " + tacheE.idTache);
                System.out.println(tacheC.nom + " " + tacheC.idTache);
                System.out.println(tacheD.nom + " " + tacheD.idTache);
                System.out.println("------ Fin - L5 -------");
                */

            projet.creerTacheEnDessous(tacheF, tacheA);
                /*
                System.out.println("------ L6 -------");
                System.out.println(tacheA.nom + " " + tacheA.idTache);
                System.out.println(tacheF.nom + " " + tacheF.idTache);
                System.out.println(tacheB.nom + " " + tacheB.idTache);
                System.out.println(tacheE.nom + " " + tacheE.idTache);
                System.out.println(tacheC.nom + " " + tacheC.idTache);
                System.out.println(tacheD.nom + " " + tacheD.idTache);
                System.out.println("------ Fin - L6 -------");
                */

            projet.creerTacheEnDessous(tacheG, tacheF);
                /*
                System.out.println("------ L7 -------");
                System.out.println(tacheA.nom + " " + tacheA.idTache);
                System.out.println(tacheF.nom + " " + tacheF.idTache);
                System.out.println(tacheG.nom + " " + tacheG.idTache);
                System.out.println(tacheB.nom + " " + tacheB.idTache);
                System.out.println(tacheE.nom + " " + tacheE.idTache);
                System.out.println(tacheC.nom + " " + tacheC.idTache);
                System.out.println(tacheD.nom + " " + tacheD.idTache);
                System.out.println("------ Fin - L7 -------");
                */

            projet.creerSousTache(tacheH, tacheF);
                /*
                System.out.println("------ L8 -------");
                System.out.println(tacheF.nom + " " + tacheF.idTache);
                System.out.println(tacheH.nom + " " + tacheH.idTache);
                System.out.println("------ Fin - L8 -------");
                */

            projet.creerTacheEnDessous(tacheI, tacheH);
                /*
                System.out.println("------ L9 -------");
                System.out.println(tacheF.nom + " " + tacheF.idTache);
                System.out.println(tacheH.nom + " " + tacheH.idTache);
                System.out.println(tacheI.nom + " " + tacheI.idTache);
                System.out.println("------ Fin - L9 -------");
                */

            projet.creerTacheEnDessous(tacheJ, tacheI);
                /*
                System.out.println("------ L10 -------");
                System.out.println(tacheF.nom + " " + tacheF.idTache);
                System.out.println(tacheH.nom + " " + tacheH.idTache);
                System.out.println(tacheI.nom + " " + tacheI.idTache);
                System.out.println(tacheJ.nom + " " + tacheJ.idTache);
                System.out.println("------ Fin - L10 -------");
                */

            projet.creerTacheEnDessous(tacheK, tacheJ);
                /*
                System.out.println("------ L11 -------");
                System.out.println(tacheF.nom + " " + tacheF.idTache);
                System.out.println(tacheH.nom + " " + tacheH.idTache);
                System.out.println(tacheI.nom + " " + tacheI.idTache);
                System.out.println(tacheJ.nom + " " + tacheJ.idTache);
                System.out.println(tacheK.nom + " " + tacheK.idTache);
                System.out.println("------ Fin - L11 -------");
                */

            projet.creerTacheEnDessous(tacheL, tacheH);
                /*
                System.out.println("------ L12 -------");
                System.out.println(tacheF.nom + " " + tacheF.idTache);
                System.out.println(tacheH.nom + " " + tacheH.idTache);
                System.out.println(tacheL.nom + " " + tacheL.idTache);
                System.out.println(tacheI.nom + " " + tacheI.idTache);
                System.out.println(tacheJ.nom + " " + tacheJ.idTache);
                System.out.println(tacheK.nom + " " + tacheK.idTache);
                System.out.println("------ Fin - L12 -------");
                */

            projet.creerTacheEnDessous(tacheM, tacheL);
                /*
                System.out.println("------ L13 -------");
                System.out.println(tacheF.nom + " " + tacheF.idTache);
                System.out.println(tacheH.nom + " " + tacheH.idTache);
                System.out.println(tacheL.nom + " " + tacheL.idTache);
                System.out.println(tacheM.nom + " " + tacheM.idTache);
                System.out.println(tacheI.nom + " " + tacheI.idTache);
                System.out.println(tacheJ.nom + " " + tacheJ.idTache);
                System.out.println(tacheK.nom + " " + tacheK.idTache);
                System.out.println("------ Fin - L13 -------");
                */

            Projet projetSelected = Projet.find.byId(projet.id);
            Tache tacheASelected = Tache.find.byId(tacheA.id);
            Tache tacheBSelected = Tache.find.byId(tacheB.id);
            Tache tacheCSelected = Tache.find.byId(tacheC.id);
            Tache tacheDSelected = Tache.find.byId(tacheD.id);
            Tache tacheESelected = Tache.find.byId(tacheE.id);
            Tache tacheFSelected = Tache.find.byId(tacheF.id);
            Tache tacheGSelected = Tache.find.byId(tacheG.id);
            Tache tacheHSelected = Tache.find.byId(tacheH.id);
            Tache tacheISelected = Tache.find.byId(tacheI.id);
            Tache tacheJSelected = Tache.find.byId(tacheJ.id);
            Tache tacheKSelected = Tache.find.byId(tacheK.id);
            Tache tacheLSelected = Tache.find.byId(tacheL.id);
            Tache tacheMSelected = Tache.find.byId(tacheM.id);

                /*
                Logger.debug("IDTACHE");
                Logger.debug(tacheASelected.idTache);
                Logger.debug(tacheBSelected.idTache);
                Logger.debug(tacheCSelected.idTache);
                Logger.debug(tacheDSelected.idTache);
                Logger.debug(tacheESelected.idTache);
                Logger.debug(tacheFSelected.idTache);
                Logger.debug(tacheGSelected.idTache);
                Logger.debug(tacheHSelected.idTache);
                Logger.debug(tacheISelected.idTache);
                Logger.debug(tacheJSelected.idTache);
                Logger.debug(tacheKSelected.idTache);
                Logger.debug(tacheLSelected.idTache);
                Logger.debug(tacheMSelected.idTache);
                */

            // Verifier si il y a 13 taches dans le projet
            assertEquals(projetSelected.listTaches().size(), 13);

            // Verifier les idTache
            assertEquals(tacheASelected.idTache, "1");
            assertEquals(tacheBSelected.idTache, "4");
            assertEquals(tacheCSelected.idTache, "6");
            assertEquals(tacheDSelected.idTache, "7");
            assertEquals(tacheESelected.idTache, "5");
            assertEquals(tacheFSelected.idTache, "2");
            assertEquals(tacheGSelected.idTache, "3");
            assertEquals(tacheHSelected.idTache, "2.1");
            assertEquals(tacheISelected.idTache, "2.4");
            assertEquals(tacheJSelected.idTache, "2.5");
            assertEquals(tacheKSelected.idTache, "2.6");
            assertEquals(tacheLSelected.idTache, "2.2");
            assertEquals(tacheMSelected.idTache, "2.3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testCalculeCheminCritique() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,2,4, dateDebutReel: 2016,2,2, dateFinReelTot: 2016,2,4, dateFinReelTard: 2016,2,4
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 2), Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 2),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        //tache dateDebut: (2016,2,2), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tacheA = new Tache("TacheA", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 2),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 0D, 20D, null, null, null, null, null);
        tacheA.save();

        //tache dateDebut: (2016,2,4), dateFinTot: (2016,2,18), dateFinTard: (2016,2,18)
        Tache tacheB = new Tache("TacheB", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 18), Utils.getDateFrom(2016, 2, 18), 20D, 0D, 20D, null, null, tacheA, null, null);
        tacheB.save();

        //tache dateDebut: (2016,2,18), dateFinTot: (2016,2,28), dateFinTard: (2016,2,28)
        Tache tacheC = new Tache("TacheC", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 18),
                Utils.getDateFrom(2016, 2, 28), Utils.getDateFrom(2016, 2, 28), 20D, 0D, 20D, null, null, tacheB, null, null);
        tacheC.save();

        //tache dateDebut: (2016,2,28), dateFinTot: (2016,3,3), dateFinTard: (2016,3,3)
        Tache tacheD = new Tache("TacheD", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 28),
                Utils.getDateFrom(2016, 3, 3), Utils.getDateFrom(2016, 3, 3), 20D, 0D, 20D, null, null, tacheC, null, null);
        tacheD.save();

        //tache dateDebut: (2016,2,18), dateFinTot: (2016,2,20), dateFinTard: (2016,2,20)
        Tache tacheE = new Tache("TacheE", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 18),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 20), 20D, 0D, 20D, null, null, tacheB, null, null);
        tacheE.save();

        //tache dateDebut: (2016,2,4), dateFinTot: (2016,2,20), dateFinTard: (2016,2,20)
        Tache tacheF = new Tache("TacheF", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 20), 20D, 0D, 20D, null, null, tacheA, null, null);
        tacheF.save();

        //tache dateDebut: (2016,2,20), dateFinTot: (2016,2,28), dateFinTard: (2016,2,28)
        Tache tacheG = new Tache("TacheG", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 20),
                Utils.getDateFrom(2016, 2, 28), Utils.getDateFrom(2016, 2, 28), 20D, 0D, 20D, null, null, tacheF, null, null);
        tacheG.save();

        try {
            projet.creerTacheInitialisationProjet(tacheA);
            projet.creerTacheEnDessous(tacheB, tacheA);
            projet.creerTacheEnDessous(tacheC, tacheB);
            projet.creerTacheEnDessous(tacheD, tacheC);
            projet.creerTacheEnDessous(tacheE, tacheB);
            projet.creerTacheEnDessous(tacheF, tacheA);
            projet.creerTacheEnDessous(tacheG, tacheF);

            //Projet projetSelected = Projet.find.byId(projet.id);
            Tache tacheASelected = Tache.find.byId(tacheA.id);
            Tache tacheBSelected = Tache.find.byId(tacheB.id);
            Tache tacheCSelected = Tache.find.byId(tacheC.id);
            Tache tacheDSelected = Tache.find.byId(tacheD.id);
            Tache tacheESelected = Tache.find.byId(tacheE.id);
            Tache tacheFSelected = Tache.find.byId(tacheF.id);
            Tache tacheGSelected = Tache.find.byId(tacheG.id);

            // Verifier les structure des taches de projet est bien MAJ
            assertTrue(tacheASelected.getSuccesseurs().contains(tacheBSelected));
            assertTrue(tacheASelected.getSuccesseurs().contains(tacheFSelected));
            assertTrue(tacheBSelected.getSuccesseurs().contains(tacheCSelected));
            assertTrue(tacheBSelected.getSuccesseurs().contains(tacheESelected));
            assertTrue(tacheCSelected.getSuccesseurs().contains(tacheDSelected));
            assertTrue(tacheFSelected.getSuccesseurs().contains(tacheGSelected));

                /*
                assertTrue(tacheFSelected.enfants().contains(tacheHSelected));
                assertTrue(tacheFSelected.enfants().contains(tacheISelected));
                assertTrue(tacheFSelected.enfants().contains(tacheJSelected));
                assertTrue(tacheFSelected.enfants().contains(tacheKSelected));
                assertTrue(tacheFSelected.enfants().contains(tacheLSelected));
                assertTrue(tacheFSelected.enfants().contains(tacheMSelected));

                assertTrue(tacheHSelected.getSuccesseurs().contains(tacheISelected));
                assertTrue(tacheHSelected.getSuccesseurs().contains(tacheLSelected));
                assertTrue(tacheISelected.getSuccesseurs().contains(tacheJSelected));
                assertTrue(tacheJSelected.getSuccesseurs().contains(tacheKSelected));
                assertTrue(tacheLSelected.getSuccesseurs().contains(tacheMSelected));
                */


            // 1er test
            // Verifier le chemin critique
            assertTrue(tacheASelected.critique);
            assertTrue(tacheBSelected.critique);
            assertTrue(tacheCSelected.critique);
            assertTrue(tacheDSelected.critique);

            assertFalse(tacheESelected.critique);
            assertFalse(tacheFSelected.critique);
            assertFalse(tacheGSelected.critique);

            // 2ème test
            // Ajouter tacheZ après tacheG
            //tache dateDebut: (2016,2,28), dateFinTot: (2016,3,4), dateFinTard: (2016,3,4)
            Tache tacheZ = new Tache("TacheZ", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 28),
                    Utils.getDateFrom(2016, 3, 4), Utils.getDateFrom(2016, 3, 4), 20D, 0D, 20D, null, null, tacheG, null, null);
            tacheZ.save();
            projet.creerTacheEnDessous(tacheZ, tacheG);
            // Verifier le chemin critique
            assertTrue(Tache.find.byId(tacheA.id).critique);
            assertTrue(Tache.find.byId(tacheF.id).critique);
            assertTrue(Tache.find.byId(tacheG.id).critique);
            assertTrue(Tache.find.byId(tacheZ.id).critique);

            assertFalse(Tache.find.byId(tacheB.id).critique);
            assertFalse(Tache.find.byId(tacheC.id).critique);
            assertFalse(Tache.find.byId(tacheD.id).critique);
            assertFalse(Tache.find.byId(tacheE.id).critique);

            // 3ème test
            // Ajouter les sous taches de TacheF(tacheH, tacheI, tacheJ, tacheK, tacheL et tacheM)
            tacheF.dateFinTard = Utils.getDateFrom(2016, 3, 5);
            tacheF.dateFinTot = Utils.getDateFrom(2016, 3, 5);
            tacheF.save();

            //tache dateDebut: (2016,2,4), dateFinTot: (2016,2,5), dateFinTard: (2016,2,5)
            Tache tacheH = new Tache("TacheH", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                    Utils.getDateFrom(2016, 2, 5), Utils.getDateFrom(2016, 2, 5), 20D, 0D, 20D, null, null, tacheA, null, null);
            tacheH.save();

            //tache dateDebut: (2016,2,5), dateFinTot: (2016,2,10), dateFinTard: (2016,2,10)
            Tache tacheI = new Tache("TacheI", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 5),
                    Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 10), 20D, 0D, 20D, null, null, tacheH, null, null);
            tacheI.save();

            //tache dateDebut: (2016,2,10), dateFinTot: (2016,2,13), dateFinTard: (2016,2,13)
            Tache tacheJ = new Tache("TacheJ", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 10),
                    Utils.getDateFrom(2016, 2, 13), Utils.getDateFrom(2016, 2, 13), 20D, 0D, 20D, null, null, tacheI, null, null);
            tacheJ.save();

            //tache dateDebut: (2016,2,13), dateFinTot: (2016,2,18), dateFinTard: (2016,2,18)
            Tache tacheK = new Tache("TacheK", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 13),
                    Utils.getDateFrom(2016, 2, 18), Utils.getDateFrom(2016, 2, 18), 20D, 0D, 20D, null, null, tacheJ, null, null);
            tacheK.save();

            //tache dateDebut: (2016,2,5), dateFinTot: (2016,2,10), dateFinTard: (2016,2,10)
            Tache tacheL = new Tache("TacheL", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 5),
                    Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 10), 20D, 0D, 20D, null, null, tacheH, null, null);
            tacheL.save();

            //tache dateDebut: (2016,2,10), dateFinTot: (2016,3,5), dateFinTard: (2016,3,5)
            Tache tacheM = new Tache("TacheM", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 10),
                    Utils.getDateFrom(2016, 3, 5), Utils.getDateFrom(2016, 3, 5), 20D, 0D, 20D, null, null, tacheL, null, null);
            tacheM.save();

            projet.creerSousTache(tacheH, tacheF);

            projet.creerTacheEnDessous(tacheI, tacheH);
            projet.creerTacheEnDessous(tacheJ, tacheI);
            projet.creerTacheEnDessous(tacheK, tacheJ);
            projet.creerTacheEnDessous(tacheL, tacheH);
            projet.creerTacheEnDessous(tacheM, tacheL);

            // Afficher l'attribut critique de chaque tache
                /*
                Logger.debug("CRITIQUE");
                Logger.debug("A:" + Tache.find.byId(tacheA.id).critique.toString());
                Logger.debug("B:" + Tache.find.byId(tacheB.id).critique.toString());
                Logger.debug("C:" + Tache.find.byId(tacheC.id).critique.toString());
                Logger.debug("D:" + Tache.find.byId(tacheD.id).critique.toString());
                Logger.debug("E:" + Tache.find.byId(tacheE.id).critique.toString());
                Logger.debug("F:" + Tache.find.byId(tacheF.id).critique.toString());
                Logger.debug("G:" + Tache.find.byId(tacheG.id).critique.toString());
                Logger.debug("H:" + Tache.find.byId(tacheH.id).critique.toString());
                Logger.debug("I:" + Tache.find.byId(tacheI.id).critique.toString());
                Logger.debug("J:" + Tache.find.byId(tacheJ.id).critique.toString());
                Logger.debug("K:" + Tache.find.byId(tacheK.id).critique.toString());
                Logger.debug("L:" + Tache.find.byId(tacheL.id).critique.toString());
                Logger.debug("M:" + Tache.find.byId(tacheM.id).critique.toString());
                Logger.debug("Z:" + Tache.find.byId(tacheZ.id).critique.toString());
                */

            // Verifier le chemin critique
            assertTrue(Tache.find.byId(tacheA.id).critique);
            assertTrue(Tache.find.byId(tacheF.id).critique);
            assertTrue(Tache.find.byId(tacheH.id).critique);
            assertTrue(Tache.find.byId(tacheL.id).critique);
            assertTrue(Tache.find.byId(tacheM.id).critique);

            assertFalse(Tache.find.byId(tacheB.id).critique);
            assertFalse(Tache.find.byId(tacheC.id).critique);
            assertFalse(Tache.find.byId(tacheD.id).critique);
            assertFalse(Tache.find.byId(tacheE.id).critique);
            assertFalse(Tache.find.byId(tacheI.id).critique);
            assertFalse(Tache.find.byId(tacheJ.id).critique);
            assertFalse(Tache.find.byId(tacheK.id).critique);
            assertFalse(Tache.find.byId(tacheZ.id).critique);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateAvancementGlobal() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,3,4, dateDebutReel: 2016,2,2, dateFinReelTot: 2016,3,3, dateFinReelTard: 2016,3,3
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 2), Utils.getDateFrom(2016, 3, 4), Utils.getDateFrom(2016, 2, 2),
                Utils.getDateFrom(2016, 3, 3), Utils.getDateFrom(2016, 3, 3), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        //tache dateDebut: (2016,2,2), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tacheA = new Tache("TacheA", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 2),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 0D, 20D, null, null, null, null, null);
        tacheA.save();

        //tache dateDebut: (2016,2,4), dateFinTot: (2016,2,18), dateFinTard: (2016,2,18)
        Tache tacheB = new Tache("TacheB", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 18), Utils.getDateFrom(2016, 2, 18), 20D, 0D, 20D, null, null, tacheA, null, null);
        tacheB.save();

        //tache dateDebut: (2016,2,18), dateFinTot: (2016,2,28), dateFinTard: (2016,2,28)
        Tache tacheC = new Tache("TacheC", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 18),
                Utils.getDateFrom(2016, 2, 28), Utils.getDateFrom(2016, 2, 28), 20D, 0D, 20D, null, null, tacheB, null, null);
        tacheC.save();

        //tache dateDebut: (2016,2,28), dateFinTot: (2016,3,3), dateFinTard: (2016,3,3)
        Tache tacheD = new Tache("TacheD", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 28),
                Utils.getDateFrom(2016, 3, 3), Utils.getDateFrom(2016, 3, 3), 20D, 0D, 20D, null, null, tacheC, null, null);
        tacheD.save();

        //tache dateDebut: (2016,2,18), dateFinTot: (2016,2,20), dateFinTard: (2016,2,20)
        Tache tacheE = new Tache("TacheE", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 18),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 20), 20D, 0D, 20D, null, null, tacheB, null, null);
        tacheE.save();

        //tache dateDebut: (2016,2,4), dateFinTot: (2016,2,20), dateFinTard: (2016,2,20)
        Tache tacheF = new Tache("TacheF", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 20), 20D, 0D, 20D, null, null, tacheA, null, null);
        tacheF.save();

        //tache dateDebut: (2016,2,20), dateFinTot: (2016,2,28), dateFinTard: (2016,2,28)
        Tache tacheG = new Tache("TacheG", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 20),
                Utils.getDateFrom(2016, 2, 28), Utils.getDateFrom(2016, 2, 28), 20D, 0D, 20D, null, null, tacheF, null, null);
        tacheG.save();

        //tache dateDebut: (2016,2,4), dateFinTot: (2016,2,5), dateFinTard: (2016,2,5)
        Tache tacheH = new Tache("TacheH", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 5), Utils.getDateFrom(2016, 2, 5), 20D, 0D, 20D, null, null, null, null, null);
        tacheH.save();

        //tache dateDebut: (2016,2,5), dateFinTot: (2016,2,10), dateFinTard: (2016,2,10)
        Tache tacheI = new Tache("TacheI", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 5),
                Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 10), 20D, 0D, 20D, null, null, tacheH, null, null);
        tacheI.save();

        //tache dateDebut: (2016,2,10), dateFinTot: (2016,2,13), dateFinTard: (2016,2,13)
        Tache tacheJ = new Tache("TacheJ", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 10),
                Utils.getDateFrom(2016, 2, 13), Utils.getDateFrom(2016, 2, 13), 20D, 0D, 20D, null, null, tacheI, null, null);
        tacheJ.save();

        //tache dateDebut: (2016,2,13), dateFinTot: (2016,2,18), dateFinTard: (2016,2,18)
        Tache tacheK = new Tache("TacheK", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 13),
                Utils.getDateFrom(2016, 2, 18), Utils.getDateFrom(2016, 2, 18), 20D, 0D, 20D, null, null, tacheJ, null, null);
        tacheK.save();

        //tache dateDebut: (2016,2,5), dateFinTot: (2016,2,10), dateFinTard: (2016,2,10)
        Tache tacheL = new Tache("TacheL", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 5),
                Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 10), 20D, 0D, 20D, null, null, tacheH, null, null);
        tacheL.save();

        //tache dateDebut: (2016,2,10), dateFinTot: (2016,2,20), dateFinTard: (2016,2,20)
        Tache tacheM = new Tache("TacheM", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 10),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 20), 20D, 0D, 20D, null, null, tacheL, null, null);
        tacheM.save();

        try {
            projet.creerTacheInitialisationProjet(tacheA);
            projet.creerTacheEnDessous(tacheB, tacheA);
            projet.creerTacheEnDessous(tacheC, tacheB);
            projet.creerTacheEnDessous(tacheD, tacheC);
            projet.creerTacheEnDessous(tacheE, tacheB);
            projet.creerTacheEnDessous(tacheF, tacheA);
            projet.creerTacheEnDessous(tacheG, tacheF);

            projet.creerSousTache(tacheH, tacheF);

            projet.creerTacheEnDessous(tacheI, tacheH);
            projet.creerTacheEnDessous(tacheJ, tacheI);
            projet.creerTacheEnDessous(tacheK, tacheJ);
            projet.creerTacheEnDessous(tacheL, tacheH);
            projet.creerTacheEnDessous(tacheM, tacheL);

            Projet projetSelected = Projet.find.byId(projet.id);
            Tache tacheASelected = Tache.find.byId(tacheA.id);
            Tache tacheBSelected = Tache.find.byId(tacheB.id);
            Tache tacheCSelected = Tache.find.byId(tacheC.id);
            Tache tacheDSelected = Tache.find.byId(tacheD.id);
            Tache tacheESelected = Tache.find.byId(tacheE.id);
            Tache tacheFSelected = Tache.find.byId(tacheF.id);
            Tache tacheGSelected = Tache.find.byId(tacheG.id);
            Tache tacheHSelected = Tache.find.byId(tacheH.id);
            Tache tacheISelected = Tache.find.byId(tacheI.id);
            Tache tacheJSelected = Tache.find.byId(tacheJ.id);
            Tache tacheKSelected = Tache.find.byId(tacheK.id);
            Tache tacheLSelected = Tache.find.byId(tacheL.id);
            Tache tacheMSelected = Tache.find.byId(tacheM.id);

                /*
                Logger.debug("CHARGE CONSOMMEE");
                Logger.debug(tacheASelected.chargeConsommee.toString());
                Logger.debug(tacheBSelected.chargeConsommee.toString());
                Logger.debug(tacheCSelected.chargeConsommee.toString());
                Logger.debug(tacheDSelected.chargeConsommee.toString());
                Logger.debug(tacheESelected.chargeConsommee.toString());
                Logger.debug(tacheFSelected.chargeConsommee.toString());
                Logger.debug(tacheGSelected.chargeConsommee.toString());
                Logger.debug(tacheHSelected.chargeConsommee.toString());
                Logger.debug(tacheISelected.chargeConsommee.toString());
                Logger.debug(tacheJSelected.chargeConsommee.toString());
                Logger.debug(tacheKSelected.chargeConsommee.toString());
                Logger.debug(tacheLSelected.chargeConsommee.toString());
                Logger.debug(tacheMSelected.chargeConsommee.toString());

                Logger.debug("CHARGE RESTANTE");
                Logger.debug(tacheASelected.chargeRestante.toString());
                Logger.debug(tacheBSelected.chargeRestante.toString());
                Logger.debug(tacheCSelected.chargeRestante.toString());
                Logger.debug(tacheDSelected.chargeRestante.toString());
                Logger.debug(tacheESelected.chargeRestante.toString());
                Logger.debug(tacheFSelected.chargeRestante.toString());
                Logger.debug(tacheGSelected.chargeRestante.toString());
                Logger.debug(tacheHSelected.chargeRestante.toString());
                Logger.debug(tacheISelected.chargeRestante.toString());
                Logger.debug(tacheJSelected.chargeRestante.toString());
                Logger.debug(tacheKSelected.chargeRestante.toString());
                Logger.debug(tacheLSelected.chargeRestante.toString());
                Logger.debug(tacheMSelected.chargeRestante.toString());
                */

            // 1er test
            assertEquals(tacheASelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheBSelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheCSelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheDSelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheESelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheFSelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheGSelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheHSelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheISelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheJSelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheKSelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheLSelected.chargeConsommee, Double.valueOf(0));
            assertEquals(tacheMSelected.chargeConsommee, Double.valueOf(0));

            assertEquals(tacheASelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheBSelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheCSelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheDSelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheESelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheFSelected.chargeRestante, Double.valueOf(120));
            assertEquals(tacheGSelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheHSelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheISelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheJSelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheKSelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheLSelected.chargeRestante, Double.valueOf(20));
            assertEquals(tacheMSelected.chargeRestante, Double.valueOf(20));

            assertEquals(projetSelected.avancementGlobal.toString(), "0");

            // 2ème test
            // Modifier la charge consommee de tacheC à 10D => tacheC: chargeConsommee=10D, chargeRestante=20D
            tacheC.setChargeConsommee(10D);
            assertEquals(Tache.find.byId(tacheC.id).chargeConsommee, Double.valueOf(10));
            // Attention C'est obligé
            projet.updateAvancementGlobal();
            // Après MAJ, l'avancement = (0+10)/((20+20+20+20+20+(20+20+20+20+20+20)+20)+(0+10)) = 0.04 => "4"
            assertEquals(Projet.find.byId(projet.id).avancementGlobal.toString(), "4");

            // 3ème test
            // Modifier la charge restante de tacheG à 45D => tacheG: chargeConsommee=0D, chargeRestante=45D
            tacheG.setChargeRestante(65D);
            assertEquals(Tache.find.byId(tacheG.id).chargeRestante, Double.valueOf(65));
            projet.updateAvancementGlobal();
            // Après MAJ, l'avancement = (0+10)/((20+20+20+20+20+(20+20+20+20+20+20)+65)+(0+10)) = 0.03 => "3"
            assertEquals(Projet.find.byId(projet.id).avancementGlobal.toString(), "3");

            // 4ème test
            // Modifier la charge consommee de tacheJ(sous tache de tacheF) à 15D => tacheJ: chargeConsommee=15D, chargeRestante=20D
            //                                                                    => tacheF: chargeConsommee=15D, chargeRestante=120D
            tacheJ.setChargeConsommee(15D);
            assertEquals(Tache.find.byId(tacheJ.id).chargeConsommee, Double.valueOf(15));
            assertEquals(Tache.find.byId(tacheF.id).chargeConsommee, Double.valueOf(15));
            // Modifier la charge restante de tacheJ(sous tache de tacheF) à 45D => tacheJ: chargeConsommee=15D, chargeRestante=45D
            //                                                                   => tacheF: chargeConsommee=15D, chargeRestante=145D
            tacheJ.setChargeRestante(45D);
            assertEquals(Tache.find.byId(tacheJ.id).chargeRestante, Double.valueOf(45));
            assertEquals(Tache.find.byId(tacheF.id).chargeRestante, Double.valueOf(145));
            projet.updateAvancementGlobal();
            // Après MAJ, l'avancement = (0+10+15)/((20+20+20+20+20+(20+20+45+20+20+20)+65)+(0+10+15)) = 0.07 => "7"
            assertEquals(Projet.find.byId(projet.id).avancementGlobal.toString(), "7");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testImportContactClient() {

        Client client = new Client("Applee", 2, true, null, null, null);
        client.save();
        assertNotNull(client.id);
        assertEquals(client.listeContacts.size(), 0);

        List<Contact> listContacts = new BeanList<>();
        Contact contact1 = new Contact("Jobsa", "Stevea", "s.j@apple.coma", "0211465978", null);
        Contact contact2 = new Contact("Jameas", "Franka", "f.j@apple.coma", "0215461979", null);
        listContacts.add(contact1);
        listContacts.add(contact2);
        client.importerListContacts(listContacts);

        Logger.debug(contact1.toString());
        Logger.debug(contact2.toString());
        assertEquals(contact1, Contact.find.where().eq("nom", "Jobsa").findUnique());
        assertEquals(contact2, Contact.find.where().eq("nom", "Jameas").findUnique());
        contact1.save();
        contact2.save();

        Client cl2 = Client.find.byId(client.id);
        Logger.debug(client.toString());
        Logger.debug(cl2.toString());

        Logger.debug(listContacts.toString());
        Logger.debug(cl2.listeContacts.toString());

        assertTrue(Utils.isEqualList(listContacts, cl2.listeContacts));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testAjouterContactClientException() {

        Client cl = new Client("Applee", 2, true, null, null, null);
        cl.save();
        Logger.debug(cl.toString());
        assertNotNull(cl.id);
        assertEquals(cl.listeContacts.size(), 0);

        Contact c1 = new Contact("Jobsa", "Stevea", "s.j@apple.coma", "0211465978");
        Contact c2 = new Contact("Jobsa", "Stevea", "s.j@apple.coma", "0211465978");

            /* EXCEPTION THROW 2 equals contacts*/
        cl.ajouterContact(c1);
        cl.ajouterContact(c2);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testAffecterProjetClientException() {

        Client cl = new Client("Google", 3, true, null, null, null);
        cl.save();

        assertNotNull(cl.id);
        assertEquals(cl.listeProjets.size(), 0);

        Projet pr = new Projet();
        pr.nom = "Site Google";
        pr.description = "Développement du nouveau site de Google";
        pr.dateDebutTheorique = Utils.getDateFrom(2016, 2, 2);
        pr.dateFinTheorique = Utils.getDateFrom(2016, 2, 10);
        pr.dateDebutReel = Utils.getDateFrom(2016, 2, 3);
        pr.dateFinReelTot = Utils.getDateFrom(2016, 2, 9);
        pr.chargeInitiale = 24D;
        pr.unite = UniteProjetEnum.SEMAINE;
        pr.avancementGlobal = new Byte("0");

        pr.enCours = true;
        pr.archive = false;
        pr.client = cl;
        pr.priorite = 1;

        cl.affecterProjet(pr);
        cl.affecterProjet(pr);
    }

    @Test
    public void testAffecterProjetClient() {

        Client cl = new Client("Atos", 1, true, null, null, null);
        cl.save();

        assertNotNull(cl.id);
        assertEquals(cl.listeProjets.size(), 0);

        Projet pr = new Projet();
        pr.nom = "Site Atos";
        pr.description = "Développement du nouveau site d'Atos";
        pr.dateDebutTheorique = Utils.getDateFrom(2016, 2, 2);
        pr.dateFinTheorique = Utils.getDateFrom(2016, 2, 10);
        pr.dateDebutReel = Utils.getDateFrom(2016, 2, 3);
        pr.dateFinReelTot = Utils.getDateFrom(2016, 2, 9);
        pr.dateFinReelTard = Utils.getDateFrom(2016, 2, 9);
        pr.chargeInitiale = 24D;
        pr.unite = UniteProjetEnum.SEMAINE;
        pr.avancementGlobal = new Byte("0");

        pr.enCours = true;
        pr.archive = false;
        pr.client = cl;
        pr.priorite = 1;
        pr.save();

        Logger.debug("project size 1 : " + cl.listeProjets.size());
        cl.affecterProjet(pr);
        Logger.debug("project size 2 : " + cl.listeProjets.size());
        cl.save();

        Client cl2 = Client.find.where().eq("nom", "Atos").findUnique();
        Logger.debug(cl.listeProjets.get(0).toString());
        Logger.debug(cl2.listeProjets.get(0).toString());

        assertEquals(cl.listeProjets.get(0), cl2.listeProjets.get(0));
    }

    @Test
    public void testSupprimerTacheProjet() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,2,29, dateDebutReel: 2016,2,2, dateFinReelTot: 2016,2,28, dateFinReelTard: 2016,2,28
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 2), Utils.getDateFrom(2016, 2, 29), Utils.getDateFrom(2016, 2, 2),
                Utils.getDateFrom(2016, 2, 28), Utils.getDateFrom(2016, 2, 28), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        //tache dateDebut: (2016,2,2), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tacheA = new Tache("TacheA", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 2),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 0D, 20D, null, null, null, null, null);
        tacheA.save();

        //tache dateDebut: (2016,2,4), dateFinTot: (2016,2,18), dateFinTard: (2016,2,18)
        Tache tacheB = new Tache("TacheB", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 18), Utils.getDateFrom(2016, 2, 18), 20D, 0D, 20D, null, null, tacheA, null, null);
        tacheB.save();

        //tache dateDebut: (2016,2,18), dateFinTot: (2016,2,28), dateFinTard: (2016,2,28)
        Tache tacheC = new Tache("TacheC", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 18),
                Utils.getDateFrom(2016, 2, 28), Utils.getDateFrom(2016, 2, 28), 20D, 0D, 20D, null, null, tacheB, null, null);
        tacheC.save();

        try {
            projet.creerTacheInitialisationProjet(tacheA);
            projet.creerTacheEnDessous(tacheB, tacheA);
            projet.creerTacheEnDessous(tacheC, tacheB);
            projet.supprimerTache(tacheB);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(Projet.find.byId(projet.id).listTaches().size(), 2);
        assertTrue(!Projet.find.byId(projet.id).listTaches().contains(Tache.find.byId(tacheB.id)));
        assertEquals(Tache.find.byId(tacheC.id).predecesseur, Tache.find.byId(tacheA.id));
        Tache a = Tache.find.byId(tacheA.id);
        Tache c = Tache.find.byId(tacheC.id);
        final List<Tache> successeursA = Tache.find.where().eq("predecesseur", a).findList();

        Logger.debug("==================================================================");
        successeursA.stream().map(tache -> tache.toString()).forEach(Logger::debug);
        Logger.debug("==================================================================");
        a.getSuccesseurs().stream().map(tache -> tache.toString()).forEach(Logger::debug);
        Logger.debug("==================================================================");
        assertEquals(a.getSuccesseurs().get(0), c);
    }

    @Test
    public void testUtilisateurCheckPassword() {
        String password = "aZERTY123456";
        String passwordF = "aZERTY123457";

        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", password);
        utilisateur.save();

        assertNotNull(utilisateur.id);
        Logger.debug(utilisateur.toString());
        Utilisateur utilisateur2 = Utilisateur.find.byId(utilisateur.id);
        Logger.debug(utilisateur2.toString());

        assertEquals(utilisateur, utilisateur2);
        //Logger.debug("Utilisateur Id: " + utilisateur.id);
        //Logger.debug("Utilisateur Id: " + Utilisateur.find.byId(utilisateur.id).id);
        Logger.debug("hachage: " + utilisateur2.hachage(utilisateur2.id, password));
        Logger.debug("this.pwd: " + utilisateur2.getPassword());
        Logger.debug("check pwd: " + utilisateur2.hachage(utilisateur2.id, password).equals(utilisateur2.getPassword()));

        assertTrue(utilisateur2.checkPassword(password));
        assertFalse(utilisateur2.checkPassword(passwordF));
    }

    @Test
    public void testVerifierCoherenceDesDates() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();
        Tache tache1 = new Tache("Etude 11", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 3, true, Utils.getDateFrom(2016, 2, 1),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 25), 20D, 0D, 20D, null, null, null, null, null);
        tache1.save();

        Tache tache2 = new Tache("Etude 12", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 3, true, Utils.getDateFrom(2016, 2, 18),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 17), 20D, 0D, 20D, null, null, null, null, null);
        tache2.save();
        assertTrue(Tache.find.byId(tache1.id).verifierCoherenceDesDates());
        assertFalse(Tache.find.byId(tache2.id).verifierCoherenceDesDates());
    }

    @Test
    public void testVerifierOrdreSousTaches() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,2,29, dateDebutReel: 2016,2,2, dateFinReelTot: 2016,2,28, dateFinReelTard: 2016,2,28
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 1), Utils.getDateFrom(2016, 3, 25), Utils.getDateFrom(2016, 2, 1),
                Utils.getDateFrom(2016, 3, 25), Utils.getDateFrom(2016, 3, 25), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        Tache tacheParent2 = new Tache("Tache parent2", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 2, true, Utils.getDateFrom(2016, 2, 1),
                Utils.getDateFrom(2016, 3, 20), Utils.getDateFrom(2016, 3, 25), 20D, 0D, 20D, null, null, null, null, null);
        tacheParent2.save();

        Tache tache21 = new Tache("Tache 21", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 3, true, Utils.getDateFrom(2016, 2, 1),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 25), 20D, 0D, 20D, null, null, null, null, null);
        tache21.save();

        Tache tache22 = new Tache("Tache 22", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 3, true, Utils.getDateFrom(2016, 2, 26),
                Utils.getDateFrom(2016, 2, 27), Utils.getDateFrom(2016, 2, 28), 20D, 0D, 20D, null, null, tache21, null, null);
        tache22.save();

        Tache tache23 = new Tache("Tache 23", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 3, true, Utils.getDateFrom(2016, 2, 28),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 3, 25), 20D, 0D, 20D, null, null, tache22, null, null);
        tache23.save();

        try {
            projet.creerTacheInitialisationProjet(tacheParent2);
            projet.creerSousTache(tache21, tacheParent2);
            projet.creerTacheEnDessous(tache22, tache21);
            projet.creerTacheEnDessous(tache23, tache22);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(Tache.find.byId(tacheParent2.id).verifierOrdreSousTaches());
    }

    @Test
    public void testTacheEstDisponible() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,2,29, dateDebutReel: 2016,2,2, dateFinReelTot: 2016,2,28, dateFinReelTard: 2016,2,28
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 1), Utils.getDateFrom(2016, 3, 25), Utils.getDateFrom(2016, 2, 1),
                Utils.getDateFrom(2016, 3, 25), Utils.getDateFrom(2016, 3, 25), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        Tache tacheParent2 = new Tache("Tache parent2", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 2, true, Utils.getDateFrom(2016, 2, 1),
                Utils.getDateFrom(2016, 3, 20), Utils.getDateFrom(2016, 3, 25), 20D, 0D, 20D, null, null, null, null, null);
        tacheParent2.save();

        Tache tache21 = new Tache("Tache 21", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 3, true, Utils.getDateFrom(2016, 2, 1),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 2, 25), 20D, 0D, 20D, null, null, null, null, null);
        tache21.save();

        Tache tache22 = new Tache("Tache 22", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 3, true, Utils.getDateFrom(2016, 2, 26),
                Utils.getDateFrom(2016, 2, 27), Utils.getDateFrom(2016, 2, 28), 20D, 0D, 20D, null, null, tache21, null, null);
        tache22.save();

        Tache tache23 = new Tache("Tache 23", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 3, true, Utils.getDateFrom(2016, 2, 28),
                Utils.getDateFrom(2016, 2, 20), Utils.getDateFrom(2016, 3, 25), 20D, 0D, 20D, null, null, tache22, null, null);
        tache23.save();

        try {
            projet.creerTacheInitialisationProjet(tacheParent2);
            projet.creerSousTache(tache21, tacheParent2);
            projet.creerTacheEnDessous(tache22, tache21);
            projet.creerTacheEnDessous(tache23, tache22);

            assertFalse(Tache.find.byId(tache22.id).estDisponible());
            tache21.modifierCharge(20D, 0D);
            assertTrue(Tache.find.byId(tache22.id).estDisponible());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUtilisateurAffecterTache() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,2,29, dateDebutReel: 2016,2,2, dateFinReelTot: 2016,2,28, dateFinReelTard: 2016,2,28
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 1), Utils.getDateFrom(2016, 3, 25), Utils.getDateFrom(2016, 2, 1),
                Utils.getDateFrom(2016, 3, 25), Utils.getDateFrom(2016, 3, 25), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        Tache tache = new Tache("Tache", "Cette tâche permet de réaliser l'étude du projet", null, 2, true, Utils.getDateFrom(2016, 2, 1),
                Utils.getDateFrom(2016, 3, 20), Utils.getDateFrom(2016, 3, 25), 20D, 0D, 20D, null, null, null, null, null);
        tache.save();

        try {
            utilisateur.affectTache(tache);
            projet.creerTacheInitialisationProjet(tache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUtilisateurGenererPassword() {
        String oldPassword = "aZERTY123456";

        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", oldPassword);
        utilisateur.save();

        assertNotNull(utilisateur.id);
        Logger.debug(utilisateur.toString());

        Utilisateur utilisateur2 = Utilisateur.find.byId(utilisateur.id);
        Logger.debug(utilisateur2.toString());

        assertTrue(utilisateur2.checkPassword(oldPassword));

        String newPassword = utilisateur2.genererPassword();
        utilisateur2.setPassword(newPassword);
        utilisateur2.save();

        assertTrue(utilisateur2.checkPassword(newPassword));
        assertFalse(utilisateur2.checkPassword(oldPassword));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testUtilisateurPasswordEception() {
        String oldPassword = "AZERTY";
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", oldPassword);
        utilisateur.save();
        utilisateur.setPassword(oldPassword);
    }

    @Test(expected = Exception.class)
    public void testAssocierSuccesseurEception() {
        Utilisateur utilisateur = Utilisateur.create("Z", "Z", "z.z@gmail.com", "1234567980", "123456Aa");
        utilisateur.save();

        //projet dateDebutTheorique: 2016,2,2, dateFinTheorique: 2016,2,10, dateDebutReel: 2016,2,3, dateFinReelTot: 2016,2,9, dateFinReelTard: 2016,2,9
        Projet projet = new Projet("Site Apple", "Développement du nouveau site d'Apple", utilisateur,
                Utils.getDateFrom(2016, 2, 2), Utils.getDateFrom(2016, 2, 10), Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 9), Utils.getDateFrom(2016, 2, 9), 24D, UniteProjetEnum.SEMAINE, new Byte("0"), false, false, null, 3, null, null);
        projet.save();

        //tache dateDebut: (2016,2,3), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tache1 = new Tache("Tache1", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 5), Utils.getDateFrom(2016, 2, 5), 20D, 10D, 20D, null, null, null, null, null);
        tache1.save();

        //tache dateDebut: (2016,2,3), dateFinTot: (2016,2,4), dateFinTard: (2016,2,4)
        Tache tache2 = new Tache("Tache2", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 3),
                Utils.getDateFrom(2016, 2, 4), Utils.getDateFrom(2016, 2, 4), 20D, 10D, 20D, null, null, null, null, null);
        tache2.save();

        Tache tache3 = new Tache("Tache3", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 5), Utils.getDateFrom(2016, 2, 5), 20D, 10D, 20D, null, null, tache2, null, null);
        tache3.save();

        Tache tache4 = new Tache("Tache4", "Cette tâche permet de réaliser l'étude du projet", utilisateur, 0, true, Utils.getDateFrom(2016, 2, 4),
                Utils.getDateFrom(2016, 2, 5), Utils.getDateFrom(2016, 2, 5), 20D, 10D, 20D, null, null, tache2, null, null);
        tache4.save();

        try {
            projet.creerTacheInitialisationProjet(tache1);
            projet.creerSousTache(tache2, tache1);
            projet.creerTacheEnDessous(tache3, tache2);
            projet.creerSousTache(tache4, tache3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(Projet.find.byId(projet.id).listTaches().contains(Tache.find.byId(tache1.id)));
        assertTrue(Projet.find.byId(projet.id).listTaches().contains(Tache.find.byId(tache2.id)));
        assertTrue(Tache.find.byId(tache1.id).enfants().contains(Tache.find.byId(tache2.id)));

        assertEquals(Tache.find.byId(tache1.id).idTache, "1");
        assertEquals(Tache.find.byId(tache2.id).idTache, "1.1");
        assertEquals(Tache.find.byId(tache3.id).idTache, "1.2");

        assertEquals(Tache.find.byId(tache1.id).idTache, "1");
        assertEquals(Tache.find.byId(tache2.id).idTache, "1.1");
        assertEquals(Tache.find.byId(tache3.id).idTache, "1.2");
        assertEquals(Tache.find.byId(tache4.id).idTache, "1.2.1");
        assertTrue(Tache.find.byId(tache1.id).enfants().contains(Tache.find.byId(tache2.id)));
        assertTrue(Tache.find.byId(tache1.id).enfants().contains(Tache.find.byId(tache3.id)));
        assertTrue(Tache.find.byId(tache3.id).enfants().contains(Tache.find.byId(tache4.id)));

        assertFalse(Tache.find.byId(tache1.id).enfants().contains(Tache.find.byId(tache4.id)));
        assertFalse(Tache.find.byId(tache2.id).enfants().contains(Tache.find.byId(tache3.id)));
        assertFalse(Tache.find.byId(tache2.id).enfants().contains(Tache.find.byId(tache4.id)));

        assertTrue(Tache.find.byId(tache4.id).predecesseur.equals(Tache.find.byId(tache2.id)));
        assertFalse(Tache.find.byId(tache4.id).predecesseur.equals(Tache.find.byId(tache1.id)));
        assertFalse(Tache.find.byId(tache4.id).predecesseur.equals(Tache.find.byId(tache4.id)));
        assertFalse(Tache.find.byId(tache4.id).predecesseur.equals(Tache.find.byId(tache3.id)));
        assertTrue(Tache.find.byId(tache4.id).getSuccesseurs().isEmpty());

        assertTrue(Tache.find.byId(tache2.id).getSuccesseurs().contains(Tache.find.byId(tache3.id)));
        assertFalse(Tache.find.byId(tache2.id).getSuccesseurs().contains(Tache.find.byId(tache1.id)));
        assertFalse(Tache.find.byId(tache2.id).getSuccesseurs().contains(Tache.find.byId(tache2.id)));
        assertTrue(Tache.find.byId(tache2.id).getSuccesseurs().contains(Tache.find.byId(tache4.id)));
        assertTrue(!Tache.find.byId(tache2.id).hasPredecesseur());

        // Tache1: 1; Tache2: 1.1; Tache3: 1.2; Tache4: 1.2.1
        tache4.associerSuccesseur(tache1);
    }

    @Test
    public void testModifierTache() {
        //Map<String, List<Object>> all = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
        //all.forEach((key, value) -> Ebean.save(value));
        //
        //Map<String, String[]> map = new HashMap<>();
        //map.put("form-modif-tache-nom", new String[]{"NewNom"});
        //map.put("form-modif-tache-desc", new String[]{"newDescription"});
        //map.put("id-tache", new String[]{"35"});
        //map.put("niveau", new String[]{"0"});
        //map.put("DD-modifier", new String[]{"12/02/2016"});
        //map.put("DFTO-modifier", new String[]{"13/02/2016"});
        //map.put("DFTA-modifier", new String[]{"14/02/2016"});
        //map.put("form-modif-tache-ch-init", new String[]{"4"});
        //map.put("form-modif-tache-ch-cons", new String[]{"1"});
        //map.put("form-modif-tache-ch-rest", new String[]{"3"});
        //map.put("predecesseur", new String[]{"3"});
        //map.put("successeurs", new String[]{"36,"});
        //map.put("interlocuteurs", new String[]{"4,5,"});
        //map.put("responsable", new String[]{"13"});
        //
        //Logger.debug("SIZE of taches: " + Tache.find.findRowCount());
        //Tache.find.all().stream().forEach(tache -> {
        //    Logger.debug("Tache: id -> " + tache.id + ", nom -> " + tache.nom);
        //});
        //System.out.println(Tache.find.byId(Long.parseLong(map.get("id-tache")[0])));
        //
        //DashboardController.modifierTacheMap(map);
        //
        //Tache tache = Tache.find.byId(Long.parseLong(map.get("id-tache")[0]));
        //
        //assertEquals(tache.nom, map.get("form-modif-tache-nom")[0]);
        //assertEquals(tache.description, map.get("form-modif-tache-desc")[0]);
        //String[] dateDebut = map.get("DD-modifier")[0].split("/");
        //Date newDebut = Utils.getDateFrom(Integer.parseInt(dateDebut[2]), Integer.parseInt(dateDebut[1]), Integer.parseInt(dateDebut[0]));
        //
        //assertEquals(tache.dateDebut.toString(), newDebut.toString());
        //String[] dateFinProche = map.get("DFTO-modifier")[0].split("/");
        //Date newFinTot = Utils.getDateFrom(Integer.parseInt(dateFinProche[2]), Integer.parseInt(dateFinProche[1]), Integer.parseInt(dateFinProche[0]));
        //
        //assertEquals(tache.dateFinTot.toString(), newFinTot.toString());
        //
        //String[] dateFinTard = map.get("DFTA-modifier")[0].split("/");
        //Date newFinTard = Utils.getDateFrom(Integer.parseInt(dateFinTard[2]), Integer.parseInt(dateFinTard[1]), Integer.parseInt(dateFinTard[0]));
        //
        //assertEquals(tache.dateFinTard.toString(), newFinTard.toString());
        //assertEquals(tache.chargeInitiale.doubleValue(), Double.parseDouble(map.get("form-modif-tache-ch-init")[0]), 0);
        //assertEquals(tache.chargeConsommee.doubleValue(), Double.parseDouble(map.get("form-modif-tache-ch-cons")[0]), 0);
        //assertEquals(tache.chargeRestante.doubleValue(), Double.parseDouble(map.get("form-modif-tache-ch-rest")[0]), 0);
        //Tache newPredecesseur = Tache.find.byId(Long.parseLong(map.get("predecesseur")[0]));
        //assertEquals(tache.predecesseur, newPredecesseur);
        //
        //List<Tache> successeurs = new ArrayList<>();
        //String[] tabSucc = map.get("successeurs")[0].split(",");
        //for (String idSucc : tabSucc) {
        //    if (!idSucc.equals("")) {
        //        successeurs.add(Tache.find.byId(Long.parseLong(idSucc)));
        //    }
        //}
        //assertTrue(successeurs.containsAll(tache.getSuccesseurs()));
        //Logger.debug(successeurs.toString());
        //Logger.debug(tache.getSuccesseurs().toString());
        //assertTrue(tache.getSuccesseurs().containsAll(successeurs));
        //
        //
        //List<Contact> interlocuteurs = new ArrayList<>();
        //String[] tabInterlocuteurs = map.get("interlocuteurs")[0].split(",");
        //for (String idContact : tabInterlocuteurs) {
        //    if (!idContact.equals("undefined") && !idContact.equals("")) {
        //        interlocuteurs.add(Contact.find.byId(Long.parseLong(idContact)));
        //    }
        //}
        //
        //
        //Logger.debug(interlocuteurs.toString());
        //Logger.debug(tache.getInterlocuteurs().toString());
        //assertTrue(interlocuteurs.containsAll(tache.getInterlocuteurs()));
        //assertTrue(tache.getInterlocuteurs().containsAll(interlocuteurs));
        //
        //assertEquals(tache.responsableTache, Utilisateur.find.byId(Long.parseLong(map.get("responsable")[0])));

    }

}