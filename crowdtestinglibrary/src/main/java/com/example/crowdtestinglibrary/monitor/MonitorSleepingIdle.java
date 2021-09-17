package com.example.crowdtestinglibrary.monitor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class MonitorSleepingIdle implements SensorEventListener {
    private SensorManager sensorManager;
    private Context context;

    private boolean meetBottomLine;
    private boolean isInstanceIdleMet;
    private boolean isFlat;

    public MonitorSleepingIdle(Context c){
        context = c;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        meetBottomLine = InstanceIdleUtil.checkBottomLineRequirement(context);
        isInstanceIdleMet = InstanceIdleUtil.checkInstanceSleepingState(context);
    }

    public boolean getIdleState(){
        return meetBottomLine&&isInstanceIdleMet&&isFlat;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        meetBottomLine = InstanceIdleUtil.checkBottomLineRequirement(context);
        isInstanceIdleMet = InstanceIdleUtil.checkInstanceSleepingState(context);

        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];
        float norm_Of_g =(float) Math.sqrt(x * x + y * y + z * z);

        // Normalize the accelerometer vector
        x = (x / norm_Of_g);
        y = (y / norm_Of_g);
        z = (z / norm_Of_g);
        int inclination = (int) Math.round(Math.toDegrees(Math.acos(y)));
        Log.i("tag","incline is:"+inclination);

        if (inclination < 25 || inclination > 155)
        {
            isFlat = true;
        }else {
            isFlat = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
