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

public class CustomNavigationActivity extends NavigationMapsActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLaunchWithNavigationView = true;
    }
}