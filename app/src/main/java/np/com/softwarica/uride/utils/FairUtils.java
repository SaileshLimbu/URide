package np.com.softwarica.uride.utils;

import np.com.softwarica.uride.Store;

public class FairUtils {
    public static float getFair(float distance, String vehicleType) {
        float totalFair = 0;
        switch (vehicleType.toUpperCase()) {
            case "REGULAR":
                totalFair = (float) (distance * Store.getInstance().getFairDetails().getRegular());
                break;
            case "MOTOR BIKE":
                totalFair = (float) (distance * Store.getInstance().getFairDetails().getBike());
                break;
        }

        return totalFair;
    }

    public static float beT(String full) {
        return Float.parseFloat(full.split(" ")[0]);
    }
}
