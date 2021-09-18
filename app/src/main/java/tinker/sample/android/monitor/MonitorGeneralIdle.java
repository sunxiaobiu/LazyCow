package tinker.sample.android.monitor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MonitorGeneralIdle implements SensorEventListener {
    private Context context;
    private boolean isIdle;
    private SensorManager sensorManager;

    public MonitorGeneralIdle(Context context){
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        isIdle =  InstanceIdleUtil.checkInstanceGeneralIdleState(context);
    }

    public boolean getIdleState(){
        return isIdle;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        isIdle = InstanceIdleUtil.checkInstanceGeneralIdleState(context);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
