package np.com.softwarica.uride.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class MapUtils {
    public static void zoomMap(GoogleMap map, LatLng location, int zoomLevel) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(zoomLevel)
                .tilt(60)
                .build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public static String getDistanceUrl(LatLng origin, LatLng dest, String mode) {
        String str_en_origin = "origins=" + origin.latitude + "," + origin.longitude;
        String str_en_dest = "destinations=" + dest.latitude + "," + dest.longitude;
        String travelMode = "mode=" + mode;
        String parameters = str_en_origin + "&" + str_en_dest + "&" + travelMode + "&key=AIzaSyBU3a0tdmddXiZF2umxf0GQesk9oOwiq6M";
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&" + parameters;
        return url;
    }

    public static float getDistanceBetween(LatLng pickupLocation, LatLng dropoffLocation) {
        Location pickup = new Location("");
        pickup.setLatitude(pickupLocation.latitude);
        pickup.setLongitude(pickupLocation.longitude);

        Location dropoff = new Location("");
        dropoff.setLatitude(dropoffLocation.latitude);
        dropoff.setLongitude(dropoffLocation.longitude);

        return pickup.distanceTo(dropoff);
    }

    public static String downloadUrl(String strUrl) {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Error", e.toString());
        } finally {
            try {
                iStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlConnection.disconnect();
        }
        return data;
    }

    public static boolean isGPSEnable(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static LatLng getLatlng(Object obj) {
        List<Double> list = (List<Double>) obj;
        return new LatLng(list.get(0), list.get(1));
    }

    public static LatLng getLatlng(List<Double> list) {
        return new LatLng(list.get(0), list.get(1));
    }

    public static LatLng getLatlng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static Marker drawAndReturnMarker(Context context, LatLng location, GoogleMap mMap, int markerWidth, int markerHeight, int icon) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.resizeMapIcons(context, icon, markerWidth, markerHeight)));
        return mMap.addMarker(markerOptions);
    }

    public static void animateMarker(final LatLng toPosition, final Marker marker, GoogleMap googleMap) {
        if (marker == null) return;
        final LatLng startPosition = marker.getPosition();
        final LatLng finalPosition = toPosition;
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;
        final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + finalPosition.latitude * t,
                        startPosition.longitude * (1 - t) + finalPosition.longitude * t);
                marker.setPosition(currentPosition);

                if (t < 1) {
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    public static void buildAlertMessageNoGps(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static List<Double> getCordinate(LatLng latLng) {
        List<Double> list = new ArrayList<>();
        list.add(latLng.latitude);
        list.add(latLng.longitude);
        return list;
    }
}