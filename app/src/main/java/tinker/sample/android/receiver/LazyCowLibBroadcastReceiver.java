package tinker.sample.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

// Broadcast receiver for LazyCow Library
public class LazyCowLibBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("============== LazyCowLibBroadcastReceiver: Data Received from Library");
        String developerId = intent.getStringExtra("developerId");
        int testCaseNum  = intent.getIntExtra("testCaseNum", 0);

        System.out.println("============== LazyCowLibBroadcastReceiver: Data Received from Library");
        System.out.println("developerId: " + developerId);
        System.out.println("testCaseNum: " + testCaseNum);

        // execute test cases
    }
}
