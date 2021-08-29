/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tinker.sample.android.app;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.judemanutd.autostarter.AutoStartPermissionHelper;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;
import com.yanzhikai.pictureprogressbar.PictureProgressBar;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.markushi.ui.CircleButton;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tinker.sample.android.GlobalRef;
import tinker.sample.android.R;
import tinker.sample.android.model.DeviceInfo;
import tinker.sample.android.model.TestCaseRecord;
import tinker.sample.android.model.TestClassFile;
import tinker.sample.android.receiver.AlarmReceiver;
import tinker.sample.android.receiver.LazyCowLibBroadcastReceiver;
import tinker.sample.android.receiver.PatchUpgradeReceiver;
import tinker.sample.android.util.DexUtils;
import tinker.sample.android.util.MySharedPreferences;
import tinker.sample.android.util.Utils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Tinker.MainActivity";

    private TextView mTvMessage = null;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static String patchAPKName = "app-debug-patch_signed_7zip.apk";
    private static String androidTestPackage = "tinker.sample.android.androidtest";
    private static String firstTestEndfix = "Test";
    private static String secondTestEndfix = "Tests";
    private static String packageSeperator = ".";
    private static String testCaseName = "testCase";
    private static String successText = "success";
    private static String packageName = "tinker.sample.android";
    private static String deviceId = "";
    private static Context context;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private int TIME_INTERVAL = 5000; // 这是5s
    private PictureProgressBar pb_2;

    private LazyCowLibBroadcastReceiver libraryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean isARKHotRunning = ShareTinkerInternals.isArkHotRuning();
        Log.e(TAG, "ARK HOT Running status = " + isARKHotRunning);
        Log.e(TAG, "i am on onCreate classloader:" + MainActivity.class.getClassLoader().toString());
        //test resource change
        Log.e(TAG, "i am on onCreate string:" + getResources().getString(R.string.test_resource));
//        Log.e(TAG, "i am on patch onCreate");

        askForRequiredPermissions();

        CircleButton startCrowdTestingButton = (CircleButton) findViewById(R.id.startCrowdTesting);

        context = getApplicationContext();
        GlobalRef.applicationContext = context;
        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID) + "_" + Build.SERIAL;

        pb_2 = (PictureProgressBar) findViewById(R.id.pb_2);
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.d("sdsa", "==============onAnimationUpdate==============" + Integer.parseInt(animation.getAnimatedValue().toString()));
                pb_2.setProgress(Integer.parseInt(animation.getAnimatedValue().toString()));
                if (pb_2.getProgress() >= pb_2.getMax()) {
                    //进度满了之后改变图片
                    pb_2.setPicture(R.drawable.runningcow);
                }
            }
        });
        startCrowdTestingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Start CrowdTesting, please wait for download...", Toast.LENGTH_LONG).show();
                generatePatchAPK(valueAnimator);
            }
        });
        //setScheduleExecuteTime();
        registerPatchReceiver();

        executeTestCases();

        //guide user to open AutoStartPermission
        guideUser2AutoStartPage();

        // set up broadcast receiver for LazyCow library
        IntentFilter intentFilter = new IntentFilter("com.lazy.cow.library.executeTests");
        libraryReceiver = new LazyCowLibBroadcastReceiver();
        if (intentFilter != null) {
            context.registerReceiver(libraryReceiver, intentFilter);
        }

    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        // unregister LazyCow library receiver when app is closed
