package ai.nextbillion.navigation.demo.speech;

import ai.nextbillion.navigation.ui.voice.SpeechAnnouncement;


/**
 * A interface to listen to speech events.

 */
public interface CustomSpeechListener {
    void onStart();

    void onDone();

    void onError(String var1, SpeechAnnouncement var2);
}

