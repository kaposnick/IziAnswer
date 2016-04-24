package com.hellasDevelops.nickapostol.izianswer.AutoAnswerPackage;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;

import com.hellasDevelops.nickapostol.izianswer.ReceiverAndServiceCommunicator;

import java.util.Date;

/**
 * Created by nickapostol on 17/1/2016.
 */
public class CallReceiver extends PhoneCallReceiver {

    private static final String TAG = "CallReceiver";

    private int elapsedtime_inseconds;

    private CountDownTimer timer;
    private static boolean isTimerRunning = false;

    private boolean isSpeakerOn;

    private AutoAnswerService autoAnswerService;

    public CallReceiver(int elapsedtime_inseconds, AutoAnswerService autoAnswerService) {
        this.elapsedtime_inseconds = elapsedtime_inseconds;
        this.autoAnswerService = autoAnswerService;
    }


    @Override
    protected void onIncomingCallStarted(final Context ctx, String number, Date start) {
        Log.d(TAG, "Incoming Call started");
        timer = new CountDownTimer(elapsedtime_inseconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                isTimerRunning = true;
                Log.d(TAG, "Answering call in " + millisUntilFinished / 1000 + " seconds");
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
                buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
                        KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
                autoAnswerService.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
                Log.d(TAG, "Answer up call.");
            }
        }.start();

    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d(TAG, "Incoming  Call ended");
        turnOffTimer(timer);

        ReceiverAndServiceCommunicator callStarted = autoAnswerService;
        callStarted.onCallEnded();

    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.d(TAG, "Outgoing Call started");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d(TAG, "Outgoing Call ended");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Log.d(TAG, "Missed Call");
        turnOffTimer(timer);
    }

    @Override
    protected void onIngoingCallAnswered(Context context, String savedNumber, Date callStartTime) {
        ReceiverAndServiceCommunicator callStarted = autoAnswerService;
        callStarted.onCallStarted();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    protected void changeSecondsValue(int seconds) {
        super.changeSecondsValue(seconds);
        ReceiverAndServiceCommunicator mCallback = autoAnswerService;
        mCallback.onNewValueSecondsArrived(seconds);
        this.elapsedtime_inseconds = seconds;
    }


    /**
     * Turn off the current timer that is running.
     * Change the boolean value.
     *
     * @param cdTimer the timer to cancel.
     */
    private static void turnOffTimer(CountDownTimer cdTimer) {
        if (isTimerRunning) {
            cdTimer.cancel();
            isTimerRunning = false;
        }
    }
}
