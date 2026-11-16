-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''
-dontnote **
# Сервис и activity из манифеста сохраняются автоматически правилами AAPT.
# Preference из res/xml/settings.xml создаются по имени через рефлексию, поэтому
# их нельзя переименовывать и переносить в другой пакет (-repackageclasses).
-keep public class * extends android.preference.Preference {
    public <init>(android.content.Context, android.util.AttributeSet);
}
