package lbn.geospark.com.geosparknotify;

import android.content.Context;

class SharedPreference {

    private static String PREFS = "NOTIFY_DB";

    static void setToken(Context context, String token) {
        android.content.SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sp.edit();
        editor.putString("DEVICETOKEN", token);
        editor.apply();
        editor.commit();
    }

    static String getToken(Context context) {
        android.content.SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString("DEVICETOKEN", null);
    }
}
