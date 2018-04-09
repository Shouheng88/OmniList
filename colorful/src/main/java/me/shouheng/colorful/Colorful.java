package me.shouheng.colorful;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.util.Log;

public class Colorful {

    private static ThemeDelegate delegate;

    private static AccentColor accentColor = Defaults.accentColor;

    private static boolean isTranslucent = Defaults.trans;

    private static boolean isDark = Defaults.darkTheme;

    private static boolean isColoredNavigation = Defaults.isColoredNavigation;

    private static String themeString;

    public static void init(Context context) {
        themeString = PreferenceManager.getDefaultSharedPreferences(context).getString(Util.PREFERENCE_KEY, null);

        if (themeString == null) {
            accentColor = Defaults.accentColor;
            isTranslucent = Defaults.trans;
            isDark = Defaults.darkTheme;
            isColoredNavigation = Defaults.isColoredNavigation;
            themeString = generateThemeString();
        } else {
            String [] colors = themeString.split(Util.SPLIT);
            isDark = Boolean.parseBoolean(colors[0]);
            isTranslucent = Boolean.parseBoolean(colors[1]);
            accentColor = Colorful.AccentColor.getByAccentName(colors[3]);
            isColoredNavigation = Boolean.parseBoolean(colors[4]);
        }

        delegate = new ThemeDelegate(context, accentColor, isTranslucent, isDark);
    }

    public static void applyTheme(@NonNull Activity activity) {
        applyTheme(activity, true);
    }

    private static void applyTheme(@NonNull Activity activity, boolean overrideBase) {
        if (overrideBase) activity.setTheme(getThemeDelegate().getStyleResBase());
        activity.getTheme().applyStyle(getThemeDelegate().getStyleResAccent(), true);
    }

    private static void writeValues(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(
                Util.PREFERENCE_KEY, generateThemeString()).apply();
    }

    private static String generateThemeString() {
        return isDark + Util.SPLIT
                + isTranslucent + Util.SPLIT
                + accentColor.getAccentName() + Util.SPLIT
                + isColoredNavigation;
    }

    static ThemeDelegate getThemeDelegate() {
        if (delegate == null) {
            Log.e(Util.LOG_TAG, "getThemeDelegate() called before init(Context)." +
                    " Call Colorful.init(Context) in your application class");
        }
        return delegate;
    }

    static String getThemeString() {
        return themeString;
    }

    public static Config config(Context context) {
        return new Config(context);
    }

    public static Defaults defaults() {
        return new Defaults();
    }

    public enum AccentColor {
        RED_100(R.color.md_red_A100, "accent_red_100", "#FF8A80"),
        RED_200(R.color.md_red_A200, "accent_red_100", "#FF5252"),
        RED_400(R.color.md_red_A400, "accent_red_100", "#FF1744"),
        RED_700(R.color.md_red_A700, "accent_red_100", "#D50000"),

        PINK_100(R.color.md_pink_A100, "accent_pink_100", "#FF80AB"),
        PINK_200(R.color.md_pink_A200, "accent_pink_200", "#FF4081"),
        PINK_400(R.color.md_pink_A400, "accent_pink_400", "#F50057"),
        PINK_700(R.color.md_pink_A700, "accent_pink_700", "#C51162"),

        PURPLE_100(R.color.md_purple_A100, "accent_purple_100", "#EA80FC"),
        PURPLE_200(R.color.md_purple_A200, "accent_purple_200", "#E040FB"),
        PURPLE_400(R.color.md_purple_A400, "accent_purple_400", "#D500F9"),
        PURPLE_700(R.color.md_purple_A700, "accent_purple_700", "#AA00FF"),

        DEEP_PURPLE_100(R.color.md_deep_purple_A100, "accent_deep_purple_100", "#B388FF"),
        DEEP_PURPLE_200(R.color.md_deep_purple_A200, "accent_deep_purple_200", "#7C4DFF"),
        DEEP_PURPLE_400(R.color.md_deep_purple_A400, "accent_deep_purple_400", "#651FFF"),
        DEEP_PURPLE_700(R.color.md_deep_purple_A700, "accent_deep_purple_700", "#6200EA"),

