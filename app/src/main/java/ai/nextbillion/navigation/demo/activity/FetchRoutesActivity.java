package ai.nextbillion.navigation.demo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ai.nextbillion.kits.directions.models.DirectionsResponse;
import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.directions.models.RouteRequestParams;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.navigation.core.routefetcher.RequestParamConsts;
import ai.nextbillion.navigation.core.routefetcher.RouteFetcher;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.demo.utils.ErrorMessageUtils;
import androidx.appcompat.app.AppCompatActivity;
import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FetchRoutesActivity extends AppCompatActivity implements View.OnClickListener {
    private Button fetchRoute;
    private TextView routeGeometry;
    private DirectionsRoute directionsRoute;
    private ProgressBar progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch_routes);
        fetchRoute = findViewById(R.id.fetchRoute);
        routeGeometry = findViewById(R.id.routeGeometry);
        progress = findViewById(R.id.progress);
        fetchRoute.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fetchRoute) {
            progress.setVisibility(View.VISIBLE);
            Point origin = Point.fromLngLat(103.75986708439264, 1.312533169133601);
            Point destination = Point.fromLngLat(103.77982271935586, 1.310473772283314);

            List<String> avoid = new ArrayList<>();
            avoid.add(RequestParamConsts.AVOID_HIGHWAY);

            List<Point> waypoints = new ArrayList<>();
            waypoints.add(Point.fromLngLat(103.76974384826651,1.3207136058314086));

            RouteRequestParams.Builder builder = RouteRequestParams.builder()
                    .origin(origin)
                    .destination(destination)
                    .alternatives(true)
                    .altCount(2)
                    .avoid(avoid)
                    .language("en")
                    .waypoints(waypoints)
                    .option(RequestParamConsts.FLEXIBLE)
                    .departureTime((int) (System.currentTimeMillis()/1000));

            RouteFetcher.getRoute(builder.build(), new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                    progress.setVisibility(View.GONE);
                    //start navigation with the route we just fetched.
                    if (response.body() != null && !response.body().routes().isEmpty()) {
                        directionsRoute = response.body().routes().get(0);
                        routeGeometry.setText(String.format("Route Geometry: %s", directionsRoute.geometry()));
                    } else {
                        String errorMessage = ErrorMessageUtils.getErrorMessage(TextStreamsKt.readText(response.errorBody().charStream()));
                        Toast.makeText(FetchRoutesActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                }
            });
        }
    }

}