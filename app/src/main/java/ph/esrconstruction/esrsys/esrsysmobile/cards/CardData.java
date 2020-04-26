package ph.esrconstruction.esrsys.esrsysmobile.cards;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orhanobut.logger.Logger;

public class CardData {

        public String CardID;
        public String CardType = "Card";


        public CardData(String CardID) {
            this.CardID = CardID;
        }

        public void parseJsonToMe(String json){
            Gson g = new Gson();
            CardData ecc = g.fromJson(json, CardData.class);
            this.CardID = ecc.CardID;
        }

}
