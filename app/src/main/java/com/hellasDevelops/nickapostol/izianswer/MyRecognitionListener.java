package com.hellasDevelops.nickapostol.izianswer;

import android.app.Activity;
import android.app.ProgressDialog;;
import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.hellasDevelops.nickapostol.izianswer.AutoAnswerPackage.AutoAnswerActivity;

import java.util.ArrayList;

/**
 * Created by nickapostol on 24/1/2016.
 */
public class MyRecognitionListener implements RecognitionListener {
    String bestWord = null;
    TextToSpeech textToSpeech;

    private Context context;
    private Activity activity;

    private ProgressDialog progress;

    private int currentState;
    private final static int COMMAND_RECOGNITION_STATE = 0x01;
    private final static int NUMBER_RECOGNITION_STATE = 0x02;
    private final static int NUMBER_VERIFICATION_RECOGNITION_STATE = 0x03;
    private final static int CANCEL_COMMAND_RECOGNITION_STATE = 0x04;

    public MyRecognitionListener(Context ctx, TextToSpeech ttsEngine, Activity act) {
        this.textToSpeech = ttsEngine;
        this.currentState = COMMAND_RECOGNITION_STATE;
        this.context = ctx;
        this.activity = act;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(AutoAnswerActivity.TAG, "Ready for speech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(AutoAnswerActivity.TAG, "Beginning of speech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d(AutoAnswerActivity.TAG, "End of speech called");
    }

    @Override
    public void onError(int error) {
        String errorString = "";
        switch (error) {
            case SpeechRecognizer.ERROR_NO_MATCH:
                errorString = "No recognition result matched";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                errorString = "No speech input";
                break;
            case SpeechRecognizer.ERROR_AUDIO:
                errorString = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                errorString = "Other client side errors";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                errorString = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                errorString = "Network related errors";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                errorString = "Network operation  timed out";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                errorString = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                errorString = "Server sends error status";
                break;
            default:
                break;
        }
        Log.d(AutoAnswerActivity.TAG, errorString);
        ((Communicator) activity).onHappenedError(errorString);

    }

    @Override
    public void onResults(Bundle results) {
        Log.d(AutoAnswerActivity.TAG, "OnResults called");
        ArrayList<String> voiceResults = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (voiceResults == null) {
            Log.e(AutoAnswerActivity.TAG, "No voice results");
        } else {
            if (currentState == COMMAND_RECOGNITION_STATE) {
                for (String match : voiceResults) {
                    if (match.toLowerCase().equals("call")) {
                        Log.d(AutoAnswerActivity.TAG, "Word Call detected");
                        Text2SpeechSpeaker.speak(textToSpeech, TextToSpeech.QUEUE_ADD, SecondaryActivity.GIVE_NUMBER_REQUEST, "Give the number to call");
                        currentState = NUMBER_RECOGNITION_STATE;
                        return;
                    }
                }
                Log.d(AutoAnswerActivity.TAG, "Word Call not detected");
                Text2SpeechSpeaker.speak(textToSpeech, TextToSpeech.QUEUE_ADD, SecondaryActivity.GIVE_COMMAND_REQUEST, "Please repeat the command");
            } else if (currentState == NUMBER_RECOGNITION_STATE) {
                ArrayList<String> possibleNumbers = new ArrayList<>();
                for (String possibleCallingNumber : voiceResults) {
                    possibleCallingNumber = replaceNonDigits(possibleCallingNumber);
                    Log.d(AutoAnswerActivity.TAG, possibleCallingNumber);
                    if (possibleCallingNumber.length() == 10 || possibleCallingNumber.length() == 12) {
                        possibleNumbers.add(possibleCallingNumber);
                    }

                    /*TODO: must create a background task that according to the list with the possible numbers will query into the contact table */
                }
                SearchThroughContactTask myTask = new SearchThroughContactTask(context);
                myTask.execute(possibleNumbers);
                if (possibleNumbers.size() > 0) {
                    Text2SpeechSpeaker.speak(textToSpeech,
                            TextToSpeech.QUEUE_ADD,
                            SecondaryActivity.VERIFICATION_REQUEST,
                            "Are you sure you want to call" + possibleNumbers.get(0) + " ?");
                    currentState = NUMBER_VERIFICATION_RECOGNITION_STATE;
                } else {
                    Text2SpeechSpeaker.speak(textToSpeech,
                            TextToSpeech.QUEUE_ADD,
                            SecondaryActivity.GIVE_NUMBER_REQUEST,
                            "Please repeat the number");
                }

            } else if (currentState == NUMBER_VERIFICATION_RECOGNITION_STATE) {
                    /* TODO: if we have received a valid number from the user must verify that the user want this contact to call */
                for (String match : voiceResults) {
                    if (match.toLowerCase().equals("yes")) {
                        Communicator comm = (Communicator) activity;
                        comm.MakeCall("");
                        currentState = COMMAND_RECOGNITION_STATE;
                        return;
                    } else if (match.toLowerCase().equals("no")) {

                        currentState = COMMAND_RECOGNITION_STATE;
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d(AutoAnswerActivity.TAG, "onEvent called");
    }

    private static String replaceNonDigits(final String string) {
        if (string == null || string.length() == 0) {
            return "";
        }
        return string.replaceAll("[^0-9]+", "");
    }
}
