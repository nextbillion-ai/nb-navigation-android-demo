package ai.nextbillion.navigation.demo.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ai.nextbillion.kits.directions.models.DirectionsResponse;
import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.ui.NBNavigation;
import ai.nextbillion.navigation.ui.NavViewConfig;
import ai.nextbillion.navigation.ui.NavigationView;
import ai.nextbillion.navigation.ui.OnNavigationReadyCallback;
import ai.nextbillion.navigation.ui.listeners.NavigationListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationFragment extends Fragment implements OnNavigationReadyCallback, NavigationListener {

    private static final double ORIGIN_LONGITUDE = -77.04012393951416;
    private static final double ORIGIN_LATITUDE = 38.9111117447887;
    private static final double DESTINATION_LONGITUDE = -77.03847169876099;
    private static final double DESTINATION_LATITUDE = 38.91113678979344;

    private NavigationView navigationView;
    private DirectionsRoute directionsRoute;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.navigation_view_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navigationView = view.findViewById(R.id.navigation_view_fragment);
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        navigationView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            navigationView.onRestoreInstanceState(savedInstanceState);
        }
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
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        navigationView.onDestroy();
    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        Point origin = Point.fromLngLat(ORIGIN_LONGITUDE, ORIGIN_LATITUDE);
        Point destination = Point.fromLngLat(DESTINATION_LONGITUDE, DESTINATION_LATITUDE);
        fetchRoute(origin, destination);
    }

    @Override
    public void onCancelNavigation() {
        navigationView.stopNavigation();
        stopNavigation();
    }

    @Override
    public void onNavigationFinished() {
        // no-op
    }

    @Override
    public void onNavigationRunning() {
        // no-op
    }

    private void fetchRoute(Point origin, Point destination) {

        Point origin1 = Point.fromLngLat(78.39382912963629, 17.49361551715865);
        Point destination1 = Point.fromLngLat(78.39086260646582, 17.484744887695726);

        NBNavigation.fetchRoute(origin1, destination1, new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {

                //start navigation with the route we just fetched.
                if (response.body() != null && !response.body().routes().isEmpty()) {
                    directionsRoute = response.body().routes().get(0);
                    startNavigation();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {

            }
        });
    }

    private void startNavigation() {
        if (directionsRoute == null) {
            return;
        }
        NavViewConfig options = NavViewConfig.builder()
                .route(directionsRoute)
                .shouldSimulateRoute(true)
                .navigationListener(NavigationFragment.this)
                .build();
        navigationView.startNavigation(options);
    }

    private void stopNavigation() {

    }
}
