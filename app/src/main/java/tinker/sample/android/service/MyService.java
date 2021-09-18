package tinker.sample.android.service;

import android.content.Intent;
import android.os.IBinder;

public class MyService extends android.app.Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
