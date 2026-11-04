package com.m929.lowkeyboard;

import java.util.Locale;

/**
 * GENERATED FILE. Layouts are stored as short strings (one string = one row of keys),
 * so APK size does not grow with the number of languages.
 */
final class Layouts {
    private Layouts() {}

    static final int COUNT = 24;
    static final int ALL_MASK = (1 << COUNT) - 1;
    static final int TR = 14; // Turkish: i -> \u0130 in upper case

    /** Short codes shown on the globe key. */
    static final String[] CODES = {"EN", "RU", "UK", "BE", "BG", "SR", "EL", "DE", "FR", "ES", "PT", "IT", "PL", "CS", "TR", "SV", "NO", "RO", "HU", "ID", "VI", "AR", "HE", "FA"};

    /** Native names shown in the settings list. */
    static final String[] TITLES = {
            "English",
            "\u0420\u0443\u0441\u0441\u043a\u0438\u0439",
            "\u0423\u043a\u0440\u0430\u0457\u043d\u0441\u044c\u043a\u0430",
            "\u0411\u0435\u043b\u0430\u0440\u0443\u0441\u043a\u0430\u044f",
            "\u0411\u044a\u043b\u0433\u0430\u0440\u0441\u043a\u0438",
            "\u0421\u0440\u043f\u0441\u043a\u0438",
            "\u0395\u03bb\u03bb\u03b7\u03bd\u03b9\u03ba\u03ac",
            "Deutsch",
            "Fran\u00e7ais",
            "Espa\u00f1ol",
            "Portugu\u00eas",
            "Italiano",
            "Polski",
            "\u010ce\u0161tina",
            "T\u00fcrk\u00e7e",
            "Svenska / Suomi",
            "Norsk / Dansk",
            "Rom\u00e2n\u0103",
            "Magyar",
            "Bahasa Indonesia",
            "Ti\u1ebfng Vi\u1ec7t",
            "\u0627\u0644\u0639\u0631\u0628\u064a\u0629",
            "\u05e2\u05d1\u05e8\u05d9\u05ea",
            "\u0641\u0627\u0631\u0633\u06cc"};

    /** Scripts without letter case (the shift key is hidden). */
    static final boolean[] CASED = {true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false};

    static final String DIGITS = "1234567890";

