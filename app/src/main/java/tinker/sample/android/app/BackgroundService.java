package tinker.sample.android.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.tencent.tinker.lib.tinker.Tinker;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tinker.sample.android.model.DeviceInfo;
import tinker.sample.android.model.TestCaseRecord;
import tinker.sample.android.model.TestClassFile;
import tinker.sample.android.util.DexUtils;

public class BackgroundService extends Service {
    private static final String TAG = "Tinker.BgService";
    private static String patchAPKName = "app-debug-patch_signed_7zip.apk";
    private static String deviceId = "";
    private static Context context;
    private static String firstTestEndfix = "Test";
    private static String secondTestEndfix = "Tests";
    private static String androidTestPackage = "tinker.sample.android.androidtest";
    private static String successText = "success";
    private static String packageSeperator = ".";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // download APK
        generatePatchAPK();
        // execute tests
        executeTestCases();
        return super.onStartCommand(intent, flags, startId);
    }

    // As per required, display notification in order to keep background task running
    @RequiresApi(26)
    private String getNotificationChannel(String channelId, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(channel);
        return channelId;
    }

    // REVIEW: method to take Information from LazyCow Library and use it to request test cases
    public void generatePatchAPK() {
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

    // Receiver should receive the broadcast and do the installation of test cases.

    // REVIEW: method to return the test result, and then finish the service by calling stopSelf()
    public void executeTestCases() {
        Context context = getApplicationContext();
        //execute test cases if patch is successfully installed
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
                executeSingleTest(testCaseClass);
            } catch (Exception e) {
                System.out.println("==========================Test Case Exception==========================" + testCaseClass);
                continue;
            }
        }
        System.out.println("==========================End Test Case=================================");
    }

    public void executeSingleTest(final String testCaseClass) throws Exception {
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

    private static TestCaseRecord constructTestCaseRecord(String deviceId, boolean isSuccess, String testCaseName, String res) {
        TestCaseRecord testCaseRecord = new TestCaseRecord();

        testCaseRecord.setSuccess(isSuccess);
        testCaseRecord.setDeviceId(deviceId);
        testCaseRecord.setTestCaseName(testCaseName);
        testCaseRecord.setResult(res);

        return testCaseRecord;
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
}
