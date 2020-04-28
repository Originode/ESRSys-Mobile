package ph.esrconstruction.esrsys.esrsysmobile;


import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;


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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

import io.realm.Realm;
import io.realm.RealmResults;
import ph.esrconstruction.esrsys.esrsysmobile.cards.CardData;
import ph.esrconstruction.esrsys.esrsysmobile.cards.EmployeeCardData;
import ph.esrconstruction.esrsys.esrsysmobile.events.MessageEvent;
import ph.esrconstruction.esrsys.esrsysmobile.events.TimerEvent;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NFCReadFragment;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NFCWriteFragment;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NfcListener;
import ph.esrconstruction.esrsys.esrsysmobile.utils.etc;
import ph.esrconstruction.esrsys.esrsysmobile.utils.nfcUtils;

import com.suprema.BioMiniFactory;
import com.suprema.IBioMiniDevice;

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

    //Flag.
    public static final boolean mbUsbExternalUSBManager = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent= null;
    //
    private static BioMiniFactory mBioMiniFactory = null;
    public static final int REQUEST_WRITE_PERMISSION = 786;
    public IBioMiniDevice mCurrentDevice = null;


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

/////////////network observer

            try {
                //NdefWriterDemo demo = new NdefWriterDemo();
               // demo.runNdefWriter();

                //NdefWriter www = new NdefWriter();


            }
            catch (Exception e) {
               // e.printStackTrace();
            }



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
                navController.navigate(R.id.secondFragment);
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
        Logger.t(TAG).d(etc.bytesToHexString(tagId));

        // check for NFC related actions
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

        } else {
            // ignore
        }

            //formatMifareClassicToEmployeeCard(intent);
        nfcUtils.readCard(intent, (checkHash, card_type, info) -> {
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

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent m) {
        Logger.t(TAG + "-onMessageEvent").d(m.message);

        switch(m.message){
            case MessageEvent.Messages.NFCWriteFragment_closed:
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
