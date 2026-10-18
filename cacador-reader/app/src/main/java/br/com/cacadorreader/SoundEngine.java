package br.com.cacadorreader;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Build;

/**
 * Efeitos acústicos próprios do Lume: discretos, orgânicos e sem estética de videogame.
 * Os sons são sintetizados localmente; não há arquivos ou serviços externos.
 */
public final class SoundEngine {
    public static final int FOCUS = 0;
    public static final int ACTIVATE = 1;
    public static final int BACK = 2;
    public static final int BOUNDARY = 3;
    public static final int TOGGLE = 4;

    private static final int RATE = 22050;
    private SoundEngine() {}

    public static void play(int kind, float volume) {
        final float safeVolume = Math.max(0f, Math.min(1f, volume));
        if (safeVolume <= 0.01f) return;
        Thread worker = new Thread(() -> renderAndPlay(kind, safeVolume), "Lume-AudioCue");
        worker.setDaemon(true);
        worker.start();
    }

    private static void renderAndPlay(int kind, float volume) {
        int durationMs = kind == ACTIVATE ? 210 : (kind == BACK ? 170 : 105);
        int count = RATE * durationMs / 1000;
        short[] pcm = new short[count];
        long seed = System.nanoTime();
        for (int i = 0; i < count; i++) {
            float t = (float)i / RATE;
            float duration = (float)count / RATE;
            float x = (float)i / Math.max(1, count - 1);
            float attack = Math.min(1f, x * (kind == FOCUS ? 45f : 18f));
            float release = Math.min(1f, (1f - x) * (kind == FOCUS ? 13f : 7f));
            float envelope = attack * release;
            double wave = 0;
            if (kind == FOCUS) {
                wave = Math.sin(2 * Math.PI * 620 * t) * 0.48
                     + Math.sin(2 * Math.PI * 930 * t) * 0.16;
            } else if (kind == ACTIVATE) {
                double glide = 440 + 210 * x;
                wave = Math.sin(2 * Math.PI * glide * t) * 0.42
                     + Math.sin(2 * Math.PI * glide * 1.5 * t) * 0.14
                     + Math.sin(2 * Math.PI * 220 * t) * 0.10;
            } else if (kind == BACK) {
                double glide = 540 - 210 * x;
                wave = Math.sin(2 * Math.PI * glide * t) * 0.38
                     + Math.sin(2 * Math.PI * glide * 1.25 * t) * 0.10;
            } else if (kind == BOUNDARY) {
                wave = Math.sin(2 * Math.PI * 330 * t) * 0.38
                     + Math.sin(2 * Math.PI * 247 * t) * 0.12;
            } else {
                wave = Math.sin(2 * Math.PI * 520 * t) * 0.34
                     + Math.sin(2 * Math.PI * 680 * t) * 0.12;
            }
            // A hint of filtered-like noise softens the timbre without sounding like a game beep.
            seed = seed * 1103515245L + 12345L;
            double noise = (((seed >>> 16) & 0x7fff) / 16384.0 - 1.0) * 0.018;
            double sample = (wave + noise) * envelope * volume * 0.42;
            pcm[i] = (short)Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, (int)(sample * 32767)));
        }

        AudioTrack track = null;
        try {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            AudioFormat format = new AudioFormat.Builder().setSampleRate(RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build();
            track = new AudioTrack(attrs, format, pcm.length * 2,
                    AudioTrack.MODE_STATIC, AudioManager.AUDIO_SESSION_ID_GENERATE);
            track.write(pcm, 0, pcm.length);
            track.setVolume(1f);
            track.play();
            Thread.sleep(durationMs + 80L);
        } catch (Exception ignored) {
            // Efeitos sonoros nunca devem interromper a leitura por voz.
        } finally {
            if (track != null) {
                try { track.stop(); } catch (Exception ignored) {}
                track.release();
            }
        }
    }
}
