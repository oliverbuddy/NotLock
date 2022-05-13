package com.example.nolock;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyIntentService extends Service {

    public static final int NOTIFICATION_ID = 1;



    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    PowerManager.WakeLock mWakeLock;
    private NotificationBinder mBinder = new NotificationBinder();

    class NotificationBinder extends Binder {
        public void MyNotify() {
            if (mWakeLock != null)
                mWakeLock.acquire();
        }

        public void MyCancel() {
            //释放资源
            if (mWakeLock != null) {
                mWakeLock.release();
            }
            mNotificationManager.cancel(83512);
            stopSelf();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    String channelId = "123";

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //通知管理员
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //发送通知
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "111111", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        mBuilder = new NotificationCompat.Builder(this, channelId);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("屏幕常亮已经开启")
                .setTicker("开启屏幕常亮").setContentIntent(pi).setAutoCancel(false);
        startForeground(83512, mBuilder.build());

        //开启屏幕常亮
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyApp::MyWakelockTag");
        mWakeLock.acquire();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}