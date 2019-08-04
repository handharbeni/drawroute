package illiyin.mhandharbeni.libraryroute;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by root on 11/08/17.
 */

public class Navigation implements GoogleMap.InfoWindowAdapter {
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
    private NavigationListener navigationListener;

    private String titleStart;
    private String titleEnd;

    private List<String> listStartSnippet;
    private List<String> listEndSnippet;

    private Spanned spannedStart;
    private Spanned spannedEnd;

    private BitmapDescriptor markerStart;
    private BitmapDescriptor markerEnd;

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Context context = this.context;

        LinearLayout info = new LinearLayout(context);
        info.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(context);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(marker.getTitle());

        TextView snippet = new TextView(context);
        snippet.setTextColor(Color.GRAY);
        snippet.setText(marker.getSnippet());

        info.addView(title);
        info.addView(snippet);

        return info;
    }

    public interface NavigationListener{
        void onCompleteLoad(int distance, int duration);
    }

    public Navigation(
            GoogleMap map,
            LatLng startLocation,
            LatLng endLocation,
            Context context,
            Activity activity,
            String key
    ){
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

    public Navigation(){

    }

    public Navigation setKey(String key){
        this.key = key;
        return this;
    }

    public Navigation setStartLocation(LatLng startPosition){
        this.startPosition = startPosition;
        return this;
    }

    public Navigation setTitleStart(String titleStart){
        this.titleStart = titleStart;
        return this;
    }

    public Navigation setTitleEnd(String titleEnd){
        this.titleEnd = titleEnd;
        return this;
    }

    public Navigation setSnippetStart(List<String> listStartSnippet){
        this.listStartSnippet = listStartSnippet;
        return this;
    }

    public Navigation setSnippetEnd(List<String> listEndSnippet){
        this.listEndSnippet = listEndSnippet;
        return this;
    }


    public Navigation setMarkerStart(BitmapDescriptor markerStart){
        this.markerStart = markerStart;
        return this;
    }

    public Navigation setMarkerEnd(BitmapDescriptor markerEnd){
        this.markerEnd = markerEnd;
        return this;
    }

    public Navigation setEndPosition(LatLng endPosition){
        this.endPosition = endPosition;
        return this;
    }

    public Navigation setMap(GoogleMap map){
        this.map = map;
        return this;
    }

    public Navigation setContext(Context context){
        this.context = context;
        return this;
    }

    public Navigation setActivity(Activity activity){
        this.activity = activity;
        return this;
    }

    public Navigation setListener(NavigationListener navigationListener){
        this.navigationListener = navigationListener;
        return this;
    }

    public void clearMaps(){
        if (map != null){
            map.clear();
            Steps.accuratePath.clear();
            Steps.routepath1.clear();
            Steps.routepath2.clear();
        }
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
        map.setInfoWindowAdapter(this);
        zoomMaps();
    }
    public void find(boolean showDialog, boolean findAlternatives, BitmapDescriptor markerStart, BitmapDescriptor markerEnd){
        this.alternatives = findAlternatives;
        showCalc=showDialog;
        new PathCreator().execute();
        zoomMaps();
    }

    private void setMarker(LatLng latLng, String caption, BitmapDescriptor drawable, String snippet){
        map.addMarker(new MarkerOptions()
                .icon(drawable)
                .position(latLng)
                .title(caption))
                .setSnippet(snippet);
    }
    public void setMode(Directions.DrivingMode mode, long arrivalTime, int avoid){
        switch(mode){
            case DRIVING:
                this.mode = "driving";
                break;
            case MASS_TRANSIT:
                this.mode = "transit";
                this.arrivalTime = arrivalTime;
                break;
            case BYCICLE:
                this.mode = "bicycling";
                break;
            case WALKING:
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
    public void setMode(Directions.DrivingMode mode, int avoid){
        switch(mode){

            case DRIVING:
                this.mode = "driving";
                break;
            case MASS_TRANSIT:
                this.mode = "transit";
                break;
            case BYCICLE:
                this.mode = "bicycling";
                break;
            case WALKING:
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
                pd.setMessage("Reloading");
                pd.show();
            }
        }

        @Override
        protected Directions doInBackground(Void... params) {
            if(mode == null){
                mode = "driving";
            }

            String url = "https://maps.googleapis.com/maps/api/directions/json?"
                    + "origin=" + startPosition.latitude + "," + startPosition.longitude
                    + "&destination=" + endPosition.latitude + "," + endPosition.longitude
                    + "&sensor=false&units=metric&mode="+mode+"&alternatives="+ alternatives
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

            Log.d(TAG, "parseDirections doInBackground: "+url);
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(url);
                HttpResponse response = httpClient.execute(httpPost, localContext);
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    String s = EntityUtils.toString(response.getEntity());
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
//            "Jarak : "+getDistance()+" meter"
            navigationListener.onCompleteLoad(getDistance(), getDuration());

            StringBuilder startSnippet = new StringBuilder("<p>");
            if (listStartSnippet.size() > 0){
                for (String sStart : listStartSnippet){
                    startSnippet.append(sStart);
                    startSnippet.append("<br>");
                }
            }
            startSnippet.append("</p>");
            spannedStart = Html.fromHtml(startSnippet.toString());

            StringBuilder endSnippet = new StringBuilder("<p>");
            if (listEndSnippet.size() > 0){
                for (String sEnd : listEndSnippet){
                    endSnippet.append(sEnd);
                    endSnippet.append("<br>");
                }
                endSnippet.append("Jarak : ").append(getDistance()).append(" meter");
            }
            endSnippet.append("</p>");
            spannedEnd = Html.fromHtml(endSnippet.toString());


            setMarker(getStartPoint(), titleStart, markerStart, spannedStart.toString());
            setMarker(getEndPoint(), titleEnd, markerEnd, spannedEnd.toString());
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

    private Integer getDuration(){
        if (Constant.totalDuration == 0){
            new PathCreator().execute();
        }
        return Constant.totalDuration;
    }


    private Integer getDistance(){
        if (Constant.totalDistance == 0){
            new PathCreator().execute();
        }
        return Constant.totalDistance;
    }
}