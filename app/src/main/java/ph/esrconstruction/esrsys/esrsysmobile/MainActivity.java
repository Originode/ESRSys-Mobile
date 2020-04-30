package ph.esrconstruction.esrsys.esrsysmobile;


import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.orhanobut.logger.Logger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleRegistry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import io.realm.Realm;
import ph.esrconstruction.esrsys.esrsysmobile.cards.CardData;
import ph.esrconstruction.esrsys.esrsysmobile.cards.EmployeeCardData;
import ph.esrconstruction.esrsys.esrsysmobile.events.MessageEvent;
import ph.esrconstruction.esrsys.esrsysmobile.events.TimerEvent;
import ph.esrconstruction.esrsys.esrsysmobile.fp.FingerPrintCaptureEvent;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NFCReadFragment;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NFCWriteFragment;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NfcListener;
import ph.esrconstruction.esrsys.esrsysmobile.utils.Etc;
import ph.esrconstruction.esrsys.esrsysmobile.utils.NfcUtils;

import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
                                    NavigationView.OnNavigationItemSelectedListener, NfcListener {
    public Toolbar toolbar;

    public DrawerLayout drawerLayout;

    public NavController navController;

    public NavigationView navigationView;
    public static final String TAG = "MAIN";

    private NfcAdapter mNfcAdapter;
    public NFCWriteFragment mNfcWriteFragment;
    public NFCReadFragment mNfcReadFragment;




    private boolean isDialogDisplayed = false;
    private boolean isWrite = false;

    private SearchView searchView;
    private int mStackLevel = 0;

    private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.t(TAG).v("onCreate");



        if( !ESRSys.mbUsbExternalUSBManager ){
            //Button btn_checkDevice = (Button)findViewById(R.id.buttonCheckDevice);
            //btn_checkDevice.setClickable(false);
            //btn_checkDevice.setEnabled(false);
        }else{
            //((Button)findViewById(R.id.buttonCheckDevice)).setOnClickListener(new View.OnClickListener() {
            //    @Override
            //    public void onClick(View v) {
            //        checkDevice();
            //    }
            // });
        }

        //printRev(""+mBioMiniFactory.getSDKInfo());



        /////////////

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);


        setupNavigation();

    }

    // Setting Up One Time Navigation
    private void setupNavigation() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.navigationView);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);

        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);

        Menu nav_Menu = navigationView.getMenu();
        //nav_Menu.findItem(R.id.menu_login_logout).setVisible(!ESRSys.getInstance().currentLogin.isLoggedIn());
        nav_Menu.findItem(R.id.menu_login_logout).setTitle(ESRSys.getInstance().currentLogin.isLoggedIn() ? "Logout" : "Login");

    }




    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(drawerLayout, Navigation.findNavController(this, R.id.nav_host_fragment));
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        menuItem.setChecked(true);

        drawerLayout.closeDrawers();

        int id = menuItem.getItemId();

        switch (id) {

            case R.id.first:
                navController.navigate(R.id.toolsFragment);
                break;

            case R.id.second:
                navController.navigate(R.id.reportsFragment);
                break;



            case R.id.menu_sync:
               // ESREmployeeRealmSyncService esrEmployeeRealmSyncService = new ESREmployeeRealmSyncService();
                navController.navigate(R.id.employeesFragment);

                ESRSys.getInstance().startEmployeeSyncService("quicky");

                break;

            case R.id.menu_login_logout:

                if(ESRSys.getInstance().currentLogin.isLoggedIn()){
                    Logger.t(TAG).i("logout");
                    navController.navigate(R.id.homeFragment);
                }else{

                    Logger.t(TAG).i("login");
                    navController.navigate(R.id.loginFragment);
                }


                break;

        }
        return true;

    }

    /* required to make activity life cycle owner */
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }


    @Override
    public void onDialogDisplayed() {

        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {

        isDialogDisplayed = false;
        isWrite = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {}
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{
                ndefDetected,
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        };
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if(mNfcAdapter!= null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);



    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter!= null)
            mNfcAdapter.disableForegroundDispatch(this); //stop nfc listener
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }
    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onNewIntent(Intent intent) { // this method is called when an NFC tag is scanned

        Logger.t(TAG).d("onNewIntent: "+intent.getAction());
        byte[] tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        Logger.t(TAG).d(Etc.bytesToHexString(tagId));

        // check for NFC related actions
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

        } else {
            // ignore
        }

            //formatMifareClassicToEmployeeCard(intent);
        NfcUtils.readCard(intent, (checkHash, card_type, info) -> {
            Gson g = Converters.registerDateTime(new GsonBuilder()).setLenient().create();
            try {
                if(checkHash){
                    switch (card_type){
                        case CardData.CardTypes.EMPLOYEE:
                            EmployeeCardData ecd = g.fromJson(info, EmployeeCardData.class);
                            ecd.intent = intent;
                            Logger.d(ecd.IDNumber + " " + ecd.Name);
                            Realm realmz = Realm.getInstance(ESRSys.getEsrConfig());
                            realmz.executeTransaction(inRealm -> {
                                Employee e = inRealm.where(Employee.class).equalTo("EmployeeID", ecd.EmployeeID).findFirst();
                                if (e != null) {
                                    Logger.d("set scanned");
                                    e.setFlags_cardScanned(true);
                                    e.setLastScanned(DateTime.now().toDate());

                                    e.setRemarks(ecd.Message);
                                }
                            });
                            realmz.close();
                            EventBus.getDefault().post(ecd);

                        default:
                            //bytesToHexString(tagId)
                            CardData cd = g.fromJson(info, CardData.class);
                            cd.intent = intent;
                            EventBus.getDefault().post(cd);
                    }
                }else{
                    Logger.d("unformatted card");
                    CardData unformattedCard = new CardData("xxx",intent);
                    unformattedCard.CardType = CardData.CardTypes.UNKNWON;
                    EventBus.getDefault().post(unformattedCard);
                }
            }
            catch (JsonParseException e) {
                e.printStackTrace();
            }
            catch (IllegalArgumentException e){
                e.printStackTrace();
            }


        });
        closeReadFragment();

    }










    private void showWriteFragment() {
        if (mNfcWriteFragment == null) {
            mNfcWriteFragment.dismiss();
        }
            mNfcWriteFragment = NFCWriteFragment.newInstance();
        mNfcWriteFragment.show(getSupportFragmentManager(),NFCWriteFragment.TAG);
    }
    private void closeWriteFragment() {
        if (mNfcWriteFragment == null) {
            mNfcWriteFragment.dismiss();
        }
    }

    private void showReadFragment() {
      //  ((MainActivity)getActivity()).mNfcReadFragment = (NFCReadFragment) getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
        if (mNfcReadFragment != null) {
            mNfcReadFragment.dismiss();
        }
           mNfcReadFragment = NFCReadFragment.newInstance();
        mNfcReadFragment.show(getSupportFragmentManager(),NFCReadFragment.TAG);

    }
    private void closeReadFragment() {
        //  ((MainActivity)getActivity()).mNfcReadFragment = (NFCReadFragment) getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
        if (mNfcReadFragment != null) {
            mNfcReadFragment.dismiss();
        }
    }











    private CaptureResponder mCaptureResponsePrev = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                                   final IBioMiniDevice.TemplateData capturedTemplate,
                                   final IBioMiniDevice.FingerState fingerState) {

            Logger.d("CaptureResponsePrev", String.format(Locale.ENGLISH , "captureTemplate.size (%d) , fingerState(%s)" , capturedTemplate== null? 0 : capturedTemplate.data.length, String.valueOf(fingerState.isFingerExist)));
           // printState(getResources().getText(R.string.start_capture_ok));
            byte[] pImage_raw =null;
            if( (ESRSys.getInstance().mCurrentDevice!= null && (pImage_raw = ESRSys.getInstance().mCurrentDevice.getCaptureImageAsRAW_8() )!= null)) {
                Logger.d("CaptureResponsePrev ", String.format(Locale.ENGLISH, "pImage (%d) , FP Quality(%d)", pImage_raw.length , ESRSys.getInstance().mCurrentDevice.getFPQuality(pImage_raw, ESRSys.getInstance().mCurrentDevice.getImageWidth(), ESRSys.getInstance().mCurrentDevice.getImageHeight(), 2)));
            }
            EventBus.getDefault().post(new FingerPrintCaptureEvent(context,capturedImage,capturedTemplate,fingerState,FingerPrintCaptureEvent.Modes.CAPTURE));
            return true;
        }

        @Override
        public void onCaptureError(Object context, int errorCode, String error) {
            Logger.e("onCaptureError : " + error);
            Logger.e(((IBioMiniDevice)context).popPerformanceLog());
            if( errorCode != IBioMiniDevice.ErrorCode.OK.value());
               // printState(getResources().getText(R.string.start_capture_fail));
        }
    };

    private CaptureResponder mVerifyResponsePrev = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                                   final IBioMiniDevice.TemplateData capturedTemplate,
                                   final IBioMiniDevice.FingerState fingerState) {

            Logger.d("VerifyResponsePrev: " + String.format(Locale.ENGLISH , "captureTemplate.size (%d) , fingerState(%s)" , capturedTemplate== null? 0 : capturedTemplate.data.length, String.valueOf(fingerState.isFingerExist)));
            // printState(getResources().getText(R.string.start_capture_ok));
            byte[] pImage_raw =null;
            if( (ESRSys.getInstance().mCurrentDevice!= null && (pImage_raw = ESRSys.getInstance().mCurrentDevice.getCaptureImageAsRAW_8() )!= null)) {
                Logger.d("CaptureResponsePrev: " + String.format(Locale.ENGLISH, "pImage (%d) , FP Quality(%d)", pImage_raw.length , ESRSys.getInstance().mCurrentDevice.getFPQuality(pImage_raw, ESRSys.getInstance().mCurrentDevice.getImageWidth(), ESRSys.getInstance().mCurrentDevice.getImageHeight(), 2)));
            }
            EventBus.getDefault().post(new FingerPrintCaptureEvent(context,capturedImage,capturedTemplate,fingerState,FingerPrintCaptureEvent.Modes.VERIFY));
            return true;
        }

        @Override
        public void onCaptureError(Object context, int errorCode, String error) {
            Logger.e("onCaptureError : " + error);
            Logger.e(((IBioMiniDevice)context).popPerformanceLog());
            if( errorCode != IBioMiniDevice.ErrorCode.OK.value());
            // printState(getResources().getText(R.string.start_capture_fail));
        }
    };


    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent m) {
        Logger.t(TAG + "-onMessageEvent").d(m.message);

        switch(m.message){
            case MessageEvent.Messages.NFCWriteFragment_closed:
                break;
            case MessageEvent.Messages.FingerPrintScanner_start:
                if(ESRSys.getInstance().mCurrentDevice != null) {
                    IBioMiniDevice.CaptureOption option = new IBioMiniDevice.CaptureOption();
                    option.extractParam.captureTemplate = true;
                    option.captureImage = true;
                    option.captureTimeout = (int)ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.TIMEOUT).value;
                    ESRSys.getInstance().mCurrentDevice.captureSingle(option,
                            mCaptureResponsePrev,
                            true);
                }
                break;
            case MessageEvent.Messages.FingerPrintScanner_verify:
                if(ESRSys.getInstance().mCurrentDevice != null) {
                    IBioMiniDevice.CaptureOption option = new IBioMiniDevice.CaptureOption();
                    option.extractParam.captureTemplate = true;
                    option.captureImage = true;
                    option.captureTimeout = (int)ESRSys.getInstance().mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.TIMEOUT).value;
                    ESRSys.getInstance().mCurrentDevice.captureSingle(option,
                            mVerifyResponsePrev,
                            true);
                }
                break;
            case MessageEvent.Messages.FingerPrintScanner_abort:
                if(ESRSys.getInstance().mCurrentDevice != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ESRSys.getInstance().mCurrentDevice.abortCapturing();
                            int nRetryCount =0;
                            while(ESRSys.getInstance().mCurrentDevice != null && ESRSys.getInstance().mCurrentDevice.isCapturing()){
                                SystemClock.sleep(10);
                                nRetryCount++;
                            }
                            Log.d("AbortCapturing" , String.format(Locale.ENGLISH ,
                                    "IsCapturing return false.(Abort-lead time: %dms) " ,
                                    nRetryCount* 10));
                        }
                    }).start();
                }
                break;
            default:

        }


    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimerEvent(TimerEvent t) {

        Logger.t(TAG + "-onTimerEvent").d(t.message);
        switch(t.message){
            case TimerEvent.Messages.EmployeeSync:
                Logger.t(TAG).d("next employee sync in: " + t.duration + " miliseconds");
            default:

        }


    }
}
