package ph.esrconstruction.esrsys.esrsysmobile.events;

import ph.esrconstruction.esrsys.esrsysmobile.network.ESRServer;

public class ServerMessageEvent {

        public final ESRServer mServer;
        public final Byte message;
        public final Boolean connected;

        public ServerMessageEvent(ESRServer mServer, Byte message, Boolean connected) {
            this.mServer = mServer;
            this.message = message;
            this.connected = connected;
        }

    public final class Messages {
        public static final byte ServerOnline = (byte)1;
        public static final byte ServerOffline = (byte)2;
    }

}
