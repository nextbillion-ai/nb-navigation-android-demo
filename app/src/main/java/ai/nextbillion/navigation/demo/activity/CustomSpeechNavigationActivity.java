package ai.nextbillion.navigation.demo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowInsets;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.navigation.core.navigation.NavEngineConfig;
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
import ai.nextbillion.navigation.ui.utils.StatusBarUtils;
import ai.nextbillion.navigation.ui.voice.SpeechPlayer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

/**
 * A self-contained NavigationView example that replaces the SDK speech player with Android TTS.
 *
 * <p>The important integration point is {@link #createSpeechPlayer(String)} followed by
 * {@link NavViewConfig.Builder#speechPlayer(SpeechPlayer)} in {@link #onNavigationReady(boolean)}.
 * The remaining methods forward the Android activity lifecycle to NavigationView.</p>
 */
public class CustomSpeechNavigationActivity extends AppCompatActivity implements
        OnNavigationReadyCallback,
        EasyPermissions.PermissionCallbacks,
        NavigationListener,
        StatusBarUtils.OnWindowInsetsChange {

    private static final String EXTRA_ROUTE =
            "ai.nextbillion.navigation.demo.customspeech.extra.ROUTE";
    private static final String EXTRA_ROUTES =
            "ai.nextbillion.navigation.demo.customspeech.extra.ROUTES";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private NavigationView navigationView;
    private DirectionsRoute route;
    private List<DirectionsRoute> routes;
    private boolean navigationViewInitialized;

    public static Intent newIntent(Context context, DirectionsRoute route,
                                   List<DirectionsRoute> routes) {
        Intent intent = new Intent(context, CustomSpeechNavigationActivity.class);
        intent.putExtra(EXTRA_ROUTE, route);
        if (routes != null) {
            intent.putExtra(EXTRA_ROUTES, new ArrayList<>(routes));
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ai.nextbillion.navigation.ui.R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_speech_navigation);

        readRouteExtras();
        navigationView = findViewById(R.id.custom_speech_navigation_view);
        navigationView.onCreate(savedInstanceState);

        StatusBarUtils.transparentStatusBar(this, this);
        if (navigationView.isNightTheme()) {
            StatusBarUtils.setDarkMode(this);
        } else {
            StatusBarUtils.setLightMode(this);
        }

        if (route == null) {
            Toast.makeText(this, R.string.custom_speech_route_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeNavigationViewWhenPermitted();
    }

    @SuppressWarnings("unchecked")
    private void readRouteExtras() {
        Serializable routeExtra = getIntent().getSerializableExtra(EXTRA_ROUTE);
        if (routeExtra instanceof DirectionsRoute) {
            route = (DirectionsRoute) routeExtra;
        }

        Serializable routesExtra = getIntent().getSerializableExtra(EXTRA_ROUTES);
        if (routesExtra instanceof List) {
            routes = (List<DirectionsRoute>) routesExtra;
        }
        if (routes == null && route != null) {
            routes = Collections.singletonList(route);
        }
    }

    private void initializeNavigationViewWhenPermitted() {
        if (EasyPermissions.hasPermissions(this, LOCATION_PERMISSIONS)) {
            initializeNavigationView();
            return;
        }

        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(
                        this,
                        LOCATION_PERMISSION_REQUEST_CODE,
                        LOCATION_PERMISSIONS
                )
                        .setRationale(getString(R.string.custom_speech_location_permission_rationale))
                        .setNegativeButtonText(android.R.string.cancel)
                        .setPositiveButtonText(android.R.string.ok)
                        .build()
        );
    }

    private void initializeNavigationView() {
        if (!navigationViewInitialized) {
            navigationViewInitialized = true;
            navigationView.initialize(this);
        }
    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        NavEngineConfig navConfig = NavEngineConfig.builder().build();
        NavViewConfig viewConfig = NavViewConfig.builder()
                .route(route)
                .routes(routes)
                .speechPlayer(createSpeechPlayer(resolveRouteLanguage()))
                .navigationListener(this)
                .shouldSimulateRoute(true)
                .showSpeedometer(true)
                .locationLayerRenderMode(RenderMode.GPS)
                .navConfig(navConfig)
                .build();

        navigationView.startNavigation(viewConfig);
    }

    private String resolveRouteLanguage() {
        if (route.routeOptions() != null
                && !TextUtils.isEmpty(route.routeOptions().language())) {
            return route.routeOptions().language();
        }
        return new LocaleUtils().inferDeviceLanguage(getApplication());
    }

    private SpeechPlayer createSpeechPlayer(String language) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        CustomAudioFocusDelegateProvider provider =
                new CustomAudioFocusDelegateProvider(audioManager);
        CustomSpeechAudioFocusManager audioFocusManager =
                new CustomSpeechAudioFocusManager(provider);
        CustomSpeechListener speechListener = new NavSpeechListener(audioFocusManager);
        return new CustomSpeechPlayer(this, language, speechListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> permissions) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && EasyPermissions.hasPermissions(this, LOCATION_PERMISSIONS)) {
            initializeNavigationView();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> permissions) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && !EasyPermissions.hasPermissions(this, LOCATION_PERMISSIONS)) {
            Toast.makeText(
                    this,
                    R.string.custom_speech_location_permission_denied,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onApplyWindowInsets(WindowInsets windowInsets) {
        navigationView.fitSystemWindow(windowInsets);
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
    public void onPause() {
        navigationView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        navigationView.onStop();
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        if (!navigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        navigationView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navigationView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        navigationView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onCancelNavigation() {
        finish();
    }

    @Override
    public void onNavigationFinished() {
        // No-op for this sample.
    }

    @Override
    public void onNavigationRunning() {
        // No-op for this sample.
    }
}
