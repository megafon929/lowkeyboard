#!/usr/bin/env python3
# Генератор Layouts.java: все не-ASCII символы пишутся как \uXXXX.
import sys

# code, название, ряды букв, альтернативы (None = общие латинские), доп. символы, есть ли регистр
L = [
 ("EN", "English", ["qwertyuiop", "asdfghjkl", "zxcvbnm"], None, "€£¥", True),
 ("RU", "Русский", ["йцукенгшщзхъ", "фывапролджэ", "ячсмитьбю"], "её", "№«»", True),
 ("UK", "Українська", ["йцукенгшщзхї", "фівапролджє", "ґячсмитьбю"], "", "№«»", True),
 ("BE", "Беларуская", ["йцукенгшўзх'", "фывапролджэ", "ячсмітьбю"], "её", "№«»", True),
 ("BG", "Български", ["явертъуиопчш", "асдфгхйкл", "зьцжбнмю"], "шщ", "№«»", True),
 ("SR", "Српски", ["љњертзуиопшђ", "асдфгхјклчћ", "ѕџцвбнмж"], "", "№«»", True),
 ("EL", "Ελληνικά", ["ςερτυθιοπ", "ασδφγηξκλ", "ζχψωβνμ"], "αά|εέ|ηή|ιίϊ|οό|υύϋ|ωώ", "€£§", True),
 ("DE", "Deutsch", ["qwertzuiopü", "asdfghjklöä", "yxcvbnmß"], "aä|oö|uü|sß|eé", "€£§", True),
 ("FR", "Français", ["azertyuiop", "qsdfghjklm", "wxcvbn"], "aàâæ|eéèêë|iîï|oôœ|uùûü|cç|yÿ", "€£§", True),
 ("ES", "Español", ["qwertyuiop", "asdfghjklñ", "zxcvbnm"], "aá|eé|ií|oó|uúü|nñ", "¿¡€", True),
 ("PT", "Português", ["qwertyuiop", "asdfghjklç", "zxcvbnm"], "aáàâãä|eéê|ií|oóôõ|uúü|cç", "€£§", True),
 ("IT", "Italiano", ["qwertyuiop", "asdfghjkl", "zxcvbnm"], "aà|eéè|iì|oóò|uù", "€£§", True),
 ("PL", "Polski", ["qwertyuiop", "asdfghjkl", "zxcvbnm"], "aą|cć|eę|lł|nń|oó|sś|zźż", "€£§", True),
 ("CS", "Čeština", ["qwertzuiop", "asdfghjkl", "yxcvbnm"], "aá|cč|dď|eéě|ií|nň|oó|rř|sš|tť|uúů|yý|zž", "€£§", True),
 ("TR", "Türkçe", ["qwertyuıopğü", "asdfghjklşi", "zxcvbnmöç"], "", "₺€£", True),
 ("SV", "Svenska / Suomi", ["qwertyuiopå", "asdfghjklöä", "zxcvbnm"], "aåä|oö|eé", "€£§", True),
 ("NO", "Norsk / Dansk", ["qwertyuiopå", "asdfghjklæø", "zxcvbnm"], "aåæä|oøö|eéè|uü", "€£§", True),
 ("RO", "Română", ["qwertyuiopă", "asdfghjklșț", "zxcvbnmâî"], "aăâ|iî|sș|tț", "€£§", True),
 ("HU", "Magyar", ["qwertzuiop", "asdfghjkl", "yxcvbnm"], "aá|eé|ií|oóöő|uúüű", "€£§", True),
 ("ID", "Bahasa Indonesia", ["qwertyuiop", "asdfghjkl", "zxcvbnm"], "", "€£¥", True),
 ("VI", "Tiếng Việt", ["qwertyuiop", "asdfghjkl", "zxcvbnm"],
  "aăâàáảãạ|eêèéẻẽẹ|oôơòóỏõọ|uưùúủũụ|iìíỉĩị|yỳýỷỹỵ|dđ", "₫€£", True),
 ("AR", "العربية", ["ضصثقفغعهخحجد", "شسيبلاتنمكط", "ئءؤرىةوزظ"], "اأإآ|وؤ|يئ", "،؟؛", False),
 ("HE", "עברית", ["קראטוןםפ", "שדגכעיחלךף", "זסבהנמצתץ"], "", "₪€£", False),
 ("FA", "فارسی", ["ضصثقفغعهخحجچ", "شسیبلاتنمکگ", "ظطزرذدپو"], "زژ|اآ|کك", "،؟؛", False),
]


