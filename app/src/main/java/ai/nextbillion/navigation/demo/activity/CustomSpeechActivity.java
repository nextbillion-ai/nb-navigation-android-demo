package ai.nextbillion.navigation.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import ai.nextbillion.kits.directions.models.DirectionsResponse;
import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.directions.models.RouteRequestParams;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.navigation.core.routefetcher.RouteFetcher;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.demo.utils.ErrorMessageUtils;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fetches a route for the custom speech sample.
 *
 * <p>The actual NavigationView and custom SpeechPlayer integration live in
 * {@link CustomSpeechNavigationActivity}. Keeping route preparation and navigation in separate
 * activities makes the custom speech integration easier to copy into another application.</p>
 */
public class CustomSpeechActivity extends AppCompatActivity {

    private static final Point SAMPLE_ORIGIN =
            Point.fromLngLat(103.75986708439264, 1.312533169133601);
    private static final Point SAMPLE_DESTINATION =
            Point.fromLngLat(103.77982271935586, 1.310473772283314);

    private Button fetchRouteButton;
    private Button startNavigationButton;
    private TextView routeGeometryText;
    private ProgressBar progressBar;

    private DirectionsRoute directionsRoute;
    private List<DirectionsRoute> directionsRoutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_speech);

        fetchRouteButton = findViewById(R.id.custom_speech_fetch_route);
        startNavigationButton = findViewById(R.id.custom_speech_start_navigation);
        routeGeometryText = findViewById(R.id.custom_speech_route_geometry);
        progressBar = findViewById(R.id.custom_speech_progress);

        fetchRouteButton.setOnClickListener(view -> fetchRoute());
        startNavigationButton.setOnClickListener(view -> startCustomSpeechNavigation());
        startNavigationButton.setEnabled(false);
    }

    private void fetchRoute() {
        setLoading(true);

        RouteRequestParams requestParams = RouteRequestParams.builder()
                .origin(SAMPLE_ORIGIN)
                .destination(SAMPLE_DESTINATION)
                // The route language is also used to select the Android TTS language.
                .language("en")
                .departureTime((int) (System.currentTimeMillis() / 1000))
                .build();

        RouteFetcher.getRoute(requestParams, new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call,
                                   Response<DirectionsResponse> response) {
                setLoading(false);

                DirectionsResponse body = response.body();
                if (response.isSuccessful() && body != null && body.routes() != null
                        && !body.routes().isEmpty()) {
                    directionsRoutes = body.routes();
                    directionsRoute = directionsRoutes.get(0);
                    routeGeometryText.setText(getString(
                            R.string.custom_speech_route_geometry,
                            directionsRoute.geometry()
                    ));
                    startNavigationButton.setEnabled(true);
                    return;
                }

                showRouteError(response.errorBody());
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                setLoading(false);
                Toast.makeText(
                        CustomSpeechActivity.this,
                        R.string.custom_speech_route_fetch_failed,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void startCustomSpeechNavigation() {
        if (directionsRoute == null || directionsRoutes == null) {
            return;
        }

        Intent intent = CustomSpeechNavigationActivity.newIntent(
                this,
                directionsRoute,
                directionsRoutes
        );
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? ProgressBar.VISIBLE : ProgressBar.GONE);
        fetchRouteButton.setEnabled(!loading);
        startNavigationButton.setEnabled(
                !loading && directionsRoute != null && directionsRoutes != null
        );
    }

    private void showRouteError(ResponseBody errorBody) {
        String errorMessage = null;
        if (errorBody != null) {
            try {
                errorMessage = ErrorMessageUtils.getErrorMessage(errorBody.string());
            } catch (IOException ignored) {
                // Fall back to the sample's generic message below.
            }
        }

        if (TextUtils.isEmpty(errorMessage)) {
            errorMessage = getString(R.string.custom_speech_route_fetch_failed);
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
