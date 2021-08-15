package com.example.crowdtestinglibrary;

import android.content.Intent;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Callback;
import okhttp3.Response;
import com.example.crowdtestinglibrary.model.*;
import com.example.crowdtestinglibrary.util.DexUtils;

import com.example.crowdtestinglibrary.model.DeviceInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.provider.Settings;
import android.widget.Toast;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;

import android.os.Build;

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
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID) + "_" + Build.SERIAL;
    }

    public void generatePatchAPK() {
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
                writePatchAPKToExternalStorage(response, patchAPKName);
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
            Intent intent = new Intent();
            sendBroadcast(intent);
        } catch (IOException e) {
            System.out.println("========writeDatasToExternalStorage fail========" + e);
        }
    }

    private void sendBroadcast(Intent intent) {
        System.out.println("=============================[start sendBroadcast downloadPatchAPK]");
        intent.setAction("com.finish.patch.downloadPatchAPK");
        // should call Android\Sdk\sources\android-30\android\content\ContextWrapper.java
        sendBroadcast(intent);
        System.out.println("=============================[end sendBroadcast downloadPatchAPK]");
    }

    //API4 execute test cases
    public void executeTestCases() {
        Context mContext = context.getApplicationContext();
        Toast.makeText(mContext.getApplicationContext(), "Start run test cases", Toast.LENGTH_LONG).show();
        System.out.println("=============================[Start run test cases]");
        //step1. collect all test cases from DexFile
        List<String> allTestCaseClasses = new ArrayList<>();
        allTestCaseClasses.addAll(DexUtils.findClassesEndWith(firstTestEndfix));
        allTestCaseClasses.addAll(DexUtils.findClassesEndWith(secondTestEndfix));
        //step2. execute test cases
        executeTests(allTestCaseClasses);
        Toast.makeText(mContext.getApplicationContext(), "Test run is finished", Toast.LENGTH_LONG).show();
    }

    private void executeTests(List<String> testCaseClasses) {
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

    private void executeSingelTest(final String testCaseClass) throws Exception {
        final Class c = Class.forName(testCaseClass);

        final TestClassFile testClassFile = resolveTestClass(c);
        if (CollectionUtils.isEmpty(testClassFile.getTestMethodList())) {
            return;
        }
        for (final Method method : testClassFile.getTestMethodList()) {
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
}