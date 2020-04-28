package ph.esrconstruction.esrsys.esrsysmobile.cards;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orhanobut.logger.Logger;

import org.joda.time.DateTime;

import java.util.Date;

public class CardData {

        public String CardID;
        public byte CardType;
        public DateTime LastScanned;
        public String LastTerminal;
        public String Message;

        public transient Intent intent;

        public CardData(String CardID, Intent intent) {
            this.CardID = CardID;
            this.LastScanned = new DateTime();
            this.LastTerminal = "UNKNOWN-000";
            this.CardType = CardTypes.BLANK;
            this.intent = intent;
            this.Message = "Property of ESR Construction and Dev Corp";
        }



    public final class CardTypes {
        public static final byte BLANK = (byte)1;
        public static final byte EMPLOYEE = (byte)2;
        public static final byte UNKNWON = (byte)0;

    }

    public static String byteToCardTypeString(byte cardType){
            switch (cardType){
                case CardTypes.BLANK:
                    return "BLANK";
                case CardTypes.EMPLOYEE:
                    return "EMPLOYEE";
                default:
                    return "UNKNOWN";
            }

    }

}
