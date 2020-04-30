package ph.esrconstruction.esrsys.esrsysmobile.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;

import java.net.URL;
import java.util.Locale;

import ph.esrconstruction.esrsys.esrsysmobile.R;

public class FingerprintUtils {




    private IBioMiniDevice.CaptureOption mCaptureOptionDefault = new IBioMiniDevice.CaptureOption();

    private CaptureResponder mCaptureResponsePrev = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                                   final IBioMiniDevice.TemplateData capturedTemplate,
                                   final IBioMiniDevice.FingerState fingerState) {

            Log.d("CaptureResponsePrev", String.format(Locale.ENGLISH , "captureTemplate.size (%d) , fingerState(%s)" , capturedTemplate== null? 0 : capturedTemplate.data.length, String.valueOf(fingerState.isFingerExist)));
            //printState(getResources().getText(R.string.start_capture_ok));
            byte[] pImage_raw =null;
          //  if( (mCurrentDevice!= null && (pImage_raw = mCurrentDevice.getCaptureImageAsRAW_8() )!= null)) {
              //  Log.d("CaptureResponsePrev ", String.format(Locale.ENGLISH, "pImage (%d) , FP Quality(%d)", pImage_raw.length , mCurrentDevice.getFPQuality(pImage_raw, mCurrentDevice.getImageWidth(), mCurrentDevice.getImageHeight(), 2)));
         //   }

            new Thread(new Runnable() {
                public void run(){
                    if(capturedImage != null) {
                        /// ImageView iv = (ImageView) findViewById(R.id.imagePreview);
                        /// if(iv != null) {
                        ///   iv.setImageBitmap(capturedImage);
                        /// }
                    }
                }
            }).start();

            return true;
        }

        @Override
        public void onCaptureError(Object context, int errorCode, String error) {
            Logger.d("onCaptureError : " + error);
           // Logger.d(((IBioMiniDevice)context).popPerformanceLogger.d());
            if( errorCode != IBioMiniDevice.ErrorCode.OK.value());
              //  printState(getResources().getText(R.string.start_capture_fail));
        }
    };

}
