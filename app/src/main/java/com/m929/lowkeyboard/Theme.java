package com.m929.lowkeyboard;

/**
 * Цветовые схемы клавиатуры. Всё хранится в коде числами, без ресурсов и картинок,
 * поэтому темы почти не увеличивают размер APK.
 */
final class Theme {
    static final String[] NAMES = {"Holo", "Material Light", "Material Dark", "MIUI 5", "Flyme"};
    static final int COUNT = NAMES.length;

    private static final Theme[] ALL = {
            // Holo (Android 4.0): тёмный градиент, светлая кромка сверху клавиши
            new Theme(true, false, 8, 5, 3,
                    0xFF262626, 0xFF000000,  // фон: верх, низ
                    0xFF5E5E5E, 0xFF7B7B7B,  // обычная клавиша: тело, кромка
                    0xFF222222, 0xFF414141,  // служебная
                    0xFF474747, 0xFF626262,  // пробел
                    0xFF003D5C, 0xFF235873,  // клавиша действия (Enter)
                    0xFF2B8DB3, 0xFF5CC0E8,  // нажатая
                    0xFF222222,              // панель вариантов
                    0xFF33B5E5,              // акцент
                    0xFFFFFFFF, 0xFFBBBBBB, 0xFF9AA0A6, // текст, подсказки, подпись на пробеле
                    0xFFFFFFFF, 0xFF202124), // текст на клавише действия, на акценте
            // Material Light
            new Theme(false, true, 7, 4, 4,
                    0xFFECEFF1, 0xFFECEFF1,
                    0xFFFFFFFF, 0xFFB0BEC5,
                    0xFFCFD8DC, 0xFFB0BEC5,
                    0xFFFFFFFF, 0xFFB0BEC5,
                    0xFF009688, 0xFF00796B,
                    0xFFB0BEC5, 0xFF90A4AE,
                    0xFFFFFFFF,
                    0xFF009688,
                    0xFF212121, 0xFF78909C, 0xFF78909C,
                    0xFFFFFFFF, 0xFFFFFFFF),
            // Material Dark
            new Theme(false, true, 7, 4, 4,
                    0xFF263238, 0xFF263238,
                    0xFF37474F, 0xFF1C2428,
                    0xFF2B3940, 0xFF1C2428,
                    0xFF37474F, 0xFF1C2428,
                    0xFF009688, 0xFF00695C,
                    0xFF546E7A, 0xFF455A64,
                    0xFF1C2428,
                    0xFF009688,
                    0xFFFFFFFF, 0xFF90A4AE, 0xFF90A4AE,
                    0xFFFFFFFF, 0xFFFFFFFF),
            // MIUI 5: светлая плоская, оранжевая клавиша действия
            new Theme(false, true, 6, 4, 2,
                    0xFFE9EAEC, 0xFFE0E1E4,
                    0xFFFFFFFF, 0xFFC8C9CC,
                    0xFFD3D5D9, 0xFFBFC1C5,
                    0xFFFFFFFF, 0xFFC8C9CC,
                    0xFFFF8A1E, 0xFFD96F0B,
                    0xFFD9DBDE, 0xFFC0C2C6,
                    0xFFFFFFFF,
                    0xFFFF8A1E,
                    0xFF333333, 0xFF999999, 0xFF999999,
                    0xFFFFFFFF, 0xFFFFFFFF),
            // Flyme: светлая, скруглённые клавиши, синяя клавиша действия
            new Theme(false, true, 8, 5, 5,
                    0xFFF5F6F7, 0xFFF5F6F7,
                    0xFFFFFFFF, 0xFFDADCDF,
                    0xFFE5E7EA, 0xFFD0D2D6,
                    0xFFFFFFFF, 0xFFDADCDF,
                    0xFF198CFF, 0xFF0F72D8,
                    0xFFE1E3E6, 0xFFCED0D4,
                    0xFFFFFFFF,
                    0xFF198CFF,
                    0xFF2B2B2B, 0xFF9AA0A6, 0xFF9AA0A6,
                    0xFFFFFFFF, 0xFFFFFFFF),
    };

    final boolean bold;        // жирный шрифт подписей
    final boolean edgeBottom;  // кромка снизу (тень), иначе сверху (блик)
    final float gapDp, hgapDp, radiusDp;
    final int bgTop, bgBottom, key, keyEdge, dark, darkEdge, space, spaceEdge, action, actionEdge,
            pressed, pressedEdge, popup, accent, text, hint, subtle, onAction, onAccent;

    private Theme(boolean bold, boolean edgeBottom, float gapDp, float hgapDp, float radiusDp, int... c) {
        this.bold = bold;
        this.edgeBottom = edgeBottom;
        this.gapDp = gapDp;
        this.hgapDp = hgapDp;
        this.radiusDp = radiusDp;
        int i = 0;
        bgTop = c[i++];
        bgBottom = c[i++];
        key = c[i++];
        keyEdge = c[i++];
        dark = c[i++];
        darkEdge = c[i++];
        space = c[i++];
        spaceEdge = c[i++];
        action = c[i++];
        actionEdge = c[i++];
        pressed = c[i++];
        pressedEdge = c[i++];
        popup = c[i++];
        accent = c[i++];
        text = c[i++];
        hint = c[i++];
        subtle = c[i++];
        onAction = c[i++];
        onAccent = c[i++];
    }

    static Theme get(int index) {
        return ALL[index < 0 || index >= ALL.length ? 0 : index];
    }
}
