package np.com.softwarica.uride.services;


import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.List;

import np.com.softwarica.uride.activities.passengers.PickupArrivingActivity;
import np.com.softwarica.uride.callbacks.UValueEventListener;
import np.com.softwarica.uride.utils.FirebaseUtils;
import np.com.softwarica.uride.utils.SharedPref;

public class UserRideRequestService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseUtils.database.child("trips/" + FirebaseUtils.userId + "/isTripAccepted").addValueEventListener(new UValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;
                if ((boolean) dataSnapshot.getValue()) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(1000);

                    if (!isForeground() && !SharedPref.isSharedPrefSet(UserRideRequestService.this, "isArrivingActivityOnDisplay")) {
                        openPickupArrivingActivity();
                    }
                    stopSelf();
                }
            }
        });
    }

    public boolean isForeground() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals("np.com.gsewa.uffride");
    }

    private void openPickupArrivingActivity() {
        FirebaseUtils.database.child("trips/" + FirebaseUtils.userId).addListenerForSingleValueEvent(new UValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue();
                HashMap<String, Object> requestData = new HashMap<>();
                requestData.put("etaEarning", data.get("etaEarning"));
                requestData.put("etaDistance", data.get("etaDistance"));
                requestData.put("pickupAddress", data.get("pickupAddress"));
                requestData.put("dropoffAddress", data.get("dropoffAddress"));
                requestData.put("pickupLocation", data.get("pickupLocation"));
                requestData.put("dropoffLocation", data.get("dropoffLocation"));
                requestData.put("paymentOption", data.get("paymentOption"));
                requestData.put("isTripAccepted", data.get("isTripAccepted"));
                requestData.put("isTripStarted", data.get("isTripStarted"));
                if (dataSnapshot.hasChild("driverId")) {
                    requestData.put("driverId", data.get("driverId"));
                }

                Intent intent = new Intent(UserRideRequestService.this, PickupArrivingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("requestData", requestData);
                startActivity(intent);

                startService(new Intent(getApplication(), UserRideRequestService.class));
            }
        });
    }
}