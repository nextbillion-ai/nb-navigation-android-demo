package ai.nextbillion.navigation.demo.speech;

import ai.nextbillion.navigation.ui.voice.SpeechAnnouncement;

public class NavSpeechListener implements CustomSpeechListener {

    private final CustomSpeechAudioFocusManager audioFocusManager;

    public NavSpeechListener(CustomSpeechAudioFocusManager audioFocusManager) {
        this.audioFocusManager = audioFocusManager;
    }

    @Override
    public void onStart() {
        audioFocusManager.requestAudioFocus();
    }

    @Override
    public void onDone() {
        audioFocusManager.abandonAudioFocus();
    }

    @Override
    public void onError(String errorText, SpeechAnnouncement speechAnnouncement) {

    }
}
