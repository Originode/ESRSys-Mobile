package ph.esrconstruction.esrsys.esrsysmobile.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import android.os.SystemClock;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.orhanobut.logger.Logger;

import android.graphics.Bitmap;


import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import ph.esrconstruction.esrsys.esrsysmobile.ESRSys;
import ph.esrconstruction.esrsys.esrsysmobile.data.model.LoggedInUser;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;

import static java.lang.Math.max;


public class ESREmployeeRealmSyncService extends Service {

   
    int imageLoaderCount = 0;



    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    public static final String TAG = "Employeesync";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public class LocalBinder extends Binder {
        ESREmployeeRealmSyncService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ESREmployeeRealmSyncService.this;
        }
    }


    public void employeeUpdate(){



        //get login credentials
        Realm realme = Realm.getInstance(ESRSys.getEsrConfig());

        final String[] _encodedCredentials = {""};

        realme.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm inRealm) {
                Logger.i("Number of Employees in Local DB : " + inRealm.where(Employee.class).count());
                // inRealm.where<LoggedInUser>(LoggedInUser::class.java).equalTo("userId", userId).findFirst()

                LoggedInUser u = ESRSys.getInstance().currentLogin.getUser();

                if(u != null){
                    LoggedInUser r = inRealm.where(LoggedInUser.class).equalTo("userId", ESRSys.getInstance().currentLogin.getUser().getUserId()).findFirst();

                    _encodedCredentials[0] = r.getEncodedCredentials();
                }else{
                    _encodedCredentials[0] = "";
                }

            }
        });
        realme.close();

        Logger.i("Server Connected : " + ESRSys.getServer().getServerConnected().getValue());
        Logger.i("Logged in : " + ESRSys.getInstance().currentLogin.isLoggedIn());
        if(ESRSys.getServer().getServerConnected().getValue() && ESRSys.getInstance().currentLogin.isLoggedIn()) {


            // Get Images and Data and Delete old data from Realm database....
            //String url_api_get = getBaseURL + "appservices/Employee/grid1?_pageIndex=2&_pageSize=10&_sortExpression=LastName";
            String url_api_get = ESRSys.getBaseURL() + "appservices/Employee/grid1?_pageSize=5000&_sortExpression=LastName";
            Logger.i("network online: " + url_api_get);
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url_api_get, null,
                    response -> {
                        Logger.d("response");
                        //Logger.i(response.toString());
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = response.getJSONArray("Employee");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        Logger.i("found " + jsonArray.length() + " employees");
                        final JSONArray finalJsonArray = jsonArray;
                        Long[] xxx = new Long[jsonArray.length()];
                        for (int i = 0; i < finalJsonArray.length(); i++) { //todo: shorten this using map
                            try {
                                final JSONObject obj = finalJsonArray.getJSONObject(i);
                                xxx[i] = obj.getLong("EmployeeID");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        final Long zzz[] = xxx;

                        // Delete old data from local database....
                        if (!jsonArray.isNull(0)) {
                            Realm realmz = Realm.getInstance(ESRSys.getEsrConfig());
                            // Delete old data from local database....
                            realmz.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(@NonNull Realm inRealm) {

                                    RealmResults<Employee> r = inRealm.where(Employee.class).not().in("EmployeeID", zzz).findAll();
                                    Logger.i("cleaning up " + r.size() + " old employee records");
                                    r.deleteAllFromRealm();
                                }
                            });
                            realmz.close();


                            realmz = Realm.getInstance(ESRSys.getEsrConfig());
                            //add new data to local database
                            realmz.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm inRealm) {
                                    int addedCount = 0;
                                    int updatedCount = 0;
                                    for (int i = 0; i < finalJsonArray.length(); i++) {

                                        JSONObject obj = null;
                                        try {
                                            obj = finalJsonArray.getJSONObject(i);
                                            // Logger.i(obj.getString("Name"));
                                            Long xid = obj.getLong("EmployeeID");
                                            Employee employee;
                                            if (inRealm.where(Employee.class).equalTo("EmployeeID", xid).findAll().size() > 0) {
                                                //update existing employee
                                                try {
                                                    employee = inRealm.where(Employee.class).equalTo("EmployeeID", xid).findFirst();
                                                    if (obj.has("LastUpdate") && obj.getString("LastUpdate")!= "null") {
                                                        String date_modified = obj.getString("LastUpdate");
                                                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                                                        Date xxxdate = dateParser.parse(date_modified);
                                                        if (employee.getLastUpdate().compareTo(xxxdate) < 0) {
                                                            // Get Data and save on Realm DB
                                                            // employee.setEmployeeID(obj.getLong("EmployeeID"));
                                                            //employee.setQuantity(obj.getInt("quantity"));
                                                            employee.setIDNumber(obj.getString("Idnumber"));
                                                            employee.setName(obj.getString("Name"));
                                                            employee.setImageLoaded(0);
                                                            employee.setLastUpdate(xxxdate);

                                                            employee.setProjectName(obj.optString("ProjectAssignmentProjectName", ""));
                                                            employee.setRoleName(obj.optString("ProjectAssignmentRoleName", ""));
                                                            employee.setStatusName(obj.optString("ProjectAssignmentStatusName", ""));

                                                            updatedCount++;
                                                        }
                                                    }

                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                //add new employee
                                                try {
                                                    employee = inRealm.createObject(Employee.class, obj.getLong("EmployeeID"));
                                                    // Get Data and save on Realm DB
                                                    // employee.setEmployeeID(obj.getLong("EmployeeID"));
                                                    //employee.setQuantity(obj.getInt("quantity"));
                                                    employee.setIDNumber(obj.getString("Idnumber"));
                                                    employee.setName(obj.getString("Name"));
                                                    employee.setImageLoaded(0);

                                                    employee.setProjectName(obj.optString("ProjectAssignmentProjectName", ""));
                                                    employee.setRoleName(obj.optString("ProjectAssignmentRoleName", ""));
                                                    employee.setStatusName(obj.optString("ProjectAssignmentStatusName", ""));


                                                    employee.setImageURL("Blob.ashx?employeeImage=t|" + employee.getEmployeeID());
                                                  //  Logger.i(obj.optString("ProjectAssignmentProjectName", ""));
                                                    if (obj.has("LastUpdate")) {
                                                        String date_modified = obj.getString("LastUpdate");

                                                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                                                        employee.setLastUpdate(dateParser.parse(date_modified));

                                                    }
                                                    addedCount++;
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                    }
                                    Logger.i(addedCount + " new employees");
                                    Logger.i(updatedCount + " updated employees");
                                }
                            });
                            realmz.close();

                        }





/*
                        String url = getBaseURL + "Blob.ashx?employeeImage=t|" + this.getEmployeeID();
                        Logger.i(url);
                        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(final Bitmap bitmap) {


                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byte[] byteArray = stream.toByteArray();
                                Image = byteArray;



                            }
                        }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Logger.e(volleyError.getMessage());
                            }
                        });


                        ESRSys.getInstance().addToRequestQueue(imageRequest);
*/


                    }, error -> {
                Logger.d(error.getMessage());
                //System.out.println(error.getMessage());
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    //headers.put("Content-Type", "application/json");
                    String key = "Authorization";
                   // String encodedString = Base64.encodeToString(String.format("%s:%s", "mnrevelo", "mX111401!!").getBytes(), Base64.NO_WRAP);
                    //String value = String.format("Basic %s", encodedString);


                    Logger.i("Server Connected : " + ESRSys.getServer().getServerConnected().getValue());
                    Logger.i("Logged in : " + ESRSys.getInstance().currentLogin.isLoggedIn());


                    headers.put("Accept", "application/json");
                  //  headers.put(key, ESRSys.getInstance().currentLogin.getRuser().getEncodedCredentials());
                    headers.put(key, _encodedCredentials[0]);
                    /*
                    realm = Realm.getInstance(ESRSys.getEsrConfig());
                    // Delete old data from local database....
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(@NonNull Realm inRealm) {

                            // inRealm.where<LoggedInUser>(LoggedInUser::class.java).equalTo("userId", userId).findFirst()
                            LoggedInUser r = inRealm.where(LoggedInUser.class).equalTo("userId", ESRSys.getInstance().currentLogin.getUser().getUserId()).findFirst();

                            headers.put(key, r.getEncodedCredentials());
                        }
                    });
                    realm.close();
                    */

                    return headers;
                }
            };
            ESRSys.getInstance().addToRequestQueue(stringRequest);
        }else{
            //Logger.i("No network");
        }

    }
    Handler periodicEmployeeHandler = new Handler();
    private Runnable periodicEmployeeUpdate = new Runnable() {
        @Override
        public void run() {
            long periodicEmployeeUpdateDelay = 120*1000;
            periodicEmployeeHandler.postDelayed(periodicEmployeeUpdate, periodicEmployeeUpdateDelay - SystemClock.elapsedRealtime()%1000);

            // whatever you want to do below

           employeeUpdate();
        }
    };

    public static Object tryToGet(JSONObject jsonObj, String key) {
        if (jsonObj.has(key))
            return jsonObj.opt(key);
        return null;
    }




    Handler periodicTestHandler = new Handler();
    private Runnable periodicTestUpdate = new Runnable() {
        @Override
        public void run() {
            periodicTestHandler.postDelayed(periodicTestUpdate, 3*1000 - SystemClock.elapsedRealtime()%1000);
            if(ESRSys.getServer().getServerConnected().getValue()) {

                final int threads = 150;

                //laze load some images
                if(imageLoaderCount < threads){



                    Realm realm = Realm.getInstance(ESRSys.getEsrConfig());
                    realm.executeTransactionAsync(inRealm -> {

                        long ddd = inRealm.where(Employee.class).count();
                        long ccc = inRealm.where(Employee.class)
                                .equalTo("ImageLoaded", 0) //
                                .and()
                                .isNotEmpty("ImageURL")
                                .and()
                                .isNotNull("ImageURL").count();

                        long eee = inRealm.where(Employee.class)
                                .equalTo("ImageLoaded", -1).count();



                        if(ccc > 0){
                            long limits = max(threads-eee,1);
                            Logger.d("lazy loading " + eee + "/" + ccc + " of " + ddd  + " employee images");
                            final RealmResults<Employee> employees = inRealm.where(Employee.class)
                                    .equalTo("ImageLoaded", 0) //
                                    .and()
                                    .isNotEmpty("ImageURL")
                                    .and()
                                    .isNotNull("ImageURL")
                                    .limit(limits)
                                    .findAll();
                            final OrderedRealmCollection<Employee> posts = employees.createSnapshot(); // <-- snapshot
                            employees.setValue("ImageLoaded", -1);
                            lazyloadImages(posts);
                        }

                    }, () -> {

                        realm.close();
                    }, error -> {
                        realm.close();
                    });




                }
            }else{
                Logger.i("waiting for network");
            }
        }
    };

    public void lazyloadImages(OrderedRealmCollection<Employee> posts){


        //Logger.i("Network available... lazy loading " + posts.size() + " images..");
        if(posts.size() > 0){


            for (int i = 0; i < posts.size(); i++) {
                final Employee employee = posts.get(i);
                String url = employee.getImageURL() != null ? employee.getImageURL() : "";
                final long idx = employee.getEmployeeID();
                if(employee.getImageURL() != null ){
                    final int finalI = i;



                    ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(final Bitmap bitmap) {

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            final byte[] byteArray = stream.toByteArray();

                            Realm realm = Realm.getInstance(ESRSys.getEsrConfig());

                            realm.executeTransactionAsync(inRealm -> {

                                Employee employeex = inRealm.where(Employee.class).equalTo("EmployeeID",idx).findFirst();
                                employeex.setImage(byteArray);
                                employeex.setImageLoaded(1);
                            }, () -> {

                                realm.close();
                                //Logger.d("Image Loaded");
                                imageLoaderCount--;
                            }, error -> {

                                realm.close();
                                imageLoaderCount--;
                                Logger.d(error.getMessage());
                            });


                        }
                    }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {


                            Realm realm = Realm.getInstance(ESRSys.getEsrConfig());
                            realm.executeTransactionAsync(inRealm -> {
                                Employee employeex = inRealm.where(Employee.class).equalTo("EmployeeID",idx).findFirst();
                                employeex.setImageLoaded(0);
                            }, () -> {
                                realm.close();
                                Logger.d("Image Load Failed");
                            }, error -> realm.close());



                            imageLoaderCount--;
                            Logger.e(volleyError.getMessage());

                        }
                    });
                    imageLoaderCount++;
                    ESRSys.getInstance().addToRequestQueue(imageRequest);
                }


            }


        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String updateMethod = "";
        if(intent != null){
            Bundle bundle = intent.getExtras();

            if (bundle.getString("updateMethod") != null) {
                updateMethod = bundle.getString("updateMethod");
            }
        }else{
        }


        Logger.t(TAG).v("Employee Sync Started...update method = " + updateMethod);


        final int[] employeeCount = {0};
        Realm realm = Realm.getInstance(ESRSys.getEsrConfig());

        realm.executeTransaction(inRealm -> {
            employeeCount[0] = inRealm.where(Employee.class).findAll().size();

        });
        realm.close();

        /*
        if(updateMethod == "quicky"){
            Logger.i("running quick update");
            employeeUpdate();
        }else if(employeeCount[0] == 0){
            Logger.i("empty employee records...running quick update in 3 seconds");
            periodicEmployeeHandler.postDelayed(

                    new Runnable() {
                        @Override
                        public void run() {
                            employeeUpdate(); //update now
                        }
                    }
                    , 3 * 1000 - SystemClock.elapsedRealtime() % 1000);

        }else{
            // currentPeriodicEmployeeUpdateDelay
            //Logger.i("running periodic update");
        }
*/
        periodicEmployeeHandler.post(periodicEmployeeUpdate);
        periodicTestHandler.post(periodicTestUpdate);
        /////

       // getAllData();

        return START_STICKY;
    }



    private void getAllData() {

        //

            /*
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm inRealm) {

                    Number maxId = inRealm.where(Employee.class).max("EmployeeID");
                    // If there are no rows, currentId is null, so the next id must be 1
                    // If currentId is not null, increment it by 1
                    int nextId = (maxId == null) ? 1 : maxId.intValue() + 1;
                    // User object created with the new Primary key
                    Employee employee = inRealm.createObject(Employee.class, nextId);
                    // Get Data and save on Realm DB
                    // employee.setEmployeeID(obj.getLong("EmployeeID"));
                    //employee.setQuantity(obj.getInt("quantity"));
                    String IDNumber = (29990000 + nextId) + "";
                    employee.setIDNumber(IDNumber);
                    employee.setName("test subject " + IDNumber);
                }
            });
            */
    }

}
