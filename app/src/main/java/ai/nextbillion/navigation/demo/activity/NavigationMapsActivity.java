package ai.nextbillion.navigation.demo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.nextbillion.kits.directions.models.DirectionsResponse;
import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.directions.models.RouteRequestParams;
import ai.nextbillion.kits.geojson.LineString;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdate;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.exceptions.InvalidLatLngBoundsException;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.geometry.LatLngBounds;
import ai.nextbillion.maps.location.engine.LocationEngine;
import ai.nextbillion.maps.location.engine.LocationEngineCallback;
import ai.nextbillion.maps.location.engine.LocationEngineProvider;
import ai.nextbillion.maps.location.engine.LocationEngineRequest;
import ai.nextbillion.maps.location.engine.LocationEngineResult;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.navigation.core.navigation.NavigationConstants;
import ai.nextbillion.navigation.core.routefetcher.RequestParamConsts;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.ui.NBNavigation;
import ai.nextbillion.navigation.ui.NavLauncherConfig;
import ai.nextbillion.navigation.ui.NavigationLauncher;
import ai.nextbillion.navigation.ui.camera.CameraUpdateMode;
import ai.nextbillion.navigation.ui.camera.NavigationCameraUpdate;
import ai.nextbillion.navigation.ui.map.NavNextbillionMap;
import ai.nextbillion.navigation.ui.route.OnRouteSelectionChangeListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
/// todo 4: refer to the following codes example to implement your requirements
/**
 * This activity demonstrates how to fetch a route from the Nextbillion Navigation SDK and launch
 * turn-by-turn navigation with the route.
 * What you can learn from this activity:
 * - Display a map with the Nextbillion Navigation SDK and set up the map for long click events, and route selection events
 * - Request location updates and update the map with the current location
 * - Fetch a route from the Nextbillion Navigation SDK
 * - Display the routes on the map
 * - Launch turn-by-turn navigation with the route
 * - Request location updates and updatte the map with the current location
 * - Customize the route request parameters
 * - Customize the navigation view
 */
