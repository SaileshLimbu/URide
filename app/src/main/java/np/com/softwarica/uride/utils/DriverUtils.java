package np.com.softwarica.uride.utils;

import android.app.ActivityManager;
import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

public class DriverUtils {
    public static boolean isValidDriverNode(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.hasChild("coordinate")) return false;
        if (!dataSnapshot.hasChild("documentInfo")) return false;
        if (!dataSnapshot.hasChild("licenseInfo")) return false;
        if (!dataSnapshot.hasChild("vehicleInfo")) return false;
        if (!dataSnapshot.hasChild("tripData")) return false;
        if (!dataSnapshot.hasChild("phoneNumber")) return false;
        if (!dataSnapshot.hasChild("profilePic")) return false;
        if (!dataSnapshot.hasChild("isPickupModeEnabled")) return false;
        if (!dataSnapshot.hasChild("isOnTrip")) return false;
        if (!dataSnapshot.hasChild("fullName")) return false;
        if (!dataSnapshot.hasChild("email")) return false;
        if (!dataSnapshot.hasChild("documentUploadLevel")) return false;
        if (!dataSnapshot.hasChild("verified")) return false;
        if (!dataSnapshot.hasChild("isCardAdded")) return false;
        if (!dataSnapshot.hasChild("online")) return false;
        return true;
    }

    public static void changePickupMode(boolean pickupMode) {
        FirebaseUtils.database.child("drivers/" + FirebaseUtils.userId + "/isPickupModeEnabled").setValue(pickupMode);
    }

    public static boolean isDriverServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public static void updateDriverLocation(LatLng latLng) {
        FirebaseUtils.database.child("drivers/" + FirebaseUtils.userId + "/coordinate").setValue(MapUtils.getCordinate(latLng));
    }
}
