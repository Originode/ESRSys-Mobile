// Please note : @LinkingObjects and default values are not represented in the schema and thus will not be part of the generated models
package ph.esrconstruction.esrsys.esrsysmobile.realmmodules.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import org.joda.time.DateTime;

import io.realm.RealmObject;
import io.realm.annotations.Required;
import io.realm.annotations.PrimaryKey;
import ph.esrconstruction.esrsys.esrsysmobile.ESRSys;

import java.util.Date;

public class Employee extends RealmObject {
    @PrimaryKey
    private long EmployeeID;
    @Required
    private String Name;
    @Required
    private String IDNumber;
    @Required
    private Date LastUpdate;
    @Required
    private byte[] Image;

    private int ImageLoaded = 0;
    private String ImageURL = "";

    @Nullable
    private String ProjectName= "";

    @Nullable
    private String RoleName= "";

    @Nullable
    private String StatusName= "";


    @Nullable
    private String Remarks= "";

    @Nullable
    private boolean flags_cardScanned= false;

    @Nullable
    private boolean flags_writeCard= false;

    @Nullable
    private Date LastScanned;

    private int xxxxxxxxxxxxxx;

    public Employee(){

    }
    public Employee(long EmployeeID, String IDNumber, String Name){
        this.EmployeeID = EmployeeID;
        this.IDNumber = IDNumber;
        this.Name = Name;
        this.ImageLoaded = 0;

        this.flags_cardScanned = false;
        this.flags_writeCard = false;

        this.LastScanned = (new DateTime( 2020, 1, 1, 0, 0 ).toDate());

        this.LastUpdate = (new DateTime( 2020, 1, 1, 0, 0 ).toDate());
    }


    public long getEmployeeID() { return this.EmployeeID; }

    public void setEmployeeID(long EmployeeID) { this.EmployeeID = EmployeeID; }

    public String getName() { return this.Name; }

    public void setName(String Name) { this.Name = Name; }



    public String getProjectName() { return this.ProjectName; }
    public String getRoleName() { return this.RoleName; }
    public String getStatusName() { return this.StatusName; }



    public void setProjectName(String ProjectName) { this.ProjectName = (ProjectName != null && !ProjectName.isEmpty()) ? ProjectName : "unassigned"; }
    public void setRoleName(String RoleName) { this.RoleName = (RoleName != null && !RoleName.isEmpty()) ? RoleName : "unassigned"; }
    public void setStatusName(String StatusName) { this.StatusName = (StatusName != null && !StatusName.isEmpty()) ? StatusName : "unassigned"; }


    public String getIDNumber() { return IDNumber; }

    public void setIDNumber(String IDNumber) { this.IDNumber = IDNumber; }

    public Date getLastUpdate() { return this.LastUpdate; }
    public DateTime getLastUpdatex() { return new DateTime(this.LastUpdate); }
    public void setLastUpdate(Date LastUpdate) { this.LastUpdate = LastUpdate; }

    public byte[] getImage() { return this.Image; }

    public void setImage(byte[] Image) { this.Image = Image;}

    public Bitmap getImageBitmap() {
            return (this.Image != null) ? BitmapFactory.decodeByteArray(this.Image, 0, this.Image.length) : null;
    }


    public long getImageLoaded() { return this.ImageLoaded; }

    public void setImageLoaded(int ImageLoaded) { this.ImageLoaded = ImageLoaded; }

    public String getImageURL(){
        return ESRSys.getInstance().getBaseURL() + this.ImageURL;
    }
    public void setImageURL(String ImageURL){
        this.ImageURL = ImageURL;
    }

    public void loadImage(){
        String base_url = ESRSys.getInstance().getBaseURL();

    }

    public boolean isFlags_cardScanned() {
        return flags_cardScanned;
    }

    public void setFlags_cardScanned(boolean flags_cardScanned) {
        this.flags_cardScanned = flags_cardScanned;
    }

    public boolean isFlags_writeCard() {
        return flags_writeCard;
    }

    public void setFlags_writeCard(boolean flags_writeCard) {
        this.flags_writeCard = flags_writeCard;
    }

    @Nullable
    public Date getLastScanned() {
        return this.LastScanned;
    }

    @Nullable
    public DateTime getLastScannedx() {
        return new DateTime(this.LastScanned);
    }

    public void setLastScanned(@Nullable Date lastScanned) {
        this.LastScanned = lastScanned;
    }

    @Nullable
    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(@Nullable String remarks) {
        Remarks = remarks;
    }
}

