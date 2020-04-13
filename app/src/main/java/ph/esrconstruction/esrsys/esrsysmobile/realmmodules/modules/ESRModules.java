package ph.esrconstruction.esrsys.esrsysmobile.realmmodules.modules;

import io.realm.annotations.RealmModule;
import io.realm.examples.librarymodules.model.Elephant;
import io.realm.examples.librarymodules.model.Lion;
import io.realm.examples.librarymodules.model.Zebra;
import ph.esrconstruction.esrsys.esrsysmobile.data.model.DeviceSettings;
import ph.esrconstruction.esrsys.esrsysmobile.data.model.LoggedInUser;
import ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model.Employee;

@RealmModule(library = true, classes = {Employee.class, Elephant.class, Lion.class, Zebra.class, LoggedInUser.class, DeviceSettings.class})
public class ESRModules {
}
