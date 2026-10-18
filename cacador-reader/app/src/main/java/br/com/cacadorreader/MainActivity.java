package br.com.cacadorreader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private LinearLayout panel;
    private TextView statusText;
    private TextView rateText;
    private TextView volumeText;
    private TextView verbosityText;

    private int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("cacador_settings", MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(24), dp(22), dp(28));
        panel.setFocusableInTouchMode(true);
        scroll.addView(panel);

        TextView title = new TextView(this);
        title.setText("Lume");
        title.setTextSize(34);\n        title.setTextColor(Color.rgb(245, 248, 255));\n        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setContentDescription("Lume. Central de configurações do leitor de telas.");
        panel.addView(title);

        TextView intro = new TextView(this);
        intro.setText("Seu leitor, sua maneira de ouvir. Configure voz, navegação, sons acústicos e vibração.");
        intro.setTextSize(17);\n        intro.setTextColor(Color.rgb(194, 207, 229));
        intro.setPadding(0, dp(10), 0, dp(18));
        panel.addView(intro);

        statusText = new TextView(this);
        statusText.setTextSize(16);
        statusText.setPadding(0, 0, 0, dp(12));
        panel.addView(statusText);
        updateStatus();

        addButton("Ativar ou gerenciar o leitor", "Abre as configurações de acessibilidade do Android", () ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        addHeading("Voz e leitura");
        rateText = addLabel("");
        SeekBar rate = new SeekBar(this);
        rate.setMax(22);
        rate.setProgress(Math.round((prefs.getFloat("speech_rate", 0.9f) - 0.5f) / 0.05f));
        rate.setContentDescription("Velocidade da fala. De muito lenta até rápida.");\n        tintSeekBar(rate);
        panel.addView(rate, fullWidth( dp(54)));
        updateRateLabel(rate.getProgress());
        rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                float value = 0.5f + progress * 0.05f;
                prefs.edit().putFloat("speech_rate", value).apply();
                updateRateLabel(progress);
                CacadorAccessibilityService s = CacadorAccessibilityService.instance;
                if (s != null) s.setSpeechRate(value);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {
                speak("Velocidade da fala ajustada.");
            }
        });

        addHeading("Estilo de narração");
        verbosityText = addLabel("");
        SeekBar verbosity = new SeekBar(this);
        verbosity.setMax(2);
        verbosity.setProgress(prefs.getInt("verbosity", 1));
        verbosity.setContentDescription("Detalhamento da narração. Essencial, padrão ou detalhado.");\n        tintSeekBar(verbosity);
        panel.addView(verbosity, fullWidth(dp(54)));
        updateVerbosityLabel(verbosity.getProgress());
        verbosity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                prefs.edit().putInt("verbosity", progress).apply();
                updateVerbosityLabel(progress);
                applySettings();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) { speak("Estilo de narração atualizado."); }
        });

        addHeading("Sons acústicos próprios");
        addSwitch("Efeitos sonoros de navegação", "Sons suaves criados pelo próprio aplicativo; não são sons de videogame.",
                "sounds_enabled", true);
        volumeText = addLabel("");
        SeekBar volume = new SeekBar(this);
        volume.setMax(100);
        volume.setProgress(Math.round(prefs.getFloat("sound_volume", 0.55f) * 100));
        volume.setContentDescription("Volume dos efeitos sonoros");\n        tintSeekBar(volume);
        panel.addView(volume, fullWidth(dp(54)));
        updateVolumeLabel(volume.getProgress());
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                prefs.edit().putFloat("sound_volume", progress / 100f).apply();
                updateVolumeLabel(progress);
                applySettings();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {
                CacadorAccessibilityService s = CacadorAccessibilityService.instance;
                if (s != null) s.testSound();
            }
        });

        addButton("Ouvir demonstração dos sons", "Toca o efeito acústico de confirmação", () -> {
            CacadorAccessibilityService s = CacadorAccessibilityService.instance;
            if (s != null) s.testSound();
            else Toast.makeText(this, "Ative o Lume primeiro.", Toast.LENGTH_LONG).show();
        });

        addHeading("Acessibilidade tátil");
        addSwitch("Vibração discreta", "Pequenos retornos táteis ao navegar e ativar itens.",
                "haptics_enabled", true);

        addHeading("Gestos e ajuda");
        addButton("Ouvir guia completo de gestos", "Lê os gestos disponíveis no Lume", () -> {
            CacadorAccessibilityService s = CacadorAccessibilityService.instance;
            if (s != null) s.speakGestureHelp();
            else speak("Deslize para a direita para ir ao próximo item. Deslize para a esquerda para voltar. Deslize para cima ou para baixo para rolar. Toque duas vezes para ativar.");
        });
        addButton("Testar a voz", "Faz o leitor falar uma frase de teste", () ->
                speak("Teste de voz do Lume. A fala está funcionando."));
        addButton("Repetir estado do leitor", "Informa se o serviço está ativo", () -> {
            if (CacadorAccessibilityService.instance != null)
                speak("O serviço do Lume está ativo.");
            else
                speak("O serviço ainda não está ativo. Use o botão Ativar ou gerenciar o leitor.");
        });

        addHeading("Sobre");
        TextView note = new TextView(this);
        note.setText("Esta é uma versão experimental de um leitor de telas independente. Alguns aplicativos podem expor poucos elementos para acessibilidade, e os gestos podem variar conforme o aparelho. Mantenha um leitor confiável disponível enquanto testa.");
        note.setTextSize(14);\n        note.setTextColor(Color.rgb(170, 187, 211));\n        note.setBackground(rounded(Color.rgb(19, 29, 48), Color.rgb(48, 67, 94), 14));
        note.setPadding(0, dp(4), 0, dp(18));
        panel.addView(note);

        addButton("Restaurar configurações padrão", "Restaura velocidade, sons, volume, vibração e narração", () -> {
            prefs.edit().clear().apply();
            recreate();
            applySettings();
            speak("Configurações padrão restauradas.");
        });

        setContentView(scroll);
    }

    @Override protected void onResume() {
        super.onResume();
        updateStatus();
        applySettings();
    }

    private void updateStatus() {
        if (statusText == null) return;
        boolean active = CacadorAccessibilityService.instance != null;
        statusText.setText(active
                ? "Estado: leitor ativado."
                : "Estado: leitor não ativado. Use o botão abaixo para abrir as configurações.");
    }

    private void applySettings() {
        CacadorAccessibilityService s = CacadorAccessibilityService.instance;
        if (s != null) s.applySettings();
    }

    private void speak(String text) {
        CacadorAccessibilityService s = CacadorAccessibilityService.instance;
        if (s != null) s.speak(text);
        else Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void updateRateLabel(int progress) {
        if (rateText != null) rateText.setText("Velocidade da fala: " + String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.2f", 0.5f + progress * 0.05f) + "x");
    }

    private void updateVolumeLabel(int progress) {
        if (volumeText != null) volumeText.setText("Volume dos sons: " + progress + "%");
    }

    private void updateVerbosityLabel(int progress) {
        if (verbosityText != null) {
            String[] labels = {"Essencial — só informações importantes", "Padrão — equilíbrio entre detalhe e rapidez", "Detalhado — inclui instruções e estados"};
            verbosityText.setText(labels[Math.max(0, Math.min(2, progress))]);
        }
    }

    private void addHeading(String text) {
        TextView heading = new TextView(this);
        heading.setText(text);
        heading.setTextSize(21);
        heading.setPadding(0, dp(22), 0, dp(7));
        panel.addView(heading);
    }

    private TextView addLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(16);
        label.setPadding(0, dp(6), 0, dp(2));
        panel.addView(label);
        return label;
    }

    private void addSwitch(String title, String description, String key, boolean defaultValue) {
        Switch toggle = new Switch(this);
        toggle.setText(title);
        toggle.setTextSize(17);\n        toggle.setTextColor(Color.rgb(245, 248, 255));\n        toggle.setButtonTintList(ColorStateList.valueOf(Color.rgb(65, 210, 190)));
        toggle.setContentDescription(title + ". " + description);
        toggle.setChecked(prefs.getBoolean(key, defaultValue));
        toggle.setPadding(0, dp(10), 0, dp(10));
        panel.addView(toggle, fullWidth(dp(62)));
        TextView detail = new TextView(this);
        detail.setText(description);
        detail.setTextSize(14);
        detail.setPadding(0, 0, 0, dp(4));
        panel.addView(detail);
        toggle.setOnCheckedChangeListener((button, checked) -> {
            prefs.edit().putBoolean(key, checked).apply();
            applySettings();
            CacadorAccessibilityService s = CacadorAccessibilityService.instance;
            if (s != null) s.speak(title + (checked ? " ativado." : " desativado."));
        });
    }

    private void addButton(String title, String description, Runnable action) {
        Button button = new Button(this);
        button.setText(title);
        button.setTextSize(16);\n        button.setTextColor(Color.rgb(245, 248, 255));\n        button.setBackground(rounded(Color.rgb(25, 40, 65), Color.rgb(49, 190, 174), 16));\n        button.setPadding(dp(18), dp(10), dp(18), dp(10));\n        button.setElevation(dp(2));
        button.setContentDescription(title + ". " + description);
        button.setAllCaps(false);
        button.setMinHeight(dp(60));
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = fullWidth(dp(64));
        params.topMargin = dp(6);
        panel.addView(button, params);
    }

    private GradientDrawable rounded(int fill, int stroke, int radiusDp) {\n        GradientDrawable shape = new GradientDrawable();\n        shape.setColor(fill);\n        shape.setCornerRadius(dp(radiusDp));\n        shape.setStroke(dp(1), stroke);\n        return shape;\n    }\n\n    private void tintSeekBar(SeekBar bar) {\n        if (android.os.Build.VERSION.SDK_INT >= 21) {\n            bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(65, 210, 190)));\n            bar.setThumbTintList(ColorStateList.valueOf(Color.rgb(245, 248, 255)));\n            bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(57, 75, 102)));\n        }\n    }\n\n    private LinearLayout.LayoutParams fullWidth(int height) {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
    }
}
