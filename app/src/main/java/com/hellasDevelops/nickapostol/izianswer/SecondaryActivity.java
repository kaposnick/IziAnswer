package com.hellasDevelops.nickapostol.izianswer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.logging.Handler;
import java.util.zip.Inflater;

public class SecondaryActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, View.OnClickListener, TextToSpeech.OnUtteranceCompletedListener, Communicator {

    private final static int MY_DATA_CHECK_CODE = 856;

    public final static String GIVE_NUMBER_REQUEST = "GIVE A NUMBER REQUEST";
    public final static String GIVE_COMMAND_REQUEST = "GIVE A COMMAND REQUEST";
    public final static String VERIFICATION_REQUEST = "VERIFICATION REQUEST";

    private final static int START_RECOGNIZER_CONSTANT = 123456;

    private SpeechRecognizer speechRecognizer;
    private Intent startRecognitionIntent;
    private MyProcessHandler myHandler;

    private Button interactionBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);

        interactionBT = (Button) findViewById(R.id.interactionBT);
        interactionBT.setOnClickListener(this);

        myHandler = new MyProcessHandler();


        startRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startRecognitionIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        startRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startRecognitionIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

    private TextToSpeech mText2SpeechEngine;


    @Override
    public void onClick(View v) {
        Text2SpeechSpeaker.speak(mText2SpeechEngine, TextToSpeech.QUEUE_FLUSH, GIVE_COMMAND_REQUEST, "Please give a command");
    }


    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mText2SpeechEngine = new TextToSpeech(this, this);
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                speechRecognizer.setRecognitionListener(new MyRecognitionListener(this, mText2SpeechEngine, this));
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }


    @Override
    public void onInit(int status) {
        mText2SpeechEngine.setLanguage(Locale.UK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mText2SpeechEngine.setOnUtteranceProgressListener(utteranceProgressListener);
        } else {
            mText2SpeechEngine.setOnUtteranceCompletedListener(this);
        }
    }

    @Override
    public void onUtteranceCompleted(String utteranceId) {
        if (utteranceId.equals(GIVE_NUMBER_REQUEST) || utteranceId.equals(GIVE_COMMAND_REQUEST) || utteranceId.equals(VERIFICATION_REQUEST)) {
            Message msgObj = myHandler.obtainMessage(START_RECOGNIZER_CONSTANT);
            myHandler.sendMessage(msgObj);
        }
    }


    UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {
            if (utteranceId.equals(GIVE_NUMBER_REQUEST) ||
                    utteranceId.equals(GIVE_COMMAND_REQUEST) ||
                    utteranceId.equals(VERIFICATION_REQUEST)) {
                Message msgObj = myHandler.obtainMessage(START_RECOGNIZER_CONSTANT);
                myHandler.sendMessage(msgObj);
            }
        }

        @Override
        public void onError(String utteranceId) {

        }
    };

    @Override
    public void onHappenedError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void MakeCall(String number) {
        Intent phonecallIntent = new Intent(Intent.ACTION_CALL);

        phonecallIntent.setData(Uri.parse("tel:" + "6946308450"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(phonecallIntent) ;
    }

    private class MyProcessHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_RECOGNIZER_CONSTANT) {
                speechRecognizer.startListening(startRecognitionIntent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mText2SpeechEngine.shutdown();
    }
}
