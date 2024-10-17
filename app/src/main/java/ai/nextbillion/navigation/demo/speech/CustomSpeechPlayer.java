package ai.nextbillion.navigation.demo.speech;

import android.content.Context;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;


import java.util.Queue;

import ai.nextbillion.navigation.ui.voice.SpeechAnnouncement;

import ai.nextbillion.navigation.ui.voice.SpeechPlayer;

import android.speech.tts.TextToSpeech;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Locale;

import ai.nextbillion.navigation.core.utils.LogUtil;


/**
 * Default player used to play voice instructions when a connection to Polly is unable to be established.
 * <p>
 * This instruction player uses {@link TextToSpeech} to play voice instructions.
 *
 */
public class CustomSpeechPlayer implements SpeechPlayer {

    private static final String DEFAULT_UTTERANCE_ID = "default_id";

    private final TextToSpeech textToSpeech;
    private CustomSpeechListener speechListener;

    private boolean isMuted;
    private boolean languageSupported = false;

    private boolean speechHasInit = false;

    private Queue<SpeechAnnouncement> announcementQueue;

    public CustomSpeechPlayer(Context context, final String language, final CustomSpeechListener speechListener) {
        textToSpeech = new TextToSpeech(context, status -> {
            boolean ableToInitialize = status == TextToSpeech.SUCCESS && language != null;
            if (!ableToInitialize) {
                LogUtil.e("SpeechPlayer","There was an error initializing native TTS");
                return;
            }
            setSpeechListener(speechListener);
            initializeWithLanguage(new Locale(language));
            speechHasInit = true;
            playAnnouncementInQueue();
        });
    }

    /**
     * Plays the given voice instruction using TTS
     *
     * @param speechAnnouncement with voice instruction to be synthesized and played
     */
    @Override
    public void play(SpeechAnnouncement speechAnnouncement) {
        LogUtil.w("SpeechPlayer","play: " + speechAnnouncement.announcement());
        if (!speechHasInit){
            pushAnnouncementIntoQueue(speechAnnouncement);
            return;
        }
        boolean isValidAnnouncement = speechAnnouncement != null
                && !TextUtils.isEmpty(speechAnnouncement.announcement());
        boolean canPlay = isValidAnnouncement && languageSupported && !isMuted;
        if (!canPlay) {
            return;
        }

        HashMap<String, String> params = new HashMap<>(1);
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, DEFAULT_UTTERANCE_ID);
        textToSpeech.speak(speechAnnouncement.announcement(), TextToSpeech.QUEUE_ADD, params);
    }

    /**
     * Returns whether or not the AndroidSpeechPlayer is currently muted
     *
     * @return true if muted, false if not
     */
    @Override
    public boolean isMuted() {
        return isMuted;
    }

    /**
     * Mutes or un-mutes the AndroidSpeechPlayer, canceling any instruction currently being voiced,
     * and preventing subsequent instructions from being voiced
     *
     * @param isMuted true if should be muted, false if should not
     */
    @Override
    public void setMuted(boolean isMuted) {
        this.isMuted = isMuted;
        if (isMuted) {
            muteTts();
        }
    }

    /**
     * To be called during an off-route event
     */
    @Override
    public void onOffRoute() {
        // Todo : You can play a custom prompt for this off-route event.
    }

    /**
     * Stops and shuts down TTS
     */
    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (announcementQueue != null){
            announcementQueue.clear();
        }
    }

    @Override
    public boolean isSpeaking() {
        return textToSpeech.isSpeaking();
    }

    @Override
    public void stop() {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
    }

    private void muteTts() {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
    }

    private void initializeWithLanguage(Locale language) {
        boolean isLanguageAvailable = textToSpeech.isLanguageAvailable(language) == TextToSpeech.LANG_AVAILABLE;
        if (!isLanguageAvailable) {
            LogUtil.w("SpeechPlayer","The specified language is not supported by TTS");
            return;
        }
        languageSupported = true;
        textToSpeech.setLanguage(language);
    }

    private void setSpeechListener(final CustomSpeechListener speechListener) {
        this.speechListener = speechListener;

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                speechListener.onStart();
            }

            @Override
            public void onDone(String utteranceId) {
                speechListener.onDone();
            }

            @Override
            public void onError(String utteranceId) {
                speechListener.onError("Error playing TTS", null);
            }
        });
    }

    private void pushAnnouncementIntoQueue(SpeechAnnouncement announcement){
        if (announcementQueue == null){
            announcementQueue = new ArrayDeque<>();
        }
        announcementQueue.add(announcement);
    }

    private void playAnnouncementInQueue(){
        if (announcementQueue == null || announcementQueue.isEmpty()){
            return;
        }
        SpeechAnnouncement announcement = announcementQueue.poll();
        if (announcement != null) {
            play(announcement);
        }
        playAnnouncementInQueue();
    }
}