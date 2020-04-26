package ph.esrconstruction.esrsys.esrsysmobile.cards;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EmployeeCardData extends CardData {

    public String LastName = "";
    public String FirstName = "";
    public String IDNumber = "";


    public EmployeeCardData(String _CardID) {
        super(_CardID);
        CardType = "EmployeeCard";
    }
    public EmployeeCardData(String _CardID, String _IDNumber, String _LastName, String _FirstName) {
        this(_CardID);
        IDNumber = _IDNumber;
        LastName = _LastName;
        FirstName = _FirstName;
    }

    public void parseJsonToMe(String json){
        Gson g = new Gson();
        EmployeeCardData ecc = g.fromJson(json, EmployeeCardData.class);
        this.CardID = ecc.CardID;
        this.CardType = ecc.CardType;

        this.IDNumber = ecc.IDNumber;
        this.LastName = ecc.LastName;
        this.FirstName = ecc.FirstName;
    }
}
