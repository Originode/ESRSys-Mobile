package ph.esrconstruction.esrsys.esrsysmobile.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<Integer> employeesCount;

    public LiveData<Integer> getemployeesCount() {
        if (employeesCount == null) {
            employeesCount = new MutableLiveData<>();
            employeesCount.setValue(0);
        }
        return employeesCount;
    }

    public void setEmployeesCount(Integer x){
        employeesCount.setValue(x);
    }

}
