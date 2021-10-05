package tinker.sample.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemDialogReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("onReceive",
                String.format("=====================SystemDialogReceiver onReceive", intent.getAction()));
        if (intent.getAction().equals(Intent.ACTION_APP_ERROR)) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeDialog);
            Log.i("onReceive", String.format("=====================SystemDialogReceiver onReceive", intent.getAction()));
        }
    }
}