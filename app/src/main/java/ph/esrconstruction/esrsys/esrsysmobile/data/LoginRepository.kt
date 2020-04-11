package ph.esrconstruction.esrsys.esrsysmobile.data

import android.annotation.SuppressLint
import android.util.Base64
import androidx.annotation.NonNull
import com.orhanobut.logger.Logger
import io.realm.Realm
import io.realm.RealmConfiguration
import org.json.JSONException
import org.json.JSONObject
import ph.esrconstruction.esrsys.esrsysmobile.ESRSys
import ph.esrconstruction.esrsys.esrsysmobile.data.model.LoggedInUser
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import kotlin.math.log

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    var realm : Realm = Realm.getInstance(ESRSys.getEsrConfig())
        private set

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set


    fun ruser(): LoggedInUser? {
            return dataSource.ruser(user?.userId)
    }




    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun asyncLogout(myCallback: (result: Boolean) -> Boolean) {
        Logger.d("initiate logout")
        dataSource.asyncLogout(myCallback = {
            Logger.d("finish logout")
            if(it) user = null
            myCallback(it)
        })

    }
    fun asyncLogin(username: String, password: String, myCallback: (result: Result<LoggedInUser>) -> Unit) {
        // handle login


        //val value = String.format("Basic %s", encodedString)
        dataSource.asyncLogin(username, password, myCallback = {

            if (it is Result.Success) {

                setLoggedInUser(it.data)
            }
            myCallback.invoke(it)
        })


    }



    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser


        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore


    }
}
