package ph.esrconstruction.esrsys.esrsysmobile;


import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.material.navigation.NavigationView;
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


import ph.esrconstruction.esrsys.esrsysmobile.ui.NFCReadFragment;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NFCWriteFragment;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NfcListener;

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

/////////////network observer



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
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if(mNfcAdapter!= null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter!= null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "onNewIntent: "+intent.getAction());

        if(tag != null) {
            Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);

            if (isDialogDisplayed) {

                if (isWrite) {

                    //String messageToWrite = mEtMessage.getText().toString();
                    //mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);
                   // mNfcWriteFragment.onNfcDetected(ndef,messageToWrite);

                } else {

                   // mNfcReadFragment = (NFCReadFragment)getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
                    mNfcReadFragment.onNfcDetected(ndef);
                    Logger.d("REAAAAAAAAAAAD");
                }
            }
        }
    }

}
