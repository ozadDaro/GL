package models;

public class Error
{
    public boolean nomVide, nomTropLong;
    public boolean prenomVide, prenomTropLong;
    public boolean emailVide, emailIncorrecte;
    public boolean telVide, telIncorrecte;
    public boolean userExist;

    public boolean hasErrorUtilisateur()
    {
        return nomVide || nomTropLong
                || prenomVide || prenomTropLong
                || emailVide || emailIncorrecte
                || telVide || telIncorrecte || userExist;
    }

}