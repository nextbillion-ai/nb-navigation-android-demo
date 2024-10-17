package ai.nextbillion.navigation.demo.speech;


public class CustomSpeechAudioFocusManager {

    private final CustomAudioFocusDelegate audioFocusDelegate;

    public CustomSpeechAudioFocusManager(CustomAudioFocusDelegateProvider provider) {
        audioFocusDelegate = provider.retrieveAudioFocusDelegate();
    }

    void requestAudioFocus() {
        audioFocusDelegate.requestFocus();
    }

    void abandonAudioFocus() {
        audioFocusDelegate.abandonFocus();
    }
}
