package ai.nextbillion.navigation.demo.activity.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

import ai.nextbillion.kits.directions.models.DirectionsRoute;
import ai.nextbillion.kits.directions.models.LegStep;
import ai.nextbillion.kits.directions.models.instruction.BannerInstructions;
import ai.nextbillion.navigation.core.navigation.NavigationConstants;
import ai.nextbillion.navigation.core.navigation.notification.NavigationNotification;
import ai.nextbillion.navigation.core.navigator.NavProgress;
import ai.nextbillion.navigation.core.utils.DistanceFormatter;
import ai.nextbillion.navigation.core.utils.ManeuverUtils;
import ai.nextbillion.navigation.demo.R;
import androidx.core.app.NotificationCompat;

import static ai.nextbillion.navigation.core.navigation.NavigationConstants.NAVIGATION_NOTIFICATION_CHANNEL;

public class CustomNavigationNotification implements NavigationNotification {

    private static final int CUSTOM_NOTIFICATION_ID = 91234821;
    private final Notification customNotification;
    private NotificationCompat.Builder customNotificationBuilder;
    private final NotificationManager notificationManager;
    private DistanceFormatter distanceFormatter;
    private Context mContext;
    private int currentManeuverId;

    PendingIntent pendingOpenIntent;

    public CustomNavigationNotification(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mContext = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NAVIGATION_NOTIFICATION_CHANNEL, "Notification_Nav_Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        pendingOpenIntent = createPendingOpenIntent(context);
        customNotificationBuilder = new NotificationCompat.Builder(context, NAVIGATION_NOTIFICATION_CHANNEL)
                .setContentIntent(pendingOpenIntent)
                .setContentTitle("Custom Navigation Notification")
                .setSmallIcon(R.mipmap.ic_launcher);

        customNotification = customNotificationBuilder.build();
    }

    @Override
    public Notification getNotification() {
        return customNotification;
    }

    @Override
    public int getNotificationId() {
        return CUSTOM_NOTIFICATION_ID;
    }

    @Override
    public void updateNotification(NavProgress routeProgress) {
        // Customize your own notification
        DirectionsRoute route = routeProgress.route;
        distanceFormatter = new DistanceFormatter(mContext, route.routeOptions().language(), route.routeOptions().unit(), NavigationConstants.ROUNDING_INCREMENT_TEN);
        // config instruction text
        LegStep currentStep = routeProgress.currentLegProgress.currentStepProgress.currentStep;
        List<BannerInstructions> bannerInstructions = currentStep.bannerInstructions();
        if (bannerInstructions != null && !bannerInstructions.isEmpty()) {
            String instructionText = bannerInstructions.get(0).primary().text();
            customNotificationBuilder.setContentTitle(instructionText);
        }

        //config distance
        String currentDistanceText = distanceFormatter.formatDistance(
                routeProgress.currentLegProgress.currentStepProgress.distanceRemaining).toString();
        customNotificationBuilder.setContentText(currentDistanceText);

        //config maneuver icon
        String drivingSide = currentStep.drivingSide();
        String direction = "left";
        if (!TextUtils.isEmpty(drivingSide) && TextUtils.equals(drivingSide, "left")){
            direction = "right";
        }
        int maneuver = ManeuverUtils.getManeuverResource(currentStep, direction);
        if (currentManeuverId != maneuver) {
            currentManeuverId = maneuver;
            customNotificationBuilder.setSmallIcon(currentManeuverId);
        }
        notificationManager.notify(CUSTOM_NOTIFICATION_ID, customNotificationBuilder.build());
    }

    @Override
    public void onNavigationStopped(Context context) {
        notificationManager.cancel(CUSTOM_NOTIFICATION_ID);
    }

    private PendingIntent createPendingOpenIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
        intent.setPackage(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getActivity(context, 0, intent, 0);
        }

    }
}
