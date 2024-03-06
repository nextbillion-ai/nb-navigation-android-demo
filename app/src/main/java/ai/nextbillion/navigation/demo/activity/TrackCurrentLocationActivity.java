package ai.nextbillion.navigation.demo.activity;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.List;

import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.location.LocationComponent;
import ai.nextbillion.maps.location.LocationComponentActivationOptions;
import ai.nextbillion.maps.location.LocationComponentOptions;
import ai.nextbillion.maps.location.OnCameraTrackingChangedListener;
import ai.nextbillion.maps.location.engine.LocationEngine;
import ai.nextbillion.maps.location.engine.LocationEngineCallback;
import ai.nextbillion.maps.location.engine.LocationEngineProvider;
import ai.nextbillion.maps.location.engine.LocationEngineRequest;
import ai.nextbillion.maps.location.engine.LocationEngineResult;
import ai.nextbillion.maps.location.modes.CameraMode;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.maps.location.permissions.PermissionsListener;
import ai.nextbillion.maps.location.permissions.PermissionsManager;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.ui.map.NavNextbillionMap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/***
 * The example shows how to request user location and display on our MapView
 * it assumes that you have granted location permissions for your application.
 * if location permission is not granted, the demo code will not be able to fetch device location properly
 */

public class TrackCurrentLocationActivity extends AppCompatActivity implements OnMapReadyCallback, OnCameraTrackingChangedListener {
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_CAMERA_ZOOM = 16;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;

    private MapView mapView;
    private FloatingActionButton floatingActionButton;
    private NextbillionMap nbMap;
    private NavNextbillionMap navNextbillionMap;
    private final TrackCurrentLocationCallback callback = new TrackCurrentLocationCallback(this);
    private LocationEngine locationEngine;
    private Location lastLocation;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_current_location);
        mapView = findViewById(R.id.mapView_get_location);
        mapView.onCreate(savedInstanceState);
        setUpFloatingButton();

        checkPermissions();
    }

    private void checkPermissions() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            mapView.getMapAsync(this);
        } else {
            permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> permissionsToExplain) {
                    Toast.makeText(TrackCurrentLocationActivity.this, "You need to accept location permissions.",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionResult(boolean granted) {
                    if (granted) {
                        mapView.getMapAsync(TrackCurrentLocationActivity.this);
                    } else {
                        finish();
                    }
                }
            });
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void setUpFloatingButton() {
        floatingActionButton = findViewById(R.id.get_location);
        floatingActionButton.setOnClickListener(v -> {
            if (nbMap != null) {
                LocationComponent locationComponent = nbMap.getLocationComponent();
                locationComponent.setCameraMode(CameraMode.TRACKING);
            }
        });
    }

    // This function demonstrate how to focus camera to user location with certain zoom level
//    private void animateCamera() {
//        if (nbMap == null) {
//            return;
//        }
//        lastLocation = nbMap.getLocationComponent().getLastKnownLocation();
//        if (lastLocation != null) {
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), DEFAULT_CAMERA_ZOOM);
//            NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
//            navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
//            navNextbillionMap.retrieveCamera().update(navigationCameraUpdate, CAMERA_ANIMATION_DURATION);
//        }
//    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        nbMap = nextbillionMap;
        nextbillionMap.moveCamera(CameraUpdateFactory.zoomBy(16));
        nextbillionMap.setStyle(new Style.Builder().fromUri("https://api.nextbillion.io/maps/streets/style.json"), style -> {
            navNextbillionMap = new NavNextbillionMap(mapView, nextbillionMap);
            initializeLocationEngine();
            initLocationComponent(style);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void onLocationFound(Location location) {
    }

    private LocationEngineRequest buildEngineRequest() {
        return new LocationEngineRequest.Builder(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .build();
    }

    @SuppressWarnings({"MissingPermission"})
    private void initializeLocationEngine() {
        // here we use location engine to update user current location
        locationEngine = LocationEngineProvider.getBestLocationEngine(getApplicationContext());
        LocationEngineRequest request = buildEngineRequest();
        locationEngine.requestLocationUpdates(request, callback, null);
        locationEngine.getLastLocation(callback);
    }


    @SuppressLint("MissingPermission")
    private void initLocationComponent(Style style) {
        LocationComponent locationComponent = nbMap.getLocationComponent();

        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions
                        .builder(this, style)
                        //use default location engine
                        .useDefaultLocationEngine(true)
                        //or use custom location engine
//                        .locationEngine(locationEngine)
                        .locationComponentOptions(LocationComponentOptions.builder(this)
                                .pulseEnabled(true)
                                .build())
                        .build());

        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setRenderMode(RenderMode.NORMAL);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.addOnCameraTrackingChangedListener(this);
    }

    @Override
    public void onCameraTrackingDismissed() {

    }

    @Override
    public void onCameraTrackingChanged(int i) {

    }

    private static class TrackCurrentLocationCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<TrackCurrentLocationActivity> activityWeakReference;

        TrackCurrentLocationCallback(TrackCurrentLocationActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult locationEngineResult) {
            TrackCurrentLocationActivity activity = activityWeakReference.get();
            if (activity != null) {
                Location location = locationEngineResult.getLastLocation();
                if (location == null) {
                    return;
                }
                activity.onLocationFound(location);
            }
        }

        @Override
        public void onFailure(@NonNull Exception e) {

        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates(buildEngineRequest(), callback, null);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}