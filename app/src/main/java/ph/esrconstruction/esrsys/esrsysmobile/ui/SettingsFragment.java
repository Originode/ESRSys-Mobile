package ph.esrconstruction.esrsys.esrsysmobile.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;
import com.suprema.IBioMiniDevice;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import io.realm.Realm;
import ph.esrconstruction.esrsys.esrsysmobile.ESRSys;
import ph.esrconstruction.esrsys.esrsysmobile.R;
import ph.esrconstruction.esrsys.esrsysmobile.events.MessageEvent;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;

public class SettingsFragment extends Fragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
    }


    private void initViews(@NonNull View viewx) {


        Button buttonReadCaptureParam = (Button) viewx.findViewById(R.id.buttonReadCaptureParam);
        buttonReadCaptureParam.setOnClickListener(view -> {
            if(ESRSys.getInstance().mCurrentDevice != null) {
                int security_level = (int) ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.SECURITY_LEVEL).value;
                int sensitivity_level = (int) ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.SENSITIVITY).value;
                int timeout = (int) ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.TIMEOUT).value;
                int lfd_level = (int) ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.DETECT_FAKE).value;
                boolean fast_mode = ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.FAST_MODE).value == 1;
                boolean crop_mode = ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.SCANNING_MODE).value == 1;
                boolean ext_trigger = ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.EXT_TRIGGER).value == 1;
                boolean auto_sleep = ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.ENABLE_AUTOSLEEP).value == 1;
                boolean extract_mode = ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.EXTRACT_MODE_BIOSTAR).value == 1;
                ((SeekBar) viewx.findViewById(R.id.seekBarSecurityLevel)).setProgress(security_level);
                ((SeekBar) viewx.findViewById(R.id.seekBarSensitivity)).setProgress(sensitivity_level);
                ((SeekBar) viewx.findViewById(R.id.seekBarTimeout)).setProgress(timeout/1000);
                ((SeekBar) viewx.findViewById(R.id.seekBarLfdLevel)).setProgress(lfd_level);
                ((CheckBox) viewx.findViewById(R.id.checkBoxFastMode)).setChecked(fast_mode);
                ((CheckBox) viewx.findViewById(R.id.checkBoxCropMode)).setChecked(crop_mode);
                ((CheckBox) viewx.findViewById(R.id.checkBoxExtTrigger)).setChecked(ext_trigger);
                ((CheckBox) viewx.findViewById(R.id.checkBoxAutoSleep)).setChecked(auto_sleep);
                Logger.d("Read Fingerprintreader params ok");
            }

        });

        Button buttonWriteCaptureParam = (Button) viewx.findViewById(R.id.buttonWriteCaptureParam);
        buttonWriteCaptureParam.setOnClickListener(view -> {
            if(ESRSys.getInstance().mCurrentDevice != null) {
                int security_level = ((SeekBar) viewx.findViewById(R.id.seekBarSecurityLevel)).getProgress();
                int sensitivity_level = ((SeekBar) viewx.findViewById(R.id.seekBarSensitivity)).getProgress();
                int timeout = ((SeekBar) viewx.findViewById(R.id.seekBarTimeout)).getProgress();
                int lfd_level = ((SeekBar) viewx.findViewById(R.id.seekBarLfdLevel)).getProgress();
                boolean fast_mode = ((CheckBox) viewx.findViewById(R.id.checkBoxFastMode)).isChecked();
                boolean crop_mode = ((CheckBox) viewx.findViewById(R.id.checkBoxCropMode)).isChecked();
                boolean ext_trigger = ((CheckBox) viewx.findViewById(R.id.checkBoxExtTrigger)).isChecked();
                boolean auto_sleep = ((CheckBox) viewx.findViewById(R.id.checkBoxAutoSleep)).isChecked();
                boolean extract_mode = false;
                ESRSys.getInstance().mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.SECURITY_LEVEL, security_level));
                ESRSys.getInstance().mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.SENSITIVITY, sensitivity_level));
                ESRSys.getInstance().mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.TIMEOUT, timeout));
                ESRSys.getInstance().mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.DETECT_FAKE, lfd_level));
                ESRSys.getInstance().mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.FAST_MODE, fast_mode?1:0));
                ESRSys.getInstance().mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.SCANNING_MODE, crop_mode?1:0));
                ESRSys.getInstance().mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.EXT_TRIGGER, ext_trigger?1:0));
                ESRSys.getInstance().mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.ENABLE_AUTOSLEEP, auto_sleep?1:0));
                ESRSys.getInstance().mCurrentDevice.setParameter(new IBioMiniDevice.Parameter(IBioMiniDevice.ParameterType.EXTRACT_MODE_BIOSTAR, extract_mode?1:0));
                Logger.d("write Fingerprintreader params ok");
            }
        });


        // EventBus.getDefault().post(ecd);

    }
}
