package ai.nextbillion.navigation.demo.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import ai.nextbillion.navigation.demo.R;
import ai.nextbillion.navigation.demo.activity.fragment.NavigationFragment;

public class FragmentNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_navigation);

        initializeNavigationViewFragment(savedInstanceState);
    }

    private void initializeNavigationViewFragment(@Nullable Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.disallowAddToBackStack();
            transaction.add(R.id.navigation_fragment_frame, new NavigationFragment()).commit();
        }
    }
}