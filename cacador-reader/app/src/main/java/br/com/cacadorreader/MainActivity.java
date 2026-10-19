package br.com.cacadorreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

/**
 * Lume 2.0 Beta 1 settings. Category-first navigation with a clear Back control.
 * Uses native Android widgets so accessibility services can inspect their roles and states.
 */
public class MainActivity extends Activity {
    private static final int BG = Color.rgb(10, 17, 32);
    private static final int PANEL = Color.rgb(19, 29, 48);
    private static final int TEXT = Color.rgb(245, 248, 255);
    private static final int MUTED = Color.rgb(194, 207, 229);
    private static final int ACCENT = Color.rgb(42, 187, 160);

    private SharedPreferences prefs;
    private LinearLayout panel;
    private TextView statusText;
    private String currentPage = "home";

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("cacador_settings", MODE_PRIVATE);
        if (state != null) currentPage = state.getString("page", "home");
        renderPage(currentPage);
        showWelcomeIfNeeded();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putString("page", currentPage);
        super.onSaveInstanceState(out);
    }

    @Override protected void onResume() {
        super.onResume();
        updateStatus();
        applySettings();
    }

    @Override public void onBackPressed() {
        if (!"home".equals(currentPage)) {
            renderPage("home");
        } else {
            super.onBackPressed();
        }
    }

    private void renderPage(String page) {
        currentPage = page;
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(20), dp(18), dp(20), dp(28));
        panel.setBackgroundColor(BG);
        panel.setFocusableInTouchMode(true);
        scroll.addView(panel);
        setContentView(scroll);

        if ("home".equals(page)) renderHome();
        else renderCategory(page);
        panel.setFocusableInTouchMode(true);
        panel.requestFocus();
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, dp(5), 0, dp(5));
        return t;
    }

    private GradientDrawable rounded(int fill, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private void renderHome() {
        panel.addView(text("Lume", 34, TEXT, true));
        panel.addView(text("Versão 2.0 Beta 1 · Central de acessibilidade", 16, MUTED, false));
        panel.addView(text("Configurações organizadas por categoria. Abra uma categoria para encontrar seus controles e use Voltar para retornar a esta tela.", 16, MUTED, false));

        statusText = text("", 16, Color.rgb(135, 245, 213), true);
        statusText.setPadding(dp(14), dp(12), dp(14), dp(12));
        statusText.setBackground(rounded(Color.rgb(20, 43, 54), ACCENT, 14));
        LinearLayout.LayoutParams statusParams = fullWidth(dp(64));
        statusParams.topMargin = dp(10);
        statusParams.bottomMargin = dp(10);
        panel.addView(statusText, statusParams);
        updateStatus();

        addButton("Ativar ou gerenciar o leitor", "Abre as configurações de acessibilidade do Android", () ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        addCategory("Voz e leitura", "Velocidade da fala e nível de detalhamento", "voice");
        addCategory("Navegação e gestos", "Ajuda de gestos e comportamento de navegação", "navigation");
        addCategory("Sons e vibração", "Esquemas sonoros, volume e retorno tátil", "sound");
        addCategory("Acessibilidade e privacidade", "Recomendações para uso seguro e acessível", "accessibility");
        addCategory("Sobre e diagnóstico", "Estado do serviço, testes e versão", "about");

        panel.addView(text("Dica: mantenha o TalkBack ou outro leitor confiável disponível durante os testes desta versão beta.", 14, MUTED, false));
    }

    private void renderCategory(String page) {
        addButton("Voltar", "Retorna à central de configurações", () -> renderPage("home"));
        String title;
        if ("voice".equals(page)) title = "Voz e leitura";
        else if ("navigation".equals(page)) title = "Navegação e gestos";
        else if ("sound".equals(page)) title = "Sons e vibração";
        else if ("accessibility".equals(page)) title = "Acessibilidade e privacidade";
        else title = "Sobre e diagnóstico";
        panel.addView(text(title, 28, TEXT, true));
        panel.addView(text("As alterações são salvas automaticamente.", 15, MUTED, false));

        if ("voice".equals(page)) renderVoice();
        else if ("navigation".equals(page)) renderNavigation();
        else if ("sound".equals(page)) renderSound();
        else if ("accessibility".equals(page)) renderAccessibility();
        else renderAbout();
    }

    private void renderVoice() {
        addHeading("Velocidade da fala");
        SeekBar rate = new SeekBar(this);
        rate.setMax(22);
        rate.setProgress(Math.max(0, Math.min(22, Math.round((prefs.getFloat("speech_rate", 0.9f) - 0.5f) / 0.05f))));
        rate.setContentDescription("Velocidade da fala, de muito lenta a rápida");
        panel.addView(rate, fullWidth(dp(58)));
        TextView value = text("", 16, MUTED, false);
        panel.addView(value);
        updateRateLabel(value, rate.getProgress());
        rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean user) {
                float v = 0.5f + p * 0.05f;
                prefs.edit().putFloat("speech_rate", v).apply();
                updateRateLabel(value, p);
                CacadorAccessibilityService s = CacadorAccessibilityService.instance;
                if (s != null) s.setSpeechRate(v);
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) { speak("Velocidade da fala ajustada."); }
        });

        addHeading("Detalhamento da narração");
        String[] levels = {"Essencial", "Padrão", "Detalhado"};
        Spinner verbosity = spinner(levels, Math.max(0, Math.min(2, prefs.getInt("verbosity", 1))), "Nível de detalhamento da narração");
        panel.addView(verbosity, fullWidth(dp(58)));
        verbosity.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                prefs.edit().putInt("verbosity", pos).apply();
                applySettings();
            }
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        addButton("Testar a voz", "Ouve uma frase de demonstração", () -> speak("Teste de voz do Lume. A narração está funcionando."));
        addButton("Abrir configurações de síntese de voz", "Permite escolher o mecanismo de voz instalado no Android", () -> {
            try { startActivity(new Intent("com.android.settings.TTS_SETTINGS")); }
            catch (Exception e) { Toast.makeText(this, "Abra as configurações de texto para fala do Android.", Toast.LENGTH_LONG).show(); }
        });
    }

    private void renderNavigation() {
        addHeading("Ajuda de navegação");
        addButton("Ouvir guia completo de gestos", "Lê os gestos disponíveis", () -> {
            CacadorAccessibilityService s = CacadorAccessibilityService.instance;
            if (s != null) s.speakGestureHelp();
            else speak("Deslize para a direita para avançar, para a esquerda para voltar, para cima ou para baixo para rolar e toque duas vezes para ativar.");
        });
        addButton("Abrir configurações de acessibilidade", "Gerencie serviços e recursos de acessibilidade do Android", () ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        addHeading("Nota sobre conflitos");
        panel.addView(text("O Android pode limitar a exploração por toque a um único serviço ativo. Por isso, o Lume não pode garantir que seus gestos de exploração funcionem ao mesmo tempo que os do TalkBack em todos os aparelhos. Mantenha um leitor principal disponível e teste um serviço por vez.", 15, MUTED, false));
        addHeading("Comportamento atual");
        panel.addView(text("Deslizar para a direita ou esquerda tenta mover o foco entre elementos. Deslizar para cima ou para baixo tenta rolar uma área rolável. Toque duplo ativa o elemento focado.", 15, MUTED, false));
    }

    private void renderSound() {
        addHeading("Esquema de sons");
        Spinner packs = spinner(SoundEngine.PACK_NAMES, Math.max(0, Math.min(SoundEngine.PACK_NAMES.length - 1, prefs.getInt("sound_pack", 0))), "Esquema de efeitos sonoros");
        panel.addView(packs, fullWidth(dp(58)));
        packs.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                prefs.edit().putInt("sound_pack", pos).apply();
                SoundEngine.configure(pos, prefs.getFloat("sound_volume", 0.55f));
                applySettings();
            }
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        addSwitch("Efeitos sonoros de navegação", "Reproduz sons curtos para indicar foco e ativação.", "sounds_enabled", true);

        addHeading("Volume dos efeitos");
        SeekBar volume = new SeekBar(this);
        volume.setMax(100);
        volume.setProgress(Math.round(prefs.getFloat("sound_volume", 0.55f) * 100));
        volume.setContentDescription("Volume dos efeitos sonoros");
        panel.addView(volume, fullWidth(dp(58)));
        TextView volumeLabel = text("", 16, MUTED, false);
        panel.addView(volumeLabel);
        updateVolumeLabel(volumeLabel, volume.getProgress());
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean user) {
                prefs.edit().putFloat("sound_volume", p / 100f).apply();
                updateVolumeLabel(volumeLabel, p);
                applySettings();
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) { CacadorAccessibilityService s = CacadorAccessibilityService.instance; if (s != null) s.testSound(); }
        });
        addSwitch("Vibração discreta", "Pequenos retornos táteis ao navegar e ativar itens.", "haptics_enabled", true);
        addButton("Ouvir demonstração dos sons", "Toca o som de confirmação", () -> {
            CacadorAccessibilityService s = CacadorAccessibilityService.instance;
            if (s != null) s.testSound(); else speak("Ative o Lume primeiro.");
        });
    }

    private void renderAccessibility() {
        addHeading("Privacidade durante a digitação");
        panel.addView(text("O Lume não repete automaticamente cada caractere digitado. Isso ajuda a reduzir a exposição acidental de senhas e mensagens, mas também significa que esta beta ainda não oferece eco de digitação configurável.", 15, MUTED, false));
        addHeading("Uso seguro");
        panel.addView(text("O Lume é experimental. Alguns aplicativos não expõem todos os controles às APIs de acessibilidade. Não desative seu leitor principal até verificar que a navegação essencial funciona.", 15, MUTED, false));
        addButton("Abrir configurações de acessibilidade", "Gerencie os serviços instalados", () ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        addButton("Restaurar configurações do Lume", "Apaga preferências e retorna aos valores padrão", () ->
            new AlertDialog.Builder(this).setTitle("Restaurar configurações?")
                .setMessage("Velocidade, detalhamento, sons, volume e vibração voltarão aos valores padrão.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Restaurar", (d, w) -> {
                    prefs.edit().clear().putBoolean("welcome_shown", true).apply();
                    applySettings();
                    renderPage("home");
                    speak("Configurações restauradas.");
                }).show());
    }

    private void renderAbout() {
        addHeading("Estado do serviço");
        TextView state = text("", 17, TEXT, true);
        panel.addView(state);
        state.setText(CacadorAccessibilityService.instance != null ? "O serviço do Lume está ativo." : "O serviço do Lume não está ativo.");
        addButton("Repetir estado em voz alta", "Anuncia se o serviço está ativo", () -> speak(CacadorAccessibilityService.instance != null ? "O serviço do Lume está ativo." : "O serviço do Lume ainda não está ativo."));
        addButton("Abrir configurações de acessibilidade", "Ative ou gerencie o serviço", () ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        addHeading("Versão");
        panel.addView(text("Lume 2.0 Beta 1\nPacote: br.com.cacadorreader\nVersão experimental. Testes em aparelho real ainda são necessários.", 15, MUTED, false));
        addHeading("Verificação rápida");
        addButton("Testar voz e sons", "Testa o canal de voz e o efeito de confirmação", () -> {
            speak("Diagnóstico do Lume. Teste de voz iniciado.");
            CacadorAccessibilityService s = CacadorAccessibilityService.instance;
            if (s != null) s.testSound();
        });
    }

    private void addCategory(String title, String subtitle, String page) {
        Button b = new Button(this);
        b.setText(title + "\n" + subtitle + "\nAbrir categoria");
        b.setTextSize(16);
        b.setTextColor(TEXT);
        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        b.setAllCaps(false);
        b.setPadding(dp(16), dp(12), dp(16), dp(12));
        b.setBackground(rounded(PANEL, Color.rgb(48, 67, 94), 14));
        b.setContentDescription(title + ". " + subtitle + ". Toque duas vezes para abrir.");
        LinearLayout.LayoutParams lp = fullWidth(dp(88));
        lp.topMargin = dp(6);
        lp.bottomMargin = dp(6);
        panel.addView(b, lp);
        b.setOnClickListener(v -> renderPage(page));
    }

    private void addHeading(String value) {
        TextView t = text(value, 19, TEXT, true);
        t.setPadding(0, dp(18), 0, dp(8));
        panel.addView(t);
    }

    private void addButton(String title, String description, Runnable action) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setTextColor(TEXT);
        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        b.setPadding(dp(14), dp(10), dp(14), dp(10));
        b.setBackground(rounded(PANEL, Color.rgb(48, 67, 94), 12));
        b.setContentDescription(title + ". " + description);
        LinearLayout.LayoutParams lp = fullWidth(dp(58));
        lp.topMargin = dp(5);
        lp.bottomMargin = dp(5);
        panel.addView(b, lp);
        b.setOnClickListener(v -> action.run());
    }

    private void addSwitch(String title, String description, String key, boolean defaultValue) {
        Switch sw = new Switch(this);
        sw.setText(title);
        sw.setTextSize(16);
        sw.setTextColor(TEXT);
        sw.setPadding(dp(4), dp(10), dp(4), dp(10));
        sw.setContentDescription(title + ". " + description);
        sw.setChecked(prefs.getBoolean(key, defaultValue));
        panel.addView(sw, fullWidth(dp(64)));
        TextView hint = text(description, 14, MUTED, false);
        panel.addView(hint);
        sw.setOnCheckedChangeListener((button, checked) -> {
            prefs.edit().putBoolean(key, checked).apply();
            applySettings();
        });
    }

    private Spinner spinner(String[] values, int selected, String description) {
        Spinner sp = new Spinner(this);
        sp.setContentDescription(description);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        sp.setSelection(selected);
        return sp;
    }

    private LinearLayout.LayoutParams fullWidth(int height) {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
    }

    private void updateRateLabel(TextView target, int progress) {
        target.setText("Velocidade: " + String.format(Locale.forLanguageTag("pt-BR"), "%.2f", 0.5f + progress * 0.05f) + "x");
    }

    private void updateVolumeLabel(TextView target, int progress) {
        target.setText("Volume dos efeitos: " + progress + "%");
    }

    private void updateStatus() {
        if (statusText == null) return;
        statusText.setText(CacadorAccessibilityService.instance != null
            ? "Estado: leitor ativado."
            : "Estado: leitor não ativado. Abra as configurações de acessibilidade para ativá-lo.");
    }

    private void applySettings() {
        CacadorAccessibilityService s = CacadorAccessibilityService.instance;
        if (s != null) s.applySettings();
    }

    private void speak(String message) {
        CacadorAccessibilityService s = CacadorAccessibilityService.instance;
        if (s != null) s.speak(message);
        else Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showWelcomeIfNeeded() {
        if (prefs.getBoolean("welcome_shown", false)) return;
        new AlertDialog.Builder(this)
            .setTitle("Bem-vindo ao Lume 2.0")
            .setMessage("Esta é uma versão beta com configurações organizadas por categoria. Abra uma categoria, ajuste os controles e use Voltar para retornar. O Lume ainda precisa ser validado em aparelhos reais; mantenha um leitor principal disponível.")
            .setPositiveButton("Começar", (d, w) -> prefs.edit().putBoolean("welcome_shown", true).apply())
            .setOnCancelListener(d -> prefs.edit().putBoolean("welcome_shown", true).apply())
            .show();
    }
}
