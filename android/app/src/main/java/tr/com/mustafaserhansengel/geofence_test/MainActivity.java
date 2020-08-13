package tr.com.mustafaserhansengel.geofence_test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {

  List<String> requestPermissionsList = new ArrayList<>();
  private int fineLocationPermission;
  private int coarseLocationPermission;
  //Required for 29+
  private int backgroundLocationPermision;

  private static final int MULTIPLE_PERMISSIONS  = 99;

  private GeofencingClient geofencingClient;
  private  GeofenceHelper geofenceHelper;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);
    geofencingClient = LocationServices.getGeofencingClient(this);
    geofenceHelper = new GeofenceHelper(this);


    new MethodChannel(getFlutterView(), "GeofenceChannel")
            .setMethodCallHandler(new MethodChannel.MethodCallHandler() {
              @SuppressLint("MissingPermission")
              @RequiresApi(api = Build.VERSION_CODES.O)
              @Override
              public void onMethodCall(MethodCall call, MethodChannel.Result result) {
                if (call.method.equals("servisBaslat")) {
                  checkandRequestPermissions();
                  result.success("çalişti");
                }

                if (call.method.equals("addGeofence")) {
                  Double lat = call.argument("lat");
                  Double lon = call.argument("lon");
                  double radius = call.argument("radius");
                  LatLng latLng = new LatLng(lat, lon);
                  addGeofence(latLng, radius);
                }


              }
            });

  }

  private void addGeofence(LatLng latLng, double radius) {
    Geofence geofence = geofenceHelper.getGeofence("TESTAPP", latLng, (float) radius,
            Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
    GeofencingRequest request = geofenceHelper.getGeofencingRequest(geofence);
    PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
    geofencingClient.addGeofences(request, pendingIntent);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode){
      case MULTIPLE_PERMISSIONS:
        if(grantResults.length > 0){
          checkandRequestPermissions();
        }
    }
  }

  public void checkandRequestPermissions(){

    requestPermissionsList.clear();

    fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

    if(coarseLocationPermission != PackageManager.PERMISSION_GRANTED){
      requestPermissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
    }
    if(fineLocationPermission != PackageManager.PERMISSION_GRANTED){
      requestPermissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
    }else{
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        backgroundLocationPermision = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if(backgroundLocationPermision != PackageManager.PERMISSION_GRANTED){
          requestPermissionsList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
      }
    }

    if(!requestPermissionsList.isEmpty()){
      ActivityCompat.requestPermissions(MainActivity.this,
              requestPermissionsList.toArray(new String[requestPermissionsList.size()]),
              MULTIPLE_PERMISSIONS);
    }
  }

}
