package com.example.smartparking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

public class TimerNoticeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        TimerNoticeWrapper timerNoticeWrapper= new TimerNoticeWrapper(context);
        NotificationCompat.Builder nb = timerNoticeWrapper.getChannelNotification();
        timerNoticeWrapper.getManager().notify(1, nb.build());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect vibe = VibrationEffect.createOneShot(
                    2000,VibrationEffect.DEFAULT_AMPLITUDE);
            Vibrator vib = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
            vib.vibrate(vibe);
        }

        Uri notify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ring = RingtoneManager.getRingtone(context,notify);
        ring.play();
    }
}
