package np.com.softwarica.uride.utils;

import android.content.Context;

import java.util.ArrayList;

public class SharedUtils {
    public static boolean isTripRejected(Context context, String tripKey) {
        ArrayList<String> list = getAllRejectList(context);
        boolean result = false;
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(tripKey)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    public static ArrayList<String> getAllRejectList(Context context) {
        ArrayList<String> list = new ArrayList<>();
        String str = SharedPref.getString(context, "rideRejectList");
        if (str.contains(":")) {
            String[] str1 = str.split(":");
            for (String string : str1) {
                list.add(string);
            }
        }

        return list;
    }

    public static void clearRejectRide(Context context) {
        SharedPref.setString(context, "rideRejectList", "");
    }

    public static void saveData(Context context, String email, String fullName, String profilePic, String phoneNumber) {
        SharedPref.setString(context, "email", email);
        SharedPref.setString(context, "fullName", fullName);
        SharedPref.setString(context, "profilePic", profilePic);
        SharedPref.setString(context, "phoneNumber", phoneNumber);
    }
}
