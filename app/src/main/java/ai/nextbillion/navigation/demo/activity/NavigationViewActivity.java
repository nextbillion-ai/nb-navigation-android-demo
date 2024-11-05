package ai.nextbillion.navigation.demo.activity;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.List;

import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.geojson.LineString;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.navigation.core.navigation.NavEngineConfig;
import ai.nextbillion.navigation.core.navigation.NavigationTimeFormat;
import ai.nextbillion.navigation.core.navigator.NavProgress;
import ai.nextbillion.navigation.core.navigator.ProgressChangeListener;
import ai.nextbillion.navigation.core.utils.LocaleUtils;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.demo.speech.CustomAudioFocusDelegateProvider;
import ai.nextbillion.navigation.demo.speech.CustomSpeechAudioFocusManager;
import ai.nextbillion.navigation.demo.speech.CustomSpeechListener;
import ai.nextbillion.navigation.demo.speech.CustomSpeechPlayer;
import ai.nextbillion.navigation.demo.speech.NavSpeechListener;
import ai.nextbillion.navigation.ui.NavViewConfig;
import ai.nextbillion.navigation.ui.NavigationView;
import ai.nextbillion.navigation.ui.OnNavigationReadyCallback;
import ai.nextbillion.navigation.ui.listeners.NavigationListener;
import ai.nextbillion.navigation.ui.listeners.RouteListener;
import ai.nextbillion.navigation.ui.utils.StatusBarUtils;
import ai.nextbillion.navigation.ui.voice.SpeechPlayer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static ai.nextbillion.navigation.core.utils.time.TimeFormatter.formatTime;

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
        boolean customSpeech = getIntent().getBooleanExtra("customSpeech", false);
        if (customSpeech) {
            LocaleUtils localeUtils = new LocaleUtils();
            String language = null;
            assert route != null;
            if (route.routeOptions() != null) {
                language = route.routeOptions().language();
            }
            if (language == null) {
                language = localeUtils.inferDeviceLanguage(getApplication());
            }
            SpeechPlayer speechPlayer = createSpeechPlayer(language);
            viewConfigBuilder.speechPlayer(speechPlayer);
        }
        viewConfigBuilder.route(route);
        viewConfigBuilder.routes(routes);

        // Retrieved the route geometry and create a LineString from the encoded polyline.
        String encodedPolyline = route.geometry();
        LineString lineString = LineString.fromPolyline(encodedPolyline, route.precision());

        // Calculated and formatted the estimated arrival time based on the route duration.
        Calendar time = Calendar.getInstance();
        double routeDuration = route.duration();
        int timeFormatType = NavigationTimeFormat.TWELVE_HOURS;
        boolean isTwentyFourHourFormat = true;
        String arrivalTime = formatTime(time, routeDuration, timeFormatType, isTwentyFourHourFormat);
    }

    private CustomAudioFocusDelegateProvider buildAudioFocusDelegateProvider(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return new CustomAudioFocusDelegateProvider(audioManager);
    }
    private SpeechPlayer createSpeechPlayer(String language) {
        CustomAudioFocusDelegateProvider provider = buildAudioFocusDelegateProvider(this);
        CustomSpeechAudioFocusManager audioFocusManager = new CustomSpeechAudioFocusManager(provider);
        CustomSpeechListener speechListener = new NavSpeechListener( audioFocusManager);
        return new CustomSpeechPlayer(this,language,speechListener);
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
        double durationRemaining = navProgress.durationRemaining;
        double distanceRemaining = navProgress.distanceRemaining;
        int remainingWaypoints = navProgress.remainingWaypoints;

        // Detailed location information
        String locationInfo =
                "--------- Location Info --------- " + "\n" +
                        location.getProvider() + "\n" +
                        "Latitude: " + location.getLatitude() + "\n" +
                        "Longitude: " + location.getLongitude() + "\n" +
                        "Altitude: " + location.getAltitude() + "\n" +
                        "Accuracy: " + location.getAccuracy() + "\n" +
                        "Speed: " + location.getSpeed() + "\n" +
                        "Bearing: " + location.getBearing() + "\n" +
                        "Time: " + location.getTime();

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
    public void onArrival(NavProgress navProgress, int i) {

    }

    @Override
    public void onUserInTunnel(boolean b) {

    }

    @Override
    public boolean shouldShowArriveDialog(NavProgress navProgress, int i) {
        return false;
    }

    @Override
    public BottomSheetDialog customArriveDialog(NavProgress navProgress, int i) {
        return null;
    }
}