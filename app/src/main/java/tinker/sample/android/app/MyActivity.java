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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.judemanutd.autostarter.AutoStartPermissionHelper;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;
import com.yanzhikai.pictureprogressbar.PictureProgressBar;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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
import tinker.sample.android.receiver.PatchUpgradeReceiver;
import tinker.sample.android.receiver.UpdateTextListenner;
import tinker.sample.android.receiver.UpdateUIListenner;
import tinker.sample.android.util.DexUtils;
import tinker.sample.android.util.MySharedPreferences;
import tinker.sample.android.util.Utils;

public class MyActivity extends AppCompatActivity {
    private static final String TAG = "Tinker.MainActivity";
    private static String androidTestPackage = "tinker.sample.android.androidtest";
    private static String testCasePrefix = "tinker.sample.android.androidtest.";
    private static String firstTestEndfix = "Test";
    private static String secondTestEndfix = "Tests";
    private static String sencondTestCasePrefix = "tinker.sample.android.androidtest.TestCase_";
    private static String packageSeperator = ".";
    private static String successText = "success";
    private static String deviceId = "";
    private static String patchAPKName = "app-debug-patch_signed_7zip.apk";
    private static Context context;
    private PictureProgressBar pb_2;
    private CircleButton startCrowdTestingButton;
    private TextView textview;
    private PowerManager.WakeLock mWakeLock;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean isARKHotRunning = ShareTinkerInternals.isArkHotRuning();
        Log.e(TAG, "ARK HOT Running status = " + isARKHotRunning);
        Log.e(TAG, "i am on onCreate classloader:" + MyActivity.class.getClassLoader().toString());
        //test resource change
        Log.e(TAG, "i am on onCreate string:" + getResources().getString(R.string.test_resource));

        startCrowdTestingButton = (CircleButton) findViewById(R.id.startCrowdTesting);

