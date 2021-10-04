package tinker.sample.android.receiver;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import tinker.sample.android.app.BackgroundService;
import tinker.sample.android.monitor.MonitorGeneralIdle;
import tinker.sample.android.monitor.MonitorSleepingIdle;

import tinker.sample.android.app.MyActivity;

public class MonitorAndExecuteTests extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // run background service which runs test cases
        System.out.println("=========received alarm intent");
        monitorIdleAndLaunchLazyCow(context);
    }

    public void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MonitorAndExecuteTests.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        // runs every 30 minutes
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60, pi);

        System.out.println("=========alarm set");
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, MonitorAndExecuteTests.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);

        System.out.println("=========alarm cancelled");
    }

    /**
     * This method checks whether the device is idle and will launch test cases
     * on LazyCow if it is.
     */
    private void monitorIdleAndLaunchLazyCow(Context context) {
        MonitorGeneralIdle monitorGeneralIdle = new MonitorGeneralIdle(context);
        MonitorSleepingIdle monitorSleepingIdle = new MonitorSleepingIdle(context);

        // execute test cases if device is idle
        if (monitorGeneralIdle.getIdleState() || monitorSleepingIdle.getIdleState()) {
            System.out.println("=============================device is idle");
            context.startService(new Intent(context, BackgroundService.class));
            // cancel the scheduler once the background task runs
            cancelAlarm(context);
        } else {
            System.out.println("=============================device is not idle");
        }
    }
}