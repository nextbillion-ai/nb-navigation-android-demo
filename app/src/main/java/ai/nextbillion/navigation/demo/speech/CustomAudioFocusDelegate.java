package ai.nextbillion.navigation.demo.speech;

/**
 * A interface to manages audio focus for speech.

 */
interface CustomAudioFocusDelegate {

    void requestFocus();

    void abandonFocus();
}