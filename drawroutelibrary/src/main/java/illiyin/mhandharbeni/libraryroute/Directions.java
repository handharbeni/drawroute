package illiyin.mhandharbeni.libraryroute;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by root on 11/08/17.
 */

public class Directions {
    private static ArrayList<Route> routes = new ArrayList<>();
    private String directions;
    public enum DrivingMode{
        DRIVING,MASS_TRANSIT,BYCICLE,WALKING
    }
    public enum Avoid{
        TOLLS,HIGHWAYS,NONE
    }
    Directions(String directions){
        this.directions = directions;

        if(directions != null){
            parseDirections();
        }

    }
    private void parseDirections(){
        try {
            JSONObject json = new JSONObject(directions);


            if(!json.isNull("routes")){
                JSONArray route = json.getJSONArray("routes");
                Log.d(TAG, "parseDirections: routeLength "+route.length());
                for(int k=0;k<route.length(); k++){
                    JSONObject obj3 = route.getJSONObject(k);
                    routes.add(new Route(obj3,k));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Route> getRoutes(){
        return routes;
    }
}