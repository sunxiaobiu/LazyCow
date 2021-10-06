package tinker.sample.android.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Date;
import java.util.List;

import tinker.sample.android.R;

public class LongRunningService extends Service {
    public int anHour; //记录间隔时间

    private String packname = "tinker.sample.android";

    public int number = 0; //记录alertdialog出现次数

    AlarmManager manager;
    PendingIntent pi;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (checkPackInfo(packname)) {
                        boolean isAppRunningBefore = isAppRunning();
                        Log.e("bai", "========================isAppRunningBefore:"+isAppRunningBefore);
                        try {
                            Thread.sleep(2000); //2s
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        boolean isAppRunningAfter = isAppRunning();
                        Log.e("bai", "========================isAppRunningAfter:"+isAppRunningAfter);

                        if (!isAppRunningBefore && !isAppRunningAfter) {
                            PackageManager packageManager = getPackageManager();
                            Intent intent = packageManager.getLaunchIntentForPackage(packname);
                            intent.putExtra("monitorCrashStart", "true");
                            Log.e("bai", "====================isAppRunning  intent:" + intent);
                            startActivity(intent);
                        }
                    }
            }
        }

        private boolean isAppRunning() {
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
            for(int i = 0; i < processInfos.size(); i++){
                if(processInfos.get(i).processName.equals(packname)){
                    ActivityManager.RunningAppProcessInfo myProcess = processInfos.get(i);
                    ActivityManager.getMyMemoryState(myProcess);
                    Boolean isInBackground = myProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
                    Log.i("bai",String.format("=====================the LazyCow is running"));
                    Log.e("bai", "====================isInBackground" + isInBackground);
                    return !isInBackground;
                }
            }
            return false;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (number != 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e("bai", "==================executed at " + new Date().toString());
                    mHandler.sendEmptyMessage(1);
                }
            }).start();
        }
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time = 5;
        anHour = time*1000;
        Log.e("bai", "========================Time:" + time + "anhour:" + anHour);
        long triggerAtTime = SystemClock.elapsedRealtime() + (anHour);
        Intent i = new Intent(this, AlarmReceiver.class);
        pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        number++;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 检查包是否存在
     *
     * @param packname
     * @return
     */
    private boolean checkPackInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

}
