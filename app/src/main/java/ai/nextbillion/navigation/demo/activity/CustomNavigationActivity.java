package ai.nextbillion.navigation.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import ai.nextbillion.kits.directions.models.DirectionsResponse;
import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.directions.models.RouteRequestParams;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.navigation.core.routefetcher.RouteFetcher;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.demo.utils.ErrorMessageUtils;
import androidx.appcompat.app.AppCompatActivity;
import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomNavigationActivity extends AppCompatActivity implements View.OnClickListener {

    private Button fetchRoute;
    private Button startNav;
    private TextView routeGeometry;
    private DirectionsRoute directionsRoute;
    private List<DirectionsRoute> directionsRoutes;
    private ProgressBar progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_navigation);
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

            RouteRequestParams.Builder builder = RouteRequestParams.builder()
                    .origin(origin)
                    .destination(destination)
                    .language("en")
                    .departureTime((int) (System.currentTimeMillis()/1000));

            RouteFetcher.getRoute(builder.build(), new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                    progress.setVisibility(View.GONE);
                    //start navigation with the route we just fetched.
                    if (response.body() != null && !response.body().routes().isEmpty()) {
                        directionsRoute = response.body().routes().get(0);
                        directionsRoutes = response.body().routes();
                        routeGeometry.setText(String.format("Route Geometry: %s", directionsRoute.geometry()));
                        startNav.setEnabled(true);
                    } else {
                        String errorMessage = ErrorMessageUtils.getErrorMessage(TextStreamsKt.readText(response.errorBody().charStream()));
                        Toast.makeText(CustomNavigationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                }
            });
        } else if (view.getId() == R.id.startNav) {
            Intent intent = new Intent(this,  NavigationViewActivity.class);
            intent.putExtra("route", directionsRoute);
            intent.putExtra("routes", (Serializable) directionsRoutes);
            startActivity(intent);
        }
    }
}