        context = getApplicationContext();
        GlobalRef.applicationContext = context;
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID) + "_" + Build.SERIAL;

        pb_2 = (PictureProgressBar) findViewById(R.id.pb_2);

        startCrowdTestingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "Start CrowdTesting, please wait for download...", Toast.LENGTH_LONG).show();
                textview.setText("Initializing...");
                startCrowdTestingButton.setEnabled(false);
                generatePatchAPK();
            }
        });

        textview = (TextView) findViewById(R.id.textview);

        //setScheduleExecuteTime();
        registerReceiverForPatchUpgrade();

        //guide user to open AutoStartPermission
        guideUser2AutoStartPage();

        //request for WakeLock
        acquireWakeLock();

        //startTask2executeTestCases
        startTask2executeTestCases();

    }

    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "WakeLock");
        if (null != mWakeLock) {
            mWakeLock.acquire();
        }
    }

    private void registerReceiverForPatchUpgrade() {
        PatchUpgradeReceiver patchUpgradeReceiver = new PatchUpgradeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        // 2. 设置接收广播的类型
        intentFilter.addAction("com.finish.patch.upgrade");
        intentFilter.addAction("com.finish.patch.downloadPatchAPK");
        // 3. 动态注册：调用Context的registerReceiver（）方法
        registerReceiver(patchUpgradeReceiver, intentFilter);
        patchUpgradeReceiver.setOnUpdateUIListenner(new UpdateUIListenner() {
            @Override
            public void UpdateUI(int progress) {
                pb_2.setProgress(progress);
            }
        });
        patchUpgradeReceiver.setOnUpdateTextListenner(new UpdateTextListenner() {
            @Override
            public void UpdateText(String str) {
                textview.setText(str);
            }
        });
    }

    public void startTask2executeTestCases() {
        //execute test cases if patch is succeffully installed
        Tinker tinker = Tinker.with(context);
        System.out.println("=============================[tinker.isTinkerLoaded():]" + tinker.isTinkerLoaded());
        if (tinker.isTinkerLoaded()) {
            HighPriorityTask highPriorityTask = new HighPriorityTask();
            highPriorityTask.execute(deviceId);
        }
    }

    public void generatePatchAPK() {
        pb_2.setPicture(R.drawable.runningcow);
        pb_2.setProgress(30);

        DeviceInfo deviceInfo = new DeviceInfo(context);
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

    private void guideUser2AutoStartPage() {
        //创建对象
        final MySharedPreferences.SharedPreferencesUtil sharedPreferencesUtil = MySharedPreferences.SharedPreferencesUtil.getInstance(this);
        //获取存储的判断是否是第一次启动，默认为true
        boolean isFirst = (boolean) sharedPreferencesUtil.getData(GlobalRef.IS_FIRST_START, true);
        if (isFirst) {
            sharedPreferencesUtil.saveData(GlobalRef.IS_FIRST_START, false);
            Toast.makeText(context, "Please enable Auto Start Permission", Toast.LENGTH_LONG).show();
            AutoStartPermissionHelper.getInstance().getAutoStartPermission(context);
            System.out.println("isAutoStartPermissionAvailable :" + AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(context));
        }
    }

    public class HighPriorityTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            Log.i("HighPriorityTask ", "onPreExecute");
            setContentView(R.layout.activity_main);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                executeTestCases();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "finished";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("HighPriorityTask", result );
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            pb_2.setProgress(values[0]);
        }

        public void executeTestCases(){
            textview.setText("Running test cases...");
            System.out.println("=============================[Start run test cases]");
            //step1. collect all test cases from DexFile
            List<String> allTestCaseClasses = new ArrayList<>();
            allTestCaseClasses.addAll(DexUtils.findClassesStartEndWith(testCasePrefix, firstTestEndfix));
            allTestCaseClasses.addAll(DexUtils.findClassesStartWith(sencondTestCasePrefix));
            allTestCaseClasses.remove("tinker.sample.android.androidtest.BaseTest");
            allTestCaseClasses.remove("tinker.sample.android.androidtest.BandwidthTest");
            allTestCaseClasses.remove("tinker.sample.android.androidtest.FlakyTest");
            allTestCaseClasses.remove("tinker.sample.android.androidtest.MediumTest");
            allTestCaseClasses.remove("tinker.sample.android.androidtest.SmallTest");
            allTestCaseClasses.remove("tinker.sample.android.androidtest.LargeTest");
            allTestCaseClasses.remove("tinker.sample.android.androidtest.UiThreadTest");
            allTestCaseClasses.remove("tinker.sample.android.androidtest.ApplicationTest");

            //step2. execute test cases
            executeTests(allTestCaseClasses);
            textview.setText("Finished!");

            System.out.println("=============================[start cleanPatch]");
            //delete patch apk
            Tinker.with(context).cleanPatch();
        }
        public void executeTests(List<String> testCaseClasses) {
            System.out.println("==========================Begin Test Case=================================");
            int totalTestNum = testCaseClasses.size();
            int count = 0;
            System.out.println("=============================totalTestNum====================="+totalTestNum);
            for (String testCaseClass : testCaseClasses) {
                try {
                    count ++;
                    System.out.println("================count================testCaseClass====================="+count+";"+testCaseClass);
                    executeSingelTest(testCaseClass);
                    int progress = (int) (count * 1.0f / totalTestNum * 100);
                    publishProgress(progress);
                } catch (Exception e) {
                    System.out.println("==========================Test Case Exception==========================" + testCaseClass +e.getMessage());
                    continue;
                }
            }
            System.out.println("==========================End Test Case=================================");
        }

        public void executeSingelTest(final String testCaseClass) throws Exception {
            Class c = null;
            TestClassFile testClassFile = null;
            try {
                c = Class.forName(testCaseClass);
                testClassFile = resolveTestClass(c);
            } catch (Exception e) {
                System.out.println("==========================resolveTestClass Exception==========================" + testCaseClass +";"+e.getStackTrace());
                return;
            }

            if (CollectionUtils.isEmpty(testClassFile.getTestMethodList())) {
                return;
            }
            for (Method method : testClassFile.getTestMethodList()) {
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
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
//                }
//            });
//            thread.start();
            }
        }

        private TestClassFile resolveTestClass(Class c) {
            TestClassFile testClassFile = new TestClassFile();
            testClassFile.setC(c);
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
                @SuppressLint("LongLogTag")
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "【TestCaseName】" + testCaseRecord.getTestCaseName() + "----post data Failure");
                    call.cancel();
                }

                @SuppressLint("LongLogTag")
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "【TestCaseName】" + testCaseRecord.getTestCaseName() + "----post data success");
                }
            });
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

        private TestCaseRecord constructTestCaseRecord(String deviceId, boolean isSuccess, String testCaseName, String res) {
            TestCaseRecord testCaseRecord = new TestCaseRecord();

            testCaseRecord.setSuccess(isSuccess);
            testCaseRecord.setDeviceId(deviceId);
            testCaseRecord.setTestCaseName(testCaseName);
            testCaseRecord.setResult(res);

            return testCaseRecord;
        }


    }

    @Override
    protected void onResume() {
        Log.e(TAG, "i am on onResume");

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

    @Override
    protected void onDestroy() {
        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
        }
        super.onDestroy();
    }
}
