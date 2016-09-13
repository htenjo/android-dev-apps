package co.zero.geofences;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by htenjo on 9/13/16.
 */
public class GeoFenceTransitionIntentService extends IntentService{
    public static final String TAG = ":::::::: gfservice";

    /**
     *
     */
    public GeoFenceTransitionIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(geofencingEvent.hasError()){
            String error = String.valueOf(geofencingEvent.getErrorCode());
            Log.e(TAG, error);
            return;
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
                || transitionType == Geofence.GEOFENCE_TRANSITION_EXIT){
            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
            List<String> geofenceIds = new ArrayList<>();

            for(Geofence geofence : triggeredGeofences){
                geofenceIds.add(geofence.getRequestId());
            }

            sendNotification(geofenceIds.toString());
        }else{
            Log.e(TAG, "Invalid type");
        }
    }

    private void sendNotification(String notificationDetails){
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText("Click notification to return to app!!!")
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(false);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}
