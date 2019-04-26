package np.com.softwarica.uride.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import np.com.softwarica.uride.MyApp;
import np.com.softwarica.uride.callbacks.UChildEventListener;
import np.com.softwarica.uride.managers.NotifyManager;
import np.com.softwarica.uride.utils.FirebaseUtils;
import np.com.softwarica.uride.utils.MapUtils;
import np.com.softwarica.uride.utils.SharedPref;
import np.com.softwarica.uride.utils.SharedUtils;
import np.com.softwarica.uride.utils.StoreUtils;
import np.com.softwarica.uride.utils.TripUtils;

@SuppressWarnings("ALL")
public class TripListenerService extends Service {
    public int counter = 0;
    private ChildEventListener tripListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        listenForTrip();
        return START_STICKY;
    }

    private void listenForTrip() {
        String myLocationString = SharedPref.getString(TripListenerService.this, "myCurrentLocation");

        LatLng myLocation = new LatLng(Double.parseDouble(myLocationString.split(":")[0]), Double.parseDouble(myLocationString.split(":")[1]));
        tripListener = FirebaseUtils.database.child("trips").addChildEventListener(new UChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!TripUtils.isValidTrip(dataSnapshot)) return;
                LatLng pickupLocation = MapUtils.getLatlng((List<Double>) dataSnapshot.child("pickupLocation").getValue());
                if (MapUtils.getDistanceBetween(myLocation, pickupLocation) > StoreUtils.distanceRadius)
                    return;
                if (!MyApp.isDriverActivityVisible()) {
                    NotifyManager.notifyDriverAboutTrip(TripListenerService.this);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                super.onChildRemoved(dataSnapshot);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FirebaseUtils.database.child("trips").removeEventListener(tripListener);
        if (!SharedUtils.isDriver(TripListenerService.this)) return;
        Intent broadcastIntent = new Intent("com.techinherit.uffride.RestartTripListener");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  " + (counter++));
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}