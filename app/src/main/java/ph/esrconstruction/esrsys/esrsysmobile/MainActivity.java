package ph.esrconstruction.esrsys.esrsysmobile;


import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.joda.time.DateTime;


import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.ndeftools.Record;
import org.ndeftools.externaltype.AndroidApplicationRecord;
import org.nfctools.utils.NfcUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import java.util.Arrays;

import java.util.Random;

import ph.esrconstruction.esrsys.esrsysmobile.cards.CardData;
import ph.esrconstruction.esrsys.esrsysmobile.cards.EmployeeCardData;
import ph.esrconstruction.esrsys.esrsysmobile.events.NFCReadEvent;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NFCReadFragment;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NFCWriteFragment;
import ph.esrconstruction.esrsys.esrsysmobile.ui.NfcListener;

import com.suprema.BioMiniFactory;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;
import com.telpo.tps550.api.fingerprint.FingerPrint;

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
    public void onNewIntent(Intent intent) { // this method is called when an NFC tag is scanned
        Logger.t(TAG).d("onNewIntent: "+intent.getAction());
        byte[] tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        Logger.t(TAG).d(bytesToHexString(tagId));
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        // check for NFC related actions
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

        } else {
            // ignore
        }
        /*
            NdefMessage[] messages = getNdefMessages(intent);
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (messages != null) {
                for (int i = 0; i < messages.length; i++) {
                    NdefMessage message = messages[i];
                    NdefRecord[] records = message.getRecords();
                    for (int j = 0; j < records.length; j++) {
                        NdefRecord record = records[j];
                        // TODO Process the individual records.
                        String text = new String(record.getPayload());
                        Logger.t(TAG).d(text);
                    }
                }
            }
*/

            JSONObject json = new JSONObject();
            try {
                json.put("TagType","ESR-Employee");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //formatTag(tag, new NdefMessage(mimeRecord));

        try {
            json.put("IDNumber","2020333355");
            json.put("LastName","Ababa");
            json.put("FirstName","Ralph");

            JSONObject eData = new JSONObject();
            eData.put("UpdateFingerprint", 1);
            DateTime dt = new DateTime();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            String dtStr = fmt.print(dt);
            eData.put("LastScanned", dtStr);
            eData.put("LastTerminal", "UNKNOWN-000");

            json.put("eData", eData);

            EmployeeCardData ec = new EmployeeCardData(bytesToHexString(tagId),"2020333355","Ababa","Ralph");

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();


            String stringData = gson.toJson(ec);
            Logger.d(stringData);


            Logger.d("data size = " + stringData.getBytes(Charset.forName("US-ASCII")).length);
            byte[][] xdivideArray = divideArray(stringData.getBytes(Charset.forName("US-ASCII")),16);
            Logger.d("chunks = " + xdivideArray.length);
            int sectorstowrite = (int) Math.ceil(((float)xdivideArray.length+1)/3);
            int ccd = 0;
            Logger.d("sectors to use = " + sectorstowrite);

            MifareClassic mif = MifareClassic.get(tag);
            int bs = MifareClassic.BLOCK_SIZE;

            int ttype = mif.getType();
            Logger.d("MifareClassic tag type: " + ttype);

            int tsize = mif.getSize();
            Logger.t(TAG).d( "tag size: " + tsize);

            int s_len = mif.getSectorCount();
            Logger.t(TAG).d( "tag sector count: " + s_len);

            int b_len = mif.getBlockCount();
            Logger.t(TAG).d( "tag block count: " + b_len);


            try { //write
                mif.connect();
                if (mif.isConnected()){

                    for(int i=0; i< sectorstowrite+1; i++){

                        boolean isAuthenticated = bruteAuthenticateSectorWithKeyA(mif,i);


                        if(isAuthenticated && i>0) {
                            for(int j=0; j< mif.getBlockCountInSector(i)-1; j++) {
                                if(ccd >= xdivideArray.length) break;
                                int block_index = mif.sectorToBlock(i)+j;
                                if(i==1 && j==0){ //size block, write number of sectors being used in card
                                    Logger.d("writing size block =  " + xdivideArray.length + " sector " + i + " block " + j + " index:" + block_index);
                                    mif.writeBlock(block_index, ByteBuffer.allocate(16).order(ByteOrder.nativeOrder()).putInt(xdivideArray.length).array());
                                }else{ //write blocks
                                   // Logger.d("writing chunk number " + (ccd+1) + "/"+ xdivideArray.length +" sector " + i + " block " + j + " index:" + block_index);
                                    mif.writeBlock(block_index, xdivideArray[ccd]);
                                    ccd++;
                                }
                            }
                        }


                        }
                    }

                mif.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


ccd = 0;
            try { //read
                mif.connect();
                if (mif.isConnected()){

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

                    int sectors_to_read = s_len-1;
                    int blocks_to_read = (sectors_to_read*3)+1;
                    for(int i=0; i< sectors_to_read+1; i++){

                        boolean isAuthenticated = bruteAuthenticateSectorWithKeyA(mif,i);

                        if(isAuthenticated && i>0) {
                            for(int j=0; j< mif.getBlockCountInSector(i)-1; j++) {
                                if(ccd >= blocks_to_read)  break;
                                int block_index = mif.sectorToBlock(i)+j;
                                if(i==1 && j==0){ //size block,
                                    //mif.writeBlock(block_index, ByteBuffer.allocate(16).order(ByteOrder.nativeOrder()).putInt(sectorstowrite).array());

                                    byte[] block = mif.readBlock(block_index);
                                    ByteBuffer buffer = ByteBuffer.wrap(block);
                                    blocks_to_read = buffer.order(ByteOrder.nativeOrder()).getInt();
                                    sectors_to_read  = (int) Math.ceil(((float)blocks_to_read+1)/3);
                                    //Logger.d("reading size block = " + blocks_to_read  + " sectors to read = " + sectors_to_read);
                                    //outputStream.write( block );
                                }else{ //read blocks
                                    //Logger.d("reading chunk number " + (ccd+1) + " sector " + i + " block " + j + " index:" + block_index);
                                    byte[] block = mif.readBlock(block_index);
                                    outputStream.write( block );
                                    ccd++;
                                }
                            }
                        }

                    }

                    byte c[] = outputStream.toByteArray( );
                    String data = bytesToHexString(c);
                    String info = new String(c);
                    Logger.t(TAG).d( data);
                    Logger.t(TAG).d( info);



                    JsonObject jsonObject = new JsonParser().parse(info).getAsJsonObject();
                    String CardType = jsonObject.has("CardType") ? jsonObject.get("CardType").getAsString() : "unknown";
                    if(CardType.equals("Card")){
                        CardData cd = new CardData(bytesToHexString(tagId));
                    }else if(CardType.equals("EmployeeCard")){
                        EmployeeCardData cd = new EmployeeCardData(bytesToHexString(tagId));
                        cd.parseJsonToMe(info);
                        Logger.d(cd.IDNumber + " " + cd.LastName);
                    }


                }
                mif.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static String getAlphaNumericString(int n)
    {

        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString
                = new String(array, Charset.forName("US-ASCII"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < randomString.length(); k++) {

            char ch = randomString.charAt(k);

            if (((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9'))
                    && (n > 0)) {

                r.append(ch);
                n--;
            }
        }

        // return the resultant string
        return r.toString();
    }

    private boolean bruteAuthenticateSectorWithKeyA(MifareClassic mif, int sectorNumber) throws IOException {
        if (mif.authenticateSectorWithKeyA(sectorNumber, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
            return true;
        } else if (mif.authenticateSectorWithKeyA(sectorNumber, MifareClassic.KEY_DEFAULT)) {
            return true;
        } else if (mif.authenticateSectorWithKeyA(sectorNumber,MifareClassic.KEY_NFC_FORUM)) {
            return true;
        } else {
            Logger.d( "Authorization denied for sector " + sectorNumber);
            return false;
        }
    }
    public static byte[][] divideArray(byte[] source, int chunksize) {

        byte[][] ret = new byte[(int)Math.ceil(source.length / (double)chunksize)][chunksize];

        int start = 0;

        for(int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(source,start, start + chunksize);
            start += chunksize ;
        }

        return ret;
    }

    protected void onNewIntentx(Intent intent) {
        Logger.t(TAG).d("onNewIntent: "+intent.getAction());
        Tag tag;

        Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();
        byte[] tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        Logger.t(TAG).d(bytesToHexString(tagId));

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) { //todo: make this work
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag != null) {
                Ndef ndef = Ndef.get(tag);
                Logger.d(ndef.getType());
                Logger.d(ndef.getTag());


                NdefMessage[] messages = getNdefMessages(intent);
                        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (messages != null) {
                    for (int i = 0; i < messages.length; i++) {
                        NdefMessage message = messages[i];
                        NdefRecord[] records = message.getRecords();
                        for (int j = 0; j < records.length; j++) {
                            NdefRecord record = records[j];
                            // TODO Process the individual records.
                            String text = new String(record.getPayload());
                            Logger.t(TAG).d(text);
                        }
                    }
                }

                JSONObject json = new JSONObject();
                try {
                    json.put("TagType","Employee");
                    JSONObject employee = new JSONObject();
                    employee.put("EmployeeID", "20000001");
                    employee.put("FirstName", "John");
                    employee.put("LastName", "Reese");

/*
                    JSONObject eData = new JSONObject();
                    eData.put("UpdateFingerprint", 1);
                    SimpleDateFormat format = new SimpleDateFormat("Z");
                    Date date = new Date();
                    eData.put("LastScanned", "/Date(" + String.valueOf(date.getTime()) + format.format(date) + ")/");
                    eData.put("LastTerminal", "UNKNOWN-000");

                    employee.put("eData", eData);
                    */
                    json.put("employee", employee);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //JSONObject mainObject = new JSONObject(Your_Sring_data);
                //JSONObject uniObject = mainObject.getJSONObject("university");
                //String  uniName = uniObject.getString("name");
               // String uniURL = uniObject.getString("url");
                NdefRecord mimeRecord = NdefRecord.createMime("text/plain", json.toString().getBytes(Charset.forName("US-ASCII")));
                writeMessage(tag,new NdefMessage(mimeRecord));
               // writeToNfc(ndef, json.toString());
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
        }else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Logger.t(TAG).d("Detected Unformatted tag... ");
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefFormatable format = NdefFormatable.get(tag);

            JSONObject json = new JSONObject();


            try {


                NfcA nfcA = NfcA.get(tag);
                if (nfcA != null) {
                    try {
                        nfcA.connect();
                        nfcA.transceive(new byte[] {
                                (byte)0xA2,  // WRITE
                                (byte)0x03,  // page = 3
                                (byte)0xE1, (byte)0x10, (byte)0x06, (byte)0x00  // capability container (mapping version 1.0, 48 bytes for data available, read/write allowed)
                        });
                        nfcA.transceive(new byte[] {
                                (byte)0xA2,  // WRITE
                                (byte)0x04,  // page = 4
                                (byte)0x03, (byte)0x00, (byte)0xFE, (byte)0x00  // empty NDEF TLV, Terminator TLV
                        });
                    } catch (Exception e) {
                    } finally {
                        try {
                            nfcA.close();
                            Logger.d("format success");
                        } catch (Exception e) {
                        }
                    }
                }

                json.put("TagType","ESR-Blank");

                NdefRecord mimeRecord = NdefRecord.createMime("text/plain", json.toString().getBytes(Charset.forName("US-ASCII")));

                 writeMessage(tag,new NdefMessage(mimeRecord));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



    }


    private void writeMessage(Tag tag, NdefMessage ndefMessage)
    {
        try
        {
            if (tag == null)
            {
                Logger.d("An Error has Occurred, Please Try Again");
            }

            Ndef ndef = Ndef.get(tag);

            if (ndef == null)
            {
                formatTag(tag, ndefMessage);
            }
            else
            {
                ndef.connect();

                if (!ndef.isWritable())
                {
                    Logger.d("Tag is not Writable");
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
            }
        }
        catch (Exception e)
        {
            Logger.e("writeMessage", e.getMessage());
        }
    }

    private void formatTag(Tag tag, NdefMessage ndefMessage)
    {
        NdefFormatable ndefFormatable = NdefFormatable.get(tag);
        if (tag == null)
        {
            Logger.d("Tag is not NDEF formatable");
            return;
        }
        if (ndefFormatable == null)
        {
            Logger.d("Tag is not NDEF formatable");
            return;
        }

        try
        {
            ndefFormatable.connect();
            ndefFormatable.format(new NdefMessage(NdefRecord.createTextRecord("en", "ABCD")));
            ndefFormatable.close();
            Logger.d("Tag formatted successfuly!");

            /*
            Intent nextActivity = new Intent(MainActivity.this, MainActivity.class);
            startActivityForResult(nextActivity, 0);
            MainActivity.this.finish();
            */

        }
        catch (Exception e)
        {
            Logger.e("formatTag: ", e.getMessage());
            e.printStackTrace();
        }


    }
    private static NdefMessage getTestMessage() {
        byte[] mimeBytes = "application/com.android.cts.verifier.nfc"
                .getBytes(Charset.forName("US-ASCII"));
        byte[] id = new byte[] {1, 3, 3, 7};
        byte[] payload = "CTS Verifier NDEF Push Tag".getBytes(Charset.forName("US-ASCII"));
        return new NdefMessage(new NdefRecord[] {
                new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, id, payload)
        });
    }
    private NdefMessage[] getNdefMessages(Intent intent) {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages != null) {
            NdefMessage[] messages = new NdefMessage[rawMessages.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = (NdefMessage) rawMessages[i];
            }
            return messages;
        } else {
            return null;
        }
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

}