        INDIGO_100(R.color.md_indigo_A100, "accent_indigo_100", "#8C9EFF"),
        INDIGO_200(R.color.md_indigo_A200, "accent_indigo_200", "#536DFE"),
        INDIGO_400(R.color.md_indigo_A400, "accent_indigo_400", "#3D5AFE"),
        INDIGO_700(R.color.md_indigo_A700, "accent_indigo_700", "#304FFE"),

        BLUE_100(R.color.md_blue_A100, "accent_blue_100", "#82B1FF"),
        BLUE_200(R.color.md_blue_A200, "accent_blue_200", "#448AFF"),
        BLUE_400(R.color.md_blue_A400, "accent_blue_400", "#2979FF"),
        BLUE_700(R.color.md_blue_A700, "accent_blue_700", "#2962FF"),

        LIGHT_BLUE_100(R.color.md_light_blue_A100, "accent_light_blue_100", "#80D8FF"),
        LIGHT_BLUE_200(R.color.md_light_blue_A200, "accent_light_blue_200", "#40C4FF"),
        LIGHT_BLUE_400(R.color.md_light_blue_A400, "accent_light_blue_400", "#00B0FF"),
        LIGHT_BLUE_700(R.color.md_light_blue_A700, "accent_light_blue_700", "#0091EA"),

        CYAN_100(R.color.md_cyan_A100, "accent_cyan_100", "#84FFFF"),
        CYAN_200(R.color.md_cyan_A200, "accent_cyan_200", "#18FFFF"),
        CYAN_400(R.color.md_cyan_A400, "accent_cyan_400", "#00E5FF"),
        CYAN_700(R.color.md_cyan_A700, "accent_cyan_700", "#00B8D4"),

        TEAL_100(R.color.md_teal_A100, "accent_teal_100", "#A7FFEB"),
        TEAL_200(R.color.md_teal_A200, "accent_teal_200", "#64FFDA"),
        TEAL_400(R.color.md_teal_A400, "accent_teal_400", "#1DE9B6"),
        TEAL_700(R.color.md_teal_A700, "accent_teal_700", "#00BFA5"),

        GREEN_100(R.color.md_green_A100, "accent_green_100", "#B9F6CA"),
        GREEN_200(R.color.md_green_A200, "accent_green_200", "#69F0AE"),
        GREEN_400(R.color.md_green_A400, "accent_green_400", "#00E676"),
        GREEN_700(R.color.md_green_A700, "accent_green_700", "#00C853"),

        LIGHT_GREEN_100(R.color.md_light_green_A100, "accent_light_green_100", "#CCFF90"),
        LIGHT_GREEN_200(R.color.md_light_green_A200, "accent_light_green_200", "#B2FF59"),
        LIGHT_GREEN_400(R.color.md_light_green_A400, "accent_light_green_400", "#76FF03"),
        LIGHT_GREEN_700(R.color.md_light_green_A700, "accent_light_green_700", "#64DD17"),

        LIME_100(R.color.md_lime_A100, "accent_lime_100", "#F4FF81"),
        LIME_200(R.color.md_lime_A200, "accent_lime_200", "#EEFF41"),
        LIME_400(R.color.md_lime_A400, "accent_lime_400", "#C6FF00"),
        LIME_700(R.color.md_lime_A700, "accent_lime_700", "#AEEA00"),

        YELLOW_100(R.color.md_yellow_A100, "accent_yellow_100", "#FFFF8D"),
        YELLOW_200(R.color.md_yellow_A200, "accent_yellow_200", "#FFFF00"),
        YELLOW_400(R.color.md_yellow_A400, "accent_yellow_400", "#FFEA00"),
        YELLOW_700(R.color.md_yellow_A700, "accent_yellow_700", "#FFD600"),

        AMBER_100(R.color.md_amber_A100, "accent_amber_100", "#FFE57F"),
        AMBER_200(R.color.md_amber_A200, "accent_amber_200", "#FFD740"),
        AMBER_400(R.color.md_amber_A400, "accent_amber_400", "#FFC400"),
        AMBER_700(R.color.md_amber_A700, "accent_amber_700", "#FFAB00"),