def j(s):
    out = []
    for ch in s:
        o = ord(ch)
        if o < 128:
            out.append('\\\\' if ch == '\\' else ('\\"' if ch == '"' else ch))
        else:
            out.append('\\u%04x' % o)
    return '"' + ''.join(out) + '"'


def arr(items):
    return ",\n            ".join(items)


tr_idx = [x[0] for x in L].index("TR")
src = []
a = src.append
a("package com.m929.lowkeyboard;")
a("")
a("import java.util.Locale;")
a("")
a("/**")
a(" * GENERATED FILE. Layouts are stored as short strings (one string = one row of keys),")
a(" * so APK size does not grow with the number of languages.")
a(" */")
a("final class Layouts {")
a("    private Layouts() {}")
a("")
a("    static final int COUNT = %d;" % len(L))
a("    static final int ALL_MASK = (1 << COUNT) - 1;")
a("    static final int TR = %d; // Turkish: i -> \\u0130 in upper case" % tr_idx)
a("")
a("    /** Short codes shown on the globe key. */")
a("    static final String[] CODES = {" + ", ".join(j(x[0]) for x in L) + "};")
a("")
a("    /** Native names shown in the settings list. */")
a("    static final String[] TITLES = {\n            " + arr(j(x[1]) for x in L) + "};")
a("")
a("    /** Scripts without letter case (the shift key is hidden). */")
a("    static final boolean[] CASED = {" + ", ".join('true' if x[5] else 'false' for x in L) + "};")
a("")
a("    static final String DIGITS = \"1234567890\";")
a("")
a("    /** Three letter rows per language. */")
a("    static final String[][] LETTERS = {")
for x in L:
    a("            {" + ", ".join(j(r) for r in x[2]) + "}, // " + x[0])
a("    };")
a("")
a("    /** Symbols layer shared by all languages; language specific signs are appended to the last row. */")
a("    static final String[] SYMBOLS = {\"!@#$%^&*()\", \"-_=+[]{}\\\\|\", \";:'\\\"?/~\"};")
a("")
a("    static final String[] EXTRA = {\n            " + arr(j(x[4]) for x in L) + "};")
a("")
a("    /** Long-press variants: \"base+variants|base+variants\"; null = generic Latin accents. */")
a("    static final String[] ALTS = {\n            " + arr(("null" if x[3] is None else j(x[3])) for x in L) + "};")
a("")
a("""    /** Default selection: English plus the device language (if we have a layout for it). */
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
            case '.': return ",:;!?\\u2026";
            case ',': return ";:";
            case '-': return "\\u2013\\u2014_";
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
            case 'a': return "\\u00e0\\u00e1\\u00e2\\u00e4\\u00e3\\u00e5\\u00e6";
            case 'e': return "\\u00e8\\u00e9\\u00ea\\u00eb";
            case 'i': return "\\u00ec\\u00ed\\u00ee\\u00ef";
            case 'o': return "\\u00f2\\u00f3\\u00f4\\u00f6\\u00f5\\u0153";
            case 'u': return "\\u00f9\\u00fa\\u00fb\\u00fc";
            case 'c': return "\\u00e7";
            case 'n': return "\\u00f1";
            case 's': return "\\u00df";
            case 'y': return "\\u00fd\\u00ff";
            default: return null;
        }
    }
}""")
out = sys.argv[1]
with open(out, "w", encoding="utf-8") as f:
    f.write("\n".join(src) + "\n")
print("languages:", len(L))
for x in L:
    for r in x[2]:
        assert len(r) <= 12, (x[0], r)
