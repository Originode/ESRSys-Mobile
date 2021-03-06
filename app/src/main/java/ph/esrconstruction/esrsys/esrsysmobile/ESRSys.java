package ph.esrconstruction.esrsys.esrsysmobile;

import android.Manifest;
import android.app.Application;
import io.realm.Realm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import ph.esrconstruction.esrsys.esrsysmobile.data.LoginDataSource;
import ph.esrconstruction.esrsys.esrsysmobile.data.LoginRepository;
import ph.esrconstruction.esrsys.esrsysmobile.data.model.DeviceSettings;
import ph.esrconstruction.esrsys.esrsysmobile.network.ConnectionLiveData;
import ph.esrconstruction.esrsys.esrsysmobile.network.ESRServer;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.modules.ESRModules;
import ph.esrconstruction.esrsys.esrsysmobile.services.ESREmployeeRealmSyncService;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import com.android.volley.Request;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.suprema.BioMiniFactory;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;
import com.telpo.tps550.api.fingerprint.FingerPrint;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class ESRSys extends Application implements LifecycleOwner {

    public static ESRServer devServer = new ESRServer( 1,"ESR Dev", "http","10.0.0.16","51114",getInstance());
    public static ESRServer localServer = new ESRServer(2,"ESR Local","http","10.0.0.13","82",getInstance());
    public static ESRServer remoteServer = new ESRServer(3,"ESR Remote","http","119.93.149.10","82",getInstance());
    public static ESRServer offlineServer = new ESRServer(4,"Offline","http","localhost","80",getInstance());

    public LoginRepository currentLogin;

    /////////////////dev mode
    public static Boolean devMode = true;

    private static BioMiniFactory mBioMiniFactory = null;
    public static final int REQUEST_WRITE_PERMISSION = 786;
    public IBioMiniDevice mCurrentDevice = null;

    private IBioMiniDevice.CaptureOption mCaptureOptionDefault = new IBioMiniDevice.CaptureOption();

    //Flag.
    public static final boolean mbUsbExternalUSBManager = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent= null;
    //

    private static RealmConfiguration esrConfig;

    public static String getBaseURL() {

        return getServer().getURL();
    }


    public static ESRServer getServer() {

       // Logger.d("isDevOnline:" + devServer.getServerConnected().getValue().toString() + "," + devMode.toString());
        return (devServer.getServerConnected() && devMode) ? devServer : (localServer.getServerConnected() ? localServer : (remoteServer.getServerConnected() ? remoteServer : offlineServer));
    }

    /**
     * Log or request TAG
     */
    public static final String TAG = "ESRSys";

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static ESRSys sInstance;


    private Boolean IsNetworkAvailable = false;
    public static final int MobileData = 2;
    public static final int WifiData = 1;

    public DeviceSettings deviceSettings;
    private Realm DeviceSettingsRealm;

    private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    public ConnectionLiveData connectionLiveData;

    public static RealmConfiguration getEsrConfig() {
        if(esrConfig == null){

            Realm.init(getInstance());
            setEsrConfig(new RealmConfiguration.Builder()
                    .name("esrdb-v011.realm")
                    .modules(Realm.getDefaultModule(), new ESRModules())
                    .deleteRealmIfMigrationNeeded()
                    .build());
        }

        return esrConfig;
    }

    public static void setEsrConfig(RealmConfiguration esrConfig) {
        ESRSys.esrConfig = esrConfig;
    }




    public void startEmployeeSyncService(String updateMethod){

        // use this to start and trigger a service
        Intent iEmployee = new Intent(this, ESREmployeeRealmSyncService.class);
// potentially add data to the intent
        iEmployee.putExtra("updateMethod", updateMethod);
        this.startService(iEmployee);
    }

    private String getPhone() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            List<SubscriptionInfo> subscription = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
            if(subscription != null && subscription.size()>0){
                for (int i = 0; i < subscription.size(); i++) {
                    SubscriptionInfo info = subscription.get(i);
                    Logger.t(TAG).i( "number " + info.getNumber());
                    Logger.t(TAG).i( "network name : " + info.getCarrierName());
                    Logger.t(TAG).i( "country iso " + info.getCountryIso());
                }
                return subscription.get(0).getNumber();
            }else{
                TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                return phoneMgr.getLine1Number();
            }
        }else{
            TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            return phoneMgr.getLine1Number();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag("ESRSys")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        Logger.t(TAG).i("ESRSys initiating");

        // initialize the singleton
        sInstance = this;




        //device settings
        initDevSettings();
        ///////////////
        setDevSettingsSimNumber(getPhone());
        Logger.t(TAG).i("phone number = " + getPhone());


        currentLogin = new LoginRepository(new LoginDataSource());



       /*
        connectionLiveData = new ConnectionLiveData(getApplicationContext());
        connectionLiveData.observeForever(connection -> {
            //Logger.t(TAG).d("Network statex");
            devServer.ping();
            localServer.ping();
            remoteServer.ping();
        });

        devServer.getServerConnected().observe(this, serverConnected -> {
            Logger.d("server " + (serverConnected ? "online" : "offline"));

        });
        localServer.getServerConnected().observe(this, serverConnected -> {
            Logger.d("server " + (serverConnected ? "online" : "offline"));

        });
        remoteServer.getServerConnected().observe(this, serverConnected -> {
            Logger.d("server " + (serverConnected ? "online" : "offline"));

        });
*/



        periodicPingHandler.post(periodicPingUpdate);




        /////////////
        FingerPrint.fingerPrintPower(1);
        mCaptureOptionDefault.frameRate = IBioMiniDevice.FrameRate.SHIGH;

        if(mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }



        restartBioMini();

        startEmployeeSyncService("first time");


    }


    void restartBioMini() {
        if(mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }
        if( mbUsbExternalUSBManager ){
            mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
            mBioMiniFactory = new BioMiniFactory(this, mUsbManager){
                @Override
                public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                    Logger.d("----------------------------------------");
                    Logger.d("onDeviceChange : " + event + " using external usb-manager");
                    Logger.d("----------------------------------------");
                    handleDevChange(event, dev);
                }
            };
            //
            mPermissionIntent = PendingIntent.getBroadcast(this,0,new Intent(ACTION_USB_PERMISSION),0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(mUsbReceiver, filter);
            checkDevice();
        }else {
            mBioMiniFactory = new BioMiniFactory(this) {
                @Override
                public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                    Logger.d("----------------------------------------");
                    Logger.d("onDeviceChange : " + event);
                    Logger.d("----------------------------------------");
                    handleDevChange(event, dev);
                }
            };
        }
        //mBioMiniFactory.setTransferMode(IBioMiniDevice.TransferMode.MODE2);
    }
    void handleDevChange(IUsbEventHandler.DeviceChangeEvent event, Object dev) {
        if (event == IUsbEventHandler.DeviceChangeEvent.DEVICE_ATTACHED && mCurrentDevice == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int cnt = 0;
                    while (mBioMiniFactory == null && cnt < 20) {
                        SystemClock.sleep(1000);
                        cnt++;
                    }
                    if (mBioMiniFactory != null) {
                        mCurrentDevice = mBioMiniFactory.getDevice(0);
                        Log.d(TAG, "mCurrentDevice attached : " + mCurrentDevice);
                        if (mCurrentDevice != null) {
                            Logger.d(" DeviceName : " + mCurrentDevice.getDeviceInfo().deviceName);
                            Logger.d("         SN : " + mCurrentDevice.getDeviceInfo().deviceSN);
                            Logger.d("SDK version : " + mCurrentDevice.getDeviceInfo().versionSDK);

                        }
                    }
                }
            }).start();
        } else if (mCurrentDevice != null && event == IUsbEventHandler.DeviceChangeEvent.DEVICE_DETACHED && mCurrentDevice.isEqual(dev)) {
            Log.d(TAG, "mCurrentDevice removed : " + mCurrentDevice);
            mCurrentDevice = null;
        }
    }

    public void initDevSettings(){
        //device settings
        DeviceSettingsRealm = Realm.getInstance(getEsrConfig());
        DeviceSettingsRealm.executeTransaction(inRealm -> {
            if (inRealm.where(DeviceSettings.class).count() > 0) {
                Logger.i("loading device settings");
                deviceSettings = inRealm.where(DeviceSettings.class).findFirst();
            } else {
                Logger.i("creating new device settings...");
                deviceSettings = inRealm.createObject(DeviceSettings.class, 1);
            }
        });
    }
    public void setDevSettingsCachedUser(String user){
        //device settings
        DeviceSettingsRealm = Realm.getInstance(getEsrConfig());
        if(deviceSettings == null) initDevSettings();
        DeviceSettingsRealm.executeTransaction(inRealm -> {
                deviceSettings.setCachedUser(user);
        });
    }
    public void setDevSettingsSimNumber(String sim){
        //device settings
        DeviceSettingsRealm = Realm.getInstance(getEsrConfig());
        if(deviceSettings == null) initDevSettings();
        DeviceSettingsRealm.executeTransaction(inRealm -> {
            deviceSettings.setSimNumber(sim);
        });
    }
    public void setDevSettingsAutoSync(Boolean async){
        //device settings
        DeviceSettingsRealm = Realm.getInstance(getEsrConfig());
        if(deviceSettings == null) initDevSettings();
        DeviceSettingsRealm.executeTransaction(inRealm -> {
            deviceSettings.setAutoSync(async);
        });
    }
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(ACTION_USB_PERMISSION.equals(action)){
                synchronized(this){
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                        if(device != null){
                            if( mBioMiniFactory == null) return;
                            mBioMiniFactory.addDevice(device);
                            Logger.d(String.format(Locale.ENGLISH ,"Initialized device count- BioMiniFactory (%d)" , mBioMiniFactory.getDeviceCount() ));
                        }
                    }
                    else{
                        Log.d(TAG, "permission denied for device"+ device);
                    }
                }
            }
        }
    };
    public void checkDevice(){
        if(mUsbManager == null) return;
        Logger.d("checkDevice");
        HashMap<String , UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIter = deviceList.values().iterator();
        while(deviceIter.hasNext()){
            UsbDevice _device = deviceIter.next();
            if( _device.getVendorId() ==0x16d1 ){
                //Suprema vendor ID
                mUsbManager.requestPermission(_device , mPermissionIntent);
            }else{
            }
        }

    }


    Handler periodicPingHandler = new Handler();
    private Runnable periodicPingUpdate = new Runnable() {
        @Override
        public void run() {
            periodicPingHandler.postDelayed(periodicPingUpdate, 10*1000 - SystemClock.elapsedRealtime()%1000);

            if(devMode){
                devServer.ping();
            }
//////


           // if(localServer.getServerConnected().getValue())
                localServer.ping();

                remoteServer.ping();

        }
    };

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized ESRSys getInstance() {
        return sInstance;
    }
    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param
     * @param
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        //Logger.i(req.toString());
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }



    @NonNull
    /* required to make activity life cycle owner */
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}
