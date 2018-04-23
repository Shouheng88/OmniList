package me.shouheng.omnilist.utils;

import android.speech.SpeechRecognizer;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;

public class RecognizerUtils {

    public static String getErrorMessage(int error) {
        StringBuilder sb = new StringBuilder(PalmApp.getStringCompact(R.string.recognize_failed));
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append(PalmApp.getStringCompact(R.string.error_audio));
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append(PalmApp.getStringCompact(R.string.error_speech_timeout));
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append(PalmApp.getStringCompact(R.string.error_client));
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append(PalmApp.getStringCompact(R.string.error_insufficient_permissions));
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append(PalmApp.getStringCompact(R.string.error_network));
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append(PalmApp.getStringCompact(R.string.error_no_match));
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append(PalmApp.getStringCompact(R.string.error_recognizer_busy));
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append(PalmApp.getStringCompact(R.string.error_server));
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append(PalmApp.getStringCompact(R.string.error_network_timeout));
                break;
        }
        sb.append(":").append(error);
        return sb.toString();
    }
}
