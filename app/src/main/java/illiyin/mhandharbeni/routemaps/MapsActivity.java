package illiyin.mhandharbeni.routemaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import illiyin.mhandharbeni.libraryroute.Directions;
import illiyin.mhandharbeni.libraryroute.Navigation;
import illiyin.mhandharbeni.libraryroute.Route;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Navigation.NavigationListener {

    private GoogleMap mMap;
    protected LatLng start;
    protected LatLng end;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        for (int i=0;i<2;i++){
            start = new LatLng(-8.0074737,112.6243809);
            switch (i){
                case 0:
                    end = new LatLng(-7.9825665,112.6118265);
                    break;
                case 1 :
                    end = new LatLng(-7.9525665,112.6918265);
                    break;
            }
            Navigation nav = new Navigation();
            nav.setActivity(this);
            nav.setContext(this);
            nav.setMap(mMap);
            nav.setKey(getResources().getString(R.string.google_maps_key));
            nav.setListener(this);
            List<String> listString = new ArrayList<>();
            listString.add("Test1");
            listString.add("Test2");
            listString.add("Test3");
            nav.setSnippetStart(listString);
            nav.setSnippetEnd(listString);
            nav.setTitleStart("Start");
            nav.setTitleEnd("End");
            nav.setMarkerStart(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_marker_outlet));
            nav.setMarkerEnd(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_marker_customer));
            nav.clearMaps();

            nav.setStartLocation(start);
            nav.setEndPosition(end);
            nav.find(
                    true,
                    false
            );
        }
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onCompleteLoad(int distance, int duration) {
        Log.d("MapsActivity", "onMapReady: distance "+distance);
        Log.d("MapsActivity", "onMapReady: duration "+duration);
    }
}
