package ph.esrconstruction.esrsys.esrsysmobile.network;

import android.os.AsyncTask;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

import ph.esrconstruction.esrsys.esrsysmobile.ESRSys;
import ph.esrconstruction.esrsys.esrsysmobile.events.ServerMessageEvent;

class Ping extends AsyncTask<Void,Void,Boolean> {


    private ESRServer mServer;
    public  Ping(ESRServer server) { mServer = server; }

    @Override protected Boolean doInBackground(Void ... voids) { try {
        mServer.net = mServer.getNetworkType(ESRSys.getInstance());
        String hostAddress;
        long start = System.currentTimeMillis();
        hostAddress = InetAddress.getByName(mServer.url.getHost()).getHostAddress();
       // Logger.d(hostAddress);
        long dnsResolved = System.currentTimeMillis();
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(mServer.url.getHost(), mServer.url.getPort()), 1500);
        socket.close();
        long probeFinish = System.currentTimeMillis();
        mServer.dns = (int) (dnsResolved - start);
        mServer.cnt = (int) (probeFinish - dnsResolved);
        mServer.host = mServer.url.getHost();
        mServer.ip = hostAddress;
        //Logger.d("ping ! " + mServer.host + " ! " + mServer.net);
        return mServer.net != "NO_CONNECTION";
    } catch (IOException e) { return false; } }

    @Override protected void onPostExecute(Boolean internet) {
        if(Objects.requireNonNull(mServer.getServerConnected()) != internet){
            mServer.setServerConnected(internet);
            EventBus.getDefault().post(new ServerMessageEvent(mServer, internet ? ServerMessageEvent.Messages.ServerOnline : ServerMessageEvent.Messages.ServerOffline, internet));
        }
    }
}