    /** Three letter rows per language. */
    static final String[][] LETTERS = {
            {"qwertyuiop", "asdfghjkl", "zxcvbnm"}, // EN
            {"\u0439\u0446\u0443\u043a\u0435\u043d\u0433\u0448\u0449\u0437\u0445\u044a", "\u0444\u044b\u0432\u0430\u043f\u0440\u043e\u043b\u0434\u0436\u044d", "\u044f\u0447\u0441\u043c\u0438\u0442\u044c\u0431\u044e"}, // RU
            {"\u0439\u0446\u0443\u043a\u0435\u043d\u0433\u0448\u0449\u0437\u0445\u0457", "\u0444\u0456\u0432\u0430\u043f\u0440\u043e\u043b\u0434\u0436\u0454", "\u0491\u044f\u0447\u0441\u043c\u0438\u0442\u044c\u0431\u044e"}, // UK
            {"\u0439\u0446\u0443\u043a\u0435\u043d\u0433\u0448\u045e\u0437\u0445'", "\u0444\u044b\u0432\u0430\u043f\u0440\u043e\u043b\u0434\u0436\u044d", "\u044f\u0447\u0441\u043c\u0456\u0442\u044c\u0431\u044e"}, // BE
            {"\u044f\u0432\u0435\u0440\u0442\u044a\u0443\u0438\u043e\u043f\u0447\u0448", "\u0430\u0441\u0434\u0444\u0433\u0445\u0439\u043a\u043b", "\u0437\u044c\u0446\u0436\u0431\u043d\u043c\u044e"}, // BG
            {"\u0459\u045a\u0435\u0440\u0442\u0437\u0443\u0438\u043e\u043f\u0448\u0452", "\u0430\u0441\u0434\u0444\u0433\u0445\u0458\u043a\u043b\u0447\u045b", "\u0455\u045f\u0446\u0432\u0431\u043d\u043c\u0436"}, // SR
            {"\u03c2\u03b5\u03c1\u03c4\u03c5\u03b8\u03b9\u03bf\u03c0", "\u03b1\u03c3\u03b4\u03c6\u03b3\u03b7\u03be\u03ba\u03bb", "\u03b6\u03c7\u03c8\u03c9\u03b2\u03bd\u03bc"}, // EL
            {"qwertzuiop\u00fc", "asdfghjkl\u00f6\u00e4", "yxcvbnm\u00df"}, // DE
            {"azertyuiop", "qsdfghjklm", "wxcvbn"}, // FR
            {"qwertyuiop", "asdfghjkl\u00f1", "zxcvbnm"}, // ES
            {"qwertyuiop", "asdfghjkl\u00e7", "zxcvbnm"}, // PT
            {"qwertyuiop", "asdfghjkl", "zxcvbnm"}, // IT
            {"qwertyuiop", "asdfghjkl", "zxcvbnm"}, // PL
            {"qwertzuiop", "asdfghjkl", "yxcvbnm"}, // CS
            {"qwertyu\u0131op\u011f\u00fc", "asdfghjkl\u015fi", "zxcvbnm\u00f6\u00e7"}, // TR
            {"qwertyuiop\u00e5", "asdfghjkl\u00f6\u00e4", "zxcvbnm"}, // SV
            {"qwertyuiop\u00e5", "asdfghjkl\u00e6\u00f8", "zxcvbnm"}, // NO
            {"qwertyuiop\u0103", "asdfghjkl\u0219\u021b", "zxcvbnm\u00e2\u00ee"}, // RO
            {"qwertzuiop", "asdfghjkl", "yxcvbnm"}, // HU
            {"qwertyuiop", "asdfghjkl", "zxcvbnm"}, // ID
            {"qwertyuiop", "asdfghjkl", "zxcvbnm"}, // VI
            {"\u0636\u0635\u062b\u0642\u0641\u063a\u0639\u0647\u062e\u062d\u062c\u062f", "\u0634\u0633\u064a\u0628\u0644\u0627\u062a\u0646\u0645\u0643\u0637", "\u0626\u0621\u0624\u0631\u0649\u0629\u0648\u0632\u0638"}, // AR
            {"\u05e7\u05e8\u05d0\u05d8\u05d5\u05df\u05dd\u05e4", "\u05e9\u05d3\u05d2\u05db\u05e2\u05d9\u05d7\u05dc\u05da\u05e3", "\u05d6\u05e1\u05d1\u05d4\u05e0\u05de\u05e6\u05ea\u05e5"}, // HE
            {"\u0636\u0635\u062b\u0642\u0641\u063a\u0639\u0647\u062e\u062d\u062c\u0686", "\u0634\u0633\u06cc\u0628\u0644\u0627\u062a\u0646\u0645\u06a9\u06af", "\u0638\u0637\u0632\u0631\u0630\u062f\u067e\u0648"}, // FA
    };

    /** Symbols layer shared by all languages; language specific signs are appended to the last row. */
    static final String[] SYMBOLS = {"!@#$%^&*()", "-_=+[]{}\\|", ";:'\"?/~"};

    static final String[] EXTRA = {
            "\u20ac\u00a3\u00a5",
            "\u2116\u00ab\u00bb",
            "\u2116\u00ab\u00bb",
            "\u2116\u00ab\u00bb",
            "\u2116\u00ab\u00bb",
            "\u2116\u00ab\u00bb",
            "\u20ac\u00a3\u00a7",
            "\u20ac\u00a3\u00a7",
            "\u20ac\u00a3\u00a7",
            "\u00bf\u00a1\u20ac",
            "\u20ac\u00a3\u00a7",
            "\u20ac\u00a3\u00a7",
            "\u20ac\u00a3\u00a7",
            "\u20ac\u00a3\u00a7",
            "\u20ba\u20ac\u00a3",
            "\u20ac\u00a3\u00a7",
            "\u20ac\u00a3\u00a7",
            "\u20ac\u00a3\u00a7",
            "\u20ac\u00a3\u00a7",
            "\u20ac\u00a3\u00a5",
            "\u20ab\u20ac\u00a3",
            "\u060c\u061f\u061b",
            "\u20aa\u20ac\u00a3",
            "\u060c\u061f\u061b"};

