package ai.nextbillion.navigation.demo;

import android.app.Application;

import ai.nextbillion.maps.Nextbillion;

/**
 * @author qiuyu
 * @Date 2023/4/3
 **/
public class NavApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Nextbillion.getInstance(getApplicationContext(), "Your Api Key");
    }
}