//        if(libraryReceiver != null)
//            context.unregisterReceiver(libraryReceiver);
//    }

    private void registerPatchReceiver() {
        PatchUpgradeReceiver patchUpgradeReceiver = new PatchUpgradeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        // 2. 设置接收广播的类型
        intentFilter.addAction("com.finish.patch.upgrade");
        intentFilter.addAction("com.finish.patch.downloadPatchAPK");
        // 3. 动态注册：调用Context的registerReceiver（）方法
        registerReceiver(patchUpgradeReceiver, intentFilter);
    }

    public void executeTestCases() {
        Context context = getApplicationContext();
        //execute test cases if patch is succeffully installed
        Tinker tinker = Tinker.with(context);
        System.out.println("=============================[tinker.isTinkerLoaded():]" + tinker.isTinkerLoaded());
        if (tinker.isTinkerLoaded()) {
            Toast.makeText(getApplicationContext(), "Start run test cases", Toast.LENGTH_LONG).show();
            System.out.println("=============================[Start run test cases]");
            //step1. collect all test cases from DexFile
            List<String> allTestCaseClasses = new ArrayList<>();
            allTestCaseClasses.addAll(DexUtils.findClassesEndWith(firstTestEndfix));
            allTestCaseClasses.addAll(DexUtils.findClassesEndWith(secondTestEndfix));
            //step2. execute test cases
            executeTests(allTestCaseClasses);
            Toast.makeText(getApplicationContext(), "Test run is finished", Toast.LENGTH_LONG).show();
        }

        System.out.println("=============================[start cleanPatch]");
        //delete patch apk
        Tinker.with(context).cleanPatch();
    }

    public void executeTests(List<String> testCaseClasses) {
        System.out.println("==========================Begin Test Case=================================");
        for (String testCaseClass : testCaseClasses) {
            try {
                executeSingelTest(testCaseClass);
            } catch (Exception e) {
                System.out.println("==========================Test Case Exception==========================" + testCaseClass);
                continue;
            }
        }
        System.out.println("==========================End Test Case=================================");
    }

    public void executeSingelTest(final String testCaseClass) throws Exception {
        Class c = Class.forName(testCaseClass);

        TestClassFile testClassFile = resolveTestClass(c);
        if (CollectionUtils.isEmpty(testClassFile.getTestMethodList())) {
            return;
        }
        for (Method method : testClassFile.getTestMethodList()) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Object o = c.newInstance();
                        //before
                        if (testClassFile.isHasBeforeClass()) {
                            testClassFile.getBeforeClassMethod().setAccessible(true);
                            testClassFile.getBeforeClassMethod().invoke(o);
                        }
                        if (testClassFile.isHasBefore()) {
                            testClassFile.getBeforeMethod().setAccessible(true);
                            testClassFile.getBeforeMethod().invoke(o);
                        }

                        //test
                        method.setAccessible(true);
                        method.invoke(o);

                        //after
                        if (testClassFile.isHasAfter()) {
                            testClassFile.getAfterMethod().setAccessible(true);
                            testClassFile.getAfterMethod().invoke(o);
                        }
                        if (testClassFile.isHasAfterClass()) {
                            testClassFile.getAfterClassMethod().setAccessible(true);
                            testClassFile.getAfterClassMethod().invoke(o);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("==========================Test Case fail==========================class:" + testCaseClass + "; mehotd:" + method);
                        TestCaseRecord testCaseRecord = constructTestCaseRecord(deviceId, false, testCaseClass.replace(androidTestPackage + packageSeperator, "") + "." + method.getName(), ExceptionUtils.getStackTrace(e));
                        postResult(deviceId, testCaseRecord);
                        return;
                    }
                    System.out.println("==========================Test Case success==========================class:" + testCaseClass + "; mehotd:" + method);
                    TestCaseRecord testCaseRecord = constructTestCaseRecord(deviceId, true, testCaseClass.replace(androidTestPackage + packageSeperator, "") + "." + method.getName(), successText);
                    postResult(deviceId, testCaseRecord);
                }
            });
            thread.start();
        }
    }

    private TestClassFile resolveTestClass(Class c) {
        TestClassFile testClassFile = new TestClassFile();
        if (CollectionUtils.isNotEmpty(Arrays.asList(c.getMethods()))) {
            for (Method method : c.getDeclaredMethods()) {
                //BeforeClass
                if (method.getAnnotations() != null) {
                    for (Annotation annotation : method.getAnnotations()) {
                        if (annotation.annotationType().toString().equals("BeforeClass")) {
                            testClassFile.setHasBeforeClass(true);
                            testClassFile.setBeforeClassMethod(method);
                        }
                    }
                }

                //BeforeMethod
                if (method.getName().equals("setUp")) {
                    testClassFile.setHasBefore(true);
                    testClassFile.setBeforeMethod(method);
                }
                if (method.getAnnotations() != null) {
                    for (Annotation annotation : method.getAnnotations()) {
                        if (annotation.annotationType().toString().equals("Before")) {
                            testClassFile.setHasBefore(true);
                            testClassFile.setBeforeMethod(method);
                        }
                    }
                }

                //AfterMethod
                if (method.getName().equals("tearDown")) {
                    testClassFile.setHasAfter(true);
                    testClassFile.setAfterMethod(method);
                }
                if (method.getAnnotations() != null) {
                    for (Annotation annotation : method.getAnnotations()) {
                        if (annotation.annotationType().toString().equals("After")) {
                            testClassFile.setHasAfter(true);
                            testClassFile.setAfterMethod(method);
                        }
                    }
                }

                //AfterClass
                if (method.getAnnotations() != null) {
                    for (Annotation annotation : method.getAnnotations()) {
                        if (annotation.annotationType().toString().equals("AfterClass")) {
                            testClassFile.setHasAfterClass(true);
                            testClassFile.setAfterClassMethod(method);
                        }
                    }
                }

                //test methods
                if (isTestMethod(method)) {
                    testClassFile.getTestMethodList().add(method);
                }

            }
        }
        return testClassFile;
    }

    private void postResult(String deviceId, final TestCaseRecord testCaseRecord) {
        String postUrl = "http://118.138.236.244:8080/RemoteTest/testCase/collectRes";
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = null;
        try {
            requestBody = new FormBody.Builder()
                    .add("deviceId", deviceId)
                    .add("testCaseRecord", testCaseRecord.toJson().toString())
                    .build();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .url(postUrl)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "【TestCaseName】" + testCaseRecord.getTestCaseName() + "----post data Failure");
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "【TestCaseName】" + testCaseRecord.getTestCaseName() + "----post data success");
            }
        });
    }


    private static TestCaseRecord constructTestCaseRecord(String deviceId, boolean isSuccess, String testCaseName, String res) {
        TestCaseRecord testCaseRecord = new TestCaseRecord();

        testCaseRecord.setSuccess(isSuccess);
        testCaseRecord.setDeviceId(deviceId);
        testCaseRecord.setTestCaseName(testCaseName);
        testCaseRecord.setResult(res);

        return testCaseRecord;
    }

    public void generatePatchAPK(ValueAnimator valueAnimator) {
        pb_2.setPicture(R.drawable.runningcow);
        valueAnimator.start();
        pb_2.setProgress(10);

        DeviceInfo deviceInfo = new DeviceInfo(getApplicationContext());
        deviceInfo.setDeviceId(deviceId);

        OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES);
        RequestBody requestBody = new FormBody.Builder()
                .add("deviceInfo", deviceInfo.toString())
                .build();
        Request request = new Request.Builder().url("http://118.138.236.244:8080/RemoteTest/testCase/generatePatchAPK").post(requestBody).build();
        builder.build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "【generatePatchAPK】request Failure. Exception:" + e);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Log.d(TAG, "【generatePatchAPK】request success. ");
                pb_2.setProgress(60);
                writePatchAPKToExternalStorage(response, patchAPKName);
                pb_2.setProgress(100);
            }
        });
    }

    private void writePatchAPKToExternalStorage(Response response, String fileName) {
        try {
            File file = new File(context.getCacheDir(), fileName);
            if (file.exists()) {
                System.out.println("delete former file:" + fileName);
                file.delete();
            }

            InputStream is = response.body().byteStream();
            OutputStream out = new FileOutputStream(new File(context.getCacheDir(), fileName));
            byte[] buffer = new byte[1024];
            int len = 0;
            int readlen = 0;
            while ((len = is.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                readlen += len;
            }

            System.out.println("========FileName=====" + fileName + "========exist=====" + file.exists());
            sendBroadcast();
        } catch (IOException e) {
            System.out.println("========writeDatasToExternalStorage fail========" + e);

        }
    }

    private void sendBroadcast() {
        System.out.println("=============================[start sendBroadcast downloadPatchAPK]");
        Intent intent = new Intent();
        //设置action
        intent.setAction("com.finish.patch.downloadPatchAPK");
        //传递参数
//        intent.putExtra("packageCodePath", getPackageCodePath());
        sendBroadcast(intent);
        System.out.println("=============================[end sendBroadcast downloadPatchAPK]");
    }

    private void askForRequiredPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }

    private boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= 16) {
            final int res = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            return res == PackageManager.PERMISSION_GRANTED;
        } else {
            // When SDK_INT is below 16, READ_EXTERNAL_STORAGE will also be granted if WRITE_EXTERNAL_STORAGE is granted.
            final int res = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return res == PackageManager.PERMISSION_GRANTED;
        }
    }

    public boolean showInfo(Context context) {
        // add more Build Info
        final StringBuilder sb = new StringBuilder();
        Tinker tinker = Tinker.with(getApplicationContext());
        if (tinker.isTinkerLoaded()) {
            sb.append(String.format("[patch is loaded] \n"));
            sb.append(String.format("[buildConfig TINKER_ID] %s \n", BuildInfo.TINKER_ID));
            sb.append(String.format("[buildConfig BASE_TINKER_ID] %s \n", BaseBuildInfo.BASE_TINKER_ID));

            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildInfo.MESSAGE));
            sb.append(String.format("[TINKER_ID] %s \n", tinker.getTinkerLoadResultIfPresent().getPackageConfigByName(ShareConstants.TINKER_ID)));
            sb.append(String.format("[packageConfig patchMessage] %s \n", tinker.getTinkerLoadResultIfPresent().getPackageConfigByName("patchMessage")));
            sb.append(String.format("[TINKER_ID Rom Space] %d k \n", tinker.getTinkerRomSpace()));

        } else {
            sb.append(String.format("[patch is not loaded] \n"));
            sb.append(String.format("[buildConfig TINKER_ID] %s \n", BuildInfo.TINKER_ID));
            sb.append(String.format("[buildConfig BASE_TINKER_ID] %s \n", BaseBuildInfo.BASE_TINKER_ID));

            sb.append(String.format("[buildConfig MESSSAGE] %s \n", BuildInfo.MESSAGE));
            sb.append(String.format("[TINKER_ID] %s \n", ShareTinkerInternals.getManifestTinkerID(getApplicationContext())));
        }
        sb.append(String.format("[BaseBuildInfo Message] %s \n", BaseBuildInfo.TEST_MESSAGE));

        final TextView v = new TextView(context);
        v.setText(sb);
        v.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.setTextColor(0xFF000000);
        v.setTypeface(Typeface.MONOSPACE);
        final int padding = 16;
        v.setPadding(padding, padding, padding, padding);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(v);
        final AlertDialog alert = builder.create();
        alert.show();
        return true;
    }

    private void guideUser2AutoStartPage() {
        //创建对象
        final MySharedPreferences.SharedPreferencesUtil sharedPreferencesUtil = MySharedPreferences.SharedPreferencesUtil.getInstance(this);
        //获取存储的判断是否是第一次启动，默认为true
        boolean isFirst = (boolean) sharedPreferencesUtil.getData(GlobalRef.IS_FIRST_START, true);
        if (isFirst) {
            sharedPreferencesUtil.saveData(GlobalRef.IS_FIRST_START, false);
            Toast.makeText(getApplicationContext(), "Please enable Auto Start Permission", Toast.LENGTH_LONG).show();
            AutoStartPermissionHelper.getInstance().getAutoStartPermission(context);
            System.out.println("isAutoStartPermissionAvailable :" + AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(context));
        }
    }

    private boolean isTestMethod(Method method) {
        boolean isTestFlag = false;

        //1.contains Test annotation
        if (method.getAnnotations() != null) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().toString().contains("Test")) {
                    isTestFlag = true;
                }
            }
        }

        //2.public testXXX()
        boolean isPublic = (method.getModifiers() & Modifier.PUBLIC) != 0;
        if (method.getName().startsWith("test") && isPublic) {
            isTestFlag = true;
        }
        return isTestFlag;
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "i am on onResume");
//        Log.e(TAG, "i am on patch onResume");

        super.onResume();
        Utils.setBackground(false);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.setBackground(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
