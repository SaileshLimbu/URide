package np.com.softwarica.uride.utils;

import com.google.firebase.database.DataSnapshot;

public class TripUtils {
    public static boolean isValidTrip(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.hasChild("etaDistance")) return false;
        if (!dataSnapshot.hasChild("etaEarning")) return false;
        if (!dataSnapshot.hasChild("etaTime")) return false;
        if (!dataSnapshot.hasChild("fullName")) return false;
        if (!dataSnapshot.hasChild("isTripAccepted")) return false;
        if (!dataSnapshot.hasChild("isTripStarted")) return false;
        if (!dataSnapshot.hasChild("isTripOver")) return false;
        if (!dataSnapshot.hasChild("notifyUser")) return false;
        if (!dataSnapshot.hasChild("time")) return false;
        if (!dataSnapshot.hasChild("passengerKey")) return false;
        if (!dataSnapshot.hasChild("paymentOption")) return false;
        if (!dataSnapshot.hasChild("pickupAddress")) return false;
        if (!dataSnapshot.hasChild("dropoffAddress")) return false;
        if (!dataSnapshot.hasChild("pickupLocation")) return false;
        if (!dataSnapshot.hasChild("dropoffLocation")) return false;
        if (!dataSnapshot.hasChild("profilePic")) return false;
        if (!dataSnapshot.hasChild("vehicleType")) return false;
        return true;
    }
}
