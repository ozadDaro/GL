package models;

public class Error
{
    public boolean nomVide, nomTropLong, nomIncorrect;
    public boolean prenomVide, prenomTropLong, prenomIncorrect;
    public boolean emailVide, emailIncorrecte, emailTropLong;
    public boolean telVide, telIncorrecte, telTropLong;
    public boolean userExist;
    public boolean nomClientVide, nomClientTropLong;
    public boolean adresseVide, adresseTropLong;
    public boolean codePostalVide, codePostaleTropLong;
    public boolean villeVide,villeTropLong;
    public boolean paysVide, paysTropLong;
    public boolean descriptionTropLong;
    public boolean clientExiste;

    public boolean nomProjetVide, nomProjetTropLong;
    public boolean dateThDebutProjetVide,dateThFinProjetVide;
    public boolean dateFinAvantDebut;
    public boolean parseError;

    public boolean nomVideContact, nomTropLongContact,nomIncorrectContact;
    public boolean prenomVideContact, prenomTropLongContact,prenomIncorrectContact;
    public boolean emailVideContact, emailIncorrecteContact, emailTropLongContact;
    public boolean telVideContact, telIncorrecteContact, telTropLongContact;

    public boolean hasErrorUtilisateur()
    {
        return nomVide || nomTropLong || nomIncorrect
                || prenomVide || prenomTropLong || prenomIncorrect
                || emailVide || emailIncorrecte || emailTropLong
                || telVide || telIncorrecte || telTropLong
                || userExist;
    }

    public boolean hasErrorProjet(){
        return nomProjetVide || dateThDebutProjetVide || dateThFinProjetVide || descriptionTropLong || dateFinAvantDebut || parseError || nomProjetTropLong;
    }

    public boolean hasErrorContact() {
        return nomVideContact || nomTropLongContact || prenomVideContact || prenomTropLongContact || emailVideContact
                || emailIncorrecteContact || telVideContact || telIncorrecteContact || telTropLongContact || emailTropLongContact
                || nomIncorrectContact ||prenomIncorrectContact;
    }

    public boolean hasErrorClient(){
        return nomClientVide || nomClientTropLong || adresseVide || adresseTropLong || codePostalVide || codePostaleTropLong
                || villeVide || villeTropLong || paysVide || paysTropLong || clientExiste;
    }

}