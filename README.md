# NotLock



**实现android手机常亮不锁屏**

原理：
1. PowerManager开启屏幕常亮
```aidl
PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyApp::MyWakelockTag");
mWakeLock.acquire();
```
2. 启动前台服务startForeground