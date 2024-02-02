package ai.nextbillion.navigation.demo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ai.nextbillion.kits.directions.models.DirectionsResponse;
import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.demo.utils.ErrorMessageUtils;
import ai.nextbillion.navigation.ui.NBNavigation;
import ai.nextbillion.navigation.ui.NavLauncherConfig;
import ai.nextbillion.navigation.ui.NavigationLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomBottomETAPanelStyleActivity extends AppCompatActivity implements View.OnClickListener {

    private Button fetchRoute;
    private Button startNav;
    private TextView routeGeometry;
    private DirectionsRoute directionsRoute;
    private ProgressBar progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_bottom_etapanel_style);
        fetchRoute = findViewById(R.id.fetchRoute);
        startNav = findViewById(R.id.startNav);
        routeGeometry = findViewById(R.id.routeGeometry);
        progress = findViewById(R.id.progress);
        fetchRoute.setOnClickListener(this);
        startNav.setOnClickListener(this);
        startNav.setEnabled(false);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fetchRoute) {
            progress.setVisibility(View.VISIBLE);
            Point origin = Point.fromLngLat(103.75986708439264, 1.312533169133601);
            Point destination = Point.fromLngLat(103.77982271935586, 1.310473772283314);

            NBNavigation.fetchRoute(origin, destination, new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                    progress.setVisibility(View.GONE);
                    //start navigation with the route we just fetched.
                    if (response.body() != null && !response.body().routes().isEmpty()) {
                        directionsRoute = response.body().routes().get(0);
                        routeGeometry.setText(String.format("Route Geometry: %s", directionsRoute.geometry()));
                        startNav.setEnabled(true);
                    } else {
                        String errorMessage = ErrorMessageUtils.getErrorMessage(TextStreamsKt.readText(response.errorBody().charStream()));
                        Toast.makeText(CustomBottomETAPanelStyleActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable t) {
                    progress.setVisibility(View.GONE);
                }
            });
        } else if (view.getId() == R.id.startNav) {
            NavLauncherConfig.Builder configBuilder = NavLauncherConfig.builder(directionsRoute);
            configBuilder.locationLayerRenderMode(RenderMode.GPS);
            configBuilder.shouldSimulateRoute(true);
            configBuilder.lightThemeResId(R.style.Theme_CustomNavBottomETAPanel);
            configBuilder.darkThemeResId(R.style.Theme_CustomNavBottomETAPanelDark);
            NavigationLauncher.startNavigation(CustomBottomETAPanelStyleActivity.this, configBuilder.build());
        }
    }
}