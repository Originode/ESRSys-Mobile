package ph.esrconstruction.esrsys.esrsysmobile.events;

public class MessageEvent {

        public final String message;

        public MessageEvent(String message) {
            this.message = message;
        }

    public final class Messages {
        public static final String NFCWriteFragment_closed = "NFCWriteFragment-closed";
        public static final String FingerPrintScanner_start = "FingerPrintScanner-start";
        public static final String FingerPrintScanner_verify = "FingerPrintScanner-verify";
        public static final String FingerPrintScanner_captureSuccess = "FingerPrintScanner-captureSuccess";
        public static final String FingerPrintScanner_abort = "FingerPrintScanner-abort";
    }

}
