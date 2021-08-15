package com.example.crowdtestinglibrary;

import android.content.Intent;
import java.util.concurrent.TimeUnit;
import android.util.Log;
import android.content.Context;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;
import com.example.crowdtestinglibrary.model.DeviceInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.provider.Settings;
import android.os.Build;

public class CrowdTest {
    private static String deviceId = "";
    private static Context context;
    private static String TAG;
    private static String patchAPKName = "app-debug-patch_signed_7zip.apk";


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
            sendBroadcast();
        } catch (IOException e) {
            System.out.println("========writeDatasToExternalStorage fail========" + e);
        }
    }

    private void sendBroadcast() {
        System.out.println("=============================[start sendBroadcast downloadPatchAPK]");
        Intent intent = new Intent();
        intent.setAction("com.finish.patch.downloadPatchAPK");
        // should call Android\Sdk\sources\android-30\android\content\ContextWrapper.java
        sendBroadcast(intent);
        System.out.println("=============================[end sendBroadcast downloadPatchAPK]");
    }
}