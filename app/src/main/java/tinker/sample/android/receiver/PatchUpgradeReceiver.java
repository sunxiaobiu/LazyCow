package tinker.sample.android.receiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.yanzhikai.pictureprogressbar.PictureProgressBar;

import tinker.sample.android.R;
import tinker.sample.android.app.MainActivity;

public class PatchUpgradeReceiver extends BroadcastReceiver {
    @SuppressLint("WrongConstant")
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("com.finish.patch.downloadPatchAPK")){
            Log.i("onReceive","【PatchUpgradeReceiver】downloadPatchAPK msg.");
            Toast.makeText(context, "Download APK successfully,Please wait install...", Toast.LENGTH_LONG).show();
            TinkerInstaller.onReceiveUpgradePatch(context, context.getCacheDir().getAbsolutePath() + "/app-debug-patch_signed_7zip.apk");

        }else if(intent.getAction().equals("com.finish.patch.upgrade")){
            Log.i("onReceive","【PatchUpgradeReceiver】Receive msg.");

            //restart self
            System.out.println("=============================[start restart self]");
            Intent i = new Intent(context, MainActivity.class);
            int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;

            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(context, 0, i, flags));
            android.os.Process.killProcess(android.os.Process.myPid());
        }else{
            throw new RuntimeException("【Unsupported Broadcast Action Type】");
        }

    }
}
