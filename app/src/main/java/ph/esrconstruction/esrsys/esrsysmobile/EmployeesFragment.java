package ph.esrconstruction.esrsys.esrsysmobile;
import com.orhanobut.logger.Logger;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import ph.esrconstruction.esrsys.esrsysmobile.cards.EmployeeCardData;
import ph.esrconstruction.esrsys.esrsysmobile.dummy.DummyContent;
import ph.esrconstruction.esrsys.esrsysmobile.dummy.DummyContent.DummyItem;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.modules.ESRModules;
import ph.esrconstruction.esrsys.esrsysmobile.ui.EmployeesRecyclerViewAdapter;



import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ph.esrconstruction.esrsys.esrsysmobile.ui.HomeViewModel;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class EmployeesFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 3;
    View view;
    private OnListFragmentInteractionListener mListener;


    public NavController navController;

    private EmployeesRecyclerViewAdapter employeeAdapter;
    private Realm realm;
    public static final String TAG = "EmployeesFragment";
    private EmployeesRecyclerViewAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EmployeesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static EmployeesFragment newInstance(int columnCount) {
        EmployeesFragment fragment = new EmployeesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        setHasOptionsMenu(true);

        //setContentView(R.layout.fragment_employeesitem_list);
       // ButterKnife.bind(this);


        realm = Realm.getInstance(ESRSys.getEsrConfig());


        //employeeCountTextView

       // EmployeesRecyclerViewAdapter adapter = new EmployeesRecyclerViewAdapter(results, this);
        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

      //  RecyclerView recyclerView;
       // recyclerView.setLayoutManager(linearLayoutManager);

       // recyclerView.setAdapter(adapter);
        


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employeesitem_list, container, false);

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

        RealmResults<Employee> results;
        String title;

        results = realm.where(Employee.class)
                .findAll()
                .sort("Name", Sort.ASCENDING);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            this.mAdapter = new EmployeesRecyclerViewAdapter(realm,results, this);
            recyclerView.setAdapter(mAdapter);
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
     /*
       // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
      */
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.employees_menu, menu);
        SearchView searchView = new SearchView(((MainActivity)getContext()).getSupportActionBar().getThemedContext());
        MenuItem item = menu.findItem(R.id.action_search);

        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setActionView(searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {

                                          }
                                      }
        );
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }




    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Employee item);


    }




    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEmployeeCardData(EmployeeCardData cd) {
        Toast.makeText(getActivity(), "Employee card readxxx", Toast.LENGTH_SHORT).show();
        Logger.t(TAG).d(cd.IDNumber);

        EmployeesFragmentDirections.ActionEmployeesFragmentToEmployeeFragment action =  EmployeesFragmentDirections.actionEmployeesFragmentToEmployeeFragment(cd.EmployeeID);
        navController.navigate(action);
    }
}
