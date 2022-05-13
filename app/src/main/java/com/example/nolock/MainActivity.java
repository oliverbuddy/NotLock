package com.example.nolock;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;


//https://developer.android.com/training/scheduling/wakelock?hl=zh-cn#java
public class MainActivity extends AppCompatActivity {


    private boolean isOpen = false;
    Button button;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keepScreenOn(isOpen);
            }
        });
    }


    private MyIntentService.NotificationBinder mBinder;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder.MyCancel();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MyIntentService.NotificationBinder) service;
            mBinder.MyNotify();
        }
    };

    private void keepScreenOn(boolean isOpen) {
        Window window = getWindow();

        if (!isOpen) {
//            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            this.isOpen = true;
            button.setText("关闭常亮");
            Intent bindIntent = new Intent(MainActivity.this, MyIntentService.class);
            bindService(bindIntent, connection, BIND_AUTO_CREATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(bindIntent);
            } else {
                startService(bindIntent);
            }
        } else {
//            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            this.isOpen = false;
            button.setText("开启常亮");
            unbindService(connection);
            stopService(new Intent(MainActivity.this, MyIntentService.class));
        }
//        MediaPlayer player = new MediaPlayer();
//        player.setScreenOnWhilePlaying(true);
//        player.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        boolean isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations();
        if (isIgnoringBatteryOptimizations) {
            requestIgnoreBatteryOptimizations();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }


    @Override
    protected void onDestroy() {
        if (isOpen) {//这里要判断server是否被绑定，如果没有，则不需要解除绑定
            mBinder.MyCancel();
            unbindService(connection);
            stopService(new Intent(MainActivity.this, MyIntentService.class));
        }
        super.onDestroy();
    }
}