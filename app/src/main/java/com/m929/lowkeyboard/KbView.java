package com.m929.lowkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.PopupWindow;

import java.util.ArrayList;

/**
 * Клавиатура, нарисованная целиком на Canvas: без XML-раскладок, без дочерних View.
 * Один View, один проход отрисовки, ~40 маленьких объектов Key.
 */
public class KbView extends View {

    public interface Listener {
        void onText(String s);

        void onAction(int code);
    }

    // Коды служебных клавиш (0 = обычный символ)
    static final int SHIFT = -1, DEL = -2, SYM = -3, GLOBE = -4, ENTER = -5, SPACE = -6, PICKER = -7, PAGE = -8;

    private Theme theme;   // текущая цветовая схема (настраивается в приложении)
    private int curText;     // цвет текста и значков текущей клавиши

    private static final class Key {
        String text;
        int code;
        float x, y, w, h;
        String hint;   // маленькая подсказка в углу (цифра), по долгому нажатию вводится она
        boolean dark;  // тёмная служебная клавиша (запятая, точка)
    }

    private final ArrayList<Key> keys = new ArrayList<Key>(48);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint txt = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint();
    private final RectF rect = new RectF();
    private final Path globePath = new Path();
    private final Path path = new Path();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SharedPreferences prefs;

    private final float dp;
    private float keyH;                       // высота клавиши, настраивается в приложении
    private float gap, hgap, radius;
    private final int strokePx, edgePx;

    private Listener listener;
    private int lang;
    private int mask = Layouts.ALL_MASK;
    private int mode;          // 0 - буквы, 1 - символы
    private int shift;         // 0 - выкл, 1 - на один символ, 2 - caps lock
    private long lastShiftTap;
    static final int ENTER_RETURN = 0, ENTER_SEARCH = 1, ENTER_NEXT = 2, ENTER_DONE = 3, ENTER_SEND = 4;
    private int enterKind;     // что нарисовать на клавише Enter
    private boolean digitsSymOnly; // цифровой ряд только на символьной раскладке
    private boolean sound = true;  // звук нажатий: системный клик, настраивается в приложении
    private float rowH;
    private int symPage;       // страница символов (0 или 1) в компактном режиме
    private boolean rtl;       // письмо справа налево: ряды букв зеркалятся

    /** Пределы высоты клавиши в dp (те же значения использует экран настроек). */
    static final int MIN_KEY_H = 30, MAX_KEY_H = 60;

    // Компактные символы: цифровой ряд + 2 ряда на страницу, вместе с нижним рядом - те же 4 ряда, что и у букв.
    private static final String[] SYM_A = {"@#$%&-+()/", "~`|^=[]{}\\"};
    private static final String[] SYM_B = {"*\"':;!?", "<>_%"}; // на 2-й странице дописываются знаки языка (3 шт.)

    private Key pressed;
    private boolean longHandled;

    // Всплывающая панель вариантов (долгое нажатие)
    private boolean altShown;
    private String alts;
    private int altSel;
    private final RectF altRect = new RectF();
    private PopupWindow altPopup;
    private AltsView altView;

    private final Runnable longPress = new Runnable() {
        @Override
        public void run() {
            onLongPress();
        }
    };

    private final Runnable repeatDel = new Runnable() {
        @Override
        public void run() {
            if (pressed != null && pressed.code == DEL) {
                if (listener != null) listener.onAction(DEL);
                handler.postDelayed(this, 50);
            }
        }
    };

    public KbView(Context ctx) {
        super(ctx);
        prefs = ctx.getSharedPreferences("kb", Context.MODE_PRIVATE);
        dp = ctx.getResources().getDisplayMetrics().density;
        keyH = 42 * dp;
        edgePx = Math.max(1, Math.round(dp));
        line.setStyle(Paint.Style.STROKE);
        strokePx = Math.max(1, Math.round(2f * dp)); // целое число пикселей - линии не расплываются
        line.setStrokeWidth(strokePx);
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
        txt.setTextAlign(Paint.Align.CENTER);
        GlobeIcon.addTo(globePath, 0, 0, 1f);
        refreshPrefs();
    }

