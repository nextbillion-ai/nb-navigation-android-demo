package ai.nextbillion.navigation.demo.speech;

import android.media.AudioManager;
import android.os.Build;


/**
 * Provides the correct {@link CustomAudioFocusDelegate} based on the Android version.

 */
public class CustomAudioFocusDelegateProvider {

    private final CustomAudioFocusDelegate audioFocusDelegate;

    public CustomAudioFocusDelegateProvider(AudioManager audioManager) {
        audioFocusDelegate = buildAudioFocusDelegate(audioManager);
    }

    CustomAudioFocusDelegate retrieveAudioFocusDelegate() {
        return audioFocusDelegate;
    }

    private CustomAudioFocusDelegate buildAudioFocusDelegate(AudioManager audioManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Api26AudioFocusDelegate(audioManager);
        }
        return new SpeechAudioFocusDelegate(audioManager);
    }
}