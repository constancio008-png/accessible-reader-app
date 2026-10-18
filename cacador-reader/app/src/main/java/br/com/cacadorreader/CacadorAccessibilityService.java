package br.com.cacadorreader;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;

public class CacadorAccessibilityService extends AccessibilityService {
    public static CacadorAccessibilityService instance;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private float speechRate = 1.0f;
    private String lastSpoken = "";
    private long lastSpokenAt = 0L;

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("pt", "BR"));
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
                if (ttsReady) { tts.setSpeechRate(speechRate); speak("Caçador Reader ativado."); }
            }
        });
        AccessibilityServiceInfo info = getServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED
                | AccessibilityEvent.TYPE_VIEW_CLICKED
                | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                | AccessibilityEvent.TYPE_ANNOUNCEMENT;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 80;
        info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);
    }

    public void setSpeechRate(float rate) {
        speechRate = Math.max(0.5f, Math.min(1.6f, rate));
        if (tts != null && ttsReady) tts.setSpeechRate(speechRate);
    }

    public void speak(String text) {
        if (text == null || text.trim().isEmpty() || !ttsReady) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "cacador-" + System.currentTimeMillis());
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int type = event.getEventType();
        if (type == AccessibilityEvent.TYPE_VIEW_FOCUSED
                || type == AccessibilityEvent.TYPE_VIEW_CLICKED
                || type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || type == AccessibilityEvent.TYPE_ANNOUNCEMENT) {
            CharSequence text = event.getContentDescription();
            if (text == null || text.length() == 0) text = event.getText().isEmpty() ? null : event.getText().get(0);
            if (text == null && event.getSource() != null) {
                AccessibilityNodeInfo source = event.getSource();
                CharSequence desc = source.getContentDescription();
                CharSequence nodeText = source.getText();
                if (desc != null && desc.length() > 0) text = desc;
                else if (nodeText != null && nodeText.length() > 0) text = nodeText;
                else if (source.isClickable()) text = "Botão";
            }
            if (text != null) {
                String message = text.toString().trim();
                if (!message.isEmpty() && shouldSpeak(message)) speak(message);
            }
        }
    }

    private boolean shouldSpeak(String message) {
        long now = System.currentTimeMillis();
        if (message.equals(lastSpoken) && now - lastSpokenAt < 900) return false;
        lastSpoken = message;
        lastSpokenAt = now;
        return true;
    }

    @Override protected boolean onGesture(int gestureId) {
        switch (gestureId) {
            case GESTURE_SWIPE_UP:
                moveFocus(true);
                return true;
            case GESTURE_SWIPE_DOWN:
                moveFocus(false);
                return true;
            case GESTURE_SWIPE_LEFT:
                performGlobalAction(GLOBAL_ACTION_BACK);
                speak("Voltando à tela anterior.");
                return true;
            case GESTURE_SWIPE_RIGHT:
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if (root != null) {
                    AccessibilityNodeInfo focused = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                    if (focused != null) {
                        CharSequence text = focused.getContentDescription();
                        if (text == null || text.length() == 0) text = focused.getText();
                        if (text != null) speak(text.toString());
                        else speak("Elemento selecionado.");
                    } else speak("Nenhum elemento selecionado.");
                } else speak("Tela indisponível.");
                return true;
            default:
                return super.onGesture(gestureId);
        }
    }

    private void moveFocus(boolean forward) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) { speak("Tela indisponível."); return; }
        AccessibilityNodeInfo current = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        if (current == null) current = root;
        current.performAction(current.isAccessibilityFocused()
                ? AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS
                : AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
        if (forward) {
            AccessibilityNodeInfo next = current.focusSearch(View.FOCUS_FORWARD);
            if (next != null) {
                next.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                CharSequence desc = next.getContentDescription();
                CharSequence txt = next.getText();
                speak(desc != null && desc.length() > 0 ? desc.toString()
                        : (txt != null && txt.length() > 0 ? txt.toString()
                        : (next.isClickable() ? "Botão" : "Elemento")));
                return;
            }
            speak("Fim da tela.");
        } else {
            AccessibilityNodeInfo prev = current.focusSearch(View.FOCUS_BACKWARD);
            if (prev != null) {
                prev.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                CharSequence desc = prev.getContentDescription();
                CharSequence txt = prev.getText();
                speak(desc != null && desc.length() > 0 ? desc.toString()
                        : (txt != null && txt.length() > 0 ? txt.toString()
                        : (prev.isClickable() ? "Botão" : "Elemento")));
                return;
            }
            speak("Início da tela.");
        }
    }

    @Override public void onInterrupt() {
        if (tts != null) tts.stop();
    }

    @Override public void onDestroy() {
        instance = null;
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
