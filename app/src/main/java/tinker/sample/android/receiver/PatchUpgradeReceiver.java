package tinker.sample.android.receiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tinker.sample.android.app.MyActivity;
import tinker.sample.android.model.TestCaseRecord;
import tinker.sample.android.model.TestClassFile;
import tinker.sample.android.util.DexUtils;

public class PatchUpgradeReceiver extends BroadcastReceiver {

    UpdateUIListenner updateUIListenner;

    UpdateTextListenner updateTextListenner;

    @SuppressLint("WrongConstant")
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("com.finish.patch.downloadPatchAPK")){
            Log.i("onReceive","【PatchUpgradeReceiver】downloadPatchAPK msg.");
            //Toast.makeText(context, "Download APK successfully,Please wait install...", Toast.LENGTH_LONG).show();
            updateTextListenner.UpdateText("Updating test cases...");
            updateUIListenner.UpdateUI(10);
            TinkerInstaller.onReceiveUpgradePatch(context, context.getCacheDir().getAbsolutePath() + "/app-debug-patch_signed_7zip.apk");
            updateUIListenner.UpdateUI(70);
        }else if(intent.getAction().equals("com.finish.patch.upgrade")){
            Log.i("onReceive","【PatchUpgradeReceiver】Receive msg.");
            updateUIListenner.UpdateUI(100);

            //restart self
            System.out.println("=============================[start restart self]");
            Intent i = new Intent(context, MyActivity.class);
            int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;

            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1, PendingIntent.getActivity(context, 0, i, flags));
            android.os.Process.killProcess(android.os.Process.myPid());
        } else{
            throw new RuntimeException("【Unsupported Broadcast Action Type】");
        }
    }

    /**
     * 监听广播接收器的接收到的数据
     * @param updateUIListenner
     */
    public void setOnUpdateUIListenner(UpdateUIListenner updateUIListenner) {
        this.updateUIListenner = updateUIListenner;
    }

    /**
     * 监听广播接收器的接收到的数据
     * @param updateTextListenner
     */
    public void setOnUpdateTextListenner(UpdateTextListenner updateTextListenner) {
        this.updateTextListenner = updateTextListenner;
    }

}
