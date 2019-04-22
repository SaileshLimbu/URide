package np.com.softwarica.uride;

import com.facebook.drawee.backends.pipeline.Fresco;

public class MyApp extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }

    public static boolean isDriverActivityVisible() {
        return driverActivityVisible;
    }

    public static void driverActivityResumed() {
        driverActivityVisible = true;
    }

    public static void driverActivityPaused() {
        driverActivityVisible = false;
    }

    private static boolean driverActivityVisible = false;
}