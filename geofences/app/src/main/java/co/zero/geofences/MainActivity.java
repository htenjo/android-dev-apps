package co.zero.geofences;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>{

    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient googleApiClient;
    private final HashMap<String, LatLng> AREAS = new HashMap<>();
    private final ArrayList<Geofence> geofences = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        populateAreas();
        populateGeofences();

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    /**
     *
     * @param view
     */
    public void addGeoFencesButtonHandler(View view) {
        if(!googleApiClient.isConnected()){
            Toast.makeText(this, "Not connected to API", Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient, getGeofencingRequest(), getGeofencePendingIntent()
            ).setResultCallback(this);
        }catch (SecurityException e){
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     *
     */
    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    /**
     *
     */
    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    /**
     *
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.e(this.getClass().getSimpleName().toString(),
                "onConnectionSuspended... " + i);
    }

    /**
     *
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(this.getClass().getSimpleName().toString(),
                "onConnectionFailed error " + connectionResult.getErrorMessage());
    }

    /**
     *
     */
    private void populateAreas(){
        AREAS.put("Area 1", new LatLng(-75, 23));
        AREAS.put("Area 2", new LatLng(-175, 23));
        AREAS.put("Area 3", new LatLng(-275, 23));
    }

    /**
     *
     */
    private void populateGeofences(){
        Geofence geofence;

        for(Map.Entry<String, LatLng> entry: AREAS.entrySet()){
            geofence = new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(entry.getValue().latitude,
                            entry.getValue().longitude, Constants.GEOFENCE_RADIUS_IN_METERS)
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            geofences.add(geofence);
        }
    }

    /**
     *
     * @return
     */
    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    /**
     *
     * @return
     */
    private PendingIntent getGeofencePendingIntent(){
        Intent intent = new Intent(this, GeoFenceTransitionIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(@NonNull Status status) {
        if(status.isSuccess()){
            Toast.makeText(this, "Geofences Added", Toast.LENGTH_SHORT).show();
        }else{
            String errorMessage = String.valueOf(status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }
}
