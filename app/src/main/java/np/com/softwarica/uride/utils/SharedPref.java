package np.com.softwarica.uride.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class SharedPref {
    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences("Uride", MODE_PRIVATE);
    }

    public static boolean isSharedPrefSet(Context context, String key) {
        init(context);
        return sharedPreferences.contains(key);
    }

    public static void setList(Context context, String key, HashSet<String> sets) {
        init(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, sets);
        editor.apply();
    }

    public static Set<String> getList(Context context, String key) {
        init(context);
        return sharedPreferences.getStringSet(key, null);
    }

    public static void setString(Context context, String key, String value) {
        init(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(key, value);
        ed.apply();
    }

    public static void setBoolean(Context context, String key, boolean value) {
        init(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(key, value);
        ed.apply();
    }

    public static void removeShared(Context context, String key) {
        init(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.remove(key);
        ed.apply();
    }

    public static void setInt(Context context, String key, int value) {
        init(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putInt(key, value);
        ed.apply();
    }

    public static String getString(Context context, String key) {
        init(context);
        return sharedPreferences.getString(key, "");
    }

    public static boolean getBoolean(Context context, String key) {
        init(context);
        return sharedPreferences.getBoolean(key, true);
    }

    public static int getInt(Context context, String key) {
        init(context);
        return sharedPreferences.getInt(key, 0);
    }

    public static void setFloat(Context context, String key, float value) {
        init(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putFloat(key, value);
        ed.apply();
    }

    public static float getFloat(Context context, String key) {
        init(context);
        return sharedPreferences.getFloat(key, 0f);
    }

    public static void clearSharedPref(Context context) {
        init(context);
        removeShared(context, "isDriver");
    }

}
