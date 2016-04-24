package com.hellasDevelops.nickapostol.izianswer.AutoAnswerPackage;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.hellasDevelops.nickapostol.izianswer.R;
import com.hellasDevelops.nickapostol.izianswer.ReceiverAndServiceCommunicator;

public class AutoAnswerService extends Service implements ReceiverAndServiceCommunicator {

    private static final String TAG = "AutoAnswerService";

    /* change time INTENT ACTION*/
    public static final String ELAPSED_TIME_INTENT_ACTION = "changedElapsedTime";

    /* intent extra string */
    public static final String ELAPSED_TIME_INTENT_EXTRA = "seconds";

    /* Notification Tag*/
    public static final String NOTIFICATION_TAG = "ServiceNotification";
    public static final int NOTIFICATION_ID = 123;

    private static boolean isSpeakerOn;


    /* Signals Levels */
    private static final int EXCELLENT_LEVEL = 75;
    private static final int GOOD_LEVEL = 50;
    private static final int MODERATE_LEVEL = 50;
    private static final int WEAK_LEVEL = 0;

    /* seconds before answer */
    private static int elapsedtime_inseconds;

    private CallReceiver callReceiver = null;

    public AutoAnswerService() {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(AutoAnswerActivity.TAG, "Service is started!");
        elapsedtime_inseconds = intent.getExtras().getInt(AutoAnswerService.ELAPSED_TIME_INTENT_EXTRA);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        filter.addAction(AutoAnswerService.ELAPSED_TIME_INTENT_ACTION);
        callReceiver = new CallReceiver(elapsedtime_inseconds, this);
        registerReceiver(callReceiver, filter);



        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).
                setSmallIcon(R.mipmap.ears2).
                setContentTitle("IziAnswer").
                setContentText("Autoanswer service is running").
                setOngoing(true);

        Intent resultIntent = new Intent(this, AutoAnswerActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(AutoAnswerActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, builder.build());


        Log.d(TAG, "Broadcast receiver registered");
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callReceiver != null) {
            unregisterReceiver(callReceiver);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);
        }
        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onNewValueSecondsArrived(int seconds) {
        this.elapsedtime_inseconds = seconds;
    }

    @Override
    public void onCallStarted() {
        Log.d(TAG, "Ready to enable speakers");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);
        isSpeakerOn = true;
        Log.d(TAG, "Speakers: " + audioManager.isSpeakerphoneOn());
    }

    @Override
    public void onCallEnded() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isSpeakerphoneOn()) {
            Log.d(TAG, "Ready to disaple speakers");
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
            isSpeakerOn = false;
            Log.d(TAG, "Speakers: " + audioManager.isSpeakerphoneOn());
        }

    }

    @Override
    public void onCallMissed(String savedNumber) {

    }


}
