package ph.esrconstruction.esrsys.esrsysmobile.data

import android.annotation.SuppressLint
import android.util.Base64
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.orhanobut.logger.Logger
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ph.esrconstruction.esrsys.esrsysmobile.ESRSys
import ph.esrconstruction.esrsys.esrsysmobile.data.model.LoggedInUser
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

import com.android.volley.VolleyError



/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun asyncLogin(username: String, password: String, myCallback: (result: Result<LoggedInUser>) -> Unit) {
        // perform some network work
        // on network finished
        if (ESRSys.getServer().serverConnected?.value!!) {
            val url_api_get = ESRSys.getBaseURL() + "_invoke/login"

            val encodedCredentials = String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", username, password).toByteArray(), Base64.NO_WRAP))


            val queue = Volley.newRequestQueue(ESRSys.getInstance())

            try {
                // TODO: handle loggedInUser authentication
                //val fakeUser = LoggedInUser(UUID.randomUUID().toString(), "Jane Doe")
                //Result.Success(fakeUser)
                val stringRequest = object : JsonObjectRequest(Request.Method.POST, url_api_get, null,
                        { response ->
                            Logger.d(response)
                            var d = response.get("d")
                            if(d is JSONObject){
                                d = response.getJSONObject("d")
                                Logger.d(d)
                                var xusername:String = d?.optString("UserName") ?: ""
                                var xemail:String = d?.optString("Email") ?: ""
                                var xtoken:String = d?.optString("Token") ?: ""
                                Logger.d(xusername + "," + xemail + "," + xtoken)


                                var realUser:LoggedInUser = LoggedInUser()
                                var realm:Realm = Realm.getInstance(ESRSys.getEsrConfig())
                                try {
                                    realm.executeTransaction {
                                        // Add a person
                                        var temp = realm.where<LoggedInUser>(LoggedInUser::class.java).equalTo("userId", username)

                                        realUser = if (temp.count() > 0) temp.findFirst()!! else realm.createObject(LoggedInUser::class.java, username)

                                            realUser.displayName = xusername
                                            realUser.token = xtoken
                                            realUser.encodedCredentials = encodedCredentials


                                    }

                                } catch (e: Throwable) {
                                }finally {


                                    realm.close()
                                }

                                myCallback.invoke(Result.Success(realUser))


                            } else {
                                Logger.d("login failed....")
                                myCallback.invoke(Result.Error(IOException("Login Failed...")))
                            }

                        }, { error ->
                    Logger.d("is it me?")
                    Logger.d(error.message)
                    myCallback.invoke(Result.Error(IOException(error.message)))
                    // System.out.println(error.getMessage());
                }) {

                    override fun getBodyContentType(): String {
                        return "application/json"
                    }


                    @Throws(AuthFailureError::class)
                    override fun getBody(): ByteArray {
                        val params2 = HashMap<String, String>()
                        params2.put("username",username )
                        params2.put("password", password)
                        params2.put("createPersistentCookie", "true")
                        return JSONObject(params2).toString().toByteArray()
                    }


                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> { //todo: make this work
                        val headers = HashMap<String, String>()
                        //headers.put("Content-Type", "application/json");
                        val key = "Authorization"
                        // String encodedString = Base64.encodeToString(String.format("%s:%s", "mnrevelo", "mX111401!!").getBytes(), Base64.NO_WRAP);
                        //String value = String.format("Basic %s", encodedString);
                        headers["Accept"] = "application/json"
                        Logger.d(encodedCredentials)
                        headers[key] = encodedCredentials
                        return headers
                    }
                }
                queue.add(stringRequest)
            } catch (e: Throwable) {
                myCallback.invoke(Result.Error(IOException("Error logging in", e)))
            } finally {
                //
            }

        }

    }


    fun ruser(userId : String?): LoggedInUser? {
        var loggedInUser: LoggedInUser? = if (userId != null) {
            var realm:Realm = Realm.getInstance(ESRSys.getEsrConfig())

            var lll: LoggedInUser? = null
            realm.executeTransaction { inRealm ->
                inRealm.where<LoggedInUser>(LoggedInUser::class.java).equalTo("userId", userId).findFirst()
            }
            realm.close()
            return lll

        } else null
        return loggedInUser
    }




    fun asyncLogout(myCallback: (result: Boolean) -> Boolean) {
        // perform some network work
        // on network finished
        if (ESRSys.getServer().serverConnected?.value!! && ESRSys.getInstance().currentLogin.isLoggedIn) {
            val url_api_get = ESRSys.getBaseURL() + "_invoke/login"
            var user = ESRSys.getInstance().currentLogin.user


            var username = user!!.userId
            var password = "token:" + user.token

            val encodedCredentials = String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", username, password).toByteArray(), Base64.NO_WRAP))


            val queue = Volley.newRequestQueue(ESRSys.getInstance())
            try {
                // TODO: handle loggedInUser authentication
                //val fakeUser = LoggedInUser(UUID.randomUUID().toString(), "Jane Doe")
                //Result.Success(fakeUser)


                val stringRequest = object : StringRequest(Request.Method.POST, url_api_get,
                        Response.Listener(function = fun(response: String) {
                            Logger.d(response)
                            myCallback.invoke(true)
                        }),
                        Response.ErrorListener {
                            error ->
                            Logger.d("is it me?")
                            Logger.d(error.message)
                            myCallback.invoke(false)
                        }) {

                    @Throws(AuthFailureError::class)
                    override fun getBody(): ByteArray {
                        val params2 = HashMap<String, String>()
                        params2.put("username", username)
                        params2.put("password", password)
                        params2.put("createPersistentCookie", "true")
                        return JSONObject(params2).toString().toByteArray()
                    }


                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> { //todo: make this work
                        val headers = HashMap<String, String>()
                        //headers.put("Content-Type", "application/json");
                        val key = "Authorization"
                        // String encodedString = Base64.encodeToString(String.format("%s:%s", "mnrevelo", "mX111401!!").getBytes(), Base64.NO_WRAP);
                        //String value = String.format("Basic %s", encodedString);
                        headers["Accept"] = "application/json"
                        Logger.d(encodedCredentials)
                        headers[key] = encodedCredentials
                        return headers
                    }
                }



                queue.add(stringRequest)
            } catch (e: Throwable) {
                myCallback.invoke(false)
            } finally {
               // realm.close()
            }


        }
    }
}

