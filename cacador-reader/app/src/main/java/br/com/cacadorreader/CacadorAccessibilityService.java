package br.com.cacadorreader;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.speech.tts.TextToSpeech;
import android.media.AudioAttributes;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;

public class CacadorAccessibilityService extends AccessibilityService {
    public static CacadorAccessibilityService instance;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private float speechRate = 0.9f;
    private boolean soundsEnabled = true;
    private boolean hapticsEnabled = true;
    private float soundVolume = 0.55f;
    private int verbosity = 1;
    private String lastSpoken = "";
    private long lastSpokenAt = 0L;
    private long lastCueAt = 0L;
    private String lastExploredLabel = "";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingContentAnnouncement;

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        loadSettings();
        SoundEngine.init(this);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("pt", "BR"));
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
                if (ttsReady) {
                    tts.setSpeechRate(speechRate);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) tts.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());
                    speak("Lume ativado. Deslize para a direita para o próximo item e para a esquerda para voltar.");
                    cue(SoundEngine.ACTIVATE);
                }
            }
        });
        AccessibilityServiceInfo info = getServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED
                | AccessibilityEvent.TYPE_VIEW_CLICKED
                | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                | AccessibilityEvent.TYPE_ANNOUNCEMENT
                | AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED
                | AccessibilityEvent.TYPE_VIEW_HOVER_ENTER
                | AccessibilityEvent.TYPE_VIEW_HOVER_EXIT
                | AccessibilityEvent.TYPE_VIEW_SELECTED
                | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
                | AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
                | AccessibilityEvent.TYPE_VIEW_SCROLLED
                | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;
        info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
                | AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE
                | AccessibilityServiceInfo.FLAG_SERVICE_HANDLES_DOUBLE_TAP
                | AccessibilityServiceInfo.FLAG_ENABLE_ACCESSIBILITY_VOLUME;
        if (Build.VERSION.SDK_INT >= 30) {
            info.flags |= AccessibilityServiceInfo.FLAG_SERVICE_HANDLES_DOUBLE_TAP;
        }
        setServiceInfo(info);
    }

    private void loadSettings() {
        SharedPreferences p = getSharedPreferences("cacador_settings", MODE_PRIVATE);
        speechRate = p.getFloat("speech_rate", 0.9f);
        soundsEnabled = p.getBoolean("sounds_enabled", true);
        hapticsEnabled = p.getBoolean("haptics_enabled", true);
        soundVolume = p.getFloat("sound_volume", 0.55f);
        verbosity = p.getInt("verbosity", 1);
        SoundEngine.configure(p.getInt("sound_pack", 0), soundVolume);
    }

    public void applySettings() {
        loadSettings();
        if (tts != null && ttsReady) tts.setSpeechRate(speechRate);
    }

    public void setSpeechRate(float rate) {
        speechRate = Math.max(0.5f, Math.min(1.6f, rate));
        if (tts != null && ttsReady) tts.setSpeechRate(speechRate);
    }

    public void speak(String text) {
        if (text == null || text.trim().isEmpty() || !ttsReady) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lume-" + System.currentTimeMillis());
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void cue(int kind) {
        if (!soundsEnabled) return;
        long now = System.currentTimeMillis();
        if (now - lastCueAt < 45) return;
        lastCueAt = now;
        SoundEngine.play(kind, soundVolume);
    }

    private void vibrate(long duration) {
        if (!hapticsEnabled) return;
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                VibratorManager manager = (VibratorManager)getSystemService(VIBRATOR_MANAGER_SERVICE);
                if (manager != null) manager.getDefaultVibrator().vibrate(VibrationEffect.createOneShot(duration, 70));
            } else {
                Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(duration, 70));
                    else vibrator.vibrate(duration);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int type = event.getEventType();
        if (type == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER || type == AccessibilityEvent.TYPE_VIEW_HOVER_EXIT) {
            if (type == AccessibilityEvent.TYPE_VIEW_HOVER_EXIT) {
                lastExploredLabel = "";
                return;
            }
            AccessibilityNodeInfo hovered = event.getSource();
            if (hovered != null) {
                String label = labelFor(hovered);
                if (!label.isEmpty() && !label.equals(lastExploredLabel)) {
                    lastExploredLabel = label;
                    hovered.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
                    announceItem(label, hovered);
                }
            }
            return;
        }
        if (type == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED || type == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null) announceItem(labelFor(source), source);
            return;
        }
        if (type == AccessibilityEvent.TYPE_VIEW_SELECTED) {
            AccessibilityNodeInfo selected = event.getSource();
            if (selected != null) {
                String label = labelFor(selected);
                if (!label.isEmpty() && shouldSpeak(label)) {
                    cue(SoundEngine.FOCUS);
                    speak(label);
                }
            }
            return;
        }
        if (type == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
                || type == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            // Do not echo typed content: this avoids reading passwords or private messages aloud.
            if (verbosity >= 2 && type == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                AccessibilityNodeInfo editor = event.getSource();
                if (editor != null && editor.isEditable() && !editor.isPassword()) {
                    speak("Campo de texto.");
                }
            }
            return;
        }
        if (type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            if (verbosity >= 2) {
                speak("Conteúdo rolado.");
            }
            return;
        }
        if (type == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            CharSequence notification = event.getContentDescription();
            if ((notification == null || notification.length() == 0) && !event.getText().isEmpty()) {
                notification = event.getText().get(0);
            }
            if (notification != null && shouldSpeak(notification.toString())) {
                cue(SoundEngine.FOCUS);
                speak(notification.toString());
            }
            return;
        }
        if (type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            scheduleContentAnnouncement();
            return;
        }
        if (type == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            cue(SoundEngine.ACTIVATE);
            vibrate(22);
            if (verbosity >= 2) speak("Ativado.");
            return;
        }
        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence title = event.getContentDescription();
            if (title == null || title.length() == 0) title = event.getText().isEmpty() ? null : event.getText().get(0);
            if (title != null && !title.toString().trim().isEmpty()) {
                String message = title.toString().trim();
                if (shouldSpeak(message)) { cue(SoundEngine.FOCUS); speak(message); }
            }
            return;
        }
        if (type == AccessibilityEvent.TYPE_ANNOUNCEMENT) {
            CharSequence text = event.getText().isEmpty() ? event.getContentDescription() : event.getText().get(0);
            if (text != null && shouldSpeak(text.toString())) speak(text.toString());
        }
    }

    private void scheduleContentAnnouncement() {
        if (pendingContentAnnouncement != null) {
            mainHandler.removeCallbacks(pendingContentAnnouncement);
        }
        pendingContentAnnouncement = () -> {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) return;
            AccessibilityNodeInfo focused = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
            if (focused == null) focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if (focused == null) return;
            String label = labelFor(focused);
            if (!label.isEmpty()) {
                announceItem(label, focused);
            }
        };
        // Content-change events can arrive in bursts. Wait briefly so we announce the final state.
        mainHandler.postDelayed(pendingContentAnnouncement, 220);
    }

    private String labelFor(AccessibilityNodeInfo node) {
        CharSequence text = node.getContentDescription();
        if (text == null || text.length() == 0) text = node.getText();
        String label = text == null ? "" : text.toString().trim();
        if (label.isEmpty()) {
            if (node.isCheckable()) label = "Controle de seleção";
            else if (node.isEditable()) label = "Campo de texto";
            else if (node.isScrollable()) label = "Área rolável";
            else if (node.isClickable()) label = "Elemento interativo";
            else if (verbosity >= 2) label = "Elemento";
        }
        return label;
    }

    private void announceItem(String message, AccessibilityNodeInfo node) {
        if (message.isEmpty() || !shouldSpeak(message)) return;
        cue(SoundEngine.FOCUS);
        vibrate(12);
        if (verbosity == 0 && !node.isClickable() && !node.isCheckable()) return;
        String output = message;
        if (verbosity >= 1) {
            String className = node.getClassName() == null ? "" : node.getClassName().toString();
            if (className.contains("CompoundButton") || className.contains("CheckBox")) output += ", caixa de seleção";
            else if (className.contains("Switch")) output += ", interruptor";
            else if (className.contains("RadioButton")) output += ", opção";
            else if (node.isEditable()) output += ", campo de texto";
            else if (node.isScrollable()) output += ", área rolável";
            else if (node.isClickable()) output += ", botão";
            if (node.isCheckable()) output += node.isChecked() ? ", marcado" : ", desmarcado";
            if (Build.VERSION.SDK_INT >= 30) {
                CharSequence state = node.getStateDescription();
                if (state != null && state.length() > 0 && !output.toLowerCase(java.util.Locale.ROOT).contains(state.toString().toLowerCase(java.util.Locale.ROOT))) output += ", " + state;
            }
            if (!node.isEnabled()) output += ", desativado";
            else if (verbosity >= 2 && node.isClickable()) output += ", toque duas vezes para ativar";
        }
        speak(output);
    }

    private boolean shouldSpeak(String message) {
        long now = System.currentTimeMillis();
        if (message.equals(lastSpoken) && now - lastSpokenAt < 700) return false;
        lastSpoken = message;
        lastSpokenAt = now;
        return true;
    }


    @Override protected boolean onGesture(int gestureId) {
        switch (gestureId) {
            case GESTURE_SWIPE_RIGHT: moveFocus(true); return true;
            case GESTURE_SWIPE_LEFT: moveFocus(false); return true;
            case GESTURE_SWIPE_UP: scrollOrMove(false); return true;
            case GESTURE_SWIPE_DOWN: scrollOrMove(true); return true;
            case GESTURE_DOUBLE_TAP: activateFocused(); return true;
            case GESTURE_DOUBLE_TAP_AND_HOLD: speakGestureHelp(); return true;
            default: return super.onGesture(gestureId);
        }
    }

    private void moveFocus(boolean forward) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            cue(SoundEngine.BOUNDARY);
            speak("Não há uma tela disponível para navegar.");
            return;
        }

        AccessibilityNodeInfo current = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        if (current == null) current = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);

        // First ask Android for the next focusable node. Some apps return null here,
        // so use a tree-order fallback rather than making swipe navigation a dead end.
        AccessibilityNodeInfo next = current == null ? null
                : current.focusSearch(forward ? View.FOCUS_FORWARD : View.FOCUS_BACKWARD);

        if (next == null || next.equals(current) || !isNodeNavigable(next)) {
            java.util.ArrayList<AccessibilityNodeInfo> nodes = new java.util.ArrayList<>();
            collectNavigableNodes(root, nodes);
            int index = current == null ? -1 : findNodeIndex(nodes, current);
            if (forward) {
                int target = index < 0 ? 0 : index + 1;
                next = target >= 0 && target < nodes.size() ? nodes.get(target) : null;
            } else {
                int target = index < 0 ? nodes.size() - 1 : index - 1;
                next = target >= 0 && target < nodes.size() ? nodes.get(target) : null;
            }
        }

        if (next == null || (current != null && next.equals(current))) {
            cue(SoundEngine.BOUNDARY);
            vibrate(35);
            speak(forward ? "Fim dos itens desta tela." : "Início dos itens desta tela.");
            return;
        }

        if (current != null) current.performAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        boolean focused = next.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
        if (!focused) {
            // A few custom views reject accessibility focus; still announce the node
            // so the gesture never appears to do absolutely nothing.
            String label = labelFor(next);
            if (!label.isEmpty()) {
                cue(SoundEngine.FOCUS);
                speak(label);
            } else {
                speak("Próximo elemento.");
            }
            return;
        }
        String label = labelFor(next);
        announceItem(label.isEmpty() ? "Elemento" : label, next);
    }

    private boolean isNodeNavigable(AccessibilityNodeInfo node) {
        if (node == null || !node.isVisibleToUser() || !node.isEnabled()) return false;
        CharSequence text = node.getText();
        CharSequence description = node.getContentDescription();
        return node.isFocusable() || node.isClickable() || node.isCheckable()
                || node.isEditable() || node.isScrollable()
                || (text != null && text.length() > 0)
                || (description != null && description.length() > 0);
    }

    private void collectNavigableNodes(AccessibilityNodeInfo node,
            java.util.ArrayList<AccessibilityNodeInfo> result) {
        if (node == null) return;
        if (isNodeNavigable(node)) result.add(node);
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) collectNavigableNodes(child, result);
        }
    }

    private int findNodeIndex(java.util.ArrayList<AccessibilityNodeInfo> nodes,
            AccessibilityNodeInfo target) {
        for (int i = 0; i < nodes.size(); i++) {
            AccessibilityNodeInfo node = nodes.get(i);
            if (node.equals(target)) return i;
        }
        return -1;
    }

    private void scrollOrMove(boolean down) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) { speak("Tela indisponível."); return; }
        AccessibilityNodeInfo scrollable = findScrollable(root);
        if (scrollable != null && scrollable.performAction(down
                ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
            cue(SoundEngine.BACK); vibrate(18);
            if (verbosity >= 1) speak(down ? "Rolando para baixo." : "Rolando para cima.");
            return;
        }
        AccessibilityNodeInfo focused = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        if (focused != null) moveFocus(down);
        else speak("Nenhuma área rolável encontrada.");
    }

    private AccessibilityNodeInfo findScrollable(AccessibilityNodeInfo node) {
        if (node == null) return null;
        if (node.isScrollable()) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo found = findScrollable(node.getChild(i));
            if (found != null) return found;
        }
        return null;
    }

    private void activateFocused() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            cue(SoundEngine.BOUNDARY);
            speak("Tela indisponível.");
            return;
        }

        AccessibilityNodeInfo focused = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        if (focused == null) focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);

        // Try the focused node first, then clickable ancestors. Custom Android views
        // often expose the label on a child while the click action belongs to its parent.
        AccessibilityNodeInfo candidate = focused;
        while (candidate != null) {
            if (candidate.isEnabled()
                    && candidate.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                cue(SoundEngine.ACTIVATE);
                vibrate(25);
                return;
            }
            candidate = candidate.getParent();
        }

        if (focused != null && focused.isCheckable()
                && focused.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            cue(SoundEngine.ACTIVATE);
            vibrate(25);
            return;
        }

        cue(SoundEngine.BOUNDARY);
        vibrate(35);
        speak(focused == null
                ? "Nenhum item selecionado. Deslize para a esquerda ou direita para escolher um item."
                : "Não foi possível ativar este item.");
    }

    public void speakGestureHelp() {
        speak("Ajuda do Lume. Deslize para a direita para ir ao próximo item. Deslize para a esquerda para voltar ao item anterior. Deslize para cima ou para baixo para rolar a área da tela. Toque duas vezes para ativar o item selecionado. Toque duas vezes e segure para ouvir esta ajuda.");
        cue(SoundEngine.ACTIVATE);
    }

    public void testSound() { cue(SoundEngine.ACTIVATE); vibrate(24); }

    @Override public void onInterrupt() { if (tts != null) tts.stop(); }

    @Override public void onDestroy() {
        if (pendingContentAnnouncement != null) mainHandler.removeCallbacks(pendingContentAnnouncement);
        instance = null;
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
