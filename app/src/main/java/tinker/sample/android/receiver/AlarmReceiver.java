package tinker.sample.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);

    }
}
