package ai.nextbillion.navigation.demo.speech;


/**
 * Manages audio focus for speech.
 */
public class CustomSpeechAudioFocusManager {

    private final CustomAudioFocusDelegate audioFocusDelegate;

    public CustomSpeechAudioFocusManager(CustomAudioFocusDelegateProvider provider) {
        audioFocusDelegate = provider.retrieveAudioFocusDelegate();
    }

    /**
     * Requests audio focus.
     */
    void requestAudioFocus() {
        audioFocusDelegate.requestFocus();
    }

    /**
     * Abandons audio focus.
     */
    void abandonAudioFocus() {
        audioFocusDelegate.abandonFocus();
    }
}
