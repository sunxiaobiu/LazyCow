package tinker.sample.android.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tinker.sample.android.app.MyActivity;

// Broadcast receiver for LazyCow Library
public class LazyCowLibBroadcastReceiver extends BroadcastReceiver {

    MyActivity mainContext;

    public LazyCowLibBroadcastReceiver(MyActivity myActivity) {
        this.mainContext = myActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("============== LazyCowLibBroadcastReceiver: Data Received from Library");
        String developerId = intent.getStringExtra("developerId");
        int testCaseNum = intent.getIntExtra("testCaseNum", 0);

        System.out.println("============== LazyCowLibBroadcastReceiver: Data Received from Library");
        System.out.println("developerId: " + developerId);
        System.out.println("testCaseNum: " + testCaseNum);

        // execute test cases
        MonitorAndExecuteTests testCaseScheduler = new MonitorAndExecuteTests();
        testCaseScheduler.setAlarm(mainContext);
    }
}
