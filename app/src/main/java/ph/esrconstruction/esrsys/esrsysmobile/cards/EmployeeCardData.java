package ph.esrconstruction.esrsys.esrsysmobile.cards;

import android.content.Intent;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EmployeeCardData extends CardData {

    public String Name = "";
    public String IDNumber = "";
    public long EmployeeID;


    public EmployeeCardData(String _CardID, Intent intent) {
        super(_CardID, intent);
        this.CardType =  CardTypes.EMPLOYEE;
        this.Message = "...";
    }
    public EmployeeCardData(String _CardID, String _IDNumber, String _Name, long _EmployeeID, Intent intent) {
        this(_CardID, intent);
        IDNumber = _IDNumber;
        Name = _Name;

        EmployeeID = _EmployeeID;
    }
    public EmployeeCardData(CardData cardData) {
        this(cardData.CardID,"","",0,cardData.intent);
    }

}