    private void applyTheme() {
        gap = theme.gapDp * dp;      // вертикальный промежуток между рядами
        hgap = theme.hgapDp * dp;    // горизонтальный промежуток между клавишами
        radius = theme.radiusDp * dp;
        txt.setTypeface(theme.bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        if (getHeight() > 0) {
            bgPaint.setShader(new LinearGradient(0, 0, 0, getHeight(), theme.bgTop, theme.bgBottom, Shader.TileMode.CLAMP));
        }
        requestLayout();
        rebuild();
    }

    public void setListener(Listener l) {
        listener = l;
    }

    // ------------------------------------------------------------------ состояние

    /** Перечитать язык и набор активных языков из SharedPreferences. */
    public void refreshPrefs() {
        mask = prefs.getInt("mask2", Layouts.defaultMask()) & Layouts.ALL_MASK;
        if (mask == 0) mask = Layouts.ALL_MASK;
        Theme th = Theme.get(prefs.getInt("theme", 0));
        if (th != theme) {
            theme = th;
            applyTheme();
        }
        boolean ds = prefs.getBoolean("digSym", false);
        if (ds != digitsSymOnly) {
            digitsSymOnly = ds;
            requestLayout();
        }
        sound = prefs.getBoolean("sound", true);
        int khPref = prefs.getInt("keyH", 42);
        khPref = Math.max(MIN_KEY_H, Math.min(MAX_KEY_H, khPref)); // испорченное значение не ломает клавиатуру
        float kh = khPref * dp;
        if (kh != keyH) {
            keyH = kh;
            requestLayout();
        }
        int l = prefs.getInt("lang2", Layouts.defaultLang());
        if (l < 0 || l >= Layouts.COUNT || (mask & (1 << l)) == 0) {
            l = nextEnabled(l);
            prefs.edit().putInt("lang2", l).apply(); // исправленный язык сразу запоминаем
        }
        if (l != lang) {
            lang = l;
        }
        rebuild();
    }

    private int nextEnabled(int from) {
        for (int i = 1; i <= Layouts.COUNT; i++) {
            int n = (from + i) % Layouts.COUNT;
            if ((mask & (1 << n)) != 0) return n;
        }
        return 0;
    }

    public void setMode(int m) {
        if (m != mode) {
            mode = m;
            symPage = 0;
            rebuild();
        }
    }

    public void setShift(int s) {
        if (s != shift) {
            shift = s;
            rebuild();
        }
    }

    public int getShift() {
        return shift;
    }

    public void setEnterKind(int kind) {
        enterKind = kind;
        invalidate();
    }

    private void switchLang() {
        lang = nextEnabled(lang);
        prefs.edit().putInt("lang2", lang).apply();
        rebuild();
    }

    // ------------------------------------------------------------------ размеры и раскладка

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec);
        int n = digitsSymOnly ? 4 : 5; // высота зависит только от настройки, но не от раскладки: окно не прыгает
        int h = (int) (n * keyH + (n + 1) * gap);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        bgPaint.setShader(new LinearGradient(0, 0, 0, h, theme.bgTop, theme.bgBottom, Shader.TileMode.CLAMP));
        rebuild();
    }

    private void rebuild() {
        keys.clear();
        boolean sym = mode == 1;
        boolean digits = !digitsSymOnly || sym;
        boolean compact = sym && digitsSymOnly;
        int r0 = digits ? 1 : 0;
        int total = digitsSymOnly ? 4 : 5;
        rtl = !sym && Layouts.RTL[lang]; // символьные ряды общие для всех языков, их не зеркалим
        float w = getWidth();
        if (w <= 0 || getHeight() <= 0) {
            invalidate();
            return;
        }
        rowH = keyH;                                    // размер клавиш не зависит от раскладки
        String[] rows = sym ? Layouts.SYMBOLS : Layouts.LETTERS[lang];

        if (digits) addRow(Layouts.DIGITS, 0, 0, 0, 0, 0, false); // цифры всегда слева направо
        int first = keys.size();
        if (compact) {
            addRow(SYM_A[symPage], 1, 0, 0, 0, 0);
            int pk = keys.size();
            addRow(SYM_B[symPage] + (symPage == 1 ? Layouts.EXTRA[lang] : ""), 2, 1.5f, PAGE, 1.5f, DEL);
            keys.get(pk).text = symPage == 0 ? "=\\<" : "?123"; // как в Gboard: на 1-й странице "=\<", на 2-й "?123"
            addBottomRow(total - 1, sym);
            invalidate();
            return;
        }
        addRow(rows[0], r0, 0, 0, 0, 0);
        if (!digits) { // цифрового ряда нет - цифры становятся подсказками на верхнем ряду
            for (int i = 0; i < 10 && first + i < keys.size(); i++) {
                keys.get(first + i).hint = Layouts.DIGITS.substring(i, i + 1);
            }
        }
        addRow(rows[1], r0 + 1, 0, 0, 0, 0);
        if (sym) {
            addRow(rows[2] + Layouts.EXTRA[lang], r0 + 2, 0, 0, 1.5f, DEL);
        } else if (!Layouts.CASED[lang]) {
            addRow(rows[2], r0 + 2, 0, 0, 1.5f, DEL); // нет регистра - клавиша Shift не нужна
        } else {
            addRow(rows[2], r0 + 2, 1.5f, SHIFT, 1.5f, DEL);
        }

        addBottomRow(r0 + 3, sym);
        invalidate();
    }

    /** Нижний ряд: ?123 | глобус | , | пробел | . | enter */
    private void addBottomRow(int row, boolean sym) {
        float w = getWidth();
        float[] ws;
        int[] codes;
        String[] tx;
        if (Integer.bitCount(mask) > 1) {
            ws = new float[]{1.5f, 1f, 1f, 4f, 1f, 1.5f};
            codes = new int[]{SYM, GLOBE, 0, SPACE, 0, ENTER};
            tx = new String[]{sym ? "ABC" : "?123", Layouts.CODES[lang], ",", " ", ".", ""};
        } else { // включён один язык - клавиша переключения языков не нужна, её место занимает пробел
            ws = new float[]{1.5f, 1f, 5f, 1f, 1.5f};
            codes = new int[]{SYM, 0, SPACE, 0, ENTER};
            tx = new String[]{sym ? "ABC" : "?123", ",", " ", ".", ""};
        }
        float unit = w / 10f;
        float x = 0;
        for (int i = 0; i < ws.length; i++) {
            addKey(tx[i], codes[i], x, row, ws[i] * unit);
            if (codes[i] == 0) keys.get(keys.size() - 1).dark = true; // запятая и точка
            x += ws[i] * unit;
        }
    }

    private void addRow(String chars, int row, float lw, int lcode, float rw, int rcode) {
        addRow(chars, row, lw, lcode, rw, rcode, rtl);
    }

    /** Ряд клавиш; mirror = true раскладывает символы справа налево (RTL-письменности). */
    private void addRow(String chars, int row, float lw, int lcode, float rw, int rcode, boolean mirror) {
        int n = chars.length();
        float unit = getWidth() / (lw + n + rw);
        float x = 0;
        if (lw > 0) {
            addKey("", lcode, x, row, lw * unit);
            x += lw * unit;
        }
        for (int i = 0; i < n; i++) {
            int idx = mirror ? n - 1 - i : i; // при RTL первый символ строки - у правого края
            String s = chars.substring(idx, idx + 1);
            if (shift > 0 && mode == 0) s = cased(s, true);
            addKey(s, 0, x, row, unit);
            x += unit;
        }
        if (rw > 0) addKey("", rcode, x, row, rw * unit);
    }

    /** Регистр без учёта локали устройства (иначе в турецкой локали i станет İ); ß остаётся как есть. */
    private String cased(String s, boolean up) {
        char[] a = s.toCharArray();
        for (int i = 0; i < a.length; i++) {
            if (a[i] == 'ß') continue;
            if (up && lang == Layouts.TR && a[i] == 'i') {
                a[i] = '\u0130';
                continue;
            }
            a[i] = up ? Character.toUpperCase(a[i]) : Character.toLowerCase(a[i]);
        }
        return new String(a);
    }

    private void addKey(String text, int code, float x, int row, float w) {
        Key k = new Key();
        k.text = text;
        k.code = code;
        k.x = x;
        k.y = gap + row * (rowH + gap);
        k.w = w;
        k.h = rowH;
        keys.add(k);
    }

    private Key keyAt(float x, float y) {
        // ищем ключ, в который попали; промежутки между клавишами относим к ближайшему
        Key best = null;
        float bestD = Float.MAX_VALUE;
        for (int i = 0, n = keys.size(); i < n; i++) {
            Key k = keys.get(i);
            float dx = x < k.x ? k.x - x : (x > k.x + k.w ? x - k.x - k.w : 0);
            float dy = y < k.y - gap / 2 ? k.y - gap / 2 - y : (y > k.y + k.h + gap / 2 ? y - k.y - k.h - gap / 2 : 0);
            float d = dx * dx + dy * dy;
            if (d < bestD) {
                bestD = d;
                best = k;
                if (d == 0) break;
            }
        }
        return best;
    }

    // ------------------------------------------------------------------ рисование

    @Override
    protected void onDraw(Canvas c) {
        c.drawRect(0, 0, getWidth(), getHeight(), bgPaint);
        for (int i = 0, n = keys.size(); i < n; i++) drawKey(c, keys.get(i));
    }

    private void drawKey(Canvas c, Key k) {
        float g = hgap / 2;
        rect.set(k.x + g, k.y, k.x + k.w - g, k.y + k.h);
        int body, edge;
        if (k == pressed) {
            body = theme.pressed;
            edge = theme.pressedEdge;
        } else if (k.code == ENTER && enterKind != ENTER_RETURN) {
            body = theme.action;
            edge = theme.actionEdge;
        } else if (k.code == SPACE) {
            body = theme.space;
            edge = theme.spaceEdge;
        } else if (k.code != 0 || k.dark) {
            body = theme.dark;
            edge = theme.darkEdge;
        } else {
            body = theme.key;
            edge = theme.keyEdge;
        }
        fill.setColor(edge);                       // кромка: блик сверху (Holo) или тень снизу (остальные),
        c.drawRoundRect(rect, radius, radius, fill);
        if (theme.edgeBottom) rect.bottom -= edgePx; else rect.top += edgePx; // поверх неё - тело клавиши
        fill.setColor(body);
        c.drawRoundRect(rect, radius, radius, fill);
        if (theme.edgeBottom) rect.bottom += edgePx; else rect.top -= edgePx;
        if (k.hint != null) {
            txt.setColor(theme.hint);
            txt.setTextSize(keyH * 0.22f);
            c.drawText(k.hint, rect.right - keyH * 0.2f, rect.top + keyH * 0.3f, txt);
        }

        float cx = rect.centerX(), cy = rect.centerY();
        float s = keyH * 0.2f;
        curText = (k.code == ENTER && enterKind != ENTER_RETURN) ? theme.onAction : theme.text;
        line.setColor(curText);
        switch (k.code) {
            case SHIFT:
                path.reset();
                path.moveTo(cx, cy - s);
                path.lineTo(cx + s, cy);
                path.lineTo(cx + s * 0.5f, cy);
                path.lineTo(cx + s * 0.5f, cy + s);
                path.lineTo(cx - s * 0.5f, cy + s);
                path.lineTo(cx - s * 0.5f, cy);
                path.lineTo(cx - s, cy);
                path.close();
                line.setStyle(shift > 0 ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
                c.drawPath(path, line);
                if (shift == 2) {
                    line.setStyle(Paint.Style.STROKE);
                    c.drawLine(cx - s * 0.5f, cy + s * 1.5f, cx + s * 0.5f, cy + s * 1.5f, line);
                }
                line.setStyle(Paint.Style.STROKE);
                break;
            case DEL:
                path.reset();
                path.moveTo(cx - s * 1.2f, cy);
                path.lineTo(cx - s * 0.4f, cy - s * 0.8f);
                path.lineTo(cx + s * 1.2f, cy - s * 0.8f);
                path.lineTo(cx + s * 1.2f, cy + s * 0.8f);
                path.lineTo(cx - s * 0.4f, cy + s * 0.8f);
                path.close();
                c.drawPath(path, line);
                c.drawLine(cx - s * 0.1f, cy - s * 0.35f, cx + s * 0.7f, cy + s * 0.35f, line);
                c.drawLine(cx - s * 0.1f, cy + s * 0.35f, cx + s * 0.7f, cy - s * 0.35f, line);
                break;
            case ENTER:
                switch (enterKind) {
                    case ENTER_SEARCH: // лупа
                        c.drawCircle(cx - s * 0.2f, cy - s * 0.2f, s * 0.75f, line);
                        c.drawLine(cx + s * 0.35f, cy + s * 0.35f, cx + s * 1.0f, cy + s * 1.0f, line);
                        break;
                    case ENTER_NEXT: // стрелка вправо
                        path.reset();
                        path.moveTo(cx - s, cy);
                        path.lineTo(cx + s, cy);
                        path.moveTo(cx + s * 0.2f, cy - s * 0.8f);
                        path.lineTo(cx + s, cy);
                        path.lineTo(cx + s * 0.2f, cy + s * 0.8f);
                        c.drawPath(path, line);
                        break;
                    case ENTER_DONE: // галочка
                        path.reset();
                        path.moveTo(cx - s, cy + s * 0.1f);
                        path.lineTo(cx - s * 0.3f, cy + s * 0.8f);
                        path.lineTo(cx + s, cy - s * 0.7f);
                        c.drawPath(path, line);
                        break;
                    case ENTER_SEND:
                        drawFittedLabel(c, getContext().getString(R.string.enter_send), cx, cy, 0.3f);
                        break;
                    default: // перевод строки
                        path.reset();
                        path.moveTo(cx + s, cy - s * 0.8f);
                        path.lineTo(cx + s, cy + s * 0.2f);
                        path.lineTo(cx - s, cy + s * 0.2f);
                        path.moveTo(cx - s * 0.3f, cy - s * 0.5f);
                        path.lineTo(cx - s, cy + s * 0.2f);
                        path.lineTo(cx - s * 0.3f, cy + s * 0.9f);
                        c.drawPath(path, line);
                }
                break;
            case GLOBE:
                drawGlobe(c, cx, cy, keyH * 0.5f);
                break;
            case SYM:
            case PAGE:
                drawLabel(c, k.text, cx, cy, 0.34f);
                break;
            case SPACE: // на пробеле - название текущего языка
                String t = Layouts.TITLES[lang];
                float size = keyH * 0.28f;
                txt.setTextSize(size);
                float avail = rect.width() - 8 * dp;
                float tw = txt.measureText(t);
                if (tw > avail) txt.setTextSize(size * avail / tw);
                txt.setColor(theme.subtle);
                c.drawText(t, cx, cy - (txt.ascent() + txt.descent()) / 2, txt);
                break;
            default:
                drawLabel(c, k.text, cx, cy, 0.5f);
        }
    }

    private void drawLabel(Canvas c, String s, float cx, float cy, float size) {
        txt.setColor(curText);
        txt.setTextSize(keyH * size);
        c.drawText(s, cx, cy - (txt.ascent() + txt.descent()) / 2, txt);
    }

    /** Подпись, которая ужимается по ширине клавиши: переводы бывают длиннее оригинала. */
    private void drawFittedLabel(Canvas c, String s, float cx, float cy, float size) {
        txt.setColor(curText);
        txt.setTextSize(keyH * size);
        float avail = rect.width() - 6 * dp;
        float tw = txt.measureText(s);
        if (tw > avail && tw > 0) txt.setTextSize(txt.getTextSize() * avail / tw);
        c.drawText(s, cx, cy - (txt.ascent() + txt.descent()) / 2, txt);
    }

    /** Глобус из SVG-контура (GlobeIcon), залитый цветом текста; d - диаметр в пикселях. */
    private void drawGlobe(Canvas c, float cx, float cy, float d) {
        float sc = d / GlobeIcon.SIZE;
        c.save();
        c.translate(Math.round(cx - d / 2), Math.round(cy - d / 2));
        c.scale(sc, sc);
        fill.setColor(curText);
        c.drawPath(globePath, fill);
        c.restore();
    }

    // ------------------------------------------------------------------ ввод

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                press(keyAt(x, y));
                break;
            case MotionEvent.ACTION_MOVE:
                if (altShown) {
                    selectAlt(x);
                } else {
                    Key k = keyAt(x, y);
                    if (k != pressed) press(k);
                }
                break;
            case MotionEvent.ACTION_UP:
                cancelTimers();
                Key k = pressed;
                pressed = null;
                if (altShown) {
                    String a = alts.substring(altSel, altSel + 1);
                    hideAlts();
                    if (listener != null) listener.onText(a);
                } else if (k != null && !longHandled) {
                    fire(k);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelTimers();
                pressed = null;
                hideAlts();
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    private void press(Key k) {
        cancelTimers();
        hideAlts();
        longHandled = false;
        pressed = k;
        if (k != null) {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            // Системный звук клавиши: берётся из настроек звуков устройства и уважает
            // системный переключатель «звуки касаний».
            if (sound) playSoundEffect(SoundEffectConstants.CLICK);
            if (k.code == DEL) {
                handler.postDelayed(longPress, 400);
            } else if (k.code == SPACE || (k.code == 0 && (k.hint != null || Layouts.alts(lang, cased(k.text, false)) != null))) {
                handler.postDelayed(longPress, 450);
            }
        }
        invalidate();
    }

    private void cancelTimers() {
        handler.removeCallbacks(longPress);
        handler.removeCallbacks(repeatDel);
    }

    private void onLongPress() {
        Key k = pressed;
        if (k == null) return;
        if (k.code == DEL) {
            longHandled = true;
            repeatDel.run();
        } else if (k.code == SPACE) { // долгое нажатие на пробел - выбор клавиатуры
            longHandled = true;
            pressed = null;
            if (listener != null) listener.onAction(PICKER);
            invalidate();
        } else if (k.code == 0) {
            String a = Layouts.alts(lang, cased(k.text, false));
            if (k.hint != null) a = a == null ? k.hint : k.hint + a; // цифра идёт первой
            if (a == null) return;
            if (shift > 0 && mode == 0) a = cased(a, true);
            longHandled = true;
            alts = a;
            altSel = 0;
            float cw = Math.max(k.w, keyH * 0.9f);
            float w = Math.min(getWidth(), cw * a.length()); // панель не должна быть шире экрана
            float left = k.x + k.w / 2 - w / 2;
            left = Math.max(0, Math.min(getWidth() - w, left));
            altRect.set(left, 0, left + w, 0); // для выбора по x нужны только левый край и ширина
            altShown = true;
            showAlts(k, left, w);
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            invalidate();
        }
    }

    private void selectAlt(float x) {
        int i = (int) ((x - altRect.left) / (altRect.width() / alts.length()));
        i = Math.max(0, Math.min(alts.length() - 1, i));
        if (i != altSel) {
            altSel = i;
            if (altView != null) altView.invalidate();
        }
    }

    /**
     * Панель вариантов - отдельное окно над клавишей. Окно может выходить выше клавиатуры,
     * поэтому для верхнего ряда варианты не перекрывают саму клавишу.
     */
    private void showAlts(Key k, float left, float w) {
        float ph = keyH * 1.3f;
        if (altPopup != null) altPopup.dismiss();
        altView = new AltsView(getContext());
        altPopup = new PopupWindow(altView, (int) w, (int) ph);
        altPopup.setClippingEnabled(false);
        altPopup.setTouchable(false);
        altPopup.setFocusable(false);
        int[] inWin = new int[2];
        int[] onScr = new int[2];
        getLocationInWindow(inWin);
        getLocationOnScreen(onScr);
        int dy = (int) (k.y - ph - gap / 2);        // верх панели относительно вью
        if (onScr[1] + dy < 0) dy = -onScr[1];      // не уходить за верхний край экрана
        altPopup.showAtLocation(this, Gravity.NO_GRAVITY, inWin[0] + (int) left, inWin[1] + dy);
    }

    private void hideAlts() {
        altShown = false;
        if (altPopup != null) {
            altPopup.dismiss();
            altPopup = null;
        }
        altView = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelTimers(); // иначе повтор удаления продолжится после скрытия клавиатуры
        pressed = null;
        hideAlts();
        super.onDetachedFromWindow();
    }

    /** Содержимое панели вариантов. */
    private final class AltsView extends View {
        private final RectF cell = new RectF();

        AltsView(Context c) {
            super(c);
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            cell.set(0, 0, w, h);
            fill.setColor(theme.popup);
            c.drawRoundRect(cell, radius, radius, fill);
            if (alts == null) return;
            float cw = w / alts.length();
            txt.setTextSize(Math.min(keyH * 0.5f, cw * 0.7f)); // в узких ячейках шрифт мельче
            for (int i = 0; i < alts.length(); i++) {
                float x0 = i * cw;
                if (i == altSel) {
                    cell.set(x0 + 2 * dp, 2 * dp, x0 + cw - 2 * dp, h - 2 * dp);
                    fill.setColor(theme.accent);
                    c.drawRoundRect(cell, radius, radius, fill);
                    txt.setColor(theme.onAccent);
                } else {
                    txt.setColor(theme.text);
                }
                c.drawText(alts.substring(i, i + 1), x0 + cw / 2,
                        h / 2 - (txt.ascent() + txt.descent()) / 2, txt);
            }
        }
    }

    private void fire(Key k) {
        switch (k.code) {
            case 0:
                String s = k.text;
                if (shift == 1) {
                    shift = 0;
                    rebuild();
                }
                if (listener != null) listener.onText(s);
                break;
            case SHIFT:
                long now = SystemClock.uptimeMillis();
                if (shift == 1 && now - lastShiftTap < 350) shift = 2;
                else if (shift == 0) shift = 1;
                else shift = 0;
                lastShiftTap = now;
                rebuild();
                break;
            case SYM:
                mode = 1 - mode;
                symPage = 0;
                rebuild();
                break;
            case PAGE:
                symPage = 1 - symPage;
                rebuild();
                break;
            case GLOBE:
                switchLang();
                break;
            default: // DEL, ENTER, SPACE
                if (listener != null) listener.onAction(k.code);
        }
    }
}
