package ph.esrconstruction.esrsys.esrsysmobile.ui;

import androidx.lifecycle.ViewModel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;


public class EmployeeViewModel extends ViewModel {
    private MutableLiveData<Employee> employee;

    public LiveData<Employee> getEmployee() {
        if (employee == null) {
            employee = new MutableLiveData<>();
        }
        return employee;
    }

    public void setEmployee(Employee employee){
        if (this.employee == null) {
            this.employee = new MutableLiveData<>();
        }
        this.employee.setValue(employee);
    }

}
