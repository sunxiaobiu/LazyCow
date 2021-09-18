package tinker.sample.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tinker.sample.android.app.MainActivity;

// Broadcast receiver for LazyCow Library
public class LazyCowLibBroadcastReceiver extends BroadcastReceiver {

    MainActivity mainContext;

    public LazyCowLibBroadcastReceiver(MainActivity mainActivity) {
        this.mainContext = mainActivity;
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
        this.mainContext.startBackgroundService();
    }
}
