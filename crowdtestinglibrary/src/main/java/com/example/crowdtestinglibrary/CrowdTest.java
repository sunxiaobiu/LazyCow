package com.example.crowdtestinglibrary;

import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.provider.Settings;
import android.os.Build;

import com.example.crowdtestinglibrary.monitor.MonitorSleepingIdle;
import com.example.crowdtestinglibrary.monitor.MonitorGeneralIdle;

public class CrowdTest {
    private static String TAG;
    private static String deviceId = "";
    private static Context context;
    private static String patchAPKName = "app-debug-patch_signed_7zip.apk";
    private static String firstTestEndfix = "Test";
    private static String secondTestEndfix = "Tests";
    private static String androidTestPackage = "com.example.crowdtestinglibrary.androidtest";
    private static String packageSeperator = ".";
    private static String successText = "success";

    public CrowdTest(Context context, String TAG) {
        this.context = context;
        this.TAG = TAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID) + "_" + Build.SERIAL;
        }
    }

    /**
     * This method sends a request to the LazyCow application.
     *
     * @param developerId developer ID
     * @param testCaseNum number of test cases to be dispatched
     */
    public void executeTests(String developerId, int testCaseNum) {
        System.out.println("=============================[start sendBroadcast executeTests from library]");
        final Intent intent = new Intent("com.lazy.cow.library.executeTests");
        intent.putExtra("developerId", developerId);
        intent.putExtra("testCaseNum", testCaseNum);

        // sends broadcast to LazyCow even when it is not running
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);
        System.out.println("=============================end sendBroadcast executeTests from library]");
    }

    /**
     * This method checks whether the device is idle and will launch test cases
     * on LazyCow if it is.
     *
     * @param developerId developer ID
     * @param testCaseNum number of test cases to be dispatched
     */
    public void monitorIdleAndLaunchLazyCow(String developerId, int testCaseNum) {
        MonitorGeneralIdle monitorGeneralIdle = new MonitorGeneralIdle(this.context);
        MonitorSleepingIdle monitorSleepingIdle = new MonitorSleepingIdle(this.context);

        if (monitorGeneralIdle.getIdleState() && monitorSleepingIdle.getIdleState()) {
            System.out.println("=============================device is idle");
            executeTests(developerId, testCaseNum);
        } else {
            System.out.println("=============================device is not idle");

            System.out.println("=============================but let's still try to execute test cases");
            executeTests(developerId, testCaseNum);
        }
    }
}