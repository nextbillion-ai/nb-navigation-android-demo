package ai.nextbillion.navigation.demo.activity;

import android.Manifest;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;

import java.util.List;

import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.navigation.core.navigation.NavEngineConfig;
import ai.nextbillion.navigation.core.navigator.NavProgress;
import ai.nextbillion.navigation.core.navigator.ProgressChangeListener;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.ui.NavViewConfig;
import ai.nextbillion.navigation.ui.NavigationView;
import ai.nextbillion.navigation.ui.OnNavigationReadyCallback;
import ai.nextbillion.navigation.ui.listeners.NavigationListener;
import ai.nextbillion.navigation.ui.listeners.RouteListener;
import ai.nextbillion.navigation.ui.utils.StatusBarUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class NavigationViewActivity extends AppCompatActivity implements OnNavigationReadyCallback, EasyPermissions.PermissionCallbacks, NavigationListener, StatusBarUtils.OnWindowInsetsChange, ProgressChangeListener, RouteListener {

    NavigationView navigationView;

    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ai.nextbillion.navigation.ui.R.style.Theme_AppCompat_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_view);

        navigationView = findViewById(R.id.navigation_view);
        navigationView.onCreate(savedInstanceState);
        StatusBarUtils.transparentStatusBar(this,this);
        if (navigationView.isNightTheme()) {
            StatusBarUtils.setDarkMode(this);
        } else {
            StatusBarUtils.setLightMode(this);
        }

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void requestPermissions() {
        EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, 200, permissions)
                .setRationale("Navigation requires location access")
                .setNegativeButtonText("No")
                .setPositiveButtonText("OK")
                .build());
    }

    @Override
    public void onNavigationReady(boolean b) {
        NavEngineConfig.Builder navConfig = NavEngineConfig.builder();
        NavViewConfig.Builder viewConfigBuilder = NavViewConfig.builder();
        viewConfigBuilder.navigationListener(this);
        viewConfigBuilder.progressChangeListener(this);
        viewConfigBuilder.routeListener(this);
        configRoute(viewConfigBuilder);
        viewConfigBuilder.shouldSimulateRoute(true);
        viewConfigBuilder.showSpeedometer(true);
        viewConfigBuilder.locationLayerRenderMode(RenderMode.GPS);
        viewConfigBuilder.navConfig(navConfig.build());

        navigationView.startNavigation(viewConfigBuilder.build());
    }

    private void configRoute(NavViewConfig.Builder viewConfigBuilder) {
        DirectionsRoute route = (DirectionsRoute) getIntent().getSerializableExtra("route");
        List<DirectionsRoute> routes = (List<DirectionsRoute>) getIntent().getSerializableExtra("routes");
        viewConfigBuilder.route(route);
        viewConfigBuilder.routes(routes);
    }


    @Override
    public void onStart() {
        super.onStart();
        navigationView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        // If the navigation view didn't need to do anything, call super
        if (!navigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        navigationView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navigationView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        navigationView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        navigationView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationView.onDestroy();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        navigationView.initialize(this);

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onCancelNavigation() {
        finish();
    }

    @Override
    public void onNavigationFinished() {

    }

    @Override
    public void onNavigationRunning() {

    }

    @Override
    public void onApplyWindowInsets(WindowInsets windowInsets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            navigationView.fitSystemWindow(windowInsets);
        }
    }

    @Override
    public void onProgressChange(Location location, NavProgress navProgress) {

    }

    @Override
    public boolean allowRerouteFrom(Location location) {
        return true;
    }

    @Override
    public void onOffRoute(Point point) {

    }

    @Override
    public void onRerouteAlong(DirectionsRoute directionsRoute) {

    }

    @Override
    public void onFailedReroute(String s) {

    }

    @Override
    public void onArrival() {

    }

    @Override
    public void onUserInTunnel(boolean b) {

    }
}