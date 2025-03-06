package ai.nextbillion.navigation.demo.activity;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import ai.nextbillion.kits.directions.models.DirectionsResponse;
import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.directions.models.RouteRequestParams;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.location.engine.LocationEngine;
import ai.nextbillion.maps.location.engine.LocationEngineCallback;
import ai.nextbillion.maps.location.engine.LocationEngineProvider;
import ai.nextbillion.maps.location.engine.LocationEngineRequest;
import ai.nextbillion.maps.location.engine.LocationEngineResult;
import ai.nextbillion.navigation.core.location.RawLocationListener;
import ai.nextbillion.navigation.core.navigation.NBNavigatorWithoutUILauncher;
import ai.nextbillion.navigation.core.navigation.NavEngineConfig;
import ai.nextbillion.navigation.core.navigation.NavigationConstants;
import ai.nextbillion.navigation.core.navigation.NavigationEventListener;
import ai.nextbillion.navigation.core.navigator.NavProgress;
import ai.nextbillion.navigation.core.navigator.ProgressChangeListener;
import ai.nextbillion.navigation.core.offroute.NavigationState;
import ai.nextbillion.navigation.core.offroute.OffRouteListener;
import ai.nextbillion.navigation.core.offroute.OffRouteStatus;
import ai.nextbillion.navigation.core.routefetcher.NextbillionReroutingCallback;
import ai.nextbillion.navigation.core.routefetcher.RouteFetcher;
import ai.nextbillion.navigation.core.utils.DistanceFormatter;
import ai.nextbillion.navigation.core.utils.time.TimeFormatter;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.demo.activity.notification.CustomNavigationNotification;
import ai.nextbillion.navigation.demo.utils.ErrorMessageUtils;

import androidx.appcompat.app.AppCompatActivity;

