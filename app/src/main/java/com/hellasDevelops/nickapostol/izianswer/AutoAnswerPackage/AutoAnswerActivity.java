package com.hellasDevelops.nickapostol.izianswer.AutoAnswerPackage;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellasDevelops.nickapostol.izianswer.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoAnswerActivity extends AppCompatActivity implements View.OnClickListener {


    private final static String SPINNER_VALUE = "Spinner_Value";
    private final static String SWITCH_VALUE = "Switch_Value";
    private final static String SERVICE_RUNNIG_VALUE = "Service_Running_Value";
    public static final String TAG = "AutoAnswerActivity";

    private boolean isServiceRunning;
    private boolean switchChecked = false;

    private int selectedSecondsValue;

    private Toolbar toolbar;
    private Button recognizerBT;

    /* Views */
    private SwitchCompat enableServiceSwitch;
    private LinearLayout secondsLayout;
    private TextView secondsTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        enableServiceSwitch = (SwitchCompat) findViewById(R.id.enableserviceSWITCH);
        secondsLayout = (LinearLayout) findViewById(R.id.secondsBeforeAnswerLayout);
        secondsLayout.setOnClickListener(this);
        secondsTV = (TextView) findViewById(R.id.secondsToWaitTV);

        setRecognizer();


        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        int secondsValueFromPrefs = prefs.getInt(SPINNER_VALUE, -1);
        boolean switchValuefromPrefs = prefs.getBoolean(SWITCH_VALUE, false);
        isServiceRunning = prefs.getBoolean(SERVICE_RUNNIG_VALUE, false);

        if (secondsValueFromPrefs != -1) {
            selectedSecondsValue = secondsValueFromPrefs;
            secondsTV.setText(selectedSecondsValue + "");
        } else {
            secondsTV.setText("");
        }

        enableServiceSwitch.setChecked(switchValuefromPrefs);
        getContact();
    }


    private void setRecognizer() {
        recognizerBT = (Button) findViewById(R.id.callBT);
        recognizerBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                Intent startRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                startRecognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
                startRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startRecognitionIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

                speechRecognizer.setRecognitionListener(


                        new RecognitionListener() {
                            @Override
                            public void onReadyForSpeech(Bundle params) {
                                Log.d(AutoAnswerActivity.TAG, "Ready for speech.");
                            }

                            @Override
                            public void onBeginningOfSpeech() {
                                Log.d(TAG, "Speech starting");
                            }

                            @Override
                            public void onRmsChanged(float rmsdB) {

                            }

                            @Override
                            public void onBufferReceived(byte[] buffer) {
                                Log.d(AutoAnswerActivity.TAG, "On buffer received called");
                            }

                            @Override
                            public void onEndOfSpeech() {
                                Log.d(AutoAnswerActivity.TAG, "Speech ended");
                            }

                            @Override
                            public void onError(int error) {
                                Log.e(AutoAnswerActivity.TAG, "Received an error");
                                switch (error) {
                                    case SpeechRecognizer.ERROR_NO_MATCH:
                                        Log.e(AutoAnswerActivity.TAG, "No recognition result matched");
                                        break;
                                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                        Log.e(AutoAnswerActivity.TAG, "No speech Input");
                                    default:
                                        break;
                                }
                            }

                            @Override
                            public void onResults(Bundle results) {
                                Log.d(AutoAnswerActivity.TAG, "OnResults called");
                                ArrayList<String> voiceResults = results
                                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                                float[] scoresresults = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
                                if (voiceResults == null) {
                                    Log.e(AutoAnswerActivity.TAG, "No voice results");
                                } else {
                                    Log.d(AutoAnswerActivity.TAG, "Printing matches: ");
                                    Log.d(AutoAnswerActivity.TAG, scoresresults.length + "");
                                    for (String match : voiceResults) {
                                        Log.d(AutoAnswerActivity.TAG, match + " Score: " + scoresresults[voiceResults.indexOf(match)]);
                                    }
                                }
                            }

                            @Override
                            public void onPartialResults(Bundle partialResults) {
                                //Log.d(AutoAnswerActivity.TAG, "OnPartialResults called");
                            }

                            @Override
                            public void onEvent(int eventType, Bundle params) {

                            }
                        });
                speechRecognizer.startListening(startRecognitionIntent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        enableServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(AutoAnswerActivity.TAG, "Autoanswer is on!");
                    Intent newServiceIntent = new Intent(getBaseContext(), AutoAnswerService.class);
                    newServiceIntent.putExtra(AutoAnswerService.ELAPSED_TIME_INTENT_EXTRA, selectedSecondsValue);
                    startService(newServiceIntent);
                    switchChecked = true;
                    isServiceRunning = true;
                } else {
                    Log.d(AutoAnswerActivity.TAG, "Autoanswer if off!");
                    stopService(new Intent(getBaseContext(), AutoAnswerService.class));
                    isServiceRunning = false;
                    switchChecked = false;
                }
            }
        });
    }


    private void getContact() {

    }

    @Override
    protected void onPause() {

         /* A bound service runs only as long as another application component is bound to it.
        Multiple components can bind to the service at once,
        but when all of them unbind, the service is destroyed. */

        super.onPause();
        Log.d(AutoAnswerActivity.TAG, "Activity paused");

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SPINNER_VALUE, selectedSecondsValue);
        editor.putBoolean(SWITCH_VALUE, switchChecked);
        editor.putBoolean(SERVICE_RUNNIG_VALUE, isServiceRunning);
        editor.commit();
    }


    @Override
    public void onClick(View v) {
        if (v.equals(secondsLayout)) {
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selece seconds before auto-answer");
        final int[] values = getResources().getIntArray(R.array.seconds_array);
        ArrayList<String> stringInts = new ArrayList<>();
        for (int value : values) {
            stringInts.add(value + "");
        }
        CharSequence[] sequences = stringInts.toArray(new CharSequence[stringInts.size()]);

        builder.setItems(sequences, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final int newValue = values[which];
                if (selectedSecondsValue != newValue) {
                    selectedSecondsValue = newValue;
                    secondsTV.setText(selectedSecondsValue+"");
                    if (isServiceRunning) {
                        Intent newIntent = new Intent(AutoAnswerService.ELAPSED_TIME_INTENT_ACTION);
                        newIntent.putExtra(AutoAnswerService.ELAPSED_TIME_INTENT_EXTRA, selectedSecondsValue);
                        sendBroadcast(newIntent);
                    }
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
