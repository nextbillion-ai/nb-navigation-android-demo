package ai.nextbillion.navigation.demo.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.camera.CameraUpdate;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.ui.camera.CameraUpdateMode;
import ai.nextbillion.navigation.ui.camera.NavigationCameraUpdate;
import ai.nextbillion.navigation.ui.map.NavNextbillionMap;

/***
 * This class shows how to intercept the click and long click events of nbMap
 */

public class InterceptMapClickActivity extends AppCompatActivity implements OnMapReadyCallback, NextbillionMap.OnMapClickListener,
        NextbillionMap.OnMapLongClickListener {
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_CAMERA_ZOOM = 16;

    private MapView mapView;
    private NavNextbillionMap nbMap;

    // a fake point in Singapore
    private final Point fakePoint = Point.fromLngLat(103.852408, 1.276411);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intercept_click_events);
        mapView = findViewById(R.id.mapView_intercept_click_events);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng latLng) {
        Toast.makeText(
                InterceptMapClickActivity.this,
                String.format("Map was clicked with lat:%s, long: %s", latLng.getLatitude(), latLng.getLongitude()),
                Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng latLng) {
        Toast.makeText(
                InterceptMapClickActivity.this,
                String.format("Map was long clicked with lat:%s, long: %s", latLng.getLatitude(), latLng.getLongitude()),
                Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        nextbillionMap.setStyle(new Style.Builder().fromUri("https://api.nextbillion.io/maps/streets/style.json"));
        nextbillionMap.getStyle(style -> {
            // add the following listeners to nextbillionMap so the override onClick methods could work
            nextbillionMap.addOnMapClickListener(InterceptMapClickActivity.this);
            nextbillionMap.addOnMapLongClickListener(InterceptMapClickActivity.this);
            nbMap = new NavNextbillionMap(mapView, nextbillionMap);
            nbMap.updateLocationLayerRenderMode(RenderMode.COMPASS);
            animateCamera();
        });
    }

    private void animateCamera() {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(fakePoint.latitude(), fakePoint.longitude()), DEFAULT_CAMERA_ZOOM);
        NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
        navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
        nbMap.retrieveCamera().update(navigationCameraUpdate, CAMERA_ANIMATION_DURATION);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
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
