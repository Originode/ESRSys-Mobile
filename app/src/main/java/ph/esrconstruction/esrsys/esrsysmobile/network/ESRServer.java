package ph.esrconstruction.esrsys.esrsysmobile.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.orhanobut.logger.Logger;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class ESRServer {
    public int id = 0;
    public String name = "ESR";
    public String ip = "10.0.0.162";
    public String port = "53053";
    public String protocol = "http";
    public URL url;

    public String net = "NO_CONNECTION";
    public String host = "";

    public int dns = Integer.MAX_VALUE;
    public int cnt = Integer.MAX_VALUE;
    private Context context;




    public ESRServer(int Id, String Name, String Protocol, String Ip, String Port,Context context){
        this.id = Id;
        this.name = Name;
        this.ip = Ip;
        this.port = Port;
        this.protocol = Protocol;
        this.context = context;
        this.serverConnected = false;
        try {
            this.url = new URL(this.getURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    public String getURL(){
        return this.protocol + "://" + this.ip + ":" + this.port + "/";
    }
    public ESRServer ping(){
            //Logger.d("pinging " + this.name + " : " +  url.getHost() + " : "+ this.getURL() + " : "+ this.url.getPort());

            new Ping(this).execute();
        return this;
    }



    private Boolean serverConnected = false;


    public Boolean getServerConnected() {
        return serverConnected;
    }
    public void setServerConnected(Boolean x){
        serverConnected = x;
    }


    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    @Nullable
    public static String getNetworkType(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return activeNetwork.getTypeName();
        }
        return null;
    }
}

