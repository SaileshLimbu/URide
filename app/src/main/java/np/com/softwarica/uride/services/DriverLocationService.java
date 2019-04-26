package np.com.softwarica.uride.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;

import com.google.android.gms.maps.model.LatLng;

import np.com.softwarica.uride.callbacks.MyLocationChangeListener;
import np.com.softwarica.uride.managers.PermissionManager;
import np.com.softwarica.uride.utils.DriverUtils;
import np.com.softwarica.uride.utils.SharedPref;


public class DriverLocationService extends Service {
    private static final int LOCATION_INTERVAL = 2000;
    private static final float LOCATION_DISTANCE = 0;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        if (!PermissionManager.checkLocationPermission(this)) {
            return;
        }
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location;

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, new MyLocationListener());
        location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, new MyLocationListener());
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location == null) return;
    }

    private class MyLocationListener extends MyLocationChangeListener {
        @Override
        public void onLocationChanged(Location location) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            DriverUtils.updateDriverLocation(latLng);
            SharedPref.setString(DriverLocationService.this, "myCurrentLocation", location.getLatitude() + ":" + location.getLongitude());
        }
    }
}