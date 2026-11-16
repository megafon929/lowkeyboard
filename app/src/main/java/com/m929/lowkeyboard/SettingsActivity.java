package com.m929.lowkeyboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;

/**
 * Экран настроек на стандартном PreferenceActivity: пункты разложены по категориям,
 * виджеты системные. Темы - чекбоксы (включена всегда ровно одна), языки - тоже чекбоксы.
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    private SharedPreferences prefs;
    private boolean updating;   // галочки меняются программно - обработчики не должны на это реагировать
    private final CheckBoxPreference[] themes = new CheckBoxPreference[Theme.COUNT];
    private final CheckBoxPreference[] langs = new CheckBoxPreference[Layouts.COUNT];

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("kb", Context.MODE_PRIVATE);
        getPreferenceManager().setSharedPreferencesName("kb"); // те же prefs, что читает клавиатура
        addPreferencesFromResource(R.xml.settings);

        findPreference("enable").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference p) {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
                return true;
            }
        });

        findPreference("choose").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference p) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showInputMethodPicker();
                return true;
            }
        });

        buildThemes();
        buildLangs();

        findPreference("reset_langs").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference p) {
                prefs.edit().remove("mask2").remove("lang2").apply(); // снова английский + язык системы
                int m = Layouts.defaultMask();
                updating = true;
                for (int i = 0; i < langs.length; i++) langs[i].setChecked((m & (1 << i)) != 0);
                updating = false;
                return true;
            }
        });

        // Скрыть значок на рабочем столе: выключаем псевдоним с LAUNCHER (настройки остаются в «Язык и ввод»)
        final ComponentName alias = new ComponentName(this, "com.m929.lowkeyboard.LauncherAlias");
        CheckBoxPreference hide = (CheckBoxPreference) findPreference("hide_icon");
        hide.setChecked(getPackageManager().getComponentEnabledSetting(alias)
                == PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        hide.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference p, Object v) {
                getPackageManager().setComponentEnabledSetting(alias,
                        Boolean.TRUE.equals(v) ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
                return true;
            }
        });
    }

    /** Темы: чекбоксы, включённой может быть только одна. */
    private void buildThemes() {
        PreferenceCategory cat = (PreferenceCategory) findPreference("cat_theme");
        int cur = prefs.getInt("theme", 0);
        if (cur < 0 || cur >= Theme.COUNT) cur = 0;
        for (int i = 0; i < Theme.COUNT; i++) {
            final int idx = i;
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setTitle(Theme.NAMES[i]);
            cb.setOrder(i);              // чекбоксы идут до ползунка высоты (у него order 100)
            cb.setPersistent(false);     // выбор темы пишем сами, в один ключ "theme"
            cb.setChecked(i == cur);
            cb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference p, Object v) {
                    if (updating || !Boolean.TRUE.equals(v)) return false; // снять текущую тему нельзя
                    selectTheme(idx);
                    return false; // галочки выставляем сами, состояние системы не применяем
                }
            });
            themes[i] = cb;
            cat.addPreference(cb);
        }
    }

    private void selectTheme(int idx) {
        prefs.edit().putInt("theme", idx).apply();
        updating = true;
        for (int i = 0; i < themes.length; i++) themes[i].setChecked(i == idx);
        updating = false;
    }

    /** Языки в цикле глобуса; хотя бы один должен остаться включённым. */
    private void buildLangs() {
        PreferenceCategory cat = (PreferenceCategory) findPreference("cat_langs");
        int mask = prefs.getInt("mask2", Layouts.defaultMask());
        for (int i = 0; i < Layouts.COUNT; i++) {
            final int idx = i;
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setTitle(Layouts.TITLES[i]);
            cb.setOrder(i);              // языки идут до кнопки сброса (у неё порядок по умолчанию)
            cb.setPersistent(false);     // храним не отдельные флаги, а битовую маску "mask2"
            cb.setChecked((mask & (1 << i)) != 0);
            cb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference p, Object v) {
                    if (updating) return false;
                    boolean checked = Boolean.TRUE.equals(v);
                    int m = prefs.getInt("mask2", Layouts.defaultMask());
                    m = checked ? (m | (1 << idx)) : (m & ~(1 << idx));
                    if (m == 0) return false; // хотя бы один язык должен остаться
                    prefs.edit().putInt("mask2", m).apply();
                    return true;
                }
            });
            langs[i] = cb;
            cat.addPreference(cb);
        }
    }
}