        ORANGE_100(R.color.md_orange_A100, "accent_orange_100", "#FFD180"),
        ORANGE_200(R.color.md_orange_A200, "accent_orange_200", "#FFAB40"),
        ORANGE_400(R.color.md_orange_A400, "accent_orange_400", "#FF9100"),
        ORANGE_700(R.color.md_orange_A700, "accent_orange_700", "#FF6D00"),

        DEEP_ORANGE_100(R.color.md_deep_orange_A100, "accent_deep_orange_100", "#FF9E80"),
        DEEP_ORANGE_200(R.color.md_deep_orange_A200, "accent_deep_orange_200", "#FF6E40"),
        DEEP_ORANGE_400(R.color.md_deep_orange_A400, "accent_deep_orange_400", "#FF3D00"),
        DEEP_ORANGE_700(R.color.md_deep_orange_A700, "accent_deep_orange_700", "#DD2C00"),

        BROWN_200(R.color.md_brown_200, "accent_brown_200", "#BCAAA4"),
        BROWN_300(R.color.md_brown_300, "accent_brown_300", "#A1887F"),
        BROWN_400(R.color.md_brown_400, "accent_brown_400", "#8D6E63"),

        GREY_200(R.color.md_grey_200, "accent_grey_200", "#EEEEEE"),
        GREY_300(R.color.md_grey_300, "accent_grey_300", "#E0E0E0"),
        GREY_400(R.color.md_grey_400, "accent_grey_400", "#BDBDBD"),

        BLUE_GREY_200(R.color.md_blue_grey_200, "accent_blue_grey_200", "#B0BBC5"),
        BLUE_GREY_300(R.color.md_blue_grey_300, "accent_blue_grey_300", "#90A4AE"),
        BLUE_GREY_400(R.color.md_blue_grey_400, "accent_blue_grey_400", "#78909C");

        @ColorRes private int colorRes;

        private String accentName;

        private String colorName;

        AccentColor(@ColorRes int colorRes, String accentName, String colorName) {
            this.colorRes = colorRes;
            this.accentName = accentName;
            this.colorName = colorName;
        }

        public @ColorRes int getColorRes() {
            return colorRes;
        }

        public static AccentColor getByAccentName(String accentName){
            for (AccentColor accentColor : values()){
                if (accentColor.accentName.equals(accentName)){
                    return accentColor;
                }
            }
            Log.d(Util.LOG_TAG, "Unrecognized AccentColor: " + accentName);
            return Defaults.accentColor;
        }

        public static AccentColor getByColorName(String colorName) {
            for (AccentColor accentColor : values()){
                if (accentColor.colorName.equals(colorName)){
                    return accentColor;
                }
            }
            Log.d(Util.LOG_TAG, "Unrecognized AccentColor: " + colorName);
            return Defaults.accentColor;
        }

        public String getAccentName() {
            return accentName;
        }

        public String getColorName() {
            return colorName;
        }
    }

    public static class Defaults {

        private static AccentColor accentColor = AccentColor.GREEN_700;

        private static boolean trans = false;

        private static boolean darkTheme = false;

        private static boolean isColoredNavigation = false;

        public Defaults accentColor(AccentColor accent) {
            accentColor = accent;
            return this;
        }

        public Defaults translucent(boolean translucent) {
            trans = translucent;
            return this;
        }

        public Defaults dark(boolean dark) {
            darkTheme = dark;
            return this;
        }

        public Defaults coloredNavigation(boolean colored) {
            isColoredNavigation = colored;
            return this;
        }
    }

    public static class Config {

        private Context context;

        private Config(Context context) {
            this.context = context;
        }

        public Config accentColor(AccentColor accent) {
            accentColor = accent;
            return this;
        }

        public Config translucent(boolean translucent) {
            isTranslucent = translucent;
            return this;
        }

        public Config coloredNavigationBar(boolean colored) {
            isColoredNavigation = colored;
            return this;
        }

        public Config dark(boolean dark) {
            isDark = dark;
            return this;
        }

        public void apply() {
            writeValues(context);
            themeString = generateThemeString();
            delegate = new ThemeDelegate(context, accentColor, isTranslucent, isDark);
        }
    }
}
