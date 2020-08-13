package tr.com.mustafaserhansengel.geofence_test;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        String content;
        int nottificationID;
        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            content = "Alana girdi";
            nottificationID = 1010;
        }else{
            content = "Alandan çıktı";
            nottificationID = 2020;
        }

        NotificationCompat.Builder builder;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e("GEOFENCE", "Etkileşime girdi");

        Intent intent2 = new Intent(context.getApplicationContext(), MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(),
                1, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            String canalId = content;
            String canalName = content;
            String canalContent = content;
            int priorty = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel canal = manager.getNotificationChannel(canalId);
            if (canal == null) {
                canal = new NotificationChannel(canalId, canalName, priorty);
                canal.setDescription(canalContent);
                manager.createNotificationChannel(canal);
            }

            builder = new NotificationCompat.Builder(context.getApplicationContext(), canalId);
            builder.setContentTitle("GEOFENCE TEST");
            builder.setContentText(content);
            builder.setSmallIcon(R.drawable.work_notification_icon);
            builder.setAutoCancel(true);
            builder.setContentIntent(pendingIntent);

        } else {
            builder = new NotificationCompat.Builder(context.getApplicationContext());
            builder.setContentTitle("GEOFENCE TEST");
            builder.setContentText(content);
            builder.setSmallIcon(R.drawable.work_notification_icon);
            builder.setAutoCancel(true);
            builder.setContentIntent(pendingIntent);
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

        manager.notify(nottificationID, builder.build());

    }
}
