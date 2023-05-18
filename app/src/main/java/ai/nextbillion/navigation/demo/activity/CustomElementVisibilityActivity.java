package ai.nextbillion.navigation.demo.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import ai.nextbillion.kits.directions.models.DirectionsResponse;
import ai.nextbillion.kits.directions.models.RouteRequestParams;
import ai.nextbillion.navigation.core.routefetcher.RequestParamsWrapper;
import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.ui.NavViewConfig;
import ai.nextbillion.navigation.ui.NavigationView;
import ai.nextbillion.navigation.ui.OnNavigationReadyCallback;
import ai.nextbillion.navigation.ui.listeners.NavigationListener;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;


import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/***
 * This class demonstrates how to add an extra switch button on Appbar
 * and use the switch button to control the display of UI elements in Navigation view
 */

public class CustomElementVisibilityActivity extends AppCompatActivity implements OnNavigationReadyCallback, NavigationListener {
    private NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_element_visibility);
        navigationView = findViewById(R.id.custom_element_navigation_view);
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        MenuItem item = menu.findItem(R.id.myswitch);
        SwitchCompat mySwitch = (SwitchCompat) item.getActionView();

        // adding this here to keep the initial display consistency of the UI elements
        controlDisplayOfElements(mySwitch.isChecked());
        // The on click event will be triggered each time user click the switch button on App bar
        mySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            controlDisplayOfElements(isChecked);
        });
        return true;
    }

    private void controlDisplayOfElements(boolean shouldDisplay) {
        if (shouldDisplay) {
            navigationView.showRecenterBtn();
            navigationView.retrieveSoundButton().show();
        } else {
            navigationView.hideRecenterBtn();
            navigationView.retrieveSoundButton().hide();
        }
    }

    @Override
    public void onNavigationReady(boolean b) {
        // read original backend response from file
        DirectionsResponse response = DirectionsResponse.fromJson(loadJsonFromAsset());
        // modify the backend response following normal api call process
        DirectionsResponse modifiedResponse = new RequestParamsWrapper().wrapRequestParams(response, RouteRequestParams.builder().build());
        NavViewConfig.Builder config =
                NavViewConfig.builder().route(modifiedResponse.routes().get(0))
                        // The visibility of speedometer can be set here before starting navigation
                        .showSpeedometer(true)
                        // add this so the exit button in Navigation view would work
                        .navigationListener(this)
                        .shouldSimulateRoute(true);
        navigationView.startNavigation(config.build());
    }

    private static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private String loadJsonFromAsset() {
        // Using this method to load in GeoJSON files from the assets folder.
        try {
            return convertStreamToString(getApplicationContext().getAssets().open("directions-route.json"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
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
    public void onCancelNavigation() {
        finish();
    }

    @Override
    public void onNavigationFinished() {

    }

    @Override
    public void onNavigationRunning() {

    }
}