package np.com.softwarica.uride.activities.drivers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import np.com.softwarica.uride.MyApp;
import np.com.softwarica.uride.R;
import np.com.softwarica.uride.Store;
import np.com.softwarica.uride.activities.LoginActivity;
import np.com.softwarica.uride.activities.passengers.UserDashboardActivity;
import np.com.softwarica.uride.callbacks.MyLocationChangeListener;
import np.com.softwarica.uride.callbacks.UChildEventListener;
import np.com.softwarica.uride.callbacks.UValueEventListener;
import np.com.softwarica.uride.databinding.ActivityDriverDashboardBinding;
import np.com.softwarica.uride.dialogs.AccountReviewDialog;
import np.com.softwarica.uride.dialogs.AccountVerifiedDialog;
import np.com.softwarica.uride.managers.PermissionManager;
import np.com.softwarica.uride.models.CardDetails;
import np.com.softwarica.uride.models.RideRequestData;
import np.com.softwarica.uride.utils.CryptoUtils;
import np.com.softwarica.uride.utils.DriverUtils;
import np.com.softwarica.uride.utils.FirebaseUtils;
import np.com.softwarica.uride.utils.MapUtils;
import np.com.softwarica.uride.utils.RequestCodeUtils;
import np.com.softwarica.uride.utils.SharedPref;
import np.com.softwarica.uride.utils.SharedUtils;
import np.com.softwarica.uride.utils.StoreUtils;
import np.com.softwarica.uride.utils.ToastUtils;
import np.com.softwarica.uride.utils.TripUtils;

