package tinker.sample.android.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.tinker.lib.tinker.TinkerInstaller;

import tinker.sample.android.util.RebirthHelper;

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
            RebirthHelper.doRestart(context);

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
