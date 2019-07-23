package com.mhandharbeni.benibeacon.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.mhandharbeni.benibeacon.R;

import java.util.List;


public class NotificationHelper {
    private static NotificationManager mNotificationManager;
    private static NotificationCompat.Builder mBuilder;


    @TargetApi(Build.VERSION_CODES.N)
    private static int getImportance(){
        return NotificationManager.IMPORTANCE_HIGH;
    }


    public static void createNotification(
            Context mContext,
            String NOTIFICATION_CHANNEL_ID,
            String title,
            String message,
            int raw,
            Class<?> cls,
            int notificationId) {
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);

        Intent resultIntent = new Intent(mContext , cls);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                0, resultIntent, PendingIntent.FLAG_ONE_SHOT);

        if (raw != 0) {
            mBuilder.setSound(Uri.parse("android.resource://"+mContext.getPackageName()+"/"+raw));
        }
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, title, getImportance());
            notificationChannel.setBypassDnd(true);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            if(mNotificationManager.areNotificationsEnabled() && notificationChannel.getImportance() != NotificationManager.IMPORTANCE_NONE) {
                try {
                    if (raw != 0) {
                        playSound(mContext, raw);
                    }
                } catch (Exception ignored) { }
            }
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(notificationId /* Request Code */, mBuilder.build());

    }


    public static NotificationCompat.Builder createNotifications(
            Context mContext,
            String NOTIFICATION_CHANNEL_ID,
            String title,
            String message,
            int raw, Class<?> cls
    ){
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);

        Intent resultIntent = new Intent(mContext , cls);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_ONE_SHOT);


        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID, getImportance());
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setShowBadge(true);
            notificationChannel.setBypassDnd(true);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            if(mNotificationManager.areNotificationsEnabled() && notificationChannel.getImportance() != NotificationManager.IMPORTANCE_NONE) {
                try {
                    if (raw != 0) {
                        playSound(mContext, raw);
                    }
                } catch (Exception ignored) { }
            }
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(Integer.valueOf(NOTIFICATION_CHANNEL_ID) /* Request Code */, mBuilder.build());

        return mBuilder;
    }
    public static NotificationCompat.Builder createNotifications(
            Context mContext,
            String NOTIFICATION_CHANNEL_ID,
            String title,
            String message,
            int raw,
            Class<?> cls,
            int importance
    ){
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);

        Intent resultIntent = new Intent(mContext , cls);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_ONE_SHOT);

        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID, importance);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setShowBadge(true);
            notificationChannel.setBypassDnd(true);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            if(mNotificationManager.areNotificationsEnabled() && notificationChannel.getImportance() != NotificationManager.IMPORTANCE_NONE) {
                try {
                    if (raw != 0) {
                        playSound(mContext, raw);
                    }
                } catch (Exception ignored) { }
            }
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(Integer.valueOf(NOTIFICATION_CHANNEL_ID) /* Request Code */, mBuilder.build());
        return mBuilder;
    }
    public static NotificationCompat.Builder getNotification(){
        return mBuilder;
    }

    public static void updateNotification(NotificationCompat.Builder builder, String NOTIFICATION_CHANNEL_ID, String message){
        try {
            builder.setContentText(message);
            mNotificationManager.notify(Integer.valueOf(NOTIFICATION_CHANNEL_ID), builder.build());
        }catch (Exception ignored){

        }
    }

    public static void removeAllNotification(){
        try {
            mNotificationManager.cancelAll();
        }catch (Exception ignored){}
    }


    private static void playSound(Context mContext, int raw){
        Ringtone r = RingtoneManager.getRingtone(mContext, Uri.parse("android.resource://"+mContext.getPackageName()+"/"+raw));
        r.play();
    }

    private static void grantUriPermission(Context ctx, Intent intent, Uri uri) {
        List<ResolveInfo> resolvedIntentActivities = ctx.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;

            ctx.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }
}
