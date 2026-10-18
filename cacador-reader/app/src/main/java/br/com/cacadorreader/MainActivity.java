package br.com.cacadorreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("Caçador Reader");
        title.setTextSize(28);
        title.setContentDescription("Caçador Reader, leitor de telas experimental");
        root.addView(title);

        TextView intro = new TextView(this);
        intro.setText("Leitor de telas experimental em português brasileiro.");
        intro.setTextSize(18);
        intro.setPadding(0, dp(12), 0, dp(16));
        root.addView(intro);

        Button enable = button("Ativar leitor de telas", "Abre as configurações de acessibilidade para ativar o Caçador Reader");
        enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(enable, new LinearLayout.LayoutParams(-1, dp(64)));

        Button test = button("Testar fala", "Testar a voz do Caçador Reader");
        test.setOnClickListener(v -> {
            CacadorAccessibilityService service = CacadorAccessibilityService.instance;
            if (service != null) service.speak("Caçador Reader pronto. A fala está funcionando.");
            else Toast.makeText(this, "Ative o serviço de acessibilidade primeiro.", Toast.LENGTH_LONG).show();
        });
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(64));
        bp.topMargin = dp(10);
        root.addView(test, bp);

        TextView rateLabel = new TextView(this);
        rateLabel.setText("Velocidade da fala");
        rateLabel.setTextSize(18);
        rateLabel.setPadding(0, dp(22), 0, dp(4));
        root.addView(rateLabel);

        SeekBar rate = new SeekBar(this);
        rate.setMax(19);
        rate.setProgress(7);
        rate.setContentDescription("Velocidade da fala. Ajuste entre lenta e rápida.");
        root.addView(rate, new LinearLayout.LayoutParams(-1, dp(56)));
        rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                CacadorAccessibilityService service = CacadorAccessibilityService.instance;
                if (service != null) service.setSpeechRate(0.65f + progress * 0.05f);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {
                CacadorAccessibilityService service = CacadorAccessibilityService.instance;
                if (service != null) service.speak("Velocidade ajustada.");
            }
        });

        Button repeat = button("Repetir estado", "Falar o estado do serviço");
        repeat.setOnClickListener(v -> {
            if (CacadorAccessibilityService.instance != null)
                CacadorAccessibilityService.instance.speak("Caçador Reader está ativo.");
            else Toast.makeText(this, "O leitor ainda não está ativo.", Toast.LENGTH_LONG).show();
        });
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(64));
        rp.topMargin = dp(10);
        root.addView(repeat, rp);

        TextView note = new TextView(this);
        note.setText("Versão 0.3.0 INSTÁVEL. Gestos planejados: deslizar para cima/baixo para navegar; esquerda para voltar; direita para repetir. O comportamento pode variar por aplicativo.");
        note.setTextSize(14);
        note.setPadding(0, dp(20), 0, 0);
        root.addView(note);

        setContentView(root);
    }

    private Button button(String text, String description) {
        Button b = new Button(this);
        b.setText(text);
        b.setContentDescription(description);
        return b;
    }
}
