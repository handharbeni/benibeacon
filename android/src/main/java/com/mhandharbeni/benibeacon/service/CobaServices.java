package com.mhandharbeni.benibeacon.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.mhandharbeni.benibeacon.R;

public class CobaServices extends Service {
    public static String FOREGROUND = "com.mhandharbeni.benibeacon.service.CobaServices";
    private static int GEOLOCATION_NOTIFICATION_ID = 12345689;
    @Override
    public void onCreate() {
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(GEOLOCATION_NOTIFICATION_ID, getCompatNotification());
        return START_STICKY;
    }
    private Notification getCompatNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        String str = "Is using your location in the background";
        builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("App Name")
                .setContentText(str)
                .setTicker(str)
                .setWhen(System.currentTimeMillis());
        Intent startIntent = new Intent(getApplicationContext(), CobaServices.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000, startIntent, 0);
        builder.setContentIntent(contentIntent);
        return builder.build();
    }
}