@SuppressWarnings("ALL")
public class DriverDashboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityDriverDashboardBinding binding;
    private GoogleMap mMap;
    private String myVehicleType;
    private boolean isCardAdded = false;
    private boolean pickupModeEnabled = false;
    private boolean isOnTrip = false;
    private boolean onPause = false;
    private boolean listeningForRequest = false;
    private boolean isAccountReviewedDialogShown = false;
    private boolean isShowingFirstPaymentScreen = false;
    private LatLng myCurrentLocation;
    private LatLng myLastLocation;
    private Marker myMarker;
    private Context context;
    private Location myLocation;
    private CryptoUtils crypto;
    private ValueEventListener driverValueEventListener;
    private ChildEventListener tripEventListener;
    private ArrayList<Marker> listTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver_dashboard);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
        initNav();

        crypto = new CryptoUtils();
    }

    private void init() {
        context = this;
        listTrip = new ArrayList<>();

        driverValueEventListener = FirebaseUtils.database.child("drivers/" + FirebaseUtils.userId).addValueEventListener(new DriverValueEventListener());

        if (!PermissionManager.checkLocationPermission(this)) {
            PermissionManager.requestLocationPermission(this, RequestCodeUtils.GPS_REQUEST_CODE);
            return;
        }
        if (!MapUtils.isGPSEnable(this)) {
            enableGps();
        }

        //Clear reject ride request list
        SharedUtils.clearRejectRide(this);

    }

    private void enableGps() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                }).build();
        mGoogleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result
                        .getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(DriverDashboardActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    public void initNav() {
        binding.navMenuDriver.navAddPayment.setOnClickListener(view -> startActivity(new Intent(context, AddPaymentActivity.class)));
        binding.navMenuDriver.navMyRating.setOnClickListener(view -> startActivity(new Intent(context, MyRating.class)));
        binding.navMenuDriver.pickupMode.setOnCheckedChangeListener((compoundButton, b) -> DriverUtils.changePickupMode(b));
        binding.pickupMode.setOnCheckedChangeListener((compoundButton, b) -> {
            DriverUtils.changePickupMode(b);
            if (b) {
                FirebaseUtils.database.child("trips").addChildEventListener(new TripEventListener(myCurrentLocation != null ? myCurrentLocation : myLastLocation));
            } else {
                FirebaseUtils.database.child("trips").removeEventListener(tripEventListener);
                for (Marker marker : listTrip) {
                    marker.remove();
                }
                listTrip.clear();
            }
        });
        binding.navMenuDriver.navNotification.setOnClickListener(v -> startActivity(new Intent(context, NotificationAcitivity.class)));
        binding.navMenuDriver.navTripHistory.setOnClickListener(v -> startActivity(new Intent(context, RideHistory.class)));
        binding.navMenuDriver.navMyEarning.setOnClickListener(v -> startActivity(new Intent(context, MyEarningActivity.class)));
    }

    public void openProfile(View view) {
        startActivity(new Intent(this, MyAccountActivity.class));
    }

    public void logout(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Are you sure ?");
        dialog.setMessage("You want to logout from your account?");
        dialog.setNegativeButton("No", (d, i) -> d.dismiss());
        dialog.setPositiveButton("Yes", (d, i) -> {
            FirebaseAuth.getInstance().signOut();
            SharedPref.clearSharedPref(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        dialog.show();
    }

    @SuppressLint("MissingPermission")
    private void showMyLocation() {
        if (mMap == null) return;
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location;

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 25f, new MyLocationListener());
        location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 25f, new MyLocationListener());
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location == null) return;
        SharedPref.setString(this, "myCurrentLocation", location.getLatitude() + ":" + location.getLongitude());
        startDriverService();

        myLocation = location;
        myCurrentLocation = MapUtils.getLatlng(location);
        MapUtils.zoomMap(mMap, myCurrentLocation, 13);

        mMap.setOnMarkerClickListener(marker -> {
            RideRequestData data = (RideRequestData) marker.getTag();
            Intent intent = new Intent(DriverDashboardActivity.this, RideRequestActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("rideRequestData", data);
            intent.putExtras(bundle);
            startActivity(intent);
            return true;
        });

        myMarker = MapUtils.drawAndReturnMarker(DriverDashboardActivity.this, myCurrentLocation, mMap, 50, 74, R.drawable.marker_driver);
    }

    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == RequestCodeUtils.GPS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!MapUtils.isGPSEnable(this)) {
                    enableGps();
                    return;
                }
                showMyLocation();
                startDriverService();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                ToastUtils.showErrorToast(this, "Uffride cannot run without permission granted!!!");
                finish();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!PermissionManager.checkLocationPermission(this)) {
            return;
        }
        showMyLocation();
    }

    public void getMyLocation(View view) {
        mMap.clear();
        showMyLocation();
    }

    public void openFirstPaymentActivity() {
        startActivity(new Intent(context, AddFirstPaymentActivity.class));
    }

    private class DriverValueEventListener extends UValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (!DriverUtils.isValidDriverNode(dataSnapshot)) return;
            if (onPause) return;
            HashMap<String, Object> response = (HashMap<String, Object>) dataSnapshot.getValue();
            HashMap<String, Integer> tripData = (HashMap<String, Integer>) dataSnapshot.child("tripData").getValue();

            isCardAdded = (boolean) response.get("isCardAdded");
            pickupModeEnabled = (boolean) response.get("isPickupModeEnabled");
            isOnTrip = (boolean) response.get("isOnTrip");
            myVehicleType = (String) dataSnapshot.child("vehicleInfo/vehicleType").getValue();

            binding.pickupMode.setChecked(pickupModeEnabled);

            String email = (String) response.get("email");
            String fullName = (String) response.get("fullName");
            String profilePic = (String) response.get("profilePic");
            String phoneNumber = (String) response.get("phoneNumber");
            String totalTrips = tripData.get("totalTrip") + " Trips";
            String earning = String.valueOf(tripData.get("totalEarning"));
            String distance = String.valueOf(tripData.get("totalDistance"));
            String totalEarned = "$ " + String.format("%.2f", Float.parseFloat(earning));
            String totalDistance = String.format("%.2f", Float.parseFloat(distance)) + " Miles";
            boolean verified = (boolean) response.get("verified");

            SharedUtils.saveData(DriverDashboardActivity.this, email, fullName, profilePic, phoneNumber);

            if (verified && !isCardAdded & !dataSnapshot.hasChild("stripeDetails")) {
                if (!isShowingFirstPaymentScreen) {
                    AccountVerifiedDialog.showDialog(DriverDashboardActivity.this);
                    isShowingFirstPaymentScreen = true;
                }
            }

            if (!profilePic.isEmpty()) {
                binding.imgProfile.setImageURI(Uri.parse(profilePic));
                binding.navMenuDriver.imgProfile.setImageURI(Uri.parse(profilePic));
            } else {
                binding.imgProfile.setImageURI(Uri.parse((String) dataSnapshot.child("vehicleInfo/imageUrl").getValue()));
                binding.navMenuDriver.imgProfile.setImageURI(Uri.parse((String) dataSnapshot.child("vehicleInfo/imageUrl").getValue()));
            }

            if (!verified && !isAccountReviewedDialogShown) {
                AccountReviewDialog.showDialog(DriverDashboardActivity.this);
                isAccountReviewedDialogShown = true;
            }

            binding.navMenuDriver.setVerified(verified);
            binding.setVerified(verified);
            binding.navMenuDriver.txtFullname.setText(fullName);
            binding.navMenuDriver.txtPhoneNumber.setText(phoneNumber);
            binding.txtTotalTrips.setText(totalTrips);
            binding.txtTotalEarned.setText(totalEarned);
            binding.txtDistance.setText(totalDistance);
            binding.navMenuDriver.pickupMode.setChecked(pickupModeEnabled);

            if (dataSnapshot.hasChild("stripeDetails")) {
                Store.getInstance().clear();
                Iterator<DataSnapshot> iterator = dataSnapshot.child("stripeDetails").getChildren().iterator();
                while (iterator.hasNext()) {
                    DataSnapshot snapshot = iterator.next();
                    String data = snapshot.getValue().toString();
                    String decryptData = crypto.decryptMsg(Base64.decode(data, Base64.DEFAULT));
                    CardDetails cd = new CardDetails();
                    cd.setCardNumber(decryptData.split(",")[0]);
                    cd.setExpMonth(Integer.parseInt(decryptData.split(",")[1]));
                    cd.setExpYear(Integer.parseInt(decryptData.split(",")[2]));
                    cd.setCvc(decryptData.split(",")[3]);
                    cd.setKey(decryptData.split(",")[4]);
                    cd.setDataKey(snapshot.getKey());
                    Store.getInstance().addCardToArray(cd);
                }
            }

            if (dataSnapshot.hasChild("due")) {
                ToastUtils.showErrorToast(context, "You have an Uffride Comission to pay from last trip.");
                Iterator<DataSnapshot> iterator = dataSnapshot.child("due").getChildren().iterator();
                DataSnapshot snapshot = iterator.next();
                HashMap<String, Object> data = (HashMap<String, Object>) snapshot.getValue();
                String dueFullName = data.get("fullName").toString();
                String dueProfilePic = data.get("profilePic").toString();
                String dueEtaDistance = data.get("etaDistance").toString();
                String dueEtaEarning = data.get("etaEarning").toString();
                String dueEtaTime = data.get("etaTime").toString();

                float commission = Store.getInstance().getCommission();
                float amount = Float.parseFloat(dueEtaEarning) / 100 * commission;

                RideRequestData rideRequestData = new RideRequestData(dueFullName, dueEtaTime, dueEtaEarning, dueEtaDistance, "", "", null, null, "", "", dueProfilePic);
                Intent intent = new Intent(context, PayToUrideActivity.class);
                intent.putExtra("amount", amount);
                intent.putExtra("rideRequestData", rideRequestData);
                startActivity(intent);
                return;
            }

            if (dataSnapshot.hasChild("currentTrip")) {
                HashMap<String, Object> currentTrip = (HashMap<String, Object>) dataSnapshot.child("currentTrip").getValue();
                RideRequestData data = new RideRequestData(
                        (String) currentTrip.get("fullName"),
                        (String) currentTrip.get("etaTime"),
                        (String) currentTrip.get("etaEarning"),
                        (String) currentTrip.get("etaDistance"),
                        (String) currentTrip.get("pickupAddress"),
                        (String) currentTrip.get("dropoffAddress"),
                        (List<Double>) currentTrip.get("pickupLocation"),
                        (List<Double>) currentTrip.get("dropoffLocation"),
                        (String) currentTrip.get("passengerKey"),
                        (String) currentTrip.get("paymentOption"),
                        (String) currentTrip.get("profilePic"));

                SharedPref.setBoolean(context, "isTripStarted", (boolean) currentTrip.get("isTripStarted"));

                Intent intent = new Intent(context, DriverOnTripActivity.class);
                intent.putExtra("rideRequestData", data);
                startActivity(intent);

            }

            if (!listeningForRequest && verified && isCardAdded) {
                myLastLocation = MapUtils.getLatlng((List<Double>) response.get("coordinate"));
                tripEventListener = FirebaseUtils.database.child("trips").addChildEventListener(new TripEventListener(myLastLocation));
                listeningForRequest = true;
            }
        }
    }

    private class TripEventListener extends UChildEventListener {
        LatLng previousLocation;

        TripEventListener(LatLng previousLocation) {
            this.previousLocation = previousLocation;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (!TripUtils.isValidTrip(dataSnapshot)) return;
            HashMap<String, Object> response = (HashMap<String, Object>) dataSnapshot.getValue();

            LatLng pickupLocation = MapUtils.getLatlng((List<Double>) response.get("pickupLocation"));
            if (myCurrentLocation == null) {
                if (MapUtils.getDistanceBetween(previousLocation, pickupLocation) > StoreUtils.distanceRadius)
                    return;
            } else {
                if (MapUtils.getDistanceBetween(myCurrentLocation, pickupLocation) > StoreUtils.distanceRadius)
                    return;
            }


            if ((boolean) response.get("isTripAccepted")) return;
            if (!pickupModeEnabled) return;
            if (isOnTrip) return;
            if (response.get("vehicleType") == null) return;
            if (!myVehicleType.equals(response.get("vehicleType"))) return;
            if (SharedUtils.isTripRejected(DriverDashboardActivity.this, dataSnapshot.getKey()))
                return;

            float earning = Float.parseFloat((String) response.get("etaEarning"));
            float totalEarning = earning / 100 * (100 - Store.getInstance().getCommission());
            String etaEarning = new DecimalFormat("#.##").format(totalEarning);

            RideRequestData data = new RideRequestData(
                    response.get("fullName").toString(),
                    response.get("etaTime").toString(),
                    etaEarning,
                    response.get("etaDistance").toString(),
                    response.get("pickupAddress").toString(),
                    response.get("dropoffAddress").toString(),
                    (List<Double>) response.get("pickupLocation"),
                    (List<Double>) response.get("dropoffLocation"),
                    dataSnapshot.getKey(),
                    response.get("paymentOption").toString(),
                    response.get("profilePic").toString());

            View v = LayoutInflater.from(context).inflate(R.layout.trip_info_window, null);
            ((TextView) v.findViewById(R.id.txtDistance)).setText(response.get("etaDistance").toString());
            ((TextView) v.findViewById(R.id.txtEarning)).setText("$ " + data.getEtaEarning());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(MapUtils.getLatlng(data.getPickupLocation()))
                    .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(context, v))));

            marker.setTag(data);
            listTrip.add(marker);
            NotifyManager.notifyDriver(context);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if (!TripUtils.isValidTrip(dataSnapshot)) return;
            String key = dataSnapshot.getKey();
            for (Marker marker : listTrip) {
                RideRequestData data = (RideRequestData) marker.getTag();
                if (data.getTripKey().equals(key)) {
                    marker.remove();
                    listTrip.remove(marker);
                    break;
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if (!TripUtils.isValidTrip(dataSnapshot)) return;
            HashMap<String, Object> response = (HashMap<String, Object>) dataSnapshot.getValue();
            boolean isTripAccepted = (boolean) response.get("isTripAccepted");
            if (!isTripAccepted) return;
            String key = dataSnapshot.getKey();
            for (Marker marker : listTrip) {
                RideRequestData data = (RideRequestData) marker.getTag();
                if (data.getTripKey().equals(key)) {
                    marker.remove();
                    listTrip.remove(marker);
                    break;
                }
            }
        }
    }

    private class MyLocationListener extends MyLocationChangeListener {
        @Override
        public void onLocationChanged(Location location) {
            myCurrentLocation = MapUtils.getLatlng(location);
            if (myMarker == null) {
                myMarker = MapUtils.drawAndReturnMarker(DriverDashboardActivity.this, myCurrentLocation, mMap, 50, 74, R.drawable.marker_driver);
            } else {
                MapUtils.animateMarker(myCurrentLocation, myMarker, mMap);
            }
        }
    }

    public void openDrawerMenu(View view) {
        binding.drawerLayout.openDrawer(Gravity.START);
    }

    public void startDriverService() {
        if (!DriverUtils.isDriverServiceRunning(this, DriverLocationService.class)) {
            startService(new Intent(this, DriverLocationService.class));
        }
        if (!DriverUtils.isDriverServiceRunning(this, TripListenerService.class)) {
            startService(new Intent(this, TripListenerService.class));
        }
    }

    public void switchToUser(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Are you sure you want to become Uffride user?");
        dialog.setNegativeButton("No", (d, i) -> d.dismiss());
        dialog.setPositiveButton("Yes", (d, i) -> becomeAUser());
        dialog.show();
    }

    private void becomeAUser() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Switching to User...");
        dialog.setCancelable(false);
        dialog.show();
        FirebaseUtils.database.child("users/" + FirebaseUtils.userId).addListenerForSingleValueEvent(new UValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dialog.dismiss();
                if (dataSnapshot.getValue() == null) {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("fullName", SharedPref.getString(context, "fullName"));
                    data.put("phoneNumber", SharedPref.getString(context, "phoneNumber"));
                    data.put("email", SharedPref.getString(context, "email"));
                    data.put("profilePic", SharedPref.getString(context, "profilePic"));
                    FirebaseUtils.database.child("users/" + FirebaseUtils.userId).setValue(data).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SharedPref.setString(context, "isDriver", "false");
                            stopService(new Intent(context, TripListenerService.class));
                            stopService(new Intent(context, DriverLocationService.class));
                            finish();
                            startActivity(new Intent(context, UserDashboardActivity.class));
                        }
                    });
                } else {
                    SharedPref.setString(context, "isDriver", "false");
                    stopService(new Intent(context, TripListenerService.class));
                    stopService(new Intent(context, DriverLocationService.class));
                    FirebaseUtils.database.child("drivers/" + FirebaseUtils.userId + "/online").setValue(false);
                    finish();
                    startActivity(new Intent(context, UserDashboardActivity.class));
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPause = true;
        MyApp.driverActivityPaused();
        if (myMarker != null) myMarker.remove();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showMyLocation();
        startDriverService();
        onPause = false;
        MyApp.driverActivityResumed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApp.driverActivityPaused();
        if (tripEventListener != null)
            FirebaseUtils.database.child("trips").removeEventListener(tripEventListener);
        if (driverValueEventListener != null)
            FirebaseUtils.database.child("drivers/" + FirebaseUtils.userId).removeEventListener(driverValueEventListener);
    }
}