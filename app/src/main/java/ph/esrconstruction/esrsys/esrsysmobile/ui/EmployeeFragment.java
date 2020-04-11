package ph.esrconstruction.esrsys.esrsysmobile.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import ph.esrconstruction.esrsys.esrsysmobile.ESRSys;
import ph.esrconstruction.esrsys.esrsysmobile.EmployeesFragmentDirections;
import ph.esrconstruction.esrsys.esrsysmobile.MainActivity;
import ph.esrconstruction.esrsys.esrsysmobile.R;
import ph.esrconstruction.esrsys.esrsysmobile.network.ConnectionModel;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.modules.ESRModules;

import static ph.esrconstruction.esrsys.esrsysmobile.ESRSys.MobileData;
import static ph.esrconstruction.esrsys.esrsysmobile.ESRSys.WifiData;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EmployeeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EmployeeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmployeeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    EmployeeViewModel mViewModel;
    public static final String TAG = "HOME";
    private Realm realm;
    private OnFragmentInteractionListener mListener;



    private boolean isDialogDisplayed = false;
    private boolean isWrite = false;


    private EditText mEtMessage;
    private Button mBtWrite;
    private Button mBtRead;

    public EmployeeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EmployeeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EmployeeFragment newInstance(String param1, String param2) {
        EmployeeFragment fragment = new EmployeeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        realm = Realm.getInstance(ESRSys.getEsrConfig());

        Long employeeID = getArguments().getLong("employeeID");
        Logger.i("EmployeeID: " + employeeID);
        Employee xemp = realm.where(Employee.class).equalTo("EmployeeID",employeeID).findFirst();
        Logger.i(xemp.getName());

        mViewModel = ViewModelProviders.of(this).get(EmployeeViewModel.class);

        mViewModel.setEmployee(xemp);
    }

    private void initViews(@NonNull View viewx) {

        mEtMessage = (EditText) viewx.findViewById(R.id.et_message);
        mBtWrite = (Button) viewx.findViewById(R.id.btn_write);
        mBtRead = (Button) viewx.findViewById(R.id.btn_read);



        mBtWrite.setOnClickListener(view -> showWriteFragment());
        mBtRead.setOnClickListener(view -> showReadFragment());


    }

    public void updateUI(){
        Logger.d("update ui");
        TextView iDNumberTextView = getView().findViewById(R.id.IDNumberTextView);
        iDNumberTextView.setText(mViewModel.getEmployee().getValue().getIDNumber());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        TextView updatedTextView = getView().findViewById(R.id.updatedTextView);
        updatedTextView.setText(dateFormat.format(mViewModel.getEmployee().getValue().getLastUpdate()));

        TextView nameTextView = getView().findViewById(R.id.nameTextView);
        nameTextView.setText(mViewModel.getEmployee().getValue().getName());

        TextView ProjectNameTextView = getView().findViewById(R.id.projectNameTextView);
        ProjectNameTextView.setText(mViewModel.getEmployee().getValue().getProjectName());
        TextView RoleNameTextView = getView().findViewById(R.id.roleNameTextView);
        RoleNameTextView.setText(mViewModel.getEmployee().getValue().getRoleName());
        TextView StatusNameTextView = getView().findViewById(R.id.statusNameTextView);
        StatusNameTextView.setText(mViewModel.getEmployee().getValue().getStatusName());


        ImageView image = (ImageView) getView().findViewById(R.id.item_image);

        Bitmap xIcon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.logo);
        Bitmap xxx = mViewModel.getEmployee().getValue().getImageBitmap();
        if(xxx != null){
            xIcon = xxx;
        }
        image.setImageBitmap(xIcon);
    }


    @Override
    public void onStart() {
        super.onStart();
        Logger.d("start");
        mViewModel.getEmployee().observe(this, new Observer<Employee>() {
            @Override
            public void onChanged(Employee employee) {
                updateUI();
            }
        });
        //updateCounts();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_employee, container, false);




        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
       // TextView tv = view.findViewById(R.id.textViewAmount);
       // int amount = EmployeesFragmentDirections.

        //tv.setText(amount + "")
        initViews(view);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


        /*
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        */
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }






    private void showWriteFragment() {

        isWrite = true;

        ((MainActivity)getActivity()).mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);

        if (((MainActivity)getActivity()).mNfcWriteFragment == null) {

            ((MainActivity)getActivity()).mNfcWriteFragment = NFCWriteFragment.newInstance();
        }
        ((MainActivity)getActivity()).mNfcWriteFragment.show(getFragmentManager(),NFCWriteFragment.TAG);

    }

    private void showReadFragment() {

        ((MainActivity)getActivity()).mNfcReadFragment = (NFCReadFragment) getFragmentManager().findFragmentByTag(NFCReadFragment.TAG);

        if (((MainActivity)getActivity()).mNfcReadFragment == null) {

            ((MainActivity)getActivity()).mNfcReadFragment = NFCReadFragment.newInstance();
        }
        ((MainActivity)getActivity()).mNfcReadFragment.show(getFragmentManager(),NFCReadFragment.TAG);

    }


}
