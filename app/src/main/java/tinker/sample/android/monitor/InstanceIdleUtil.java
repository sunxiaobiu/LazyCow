package tinker.sample.android.monitor;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

public class InstanceIdleUtil {

    public static boolean checkBottomLineRequirement(Context context){
        //Check battery state
        IntentFilter batteryIfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, batteryIfilter);
        //If charging
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        //If above 50%
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float)scale;
        boolean isSufficientBattery = (batteryPct>50.0);
        if (!isCharging && !isSufficientBattery){
            return false;
        }

        //Check RAM
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double percentRAMAvail = mi.availMem / (double) mi.totalMem*100;
        if (percentRAMAvail < 30){
            return false;
        }

        //Check temperature
        //Check device degree
        int batteryTemp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
        if (batteryTemp > 40){
            return false;
        }
        return true;
    }

    public static boolean checkInstanceGeneralIdleState(Context context){
        //Check battery state
        IntentFilter batteryIfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, batteryIfilter);
        //If charging
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        //If above 60%
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float)scale;
        boolean isSufficientBattery = (batteryPct>60);
        if (!isCharging && !isSufficientBattery){
            return false;
        }

        //Check RAM
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double percentRAMAvail = mi.availMem / (double) mi.totalMem*100;
        if (percentRAMAvail < 60){
            return false;
        }


        //Check device on or off
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenon;
        if (Build.VERSION.SDK_INT > 23) {
            isScreenon = powerManager.isDeviceIdleMode();
        }else {
            isScreenon = powerManager.isScreenOn();
        }
        if (isScreenon){
            return false;
        }

        return true;
    }

    public static boolean checkInstanceSleepingState(Context context){
        //Check music playing
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(manager.isMusicActive())
        {
            return false;
        }

        //Check wifi connected
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return false;
        }

        //Check device on or off
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenon;
        if (Build.VERSION.SDK_INT > 23) {
            isScreenon = powerManager.isDeviceIdleMode();
        }else {
            isScreenon = powerManager.isScreenOn();
        }
        if (isScreenon){
            return false;
        }

        return true;
    }
}
