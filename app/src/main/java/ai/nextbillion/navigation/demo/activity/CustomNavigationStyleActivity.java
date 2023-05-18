package ai.nextbillion.navigation.demo.activity;

import android.location.Location;
import android.os.Bundle;

import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.Nextbillion;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/***
 * This example shows how to change the map style of NavNextbillionMap dynamically
 * You can also use style from other sources if available
 */

public class CustomNavigationStyleActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_CAMERA_ZOOM = 16;

    private MapView mapView;
    private FloatingActionButton floatingActionButton;
    private NavNextbillionMap nbMap;
    // a fake point in Singapore
    private final Point fakePoint = Point.fromLngLat(103.852408, 1.276411);
    private ArrayList<String> styleList = new ArrayList<>();
    private int floatingButtonClickCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_navigation_style);

        setUpStyleList();
        setUpFloatingActionButton();

        mapView = findViewById(R.id.mapView_customize_style);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    // a set of map styles provided by NextBillion.ai
    private void setUpStyleList() {
        styleList.add("https://api.nextbillion.io/maps/streets/style.json");
        styleList.add("https://api.nextbillion.io/maps/hybrid/style.json");
        styleList.add("https://api.nextbillion.io/maps/dark/style.json");
    }

    // here we use a counter to decide which map style should be set to the map
    private void setUpFloatingActionButton() {
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(view -> {
            floatingButtonClickCount = (floatingButtonClickCount + 1) % styleList.size();
            nbMap.retrieveMap().setStyle(new Style.Builder().fromUri(styleList.get(floatingButtonClickCount)));
        });
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        nextbillionMap.setStyle(new Style.Builder().fromUri(styleList.get(floatingButtonClickCount)));
        nextbillionMap.getStyle(style -> {
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