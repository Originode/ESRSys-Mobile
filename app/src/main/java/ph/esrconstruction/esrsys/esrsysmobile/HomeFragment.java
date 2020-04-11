package ph.esrconstruction.esrsys.esrsysmobile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.orhanobut.logger.Logger;

import java.util.concurrent.Callable;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;


import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;
import ph.esrconstruction.esrsys.esrsysmobile.ui.HomeViewModel;

public class HomeFragment extends Fragment {
    private Realm realm = ESRSys.getInstance().getRealm();
    private RealmChangeListener realmListener;
    private RealmResults<Employee> employees;

    public NavController navController;

    Button loginButton;
    TextView loginTextView;

    HomeViewModel mViewModel;
    public static final String TAG = "HOME";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        loginButton = view.findViewById(R.id.loginButton);
        loginTextView = view.findViewById(R.id.loginStatusTextView);
        HomeFragmentDirections.ActionHomeFragmentToLoginFragment action =  HomeFragmentDirections.actionHomeFragmentToLoginFragment();
       // loginTextView.setOnClickListener(this);
        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //public NavController navController;
                //navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                //navController.navigate(R.id.loginFragment);

                if(ESRSys.getInstance().currentLogin.isLoggedIn()) {
                    if(ESRSys.getInstance().currentLogin.getUser() != null){
                        ESRSys.getInstance().currentLogin.asyncLogout((Boolean result) -> {
                            //do something fancy
                            updateUI();
                            Logger.d("logout complete");
                            return result;

                        });
                    }

                }else{
                    navController.navigate(action);
                }




                // do something
            }
        });

        return view;
    }
    public void updateCounts() {
        employees = realm.where(Employee.class).findAll();
        mViewModel.setEmployeesCount(employees.size());
    }
    public void updateUI(){
        TextView x = getView().findViewById(R.id.employeeCountTextView);
        x.setText(mViewModel.getemployeesCount().getValue() + " records");


        TextView y = getView().findViewById(R.id.NetworkStatusTextView);
        TextView z = getView().findViewById(R.id.DatabaseStatusTextView);
            y.setText(ESRSys.getServer().net);
            String serverInfo = ESRSys.getServer().name + " - " + ESRSys.getServer().getURL();
            z.setText(serverInfo);

            TextView curUser = getView().findViewById(R.id.loginStatusTextView);




        NavigationView navigationView = getActivity().findViewById(R.id.navigationView);
        Menu nav_Menu = navigationView.getMenu();
        //nav_Menu.findItem(R.id.menu_login_logout).setVisible(!ESRSys.getInstance().currentLogin.isLoggedIn());
        nav_Menu.findItem(R.id.menu_login_logout).setTitle(ESRSys.getInstance().currentLogin.isLoggedIn() ? "Logout" : "Login");


            if(ESRSys.getInstance().currentLogin.isLoggedIn()) {
                if(ESRSys.getInstance().currentLogin.getUser() != null){
                    curUser.setText("Logged in as " + ESRSys.getInstance().currentLogin.getUser().getDisplayName());
                    loginButton.setText("Logout");
                }

            }else{
                curUser.setText("Not Logged in...");
                loginButton.setText("Login");
            }


    }

    public void setViewModel(HomeViewModel viewModel){
        mViewModel = viewModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setViewModel(ViewModelProviders.of(this).get(HomeViewModel.class));




        realmListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                updateCounts();
            }
        };
        realm.addChangeListener(realmListener);




        //////////


    }




    @Override
    public void onStart() {
        super.onStart();
        Logger.d("start");
        mViewModel.getemployeesCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer employeesCount) {
                updateUI();
            }
        });
        updateCounts();

        ESRSys.devServer.getServerConnected().observe(this, serverConnected -> {
            updateUI();
        });
        ESRSys.localServer.getServerConnected().observe(this, serverConnected -> {
            updateUI();
        });
        ESRSys.remoteServer.getServerConnected().observe(this, serverConnected -> {
            updateUI();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the listener.
        realm.removeChangeListener(realmListener);
        // Close the Realm instance.
        realm.close();
    }
}
