package ph.esrconstruction.esrsys.esrsysmobile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import ph.esrconstruction.esrsys.esrsysmobile.R;
import ph.esrconstruction.esrsys.esrsysmobile.ui.ToolsGridItem;
import ph.esrconstruction.esrsys.esrsysmobile.ui.ToolsGridViewAdapter;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class ToolsFragment extends Fragment {
    GridView gridView;
    ArrayList<ToolsGridItem> gridArray ;
    ToolsGridViewAdapter toolsGridAdapter;
    NavController navController;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View inflate = inflater.inflate(R.layout.tools_fragment, container, false);


        //set grid view item
        Bitmap homeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
        Bitmap userIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_person_black_24dp);

        gridArray = new ArrayList<ToolsGridItem>();
        gridArray.add(new ToolsGridItem(homeIcon,"Employees"));
        gridArray.add(new ToolsGridItem(homeIcon,"DTR/Attendance"));
        gridArray.add(new ToolsGridItem(homeIcon,"Equipment Update"));


        gridView = inflate.findViewById(R.id.tools_grid);
        toolsGridAdapter = new ToolsGridViewAdapter(this.getContext(), R.layout.row_grid, gridArray);
        gridView.setAdapter( toolsGridAdapter);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(
                        getActivity().getApplicationContext(),
                        ((TextView) v.findViewById(R.id.item_text))
                                .getText(), Toast.LENGTH_SHORT).show();

                //NavDirections action = ;
                navController.navigate(ToolsFragmentDirections.actionToolsFragmentToEmployeesFragment());
            }
        });


        return inflate;
    }
}
