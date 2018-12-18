package com.example.admin.giftbox2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NavigateActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private String coordinate;
    private String imageUrl;
    private String points;
    private String wholePath;
    private String objectId;
    private double lon,lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        coordinate = intent.getStringExtra("coordinate");
        imageUrl = intent.getStringExtra("imageUrl");
        points = intent.getStringExtra("points");
        wholePath = intent.getStringExtra("whole");
        objectId = intent.getStringExtra("objectId");
        lon = Double.parseDouble(coordinate.split(" ")[0]);
        lat = Double.parseDouble(coordinate.split(" ")[1]);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
    }

    /**
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in and move the camera
        LatLng target = new LatLng(lat, lon);

        mMap.addMarker(new MarkerOptions().position(target).title("Target gift"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(target));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient client = LocationServices.getSettingsClient(this);
        client.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback ,
                Looper.myLooper());
    }

    private boolean isWithIn(double value, double target, double tolerance){
        return Math.abs( value - target) <= tolerance;
    }

    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        System.out.println("msg:" + msg);
        if (isWithIn(lon, location.getLongitude(),0.00005) &&
                isWithIn(lat, location.getLatitude(),0.00005)){
            Intent intent = new Intent(this, OpenCvCameraActivity.class);
            intent.putExtra("imageUrl",imageUrl);
            intent.putExtra("points",points);
            intent.putExtra("whole",wholePath);
            intent.putExtra("objectId",objectId);
            System.out.println("imageUrl:"+ imageUrl);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}
