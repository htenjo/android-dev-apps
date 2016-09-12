package co.zero.locationapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener{
    private static final int LOCATION_PERMISSIONS_GRANTED = 1;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private TextView longitudeText;
    private TextView latitudeText;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latitudeText = (TextView)findViewById(R.id.value_latitude);
        longitudeText = (TextView)findViewById(R.id.value_longitude);

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
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    /**
     *
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        Log.e(this.getClass().getSimpleName().toString(),
                "onConnectionFailed error " + connectionResult.getErrorMessage());
    }

    /**
     *
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(this.getClass().getSimpleName().toString(), "onConnected... ");
        int finePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(finePermission == PackageManager.PERMISSION_GRANTED){
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            updateLongitudeAndLatitude(lastLocation);
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSIONS_GRANTED);
        }
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
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.i(this.getClass().getSimpleName(), "... onLocationChanged " + location.toString());
        updateLongitudeAndLatitude(location);
    }

    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSIONS_GRANTED:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(this.getClass().getSimpleName(), "::: PERMISSIONS GRANTED");
                }else{
                    Log.e(this.getClass().getSimpleName(), "::: PERMISSIONS DENIED");
                }
                return;
        }
    }

    /**
     *
     * @param location
     */
    private void updateLongitudeAndLatitude(Location location){
        if (location != null) {
            latitudeText.setText(String.valueOf(location.getLatitude()));
            longitudeText.setText(String.valueOf(location.getLongitude()));
        }
    }
}
