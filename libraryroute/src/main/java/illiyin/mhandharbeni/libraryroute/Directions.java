package illiyin.mhandharbeni.libraryroute;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by root on 11/08/17.
 */

public class Directions {

    private ArrayList<Route> routes = new ArrayList<Route>();
    private String directions;

    public enum DrivingMode{
        DRIVING,MASS_TRANSIT,BYCICLE,WALKING
    }

    public enum Avoid{
        TOLLS,HIGHWAYS,NONE
    }

    public Directions(String directions){
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
                Log.e("Route length", ""+route.length());
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