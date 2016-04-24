package com.hellasDevelops.nickapostol.izianswer;

/**
 * Created by nickapostol on 19/1/2016.
 */
public interface ReceiverAndServiceCommunicator {
    void onNewValueSecondsArrived(int seconds);

    void onCallStarted();

    void onCallEnded();

    void onCallMissed(String savedNumber);
}
