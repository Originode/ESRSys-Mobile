package ph.esrconstruction.esrsys.esrsysmobile.ui;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.RealmRecyclerViewAdapter;
import ph.esrconstruction.esrsys.esrsysmobile.EmployeesFragment;
import ph.esrconstruction.esrsys.esrsysmobile.EmployeesFragment.OnListFragmentInteractionListener;
import ph.esrconstruction.esrsys.esrsysmobile.EmployeesFragmentDirections;
import ph.esrconstruction.esrsys.esrsysmobile.R;
import ph.esrconstruction.esrsys.esrsysmobile.ToolsFragmentDirections;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;


public class EmployeesRecyclerViewAdapter extends RealmRecyclerViewAdapter<Employee, EmployeesRecyclerViewAdapter.ViewHolder> implements Filterable {

  //  private final RealmResults<Employee> realm;
   // private final OnListFragmentInteractionListener mListener;
    EmployeesFragment context;
    private RealmResults<Employee> adapterData;
    private Realm realm;
    private NavController navController;
    public EmployeesRecyclerViewAdapter(Realm realm, RealmResults<Employee> list, EmployeesFragment context) {


        super(list, true , true);
        this.context = context;
        adapterData = list;
        this.realm = realm;


        this.navController = Navigation.findNavController(context.getActivity(), R.id.nav_host_fragment);
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_employeesitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final Employee temp = getItem(position);


            holder.itemNumber.setText(temp.getIDNumber());
            holder.itemText.setText(temp.getName());
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            holder.itemUpdated.setText(dateFormat.format(temp.getLastUpdate()));

        Bitmap xIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        Bitmap xxx = temp.getImageBitmap();
        if(xxx != null){
            xIcon = xxx;
        }
        holder.image.setImageBitmap(xIcon);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    //v.
                    Logger.d("clicked" + temp.getName());

                    EmployeesFragmentDirections.ActionEmployeesFragmentToEmployeeFragment action = EmployeesFragmentDirections.actionEmployeesFragmentToEmployeeFragment(temp.getEmployeeID());
                    navController.navigate(action);
                    /*
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onListFragmentInteraction(holder.mItem);
                    }
                    */
                }
            });


    }
    public void filterResults(String text) {
        text = text == null ? null : text.toLowerCase().trim();
        RealmQuery<Employee> query = realm.where(Employee.class);
        if(!(text == null || "".equals(text))) {
            query.contains("Name", text, Case.INSENSITIVE).or().contains("IDNumber", text, Case.INSENSITIVE); // TODO: change field
        }
        updateData(query.findAllAsync());
    }

    public Filter getFilter() {
        EmployeeFilter filter = new EmployeeFilter(this);
        return filter;
    }

    private class EmployeeFilter
            extends Filter {
        private final EmployeesRecyclerViewAdapter adapter;

        private EmployeeFilter(EmployeesRecyclerViewAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            return new FilterResults();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.filterResults(constraint.toString());
        }
    }
    
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView itemNumber;
        public final TextView itemText;
        public final TextView itemUpdated;
        public final ImageView image;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            itemNumber = (TextView) view.findViewById(R.id.item_number);
            itemText = (TextView) view.findViewById(R.id.item_text);
            image = (ImageView) view.findViewById(R.id.item_image);
            itemUpdated = (TextView) view.findViewById(R.id.item_updated);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + itemText + "'";
        }
    }
}