package com.m929.lowkeyboard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ScrollView;
import android.widget.TextView;

/** Минимальный экран: включить клавиатуру, выбрать её и отметить языки для цикла глобуса. */
public class SettingsActivity extends Activity {

    private static final int MIN_KEY_H = 30, MAX_KEY_H = 60; // dp

    private SharedPreferences prefs;
    private boolean updating;   // игнорировать галочки, пока список обновляется программно
    private final CheckBox[] boxes = new CheckBox[Layouts.COUNT];

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("kb", Context.MODE_PRIVATE);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        Button enable = new Button(this);
        enable.setText(R.string.enable);
        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });
        root.addView(enable);

        Button choose = new Button(this);
        choose.setText(R.string.choose);
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showInputMethodPicker();
            }
        });
        root.addView(choose);

        TextView themeLabel = new TextView(this);
        themeLabel.setText(R.string.theme);
        themeLabel.setPadding(0, pad, 0, 0);
        root.addView(themeLabel);
        Spinner themes = new Spinner(this);
        final int itemH = (int) (56 * getResources().getDisplayMetrics().density); // крупные пункты
        final int itemPad = pad;
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Theme.NAMES) {
            private View big(View v) {
                TextView t = (TextView) v;
                t.setTextSize(20);
                t.setMinHeight(itemH);
                t.setGravity(Gravity.CENTER_VERTICAL);
                t.setPadding(itemPad, 0, itemPad, 0);
                return t;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return big(super.getView(position, convertView, parent));
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                return big(super.getDropDownView(position, convertView, parent));
            }
        };
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themes.setAdapter(themeAdapter);
        themes.setSelection(prefs.getInt("theme", 0));
        themes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                prefs.edit().putInt("theme", pos).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(themes);

        CheckBox dig = new CheckBox(this);
        dig.setText(R.string.digits_sym);
        dig.setChecked(prefs.getBoolean("digSym", false));
        dig.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean checked) {
                prefs.edit().putBoolean("digSym", checked).apply();
            }
        });
        root.addView(dig);

        final TextView hLabel = new TextView(this);
        hLabel.setPadding(0, pad, 0, 0);
        int kh = prefs.getInt("keyH", 42);
        hLabel.setText(getString(R.string.key_height, kh));
        root.addView(hLabel);
        SeekBar hBar = new SeekBar(this);
        hBar.setMax(MAX_KEY_H - MIN_KEY_H);
        hBar.setProgress(Math.max(MIN_KEY_H, Math.min(MAX_KEY_H, kh)) - MIN_KEY_H);
        hBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                int v = MIN_KEY_H + progress;
                hLabel.setText(getString(R.string.key_height, v));
                prefs.edit().putInt("keyH", v).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
        root.addView(hBar);

        TextView title = new TextView(this);
        title.setText(R.string.langs);
        title.setPadding(0, pad, 0, pad / 2);
        root.addView(title);

        int mask = prefs.getInt("mask2", Layouts.defaultMask());
        CompoundButton.OnCheckedChangeListener l = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean checked) {
                if (!updating) save(cb);
            }
        };
        for (int i = 0; i < Layouts.COUNT; i++) {
            CheckBox cb = new CheckBox(this);
            cb.setText(Layouts.TITLES[i]);
            cb.setChecked((mask & (1 << i)) != 0);
            cb.setOnCheckedChangeListener(l);
            boxes[i] = cb;
            root.addView(cb);
        }

        Button reset = new Button(this);
        reset.setText(R.string.reset_langs);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().remove("mask2").remove("lang2").apply(); // снова английский + язык системы
                int m = Layouts.defaultMask();
                updating = true;
                for (int i = 0; i < Layouts.COUNT; i++) boxes[i].setChecked((m & (1 << i)) != 0);
                updating = false;
            }
        });
        root.addView(reset);

        // Скрыть значок на рабочем столе: выключаем псевдоним с LAUNCHER (сами настройки остаются доступными из «Язык и ввод»)
        final ComponentName alias = new ComponentName(this, "com.m929.lowkeyboard.LauncherAlias");
        CheckBox hide = new CheckBox(this);
        hide.setText(R.string.hide_icon);
        hide.setChecked(getPackageManager().getComponentEnabledSetting(alias)
                == PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        hide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean checked) {
                getPackageManager().setComponentEnabledSetting(alias,
                        checked ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            }
        });
        hide.setPadding(hide.getPaddingLeft(), pad, hide.getPaddingRight(), 0);
        root.addView(hide);
        TextView hideHint = new TextView(this);
        hideHint.setText(R.string.hide_icon_hint);
        hideHint.setTextSize(13);
        root.addView(hideHint);

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);
    }

    private void save(CompoundButton changed) {
        int m = 0;
        for (int i = 0; i < Layouts.COUNT; i++) {
            if (boxes[i].isChecked()) m |= 1 << i;
        }
        if (m == 0) { // хотя бы один язык должен остаться
            changed.setChecked(true);
            return;
        }
        prefs.edit().putInt("mask2", m).apply();
    }
}
