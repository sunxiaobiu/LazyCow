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
    private static final String TAG = "Tinker.PatchUpgradeReceiver";
    private static String androidTestPackage = "tinker.sample.android.androidtest";
    private static String testCasePrefix = "tinker.sample.android.androidtest.";
    private static String firstTestEndfix = "Test";
    private static String secondTestEndfix = "Tests";
    private static String sencondTestCasePrefix = "tinker.sample.android.androidtest.TestCase_";
    private static String packageSeperator = ".";
    private static String successText = "success";
    private static String deviceId = "";

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
        }else if(intent.getAction().equals("lazycow.executeTestCases")){

            deviceId = intent.getStringExtra("deviceId");

            updateTextListenner.UpdateText("Running test cases...");
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
            updateTextListenner.UpdateText("Finished!");

            System.out.println("=============================[start cleanPatch]");
            //delete patch apk
            Tinker.with(context).cleanPatch();
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
                updateUIListenner.UpdateUI(progress);
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

    private static TestCaseRecord constructTestCaseRecord(String deviceId, boolean isSuccess, String testCaseName, String res) {
        TestCaseRecord testCaseRecord = new TestCaseRecord();

        testCaseRecord.setSuccess(isSuccess);
        testCaseRecord.setDeviceId(deviceId);
        testCaseRecord.setTestCaseName(testCaseName);
        testCaseRecord.setResult(res);

        return testCaseRecord;
    }
}
