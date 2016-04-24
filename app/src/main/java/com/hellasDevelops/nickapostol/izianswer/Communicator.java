package com.hellasDevelops.nickapostol.izianswer;

/**
 * Created by nickapostol on 25/1/2016.
 */
public interface Communicator {
    void onHappenedError(String error);

    void MakeCall(String number);
}
