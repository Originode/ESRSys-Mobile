package ph.esrconstruction.esrsys.esrsysmobile.data

import com.orhanobut.logger.Logger
import io.realm.Realm
import ph.esrconstruction.esrsys.esrsysmobile.ESRSys
import ph.esrconstruction.esrsys.esrsysmobile.data.model.DeviceSettings
import ph.esrconstruction.esrsys.esrsysmobile.data.model.LoggedInUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {


    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    var realm: Realm = Realm.getInstance(ESRSys.getEsrConfig())

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null

        //find cached stuff
        Logger.i("fooock")

        //get stored device settings
        var deviceSettings:DeviceSettings
        val rr = arrayOf("")

        /*
        realm = Realm.getInstance(ESRSys.getEsrConfig())
        realm.executeTransaction { inRealm ->
            if (inRealm.where(DeviceSettings::class.java).count() > 0) {
                Logger.i("loading device settings")
                deviceSettings = inRealm.where(DeviceSettings::class.java).findFirst()!!
            } else {
                Logger.i("creating new device settings...")
                deviceSettings = inRealm.createObject(DeviceSettings::class.java, 1)
            }
            rr[0] = deviceSettings.cachedUser
            Logger.i(rr[0])
            if(rr[0] != ""){

                var r = inRealm.where<LoggedInUser>(LoggedInUser::class.java).equalTo("userId", rr[0])
                Logger.i(r.count().toString())
                if(r.count()>0){
                    user = r.findFirst()
                    Logger.d("cached user: " + (user?.userId ?: ""))
                    Logger.d("cached token: " + (user?.token ?: ""))

                    var username = (user?.userId ?: "")
                    var token = (user?.token ?: "")
                    Logger.d("xxxx" + ESRSys.getServer().serverConnected)

                    if (username != "" && token != "" && ESRSys.getServer().serverConnected) {
                        Logger.d("server online... executing auto login...")

                        //todo: zzzzzzz
                    }

                }

            }
        }
        // realm.close()

         */

        realm = Realm.getInstance(ESRSys.getEsrConfig())
        realm.executeTransaction { inRealm ->

            rr[0] = ESRSys.getInstance().deviceSettings.cachedUser;
            Logger.i(rr[0])
            if(rr[0] != ""){

                var r = inRealm.where<LoggedInUser>(LoggedInUser::class.java).equalTo("userId", rr[0])
                Logger.i(r.count().toString())
                if(r.count()>0){
                    user = r.findFirst()
                    Logger.d("cached user: " + (user?.userId ?: ""))
                    Logger.d("cached token: " + (user?.token ?: ""))

                    var username = (user?.userId ?: "")
                    var token = (user?.token ?: "")
                    Logger.d("xxxx" + ESRSys.getServer().serverConnected)

                    if (username != "" && token != "" && ESRSys.getServer().serverConnected) {
                        Logger.d("server online... executing auto login...")

                       //not needed? dunno
                    }

                }

            }
        }
        // realm.close()

    }

    fun asyncAutoLogin(myCallback: (result: Result<LoggedInUser>) -> Unit) {
        // handle login


        //get stored device settings
        var deviceSettings:DeviceSettings
        val rr = arrayOf("")
        val realm = Realm.getInstance(ESRSys.getEsrConfig())
        realm.executeTransaction { inRealm ->
            rr[0] = ESRSys.getInstance().deviceSettings.cachedUser;
            Logger.i(rr[0])
            if(rr[0] != ""){

                var r = inRealm.where<LoggedInUser>(LoggedInUser::class.java).equalTo("userId", rr[0])
                Logger.i(r.count().toString())
                if(r.count()>0){
                    user = r.findFirst()
                    Logger.d("cached user: " + (user?.userId ?: ""))
                    Logger.d("cached token: " + (user?.token ?: ""))

                    var username = (user?.userId ?: "")
                    var token = (user?.token ?: "")
                    Logger.d("xxxx" + ESRSys.getServer().serverConnected)

                    if (username != "" && token != "" && ESRSys.getServer().serverConnected) {
                        Logger.d("server online... executing auto login...")

                        ////////////////*

                        //val value = String.format("Basic %s", encodedString)
                        dataSource.asyncLogin(username, "token:"+token, myCallback = {

                            if (it is Result.Success) {

                                setLoggedInUser(it.data)
                                Logger.d("autologin success: " + it.data.displayName)
                                //save cached username

                                ESRSys.getInstance().setDevSettingsCachedUser(it.data.userId);

                                /*
                                val realm = Realm.getInstance(ESRSys.getEsrConfig())
                                realm.executeTransaction { inRealm ->
                                    var deviceSettings = DeviceSettings()
                                    if(inRealm.where(DeviceSettings::class.java).count() > 0){
                                        deviceSettings = inRealm.where(DeviceSettings::class.java).findFirst()!!
                                    }
                                    deviceSettings.cachedUser = it.data.userId
                                }
                                realm.close()
                                */

                            }else{
                                Logger.d(it.toString())
                            }
                            myCallback.invoke(it)
                        })
                    }

                }

            }
        }
        // realm.close()

    }


    fun asyncLogout(myCallback: (result: Boolean) -> Boolean) {
        Logger.d("initiate logout")

        dataSource.asyncLogout(myCallback = {
            Logger.d("finish logout")
            if(it) {
                //remove cached user
                try {
                    ESRSys.getInstance().setDevSettingsCachedUser("");

                    /*
                    realm.executeTransaction { inRealm ->
                        user?.deleteFromRealm()

                        user = null
                            var deviceSettings = DeviceSettings()
                            if(inRealm.where(DeviceSettings::class.java).count() > 0){
                                deviceSettings = inRealm.where(DeviceSettings::class.java).findFirst()!!
                            }
                            deviceSettings.cachedUser = ""
                    }
                    *
                     */
                }catch (e: Throwable) {
                }finally {
                    //realm.close()
                }
                //user = null
            }
            myCallback(it)
        })


    }
    fun asyncLogin(username: String, password: String, myCallback: (result: Result<LoggedInUser>) -> Unit) {
        // handle login
        realm = Realm.getInstance(ESRSys.getEsrConfig())

        //val value = String.format("Basic %s", encodedString)
        dataSource.asyncLogin(username, password, myCallback = {

            if (it is Result.Success) {

                setLoggedInUser(it.data)

                ESRSys.getInstance().setDevSettingsCachedUser(it.data.userId);

                /*
                //save cached username
                realm.executeTransaction { inRealm ->
                    var deviceSettings = DeviceSettings()
                    if(inRealm.where(DeviceSettings::class.java).count() > 0){
                        deviceSettings = inRealm.where(DeviceSettings::class.java).findFirst()!!
                    }
                    deviceSettings.cachedUser = it.data.userId
                }

                 */
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
