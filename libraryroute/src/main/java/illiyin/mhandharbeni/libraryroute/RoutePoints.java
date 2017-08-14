package illiyin.mhandharbeni.libraryroute;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by root on 11/08/17.
 */

public class RoutePoints {
    private ArrayList<LatLng> accuratePath;

    public ArrayList<LatLng> getAccuratePath() {
        return accuratePath;
    }

    public void setAccuratePath(ArrayList<LatLng> accuratePath) {
        this.accuratePath = accuratePath;
    }


}