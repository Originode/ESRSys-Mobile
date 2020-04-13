package ph.esrconstruction.esrsys.esrsysmobile;

import android.Manifest;
import android.app.Application;
import io.realm.Realm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

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

import java.util.List;


public class ESRSys extends Application implements LifecycleOwner {

    public static ESRServer devServer = new ESRServer( "ESR Dev", "http","10.0.0.16","65012",getInstance());
    public static ESRServer localServer = new ESRServer("ESR Local","http","10.0.0.13","82",getInstance());
    public static ESRServer remoteServer = new ESRServer("ESR Remote","http","119.93.149.10","82",getInstance());
    public static ESRServer offlineServer = new ESRServer("Offline","http","localhost","80",getInstance());

    public LoginRepository currentLogin;

    /////////////////dev mode
    public static Boolean devMode = false;



    private static RealmConfiguration esrConfig;

    public static String getBaseURL() {

        return getServer().getURL();
    }


    public static ESRServer getServer() {

        //Logger.d("isDevOnline:" + devServer.getServerConnected().getValue().toString() + "," + devMode.toString());
        return (devServer.getServerConnected().getValue() && devMode) ? devServer : (localServer.getServerConnected().getValue() ? localServer : (remoteServer.getServerConnected().getValue() ? remoteServer : offlineServer));
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

    private static DeviceSettings deviceSettings;


    private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
    public ConnectionLiveData connectionLiveData;

    public static RealmConfiguration getEsrConfig() {
        if(esrConfig == null){

            Realm.init(getInstance());
            setEsrConfig(new RealmConfiguration.Builder()
                    .name("esrdb-v010.realm")
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

        Logger.t(TAG).i("phone number = " + getPhone());


        currentLogin = new LoginRepository(new LoginDataSource());



        /* Live data object and setting an oberser on it */
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



        periodicPingHandler.post(periodicPingUpdate);

        startEmployeeSyncService("first time");


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