public class NavigationMapsActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback,
        NextbillionMap.OnMapLongClickListener, OnRouteSelectionChangeListener {
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;
    private static final int DEFAULT_CAMERA_ZOOM = 16;
    private static final int REQUEST_DEPARTURE_TIME = 1784136091;
    private static final Point REQUEST_ORIGIN = Point.fromLngLat(-80.8851264, 35.1969205);
    private static final Point REQUEST_DESTINATION = Point.fromLngLat(-80.885067, 35.196718);
    private Button startNav;

    Button routesClearButton;
    private ProgressBar progress;
    MapView mapView;
    private LocationEngine locationEngine;
    private NavNextbillionMap navNextbillionMap;
    private NextbillionMap mMap;
    private final LocationCallback locationCallback = new LocationCallback(this);

    private Point currentLocation;

    private boolean locationFound;

    private final List<Point> mWayPoints = new ArrayList<>();

    private RouteRequestParams.Builder requestOptionsBuilder;

    private DirectionsRoute selectedRoute;
    private List<DirectionsRoute> mRoutes;

    boolean isLaunchWithNavigationView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);

        routesClearButton = findViewById(R.id.clearRoute);
        startNav = findViewById(R.id.startNav);
        progress = findViewById(R.id.progress);
        routesClearButton.setOnClickListener(this);
        startNav.setOnClickListener(this);
        startNav.setEnabled(false);
        routesClearButton.setEnabled(false);
        mapView.getMapAsync(this);

        requestOptionsBuilder = initRoutingOptions();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationEngine != null) {
                locationEngine.requestLocationUpdates(buildEngineRequest(), locationCallback, null);

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMap.removeOnMapLongClickListener(this);
        mapView.onDestroy();
    }

    private void showLoading() {
        progress.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        if (progress.getVisibility() == View.VISIBLE) {
            progress.setVisibility(View.INVISIBLE);
        }
    }

    RouteRequestParams.Builder initRoutingOptions() {
        return RouteRequestParams.builder()
                .mode(RequestParamConsts.MODE_CAR)
                .alternatives(false)
                .altCount(0)
                .language("en")
                .option(RequestParamConsts.FLEXIBLE)
                .crossBorder(false)
                .roadInfo(Collections.singletonList(RequestParamConsts.MAX_SPEED))
                .departureTime(REQUEST_DEPARTURE_TIME)
                .unit(RequestParamConsts.IMPERIAL)
                .overview(RequestParamConsts.OVERVIEW_FULL)
                .geometry(RequestParamConsts.GEOMETRY_POLYLINE6);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.startNav) {
            if (isLaunchWithNavigationView) {
                launchNavigationWithNavigationView();
            } else {
                launchNavigationWithNavigationLauncher();
            }

        } else if (view.getId() == R.id.clearRoute) {
            mWayPoints.clear();
            navNextbillionMap.clearMarkers();
            navNextbillionMap.removeRoute();
            startNav.setEnabled(false);
            routesClearButton.setEnabled(false);
            selectedRoute = null;
            mRoutes = null;
        }
    }

    private void launchNavigationWithNavigationLauncher() {
        NavLauncherConfig.Builder configBuilder = NavLauncherConfig.builder(selectedRoute);
        configBuilder.routes(mRoutes);
        configBuilder.locationLayerRenderMode(RenderMode.GPS);
        configBuilder.shouldSimulateRoute(true); // set to false to use real device location
        configBuilder.dissolvedRouteEnabled(true); // set to false to disable dissolved route
        configBuilder.shouldShowArriveDialog(true); // set to false to disable arrive dialog
        configBuilder.showSpeedometer(true); // set to false to disable speedometer display
        configBuilder.themeMode(NavigationConstants.NAVIGATION_VIEW_FOLLOW_SYSTEM_MODE); // set to NavigationConstants.NAVIGATION_VIEW_DARK_MODE for dark mode or NavigationConstants.NAVIGATION_VIEW_LIGHT_MODE for light mode
        NavigationLauncher.startNavigation(NavigationMapsActivity.this, configBuilder.build());
    }

    private void launchNavigationWithNavigationView() {
            Intent intent = new Intent(this,  NavigationViewActivity.class);
            intent.putExtra("route", selectedRoute);
            intent.putExtra("routes", (Serializable) mRoutes);
            startActivity(intent);
    }

    /**
     * Initialize the location engine and request location updates
     */
    private void initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission();
            return;
        }

        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates(buildEngineRequest(), locationCallback, null);
        }
    }

    @NonNull
    private LocationEngineRequest buildEngineRequest() {
        return new LocationEngineRequest.Builder(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .build();
    }

    /// MARK: - Location Engine Callback
    void updateCurrentLocation(Point currentLocation) {
        this.currentLocation = currentLocation;
    }

    void onLocationFound(Location location) {
        navNextbillionMap.updateLocation(location);
        if (!locationFound) {
            animateCamera(new LatLng(location.getLatitude(), location.getLongitude()));
            Snackbar.make(mapView, R.string.explanation_long_press_waypoint, BaseTransientBottomBar.LENGTH_LONG).show();
            locationFound = true;
            hideLoading();
        }
    }

    /// MARK: - animateCamera camera methods
    private void animateCameraBox(LatLngBounds bounds, int animationTime, int[] padding) {
        CameraPosition position = navNextbillionMap.retrieveMap().getCameraForLatLngBounds(bounds, padding);
        if (position == null) {
            return;
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
        NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
        navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
        navNextbillionMap.retrieveCamera().update(navigationCameraUpdate, animationTime);
    }

    private void animateCamera(LatLng point) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, DEFAULT_CAMERA_ZOOM);
        NavigationCameraUpdate navigationCameraUpdate = new NavigationCameraUpdate(cameraUpdate);
        navigationCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
        navNextbillionMap.retrieveCamera().update(navigationCameraUpdate, CAMERA_ANIMATION_DURATION);
    }

    public void boundCameraToRoute(DirectionsRoute route) {
        if (route != null) {
            List<Point> routeCoords = LineString.fromPolyline(route.geometry(),
                    route.precision()).coordinates();
            List<LatLng> boxPoints = new ArrayList<>();
            for (Point point : routeCoords) {
                boxPoints.add(new LatLng(point.latitude(), point.longitude()));
            }
            if (boxPoints.size() > 1) {
                try {
                    LatLngBounds bounds = new LatLngBounds.Builder().includes(boxPoints).build();
                    // left, top, right, bottom
                    int topPadding = startNav.getHeight() * 2;
                    animateCameraBox(bounds, CAMERA_ANIMATION_DURATION, new int[]{50, topPadding, 50, 100});
                } catch (InvalidLatLngBoundsException exception) {
                    Toast.makeText(this, exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /// MARK: - Map Callback
    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        mMap = nextbillionMap;
        mMap.getStyle(style -> {
            mMap.addOnMapLongClickListener(this);
            navNextbillionMap = new NavNextbillionMap(mapView, nextbillionMap);
            navNextbillionMap.setOnRouteSelectionChangeListener(this);
            navNextbillionMap.updateLocationLayerRenderMode(RenderMode.COMPASS);
            showConfiguredRoute();
            initializeLocationEngine();
        });
    }

    @Override
    public void onNewPrimaryRouteSelected(DirectionsRoute directionsRoute) {
        selectedRoute = directionsRoute;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng latLng) {
        Point selectedPoint = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
        onDestinationSelected(selectedPoint);
        return false;
    }

    private void onDestinationSelected(Point point) {
        mWayPoints.add(point);
        LatLng latLng = new LatLng(point.latitude(), point.longitude());
        navNextbillionMap.retrieveMap().easeCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().zoom(15).target(latLng).build()), 500);
        navNextbillionMap.clearMarkers();
        navNextbillionMap.addMarker(this, point);
        startNav.setEnabled(false);
        if (locationFound) {
            fetchNBRoute(currentLocation, point);
        }
    }

    private void showConfiguredRoute() {
        mWayPoints.clear();
        mWayPoints.add(REQUEST_DESTINATION);
        navNextbillionMap.clearMarkers();
        navNextbillionMap.addMarker(this, REQUEST_DESTINATION);
        animateCamera(new LatLng(REQUEST_ORIGIN.latitude(), REQUEST_ORIGIN.longitude()));
        fetchNBRoute(REQUEST_ORIGIN, REQUEST_DESTINATION);
    }

    /// MARK: - Fetch Route
    private void fetchNBRoute(Point origin, Point destination) {
        if (mWayPoints.size() >= 3) {
            List<Point> waypoints = mWayPoints.subList(1, mWayPoints.size() - 1);
            Log.w("Waypoints", "waypoints count : " + waypoints.size());
            requestOptionsBuilder.waypoints(waypoints);
        }
        Log.w("Waypoints", "mWayPoints count : " + mWayPoints.size());
        showLoading();
        NBNavigation.fetchRoute(origin, destination, requestOptionsBuilder, new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                hideLoading();
                if (response.isSuccessful()) {
                    DirectionsResponse directionsResponse = response.body();
                    if (directionsResponse == null || directionsResponse.routes() == null
                            || directionsResponse.routes().isEmpty()) {
                        Snackbar.make(mapView, "No route returned", BaseTransientBottomBar.LENGTH_SHORT).show();
                        return;
                    }
                    DirectionsRoute route = directionsResponse.routes().get(0);
                    selectedRoute = route;
                    mRoutes = directionsResponse.routes();
                    startNav.setEnabled(true);
                    routesClearButton.setEnabled(true);
                    startNav.setVisibility(View.VISIBLE);
                    navNextbillionMap.drawRoutes(mRoutes);
                    boundCameraToRoute(route);
                } else {
                    Snackbar.make(mapView, response.toString(), BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                hideLoading();
            }
        });
    }
    /// MARK: - Location Engine Implementation

    ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocation = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocation = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if (fineLocation != null && fineLocation) {
                    requestLocationUpdates();
                } else if (coarseLocation != null && coarseLocation) {
                    requestLocationUpdates();
                } else {
                    Snackbar.make(mapView, R.string.error_location_denined, BaseTransientBottomBar.LENGTH_LONG).show();
                }

            }
    );

    // Call this method to request permissions
    private void requestLocationPermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
        }
        locationPermissionRequest.launch(permissions);
    }

    private static class LocationCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<NavigationMapsActivity> activityWeakReference;

        LocationCallback(NavigationMapsActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            NavigationMapsActivity activity = activityWeakReference.get();
            if (activity != null) {
                Location location = result.getLastLocation();
                if (location == null) {
                    return;
                }
                activity.updateCurrentLocation(Point.fromLngLat(location.getLongitude(), location.getLatitude()));
                activity.onLocationFound(location);
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {

        }
    }
}