import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationServiceActivity extends AppCompatActivity implements NextbillionReroutingCallback, NavigationEventListener,
        OffRouteListener, RawLocationListener, ProgressChangeListener {

    private LocationEngine locationEngine;
    private Location lastLocation;
    private DistanceFormatter distanceFormatter;
    private NBNavigatorWithoutUILauncher launcher;
    private DirectionsRoute route;
    Button fetchRoute, startNavigation, exitNavigation;
    TextView navigationOffRouteStatus, rawLocation,
            remainingDistance, remainingDuration,
            navigationRunningStatus, origin,
            routeDistance, routeDuration;

    EditText inputDes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_service);
        fetchRoute = findViewById(R.id.fetchRoute);
        startNavigation = findViewById(R.id.startNavigation);
        exitNavigation = findViewById(R.id.exitNavigation);
        navigationOffRouteStatus = findViewById(R.id.navigationOffRouteStatus);
        rawLocation = findViewById(R.id.rawLocation);
        remainingDistance = findViewById(R.id.remainingDistance);
        remainingDuration = findViewById(R.id.remainingDuration);
        navigationRunningStatus = findViewById(R.id.navigationRunningStatus);
        origin = findViewById(R.id.origin);
        routeDistance = findViewById(R.id.routeDistance);
        routeDuration = findViewById(R.id.routeDuration);
        inputDes = findViewById(R.id.input_des);

        initializeLocationEngine();

        initNavLauncher();

        fetchRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchRoute();
            }
        });

        startNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (route != null) {
                    startNavigation.setEnabled(false);
                    startNavigation(route);
                }
            }
        });

        exitNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (launcher != null) {
                    startNavigation.setEnabled(true);
                    launcher.exitNavigation();
                }
                navigationOffRouteStatus.setText("");
                rawLocation.setText("");
                remainingDistance.setText("");
                remainingDuration.setText("");
            }
        });
    }

    @SuppressLint("MissingPermission")
    protected void initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(getApplicationContext());
        LocationEngineRequest request = buildEngineRequest();
        locationEngine.requestLocationUpdates(request, new TrackCurrentLocationCallback(this), null);
        locationEngine.getLastLocation(new TrackCurrentLocationCallback(this));
    }

    private LocationEngineRequest buildEngineRequest() {
        return new LocationEngineRequest.Builder(1000)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(500)
                .build();
    }

    private void onLocationFound(Location location) {
        lastLocation = location;
        origin.setText(location.getLatitude() + "," + location.getLongitude());
    }

    @SuppressLint("MissingPermission")
    protected void fetchRoute() {
        String destination = inputDes.getText().toString();
        if (origin.getText().length() == 0 || destination.isEmpty()) {
            Toast.makeText(this, "Please input your origin and destination", Toast.LENGTH_LONG).show();
            return;
        }

        Point desPoint = Point.fromLngLat(Double.parseDouble(destination.split(",")[1]), Double.parseDouble(destination.split(",")[0]));
        RouteRequestParams.Builder builder = RouteRequestParams.builder()
                .language("en")
                .departureTime((int) (System.currentTimeMillis() / 1000));
        builder.origin(Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude()));
        builder.destination(desPoint);
        RouteFetcher.getRoute(builder.build(), new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.body() != null && !response.body().routes().isEmpty()) {
                    route = response.body().routes().get(0);
                    distanceFormatter = new DistanceFormatter(NavigationServiceActivity.this, route.routeOptions().language(), route.routeOptions().unit(), NavigationConstants.ROUNDING_INCREMENT_TEN);

                    routeDistance.setText(distanceFormatter.formatDistance(route.distance()));
                    routeDuration.setText(TimeFormatter.formatTimeRemaining(NavigationServiceActivity.this, route.duration()).toString());
                } else {
                    String errorMessage = ErrorMessageUtils.getErrorMessage(TextStreamsKt.readText(response.errorBody().charStream()));
                    Toast.makeText(NavigationServiceActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Toast.makeText(NavigationServiceActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initNavLauncher() {
        NavEngineConfig navEngineConfig = NavEngineConfig.builder()
                .navigationNotification(new CustomNavigationNotification(this))
                .build();

        launcher = new NBNavigatorWithoutUILauncher(this, navEngineConfig);
        launcher.addReroutingCallback(this);
        launcher.addNavigationEventListener(this);
        launcher.addOffRouteListener(this);
        launcher.addRawLocationListener(this);
        launcher.addProgressChangeListener(this);
    }

    protected void startNavigation(DirectionsRoute route) {
        launcher.startNavigationWithoutUI(route);
    }

    @Override
    public void onRerouteReady(java.util.List<DirectionsRoute> routes) {
        Toast toast = Toast.makeText(this, "Find a new route!", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onReroutingError(Throwable p0) {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationUpdate(Location location) {
        rawLocation.setText(location.getLatitude() + "," + location.getLongitude());
    }

    @Override
    public void onRunning(boolean isRunning) {
        navigationRunningStatus.setText(isRunning ? "ON" : "OFF");
    }

    @Override
    public void userOffRoute(NavigationState navigationState, Location location) {
        if (navigationState == NavigationState.OFF_ROUTE) {
            Toast.makeText(this, "Deviated from the suggested route", Toast.LENGTH_SHORT).show();
        }
        if (navigationState != null) {
            navigationOffRouteStatus.setText(navigationState.toString());
        }

    }

    @Override
    public void userOffRouteByRemoveLeg(int i) {

    }

    @Override
    public boolean allowRerouteFrom(Location p0) {
        return true;
    }

    @Override
    public void onProgressChange(Location location, NavProgress routeProgress) {
        remainingDistance.setText(distanceFormatter.formatDistance(routeProgress.distanceRemaining).toString());
        remainingDuration.setText(TimeFormatter.formatTimeRemaining(this, routeProgress.durationRemaining).toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (launcher != null) {
            launcher.onDestroy();
        }
    }

    private class TrackCurrentLocationCallback implements LocationEngineCallback<LocationEngineResult> {
        private final WeakReference<NavigationServiceActivity> activityWeakReference;

        TrackCurrentLocationCallback(NavigationServiceActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult locationEngineResult) {
            NavigationServiceActivity activity = activityWeakReference.get();
            if (activity != null) {
                Location location = locationEngineResult.getLastLocation();
                if (location != null) {
                    activity.onLocationFound(location);
                }
            }
        }

        @Override
        public void onFailure(Exception e) {
        }
    }

}
