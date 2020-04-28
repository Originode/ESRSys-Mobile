package ph.esrconstruction.esrsys.esrsysmobile.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import ph.esrconstruction.esrsys.esrsysmobile.ESRSys;
import ph.esrconstruction.esrsys.esrsysmobile.MainActivity;
import ph.esrconstruction.esrsys.esrsysmobile.R;
import ph.esrconstruction.esrsys.esrsysmobile.cards.CardData;
import ph.esrconstruction.esrsys.esrsysmobile.cards.EmployeeCardData;
import ph.esrconstruction.esrsys.esrsysmobile.events.MessageEvent;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;
import ph.esrconstruction.esrsys.esrsysmobile.utils.nfcUtils;

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
    public static final String TAG = "EmployeeFragment";
    private Realm realm;
    private OnFragmentInteractionListener mListener;



    private boolean isCardScanned = false;
    private boolean isCardWrite = false;


    private EditText mEtMessage;
    private Button mBtWrite;


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




        if(getArguments() != null && getArguments().containsKey("employeeID")){
            setModel(getArguments().getLong("employeeID"));

        }

    }

    private void setModel(long employeeID) {
        realm = Realm.getInstance(ESRSys.getEsrConfig());

        Employee xemp = realm.where(Employee.class).equalTo("EmployeeID",employeeID).findFirst();

        if(xemp != null){
            Logger.i(xemp.getName());
            Logger.i("EmployeeID: " + xemp.getEmployeeID());
            mViewModel = ViewModelProviders.of(this).get(EmployeeViewModel.class);

            mViewModel.setEmployee(xemp);

                if ( mViewModel.getEmployee().hasObservers()) {
                    mViewModel.getEmployee().removeObservers(this);
                }
                mViewModel.getEmployee().observe(this, employee -> updateUI());


        }
    }

    private void initViews(@NonNull View viewx) {

        mEtMessage = (EditText) viewx.findViewById(R.id.et_message);
        mBtWrite = (Button) viewx.findViewById(R.id.btn_write);




        mBtWrite.setOnClickListener(view -> showWriteFragment());



    }

    public void updateUI(){
        Logger.d("update ui");
        if(getView() != null){
            TextView iDNumberTextView = getView().findViewById(R.id.IDNumberTextView);
            iDNumberTextView.setText(Objects.requireNonNull(mViewModel.getEmployee().getValue()).getIDNumber());
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy MMMM dd hh:mm:ss");

            TextView updatedTextView = getView().findViewById(R.id.updatedTextView);
            updatedTextView.setText(mViewModel.getEmployee().getValue().getLastUpdatex().toString(fmt));

            //TextView nameTextView = getView().findViewById(R.id.nameTextView);
            // nameTextView.setText(mViewModel.getEmployee().getValue().getName());

            TextView nameTextEdit = getView().findViewById(R.id.nameTextEdit);
            nameTextEdit.setText(mViewModel.getEmployee().getValue().getName());
            nameTextEdit.setInputType(InputType.TYPE_NULL); //readonlu

            TextView ProjectNameTextView = getView().findViewById(R.id.projectNameTextView);
            ProjectNameTextView.setText(mViewModel.getEmployee().getValue().getProjectName());
            TextView RoleNameTextView = getView().findViewById(R.id.roleNameTextView);
            RoleNameTextView.setText(mViewModel.getEmployee().getValue().getRoleName());
            TextView StatusNameTextView = getView().findViewById(R.id.statusNameTextView);
            StatusNameTextView.setText(mViewModel.getEmployee().getValue().getStatusName());

            TextView et_message = getView().findViewById(R.id.et_message);
            et_message.setText(mViewModel.getEmployee().getValue().getRemarks());

            DateTime now = DateTime.now().minusSeconds(15);
            DateTime dateTime = new DateTime(mViewModel.getEmployee().getValue().getLastScanned());// mViewModel.getEmployee().getValue().getLastScannedx();
            Logger.d(now.toString());
            Logger.d(mViewModel.getEmployee().getValue().getLastScanned().toString());
            Seconds seconds = Seconds.secondsBetween(now, dateTime); //dateTime - now
            Logger.d(seconds.getSeconds());
            if(seconds.getSeconds() > 10){
                isCardScanned = true;
                Switch cardScanned = getView().findViewById(R.id.switch_cardScanned);
                cardScanned.setChecked(isCardScanned);

                new Handler().postDelayed(() -> {
                    isCardScanned = false;
                    cardScanned.setChecked(isCardScanned);
                }, seconds.getSeconds()*1000);
            }


            Switch cardWrite = getView().findViewById(R.id.switch_cardWrite);
            cardWrite.setChecked(isCardWrite);

            ImageView image = (ImageView) getView().findViewById(R.id.item_image);

            Bitmap xIcon = BitmapFactory.decodeResource(Objects.requireNonNull(getContext()).getResources(), R.drawable.logo);
            Bitmap xxx = mViewModel.getEmployee().getValue().getImageBitmap();
            if(xxx != null){
                xIcon = xxx;
            }
            image.setImageBitmap(xIcon);
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        Logger.d("start");


        if(mViewModel != null){
            if ( mViewModel.getEmployee().hasObservers()) {
                mViewModel.getEmployee().removeObservers(this);
            }
            mViewModel.getEmployee().observe(this, employee -> updateUI());
        }

        //updateCounts();

        EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
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
        isCardWrite = true;
        Logger.d("Card write = true");
        ((MainActivity)getActivity()).mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);

        if (((MainActivity)getActivity()).mNfcWriteFragment == null) {

            ((MainActivity)getActivity()).mNfcWriteFragment = NFCWriteFragment.newInstance();
        }
        ((MainActivity)getActivity()).mNfcWriteFragment.show(getFragmentManager(),NFCWriteFragment.TAG);

    }
    private void closeWriteFragment() {
        isCardWrite = false;
        if (((MainActivity)getActivity()).mNfcWriteFragment != null) {
            ((MainActivity)getActivity()).mNfcWriteFragment.dismiss();
        }
    }


    private void writeEmployeeCard(EmployeeCardData cd) {

        EmployeeCardData ec = new EmployeeCardData(cd.CardID,mViewModel.getEmployee().getValue().getIDNumber(),mViewModel.getEmployee().getValue().getName(),mViewModel.getEmployee().getValue().getEmployeeID(), cd.intent);
        EditText et_message = getView().findViewById(R.id.et_message);
        //t_message.setText("Google is your friend.", TextView.BufferType.EDITABLE);
        ec.Message = et_message.getText().toString();

        if (nfcUtils.writeEmployeeCardToMifareClassic(ec.intent, ec)) {
            closeWriteFragment();
        }else{
            //handle error
            Logger.w("todo: handle failure");
        }
    }


    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEmployeeCardData(EmployeeCardData cd) {

        if(mViewModel != null && mViewModel.getEmployee().getValue() != null){
            if(isCardWrite){
                Logger.d("overwriting existing card");
                writeEmployeeCard(cd);
            }else{
                Logger.t(TAG).d(cd.IDNumber);
                isCardScanned = true;
                setModel(cd.EmployeeID);
                updateUI();
            }
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCardData(CardData cd) {
        if(mViewModel != null && mViewModel.getEmployee().getValue() != null){
            if(isCardWrite){
                Logger.d("writing new card");
                writeEmployeeCard(new EmployeeCardData(cd));
            }
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent m) {
        Logger.t(TAG).d(m);

        switch(m.message){
            case MessageEvent.Messages.NFCWriteFragment_closed:
                isCardWrite = false;

                if(getView() != null){
                    Switch cardWrite = getView().findViewById(R.id.switch_cardWrite);
                    cardWrite.setChecked(isCardWrite);
                }

            default:

        }


    }


}
