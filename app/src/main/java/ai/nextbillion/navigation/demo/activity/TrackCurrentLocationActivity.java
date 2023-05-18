package ai.nextbillion.navigation.demo.activity;

import android.location.Location;
import android.os.Bundle;

import ai.nextbillion.maps.Nextbillion;
import ai.nextbillion.maps.camera.CameraUpdate;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.location.engine.LocationEngine;
import ai.nextbillion.maps.location.engine.LocationEngineCallback;
import ai.nextbillion.maps.location.engine.LocationEngineProvider;
import ai.nextbillion.maps.location.engine.LocationEngineRequest;
import ai.nextbillion.maps.location.engine.LocationEngineResult;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.ui.camera.CameraUpdateMode;
import ai.nextbillion.navigation.ui.camera.NavigationCameraUpdate;
import ai.nextbillion.navigation.ui.map.NavNextbillionMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;

/***
 * The example shows how to request user location and display on our MapView
 * it assumes that you have granted location permissions for your application.
 * if location permission is not granted, the demo code will not be able to fetch device location properly
 */

public class TrackCurrentLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_CAMERA_ZOOM = 16;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;

    private MapView mapView;
    private FloatingActionButton floatingActionButton;
    private NavNextbillionMap nbMap;
    private final TrackCurrentLocationCallback callback = new TrackCurrentLocationCallback(this);
    private LocationEngine locationEngine;
    private Location lastLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_current_location);
        mapView = findViewById(R.id.mapView_get_location);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        setUpFloatingButton();
    }

    private void setUpFloatingButton() {
        floatingActionButton = findViewById(R.id.get_location);
        floatingActionButton.setOnClickListener(v -> animateCamera());
    }

    // This function demonstrate how to focus camera to user location with certain zoom level
    private void animateCamera() {
        if (lastLocation != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), DEFAULT_CAMERA_ZOOM);
            NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
            navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
            nbMap.retrieveCamera().update(navigationCameraUpdate, CAMERA_ANIMATION_DURATION);
        }
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        nextbillionMap.setStyle(new Style.Builder().fromUri("https://api.nextbillion.io/maps/streets/style.json"));
        nextbillionMap.getStyle(style -> {
            nbMap = new NavNextbillionMap(mapView, nextbillionMap);
            nbMap.updateLocationLayerRenderMode(RenderMode.GPS);
            initializeLocationEngine();
        });
    }

    private void onLocationFound(Location location) {
        lastLocation = location;
        nbMap.updateLocation(location);
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