    /** Long-press variants: "base+variants|base+variants"; null = generic Latin accents. */
    static final String[] ALTS = {
            null,
            "\u0435\u0451",
            "",
            "\u0435\u0451",
            "\u0448\u0449",
            "",
            "\u03b1\u03ac|\u03b5\u03ad|\u03b7\u03ae|\u03b9\u03af\u03ca|\u03bf\u03cc|\u03c5\u03cd\u03cb|\u03c9\u03ce",
            "a\u00e4|o\u00f6|u\u00fc|s\u00df|e\u00e9",
            "a\u00e0\u00e2\u00e6|e\u00e9\u00e8\u00ea\u00eb|i\u00ee\u00ef|o\u00f4\u0153|u\u00f9\u00fb\u00fc|c\u00e7|y\u00ff",
            "a\u00e1|e\u00e9|i\u00ed|o\u00f3|u\u00fa\u00fc|n\u00f1",
            "a\u00e1\u00e0\u00e2\u00e3\u00e4|e\u00e9\u00ea|i\u00ed|o\u00f3\u00f4\u00f5|u\u00fa\u00fc|c\u00e7",
            "a\u00e0|e\u00e9\u00e8|i\u00ec|o\u00f3\u00f2|u\u00f9",
            "a\u0105|c\u0107|e\u0119|l\u0142|n\u0144|o\u00f3|s\u015b|z\u017a\u017c",
            "a\u00e1|c\u010d|d\u010f|e\u00e9\u011b|i\u00ed|n\u0148|o\u00f3|r\u0159|s\u0161|t\u0165|u\u00fa\u016f|y\u00fd|z\u017e",
            "",
            "a\u00e5\u00e4|o\u00f6|e\u00e9",
            "a\u00e5\u00e6\u00e4|o\u00f8\u00f6|e\u00e9\u00e8|u\u00fc",
            "a\u0103\u00e2|i\u00ee|s\u0219|t\u021b",
            "a\u00e1|e\u00e9|i\u00ed|o\u00f3\u00f6\u0151|u\u00fa\u00fc\u0171",
            "",
            "a\u0103\u00e2\u00e0\u00e1\u1ea3\u00e3\u1ea1|e\u00ea\u00e8\u00e9\u1ebb\u1ebd\u1eb9|o\u00f4\u01a1\u00f2\u00f3\u1ecf\u00f5\u1ecd|u\u01b0\u00f9\u00fa\u1ee7\u0169\u1ee5|i\u00ec\u00ed\u1ec9\u0129\u1ecb|y\u1ef3\u00fd\u1ef7\u1ef9\u1ef5|d\u0111",
            "\u0627\u0623\u0625\u0622|\u0648\u0624|\u064a\u0626",
            "",
            "\u0632\u0698|\u0627\u0622|\u06a9\u0643"};

    /** Default selection: English plus the device language (if we have a layout for it). */
    static int defaultMask() {
        String l = Locale.getDefault().getLanguage();
        if ("nb".equals(l) || "nn".equals(l) || "da".equals(l)) l = "no";
        else if ("fi".equals(l)) l = "sv";
        else if ("ms".equals(l) || "in".equals(l)) l = "id"; // "in" - старый код индонезийского на Android
        else if ("iw".equals(l)) l = "he";                   // старый код иврита
        else if ("sk".equals(l)) l = "cs";
        else if ("ca".equals(l) || "gl".equals(l)) l = "es";
        int m = 1; // English is index 0
        for (int i = 0; i < COUNT; i++) {
            if (CODES[i].toLowerCase(Locale.ENGLISH).equals(l)) m |= 1 << i;
        }
        return m;
    }

    /** Раскладка, с которой клавиатура стартует по умолчанию: язык системы, а если его нет - английский. */
    static int defaultLang() {
        int m = defaultMask() & ~1;
        for (int i = 1; i < COUNT; i++) {
            if ((m & (1 << i)) != 0) return i;
        }
        return 0;
    }

    /** Long-press variants for a key. */
    static String alts(int lang, String s) {
        if (s.length() != 1) return null;
        char c = s.charAt(0);
        switch (c) {
            case '.': return ",:;!?\u2026";
            case ',': return ";:";
            case '-': return "\u2013\u2014_";
            default: break;
        }
        String t = ALTS[lang];
        if (t == null) return generic(c);
        int n = t.length();
        int i = 0;
        while (i < n) {
            int j = t.indexOf('|', i);
            if (j < 0) j = n;
            if (t.charAt(i) == c) return j - i > 1 ? t.substring(i + 1, j) : null;
            i = j + 1;
        }
        return null;
    }

    private static String generic(char c) {
        switch (c) {
            case 'a': return "\u00e0\u00e1\u00e2\u00e4\u00e3\u00e5\u00e6";
            case 'e': return "\u00e8\u00e9\u00ea\u00eb";
            case 'i': return "\u00ec\u00ed\u00ee\u00ef";
            case 'o': return "\u00f2\u00f3\u00f4\u00f6\u00f5\u0153";
            case 'u': return "\u00f9\u00fa\u00fb\u00fc";
            case 'c': return "\u00e7";
            case 'n': return "\u00f1";
            case 's': return "\u00df";
            case 'y': return "\u00fd\u00ff";
            default: return null;
        }
    }
}
