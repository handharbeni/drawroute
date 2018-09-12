package illiyin.mhandharbeni.libraryroute;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by root on 11/08/17.
 */

public class Navigation {
    private Context context;
    private String key;
    private LatLng startPosition, endPosition;
    private String mode;
    private boolean showCalc;
    private GoogleMap map;
    private Directions directions;
    private int pathColor = Color.BLUE;
    private int secondPath = Color.CYAN;
    private int thirdPath = Color.RED;
    private float pathWidth = 5;
    private OnPathSetListener listener;
    private boolean alternatives = false;
    private long arrivalTime;
    private String avoid;
    private ArrayList<Polyline> lines = new ArrayList<>();
    private Activity activity;

    public Navigation(GoogleMap map, LatLng startLocation, LatLng endLocation, Context context, Activity activity, String key){
        this.key = key;
        this.startPosition = startLocation;
        this.endPosition = endLocation;
        this.map = map;
        this.context=context;
        this.activity = activity;

        Steps.accuratePath.clear();
        Steps.routepath1.clear();
        Steps.routepath2.clear();

    }

    public interface OnPathSetListener{
        void onPathSetListener(Directions directions);
    }

    public void setOnPathSetListener(OnPathSetListener listener){
        this.listener = listener;
    }

    private LatLng getStartPoint(){
        return startPosition;
    }

    private LatLng getEndPoint(){
        return endPosition;
    }

    public void find(boolean showDialog,boolean findAlternatives){
        this.alternatives = findAlternatives;
        showCalc=showDialog;
        new PathCreator().execute();
        zoomMaps();
    }
    public void find(boolean showDialog, boolean findAlternatives, BitmapDescriptor markerStart, BitmapDescriptor markerEnd){
        this.alternatives = findAlternatives;
        showCalc=showDialog;
        new PathCreator().execute();
        setMarker(getStartPoint(), "", markerStart);
        setMarker(getEndPoint(), "", markerEnd);
        zoomMaps();
    }

    private void setMarker(LatLng latLng, String caption, BitmapDescriptor drawable){
        map.addMarker(new MarkerOptions()
                .icon(drawable)
                .position(latLng)
                .title(caption));
    }
    public void setMode(int mode, long arrivalTime,int avoid){
        switch(mode){

            case 0:
                this.mode = "driving";
                break;
            case 1:
                this.mode = "transit";
                this.arrivalTime = arrivalTime;
                break;
            case 2:
                this.mode = "bicycling";
                break;
            case 3:
                this.mode = "walking";
                break;
            default:
                this.mode = "driving";
                break;
        }

        switch(avoid){
            case 0:
                this.avoid = "tolls";
                break;
            case 1:
                this.avoid = "highways";
                break;
            default:
                break;
        }
    }
    public void setMode(int mode, int avoid){
        switch(mode){

            case 0:
                this.mode = "driving";
                break;
            case 1:
                this.mode = "transit";
                break;
            case 2:
                this.mode = "bicycling";
                break;
            case 3:
                this.mode = "walking";
                break;
            default:
                this.mode = "driving";
                break;
        }

        switch(avoid){
            case 0:
                this.avoid = "tolls";
                break;
            case 1:
                this.avoid = "highways";
                break;
            default:
                break;
        }
    }
    public Directions getDirections(){
        return directions;
    }

    public void setPathColor(int firstPath,int secondPath, int thirdPath){
        pathColor = firstPath;
    }

    public void setPathLineWidth(float width){
        pathWidth = width;
    }

    private Polyline showPath(Route route,int color,int l){
        if(l==0){
            return map.addPolyline(new PolylineOptions().addAll(route.getaccuratepath()).color(color).width(pathWidth));
        }else if(l==1){
            return map.addPolyline(new PolylineOptions().addAll(route.getaccuratepath11()).color(color).width(pathWidth));
        }else{
            return map.addPolyline(new PolylineOptions().addAll(route.getaccuratepath12()).color(color).width(pathWidth));
        }

    }

    public ArrayList<Polyline> getPathLines(){
        return lines;
    }

    @SuppressLint("StaticFieldLeak")
    private class PathCreator extends AsyncTask<Void,Void,Directions> {
        private ProgressDialog pd;
        @Override
        protected void onPreExecute(){
            if(showCalc){
                pd = new ProgressDialog(activity);
                pd.setMessage("Getting Directions");
                pd.show();
            }
        }

        @Override
        protected Directions doInBackground(Void... params) {
            if(mode == null){
                mode = "driving";
            }

            String url = "http://maps.googleapis.com/maps/api/directions/json?"
                    + "origin=" + startPosition.latitude + "," + startPosition.longitude
                    + "&destination=" + endPosition.latitude + "," + endPosition.longitude
                    + "&sensor=false&units=metric&mode="+mode+"&alternatives="+String.valueOf(alternatives)
                    + "&key="+key;

            if(mode.equals("transit")){
                if(arrivalTime > 0){
                    url += url + "&arrival_time="+arrivalTime;
                }else{
                    url += url + "&departure_time="+System.currentTimeMillis();
                }
            }

            if(avoid != null){
                url += url+"&avoid="+avoid;
            }
            Log.d(TAG, "doInBackground: "+url);
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(url);
                HttpResponse response = httpClient.execute(httpPost, localContext);

                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){

                    String s = EntityUtils.toString(response.getEntity());
                    Log.d(TAG, "doInBackground: "+s);
                    return new Directions(s);
                }


                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Directions directions){

            if(directions != null){
                Navigation.this.directions = directions;
                for(int i=0; i<directions.getRoutes().size(); i++){
                    Route r = directions.getRoutes().get(i);
                    RoutePoints rp=new RoutePoints();
                    if(i == 0){
                        lines.add(showPath(r,pathColor,i));

                    }else if(i == 1){

                        lines.add(showPath(r,secondPath,i));


                    }else if(i == 2){

                        lines.add(showPath(r,thirdPath,i));

                    }
                }

                if(listener != null){
                    listener.onPathSetListener(directions);
                }

            }

            if(showCalc && pd != null){
                pd.dismiss();
            }
        }

    }
    private void zoomMaps(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(getStartPoint());
        builder.include(getEndPoint());

        LatLngBounds latLngBounds = builder.build();

        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.30);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(latLngBounds, width, height, padding);

        map.animateCamera(cu);
    }
}