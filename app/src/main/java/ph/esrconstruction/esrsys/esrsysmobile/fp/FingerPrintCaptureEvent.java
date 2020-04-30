package ph.esrconstruction.esrsys.esrsysmobile.fp;

import android.content.Intent;
import android.graphics.Bitmap;

import com.suprema.IBioMiniDevice;

import org.joda.time.DateTime;

import ph.esrconstruction.esrsys.esrsysmobile.cards.CardData;

public class FingerPrintCaptureEvent {



    public Object context;
    public Bitmap capturedImage;
    public IBioMiniDevice.TemplateData capturedTemplate;
    public IBioMiniDevice.FingerState fingerState;
    public byte mode = Modes.CAPTURE;


        public FingerPrintCaptureEvent(Object context,Bitmap capturedImage,IBioMiniDevice.TemplateData capturedTemplate,IBioMiniDevice.FingerState fingerState, Byte mode) {
            this.context = context;
            this.capturedImage = capturedImage;
            this.capturedTemplate = capturedTemplate;
            this.fingerState = fingerState;
            this.mode = mode;
        }


    public final class Modes {
        public static final byte CAPTURE = (byte)0;
        public static final byte VERIFY = (byte)1;
    }

}
