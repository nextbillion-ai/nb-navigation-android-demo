package ai.nextbillion.navigation.demo.speech;

import android.media.AudioManager;

class SpeechAudioFocusDelegate implements CustomAudioFocusDelegate {

    private final AudioManager audioManager;

    SpeechAudioFocusDelegate(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void requestFocus() {
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
    }

    @Override
    public void abandonFocus() {
        audioManager.abandonAudioFocus(null);
    }
}
