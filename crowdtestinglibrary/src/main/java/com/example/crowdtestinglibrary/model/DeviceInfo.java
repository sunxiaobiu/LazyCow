package com.example.crowdtestinglibrary.model;

import android.content.Context;
import android.os.Build;
import org.json.JSONObject;

public class DeviceInfo {
    public String deviceId;
    public int sdkVersion;
    public String releaseVersion;
    public String deviceModel;
    public String brand;
    public String host;
    public String deviceName;
    public String hardware;
    public String language;
    public String screenSize;

    private Context context;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceInfo(Context context) {
        this.context = context;
    }

    public int getSDKVersion() {
        sdkVersion = Build.VERSION.SDK_INT;
        return sdkVersion;
    }

    public String getReleaseVersion() {
        releaseVersion = Build.VERSION.RELEASE;
        return releaseVersion;
    }

    public String getDeviceModel() {
        deviceModel = Build.MODEL;
        return deviceModel;
    }

    public String getBrand() {
        brand = Build.BRAND;
        return brand;
    }

    public String getHost() {
        host = Build.HOST;
        return host;
    }

    public String getDeviceName() {
        deviceName = Build.DEVICE;
        return deviceName;
    }

    public String getHardware() {
        hardware = Build.HARDWARE;
        return hardware;
    }

    public String getLanguage() {
        try {
            language = context.getResources().getConfiguration().locale.getLanguage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return language;
    }

    public String getScreenSize() {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        screenSize = screenWidth +  "*" + screenHeight;

        return screenSize;
    }

    public String toString() {
        String deviceMetadata = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("deviceId", getDeviceId());
            jsonObject.put("sdkVersion", getSDKVersion());
            jsonObject.put("releaseVersion", getReleaseVersion());
            jsonObject.put("deviceModel", getDeviceModel());
            jsonObject.put("brand", getBrand());
            jsonObject.put("host", getHost());
            jsonObject.put("deviceName", getDeviceName());
            jsonObject.put("hardware", getHardware());
            jsonObject.put("language", getLanguage());
            jsonObject.put("screenSize", getScreenSize());
            deviceMetadata = jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return deviceMetadata;
    }
}

