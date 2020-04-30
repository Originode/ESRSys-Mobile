package ph.esrconstruction.esrsys.esrsysmobile.ui;

import android.graphics.Bitmap;

public class ToolsGridItem {

    Bitmap image;
    String title;
    private int id = 0;

    public ToolsGridItem(Bitmap image, String title, int id) {
        super();
        this.image = image;
        this.title = title;
        this.id = id;
    }
    public Bitmap getImage() {
        return image;
    }
    public void setImage(Bitmap image) {
        this.image = image;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public final class ToolsGridItems {
        public static final int EMPLOYEES = 1;
        public static final int DTR = 2;
        public static final int EQUIPMENT_UPDATE = 3;
        public static final int SETTINGS = 4;
    }
}
