package com.hellasDevelops.nickapostol.izianswer;

import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;

/**
 * Created by nickapostol on 24/1/2016.
 */
public class Text2SpeechSpeaker {
    public static void speak(TextToSpeech text2speechEngine, int queueMode, String utteranceID, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            text2speechEngine.speak(message, queueMode, null, utteranceID);
        } else {
            HashMap<String, String> ttsParameters = new HashMap();
            ttsParameters.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, SecondaryActivity.GIVE_COMMAND_REQUEST);
            text2speechEngine.speak(message, queueMode, ttsParameters);
        }
    }
}
