package com.m929.lowkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Ползунок высоты клавиш внутри стандартного PreferenceScreen. Системного SeekBarPreference
 * в платформе нет (он есть только в AndroidX, а зависимостей в проекте нет), поэтому
 * пункт собирается из нативных виджетов.
 */
public class KeyHeightPreference extends Preference implements SeekBar.OnSeekBarChangeListener {

    private final SharedPreferences prefs;
    private final float dp;
    private TextView label;
    private int value;

    public KeyHeightPreference(Context c, AttributeSet a) {
        super(c, a);
        prefs = c.getSharedPreferences("kb", Context.MODE_PRIVATE);
        dp = c.getResources().getDisplayMetrics().density;
        int v = prefs.getInt("keyH", 42);
        value = Math.max(KbView.MIN_KEY_H, Math.min(KbView.MAX_KEY_H, v)); // испорченное значение не ломает клавиатуру
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        Context c = getContext();
        int pad = (int) (16 * dp);
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(pad, pad / 2, pad, pad / 2);

        label = new TextView(c);
        label.setTextSize(16);
        box.addView(label);

        SeekBar bar = new SeekBar(c);
        bar.setMax(KbView.MAX_KEY_H - KbView.MIN_KEY_H);
        bar.setProgress(value - KbView.MIN_KEY_H);
        bar.setOnSeekBarChangeListener(this);
        box.addView(bar);

        updateLabel();
        return box;
    }

    private void updateLabel() {
        if (label != null) label.setText(getContext().getString(R.string.key_height, value));
    }

    @Override
    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
        value = KbView.MIN_KEY_H + progress;
        updateLabel();
        prefs.edit().putInt("keyH", value).apply();
    }

    @Override
    public void onStartTrackingTouch(SeekBar bar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar bar) {
    }
}
