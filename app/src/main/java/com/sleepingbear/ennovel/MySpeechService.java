package com.sleepingbear.ennovel;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

public class MySpeechService extends Service implements TextToSpeech.OnInitListener {

    public static TextToSpeech ttsEn;
    public static TextToSpeech ttsKr;
    private String[] words;
    private String[] means;
    private String speakingWord;
    private boolean isInit;

    private int curIdx = -1;
    private String curKind = "";

    Handler mHandler = null;
    private Runnable mRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        ttsEn = new TextToSpeech(getApplicationContext(), this);
        ttsKr = new TextToSpeech(getApplicationContext(), this);
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d(CommConstants.tag, "onStartCommand");

        words = intent.getExtras().getStringArray("words");
        means = intent.getExtras().getStringArray("means");

        if (isInit) {
            speak();
        }

        return MySpeechService.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (ttsEn != null) {
            ttsEn.stop();
            ttsEn.shutdown();
        }
        if (ttsKr != null) {
            ttsKr.stop();
            ttsKr.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        //Log.d(CommConstants.tag, "onInit");

        if (status == TextToSpeech.SUCCESS) {
            int result = ttsEn.setLanguage(Locale.US);
            int result2 = ttsKr.setLanguage(Locale.KOREA);
            isInit = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED &&
                    result2 != TextToSpeech.LANG_MISSING_DATA && result2 != TextToSpeech.LANG_NOT_SUPPORTED;

            if ( isInit == true ) {
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 ){
                    ttsEn.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {
                            speak();
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                    ttsKr.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {
                            speak();
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                }else{
                    ttsEn.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                        @Override
                        public void onUtteranceCompleted(String utteranceId) {
                            speak();
                        }
                    });
                    ttsKr.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                        @Override
                        public void onUtteranceCompleted(String utteranceId) {
                            speak();
                        }
                    });
                }

                speak();
            }
        }
    }

    private void speak() {
        //Log.d("speak", curKind + " : " + curIdx + " : " + speakingWord);
        if ( curIdx < words.length ) {
            if (curIdx == -1) {
                curIdx = 0;
                curKind = "W";

                if ("W".equals(curKind)) {
                    speakingWord = words[curIdx];
                    curKind = "M";
                } else {
                    speakingWord = means[curIdx];
                    curKind = "W";
                    curIdx++;
                }
            } else {
                if ("W".equals(curKind)) {
                    speakingWord = words[curIdx];
                    curKind = "M";
                } else {
                    speakingWord = means[curIdx];
                    curKind = "W";
                    curIdx++;
                }
            }

            if ("W".equals(curKind)) {
                //뜻 TTS
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("ttsKr", curKind + " : " + curIdx + " : " + speakingWord);
                        if (ttsKr != null) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            //hm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
                            hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, CommConstants.appName);

                            ttsKr.speak(speakingWord, TextToSpeech.QUEUE_FLUSH, hm);
                        }
                    }
                };
                mHandler.postDelayed(mRunnable, 1000);
            } else {
                //단어 TTS
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("ttsEn", curKind + " : " + curIdx + " : " + speakingWord);
                        if (ttsEn != null) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            //hm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
                            hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, CommConstants.appName);

                            ttsEn.speak(speakingWord, TextToSpeech.QUEUE_FLUSH, hm);
                        }
                    }
                };
                mHandler.postDelayed(mRunnable, 1000);
            }
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
