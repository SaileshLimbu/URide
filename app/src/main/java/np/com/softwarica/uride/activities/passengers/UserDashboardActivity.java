package np.com.softwarica.uride.activities.passengers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.Store;
import np.com.softwarica.uride.activities.LoginActivity;
import np.com.softwarica.uride.activities.ProfileActivity;
import np.com.softwarica.uride.activities.drivers.RegisterDriverActivity;
import np.com.softwarica.uride.callbacks.MyLocationChangeListener;
import np.com.softwarica.uride.callbacks.UChildEventListener;
import np.com.softwarica.uride.callbacks.UValueEventListener;
import np.com.softwarica.uride.databinding.ActivityMainBinding;
import np.com.softwarica.uride.managers.PermissionManager;
import np.com.softwarica.uride.models.CarItem;
import np.com.softwarica.uride.models.FairModel;
import np.com.softwarica.uride.utils.AnimUtils;
import np.com.softwarica.uride.utils.DriverUtils;
import np.com.softwarica.uride.utils.FairUtils;
import np.com.softwarica.uride.utils.FirebaseUtils;
import np.com.softwarica.uride.utils.MapUtils;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.RequestCodeUtils;
import np.com.softwarica.uride.utils.SharedPref;
import np.com.softwarica.uride.utils.ToastUtils;

public class UserDashboardActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private ActivityMainBinding b;
    private GoogleMap map;
    private Geocoder geocoder;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference database;
    private final Context context = this;


    private int mSelectedItem;
    private int carSelectedIndex = 0;
    private String etaDistance;
    private String etaTime;
    private String etaEarning;
    private boolean dragging = false;
    private boolean pickupModeEnable = true;
    private boolean dropoffModeEnable = false;
    private List<CarItem> carListItem;
    private List<Marker> listDrivers;

    private LatLng pickupLocation;
    private LatLng dropoffLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(this, Locale.getDefault());
        initCarView();
        initDropoffView();
        initDetailsView();
        initFirebase();
        initNavItemClickEvent();
        setCarList();
        //getUserData();
        checkIfUserHasRide();
        getConfig();

        if (!PermissionManager.checkLocationPermission(this)) {
            PermissionManager.requestLocationPermission(this, RequestCodeUtils.GPS_REQUEST_CODE);
            return;
        }

        if (!MapUtils.isGPSEnable(this)) {
            enableGps();
        }

        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }
        b.fabMenu.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (!PermissionManager.checkLocationPermission(this)) {
            return;
        }

        if (!MapUtils.isGPSEnable(this)) return;

        showDrivers();

        map.setOnMapClickListener(latLng -> {
            findViewById(R.id.rl_list_cars).setVisibility(View.INVISIBLE);
            findViewById(R.id.icon_collapse_list_cars).setVisibility(View.GONE);
            //findViewById(R.id.view_car_select).setVisibility(View.VISIBLE);
        });
        map.setOnCameraMoveStartedListener(i -> {
            dragging = true;
            b.layoutCar.txtPickupAddressCar.setText("Updating Location");
            b.customMarkerText.setText("Updating Location");

            b.layoutCar.rlListCars.setVisibility(View.INVISIBLE);
            b.layoutCar.iconCollapseListCars.setVisibility(View.GONE);
        });

        map.setOnCameraIdleListener(((Runnable) () -> new Handler().post(() -> {
            if (!NetworkUtils.isConnected(this)) {
                ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
                return;
            }
            if (!dragging) return;
            if (!pickupModeEnable && !dropoffModeEnable) return;
            try {
                List<Address> list = geocoder.getFromLocation(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude, 1);
                if (list.size() != 0) {
                    String name = list.get(0).getAddressLine(0);
                    if (pickupModeEnable) {
                        pickupLocation = map.getCameraPosition().target;
                        onPickupAddressChanged(name);
                    } else if (dropoffModeEnable) {
                        dropoffLocation = map.getCameraPosition().target;
                        onDropoffAddressChanged(name);
                    }
                    b.customMarkerText.setText(name);
                }
            } catch (IOException e) {
                e.printStackTrace();
                b.customMarkerText.setText("Service not available.");
            }
        }))::run);

        showMyLocation();
        b.layoutCar.getRoot().setAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
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
                .addOnConnectionFailedListener(connectionResult -> {
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
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            final LocationSettingsStates state = result1
                    .getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(UserDashboardActivity.this, 1000);
                    } catch (IntentSender.SendIntentException e) {
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;
            }
        });
    }

    private void checkIfUserHasRide() {
        database.child("trips").keepSynced(false);
        database.child("trips").addListenerForSingleValueEvent(new UValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getKey().equals(FirebaseUtils.userId)) {
                        FirebaseUtils.database.child("trips/" + FirebaseUtils.userId).addListenerForSingleValueEvent(new UValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<String, Object> data = (HashMap<String, Object>) dataSnapshot.getValue();
                                if (!dataSnapshot.hasChild("pickupAddress")) return;
                                HashMap<String, Object> requestData = new HashMap<>();
                                requestData.put("pickupAddress", data.get("pickupAddress"));
                                requestData.put("dropoffAddress", data.get("dropoffAddress"));
                                requestData.put("pickupLocation", data.get("pickupLocation"));
                                requestData.put("dropoffLocation", data.get("dropoffLocation"));
                                requestData.put("etaEarning", data.get("etaEarning"));
                                requestData.put("paymentOption", data.get("paymentOption"));
                                requestData.put("isTripAccepted", data.get("isTripAccepted"));
                                requestData.put("isTripStarted", data.get("isTripStarted"));
                                requestData.put("isTripOver", data.get("isTripOver"));
                                requestData.put("time", data.get("time"));
                                if (dataSnapshot.hasChild("driverId")) {
                                    requestData.put("driverId", data.get("driverId"));
                                }
                                requestData.put("showDialog", false);

                                //Intent intent = new Intent(context, PickupArrivingActivity.class);
                                //intent.putExtra("requestData", requestData);
                                //startActivity(intent);

                                //startService(new Intent(context, UserRideRequestService.class));
                            }
                        });
                        break;
                    }
                }
            }
        });
    }

    public void initDropoffView() {
        b.layoutDropoff.cardDropoff.setOnClickListener(this);
        b.layoutDropoff.btnDropoff.setOnClickListener(this);
        b.layoutDropoff.btnBackToCar.setOnClickListener(this);
    }

    public void initDetailsView() {
        b.layoutDetails.textPickupAddressDetails.setOnClickListener(this);
        b.layoutDetails.textDropoffAddressDetails.setOnClickListener(this);
        b.layoutDetails.btnBackDetails.setOnClickListener(this);
        b.layoutDetails.iconNextDetails.setOnClickListener(view -> {
            if (carSelectedIndex == 1) {
                carSelectedIndex = 0;
            } else {
                carSelectedIndex++;
            }
            b.layoutDetails.iconCarDetails.setImageResource(carListItem.get(carSelectedIndex).getCarIcon());
            b.layoutDetails.textPassengerSeatDetails.setText(carListItem.get(carSelectedIndex).getCarSeats().split(" ")[0]);
            float amount = FairUtils.getFair(FairUtils.beT(etaDistance), carListItem.get(carSelectedIndex).getCarName());
            DecimalFormat df = new DecimalFormat("#.##");
            b.layoutDetails.textAmountDetails.setText(df.format(amount));
        });
    }

    public void initCarView() {
        b.layoutCar.btnPickUpCar.setOnClickListener(this);
        b.layoutCar.layoutPickupAddressCar.setOnClickListener(this);
        b.layoutCar.iconCollapseListCars.setOnClickListener(this);
        b.layoutCar.btnMyLocationFromCar.setOnClickListener(this);
        b.layoutCar.layoutCarSelect.setOnClickListener(this);

        b.setShowMarker(true);
    }

    public void initFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
    }

    private void setCarList() {
        carListItem = new ArrayList<>();
        carListItem.add(new CarItem(R.drawable.vehicle_3, "Regular", "3 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_6, "Motor Bike", "2 seats"));

        ArrayAdapter<CarItem> carListAdapter = new ArrayAdapter<CarItem>(this, R.layout.item_cars, carListItem) {
            @NonNull
            @Override
            public View getView(final int i, @Nullable View view, @NonNull ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.item_cars, null);
                ImageView icon = view.findViewById(R.id.icon_car);
                TextView carName = view.findViewById(R.id.car_name);
                TextView carSeats = view.findViewById(R.id.car_seats);
                icon.setImageResource(carListItem.get(i).getCarIcon());
                carName.setText(carListItem.get(i).getCarName());
                carSeats.setText(carListItem.get(i).getCarSeats());

                view.findViewById(R.id.item_car).setOnClickListener(v -> {
                    ((ImageView) findViewById(R.id.icon_car_select)).setImageResource(carListItem.get(i).getCarIcon());
                    findViewById(R.id.rl_list_cars).setVisibility(View.INVISIBLE);
                    findViewById(R.id.icon_collapse_list_cars).setVisibility(View.GONE);
                    v.findViewById(R.id.item_car).setBackgroundColor(Color.parseColor("#DDDDDD"));
                    b.layoutDetails.iconCarDetails.setImageResource(carListItem.get(i).getCarIcon());
                    b.layoutDetails.textPassengerSeatDetails.setText(carListItem.get(i).getCarSeats().split(" ")[0]);
                    mSelectedItem = i;
                    carSelectedIndex = i;
                    notifyDataSetChanged();
                });

                if (mSelectedItem == i) {
                    view.findViewById(R.id.item_car).setBackgroundColor(Color.parseColor("#DDDDDD"));
                }

                return view;
            }
        };

        b.layoutCar.listView.setAdapter(carListAdapter);
        setListViewHeightBasedOnChildren(b.layoutCar.listView);
    }

    public void getConfig() {
        FirebaseUtils.database.child("admin/config").addListenerForSingleValueEvent(new UValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> snapshot = (HashMap<String, String>) dataSnapshot.child("fairs").getValue();
                double regular = Double.parseDouble(snapshot.get("REGULAR"));
                double bike = Double.parseDouble(snapshot.get("MOTOR BIKE"));
                FairModel model = new FairModel(regular, bike);
                Store.getInstance().setFairDetails(model);
            }
        });
    }

//    public void getUserData() {
//        database.child("users").child(user.getUid()).addValueEventListener(new UffValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.getValue();
//                String email = data.get("email");
//                String fullName = data.get("fullName");
//                String phoneNumber = data.get("phoneNumber");
//                String proPic = data.get("profilePic");
//
//                SharedUtils.saveData(context, email, fullName, proPic, phoneNumber);
//
//                b.navItem.txtFullname.setText(fullName);
//                if (!proPic.isEmpty()) {
//                    b.navItem.profilePic.setImageURI(Uri.parse(proPic));
//                }
//                b.layoutDetails.textPassengerNameDetails.setText(fullName);
//
//                if (dataSnapshot.hasChild("stripeDetails")) {
//                    Store.getInstance().clear();
//                    Iterator<DataSnapshot> iterator = dataSnapshot.child("stripeDetails").getChildren().iterator();
//                    while (iterator.hasNext()) {
//                        DataSnapshot snapshot = iterator.next();
//                        String encryptData = snapshot.getValue().toString();
//                        String decryptData = crypto.decryptMsg(Base64.decode(encryptData, Base64.DEFAULT));
//                        CardDetails cd = new CardDetails();
//                        cd.setCardNumber(decryptData.split(",")[0]);
//                        cd.setExpMonth(Integer.parseInt(decryptData.split(",")[1]));
//                        cd.setExpYear(Integer.parseInt(decryptData.split(",")[2]));
//                        cd.setCvc(decryptData.split(",")[3]);
//                        cd.setKey(decryptData.split(",")[4]);
//                        cd.setDataKey(snapshot.getKey());
//                        Store.getInstance().addCardToArray(cd);
//                    }
//                }
//            }
//        });
//    }

    private void initNavItemClickEvent() {
//        b.navItem.navAddPayment.setOnClickListener(v -> startActivity(new Intent(this, AddPaymentActivity.class)));
//        b.navItem.navRideHistory.setOnClickListener(v -> startActivity(new Intent(this, RideHistoryActivity.class)));
//        b.navItem.navSetting.setOnClickListener(v -> startActivity(new Intent(this, SettingActivity.class)));
//        b.navItem.navHelp.setOnClickListener(v -> startActivity(new Intent(context, HelpActivity.class)));
//        b.navItem.navNofitication.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
    }

    private void onPickupAddressChanged(String text) {
        b.layoutCar.txtPickupAddressCar.setText(text);
        b.layoutDetails.textPickupAddressDetails.setText(text);
        b.customMarkerText.setText(text);
    }

    private void onDropoffAddressChanged(String text) {
        b.layoutDetails.textDropoffAddressDetails.setText(text);
        b.layoutDropoff.textDropoffAddress.setText(text);
        b.customMarkerText.setText(text);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = (totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1))) + (listAdapter.getCount() * 3);
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @SuppressLint("MissingPermission")
    private void showMyLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new MyLocationChangeListener() {
        });
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new MyLocationChangeListener() {
            });
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location == null) return;
        MapUtils.zoomMap(map, new LatLng(location.getLatitude(), location.getLongitude()), 13);
    }

    private void calculateDistanceForDetails() {
        String url = MapUtils.getDistanceUrl(pickupLocation, dropoffLocation, (carSelectedIndex == 1) ? "bicycling" : "driving");
        showDrivers();
        System.out.println(url);
        new GetDistance().execute(url);
    }

    public void showDrivers() {
        listDrivers = new ArrayList<>();
        database.child("drivers").addChildEventListener(new UChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot data, String s) {
                if (!DriverUtils.isValidDriverNode(data)) return;
                HashMap<String, Object> driverData = (HashMap<String, Object>) data.getValue();
                boolean isOnTrip = (boolean) driverData.get("isOnTrip");
                boolean isOnline = (boolean) driverData.get("online");
                boolean pickupMode = (boolean) driverData.get("isPickupModeEnabled");
                if (!isOnline || isOnTrip) return;
                if (pickupMode) {
                    LatLng latLng = MapUtils.getLatlng(data.child("coordinate").getValue());
                    Marker marker = MapUtils.drawAndReturnMarker(context, latLng, map, 50, 74, R.drawable.marker_driver);
                    marker.setTag(data.getKey());
                    listDrivers.add(marker);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot data, String s) {
                if (!DriverUtils.isValidDriverNode(data)) return;
                boolean isAlreadyExist = false;
                Marker markerDriver = null;
                for (Marker markerIt : listDrivers) {
                    if (markerIt.getTag().equals(data.getKey())) {
                        isAlreadyExist = true;
                        markerDriver = markerIt;
                    }
                }
                HashMap<String, Object> driverData = (HashMap<String, Object>) data.getValue();
                LatLng latLng = MapUtils.getLatlng(data.child("coordinate").getValue());
                boolean isOnline = (boolean) driverData.get("online");
                boolean pickupMode = (boolean) driverData.get("isPickupModeEnabled");
                if (!isOnline || !pickupMode) {
                    if (isAlreadyExist) {
                        markerDriver.remove();
                        listDrivers.remove(markerDriver);
                    }
                    return;
                }

                if (!isAlreadyExist) {
                    Marker marker = MapUtils.drawAndReturnMarker(context, latLng, map, 50, 74, R.drawable.marker_driver);
                    marker.setTag(data.getKey());
                    listDrivers.add(marker);
                } else {
                    MapUtils.animateMarker(latLng, markerDriver, map);
                }
            }
        });
    }

    private void setDropOff(boolean value) {
        if (value) {
            b.ll.setBackgroundResource(R.drawable.background_marker_dropoff);
        } else {
            b.ll.setBackgroundResource(R.drawable.background_marker);
        }
    }

    public void switchToDriver(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Are you sure you want to become Uffride driver?");
        dialog.setNegativeButton("No", (d, i) -> d.dismiss());
        dialog.setPositiveButton("Yes", (d, i) -> becomeADriver());
        dialog.show();
    }

    private void becomeADriver() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Switching to Driver...");
        dialog.setCancelable(false);
        dialog.show();
        FirebaseUtils.database.child("drivers/" + FirebaseUtils.userId).addListenerForSingleValueEvent(new UValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dialog.dismiss();
                if (dataSnapshot.getValue() == null) {
                    SharedPref.removeShared(context, "isDriver");
                    finish();
                    startActivity(new Intent(context, RegisterDriverActivity.class));
                    finish();
                } else {
                    SharedPref.setString(context, "isDriver", "true");
                    finish();
                    startActivity(new Intent(context, LoginActivity.class));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (true) {//b.isDrawerOpen(GravityCompat.START)) {
            //b.closeDrawer(GravityCompat.START);
        } else if (!pickupModeEnable && !dropoffModeEnable) {
            b.layoutDetails.getRoot().startAnimation(AnimUtils.slideOutRight(this));
            b.layoutDetails.getRoot().setVisibility(View.INVISIBLE);
            b.layoutDropoff.getRoot().setVisibility(View.VISIBLE);
            b.layoutDropoff.getRoot().startAnimation(AnimUtils.slideInRight(this));
            pickupModeEnable = false;
            dropoffModeEnable = true;
            b.layoutDetails.textEtaDetails.setText("No destination selected");
            b.layoutDetails.textAmountDetails.setText("00.00");
            b.customMarkerText.setText("Select Dropoff Address");
            map.clear();
            showDrivers();
            b.setShowMarker(true);
        } else if (!pickupModeEnable && dropoffModeEnable) {
            b.layoutDropoff.getRoot().startAnimation(AnimUtils.slideOutRight(this));
            b.layoutDropoff.getRoot().setVisibility(View.INVISIBLE);
            b.layoutCar.getRoot().setVisibility(View.VISIBLE);
            b.layoutCar.getRoot().startAnimation(AnimUtils.slideInRight(this));
            pickupModeEnable = true;
            dropoffModeEnable = false;
            b.layoutDropoff.textDropoffAddress.setText("Updating Location");
            b.customMarkerText.setText("Select Pickup Address");
            setDropOff(false);
        } else {
            super.onBackPressed();
        }
    }

    public void logout(View view) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Are you sure ?");
        dialog.setMessage("You want to logout from your account?");
        dialog.setNegativeButton("No", (d, i) -> d.dismiss());
        dialog.setPositiveButton("Yes", (d, i) -> {
            auth.signOut();
            finish();
            SharedPref.clearSharedPref(context);
            startActivity(new Intent(context, LoginActivity.class));
        });
        dialog.show();
    }

    public void resetView() {
        dropoffLocation = null;
        pickupLocation = null;
        pickupModeEnable = true;
        dropoffModeEnable = false;
        b.layoutDetails.getRoot().setVisibility(View.GONE);
        b.layoutCar.getRoot().setVisibility(View.VISIBLE);
        b.customMarkerText.setText("Updating Location");
        b.ll.setBackgroundResource(R.drawable.background_marker);
        b.layoutDetails.textEtaDetails.setText("No destination selected");
        b.layoutDetails.textAmountDetails.setText("00.00");
        showMyLocation();
        showDrivers();
        b.setShowMarker(true);
    }

    @SuppressLint("RestrictedApi")
    public void requestRide(View view) {
        if (b.layoutDetails.textEtaDetails.getText().equals("No destination selected")) {
            ToastUtils.showErrorToast(context, "Please try again...");
            return;
        }

        LatLng pLocation = new LatLng(pickupLocation.latitude, pickupLocation.longitude);
        LatLng dLocation = new LatLng(dropoffLocation.latitude, dropoffLocation.longitude);

        HashMap<String, Object> requestData = new HashMap<>();
        requestData.put("fullName", SharedPref.getString(this, "fullName"));
        requestData.put("profilePic", SharedPref.getString(this, "profilePic"));
        requestData.put("pickupAddress", b.layoutDetails.textPickupAddressDetails.getText().toString());
        requestData.put("dropoffAddress", b.layoutDetails.textDropoffAddressDetails.getText().toString());
        requestData.put("pickupLocation", MapUtils.getCordinate(pLocation));
        requestData.put("dropoffLocation", MapUtils.getCordinate(dLocation));
        requestData.put("etaTime", etaTime);
        requestData.put("etaEarning", etaEarning);
        requestData.put("etaDistance", etaDistance);
        requestData.put("vehicleType", carListItem.get(carSelectedIndex).getCarName());
        requestData.put("isTripAccepted", false);
        requestData.put("isTripStarted", false);
        requestData.put("isTripOver", false);
        requestData.put("notifyUser", false);
        requestData.put("time", System.currentTimeMillis());
        requestData.put("passengerKey", auth.getUid());

//        Intent intent = new Intent(this, ConfirmBookingActivity.class);
//        intent.putExtra("requestData", requestData);
//        startActivity(intent);

//        StoreUtils.context = this;
    }

    public void openProfile(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabMenu:
                b.drawerLayout.openDrawer(Gravity.START);
                break;
            case R.id.layout_car_select:
                findViewById(R.id.rl_list_cars).setVisibility(View.VISIBLE);
                findViewById(R.id.icon_collapse_list_cars).setVisibility(View.VISIBLE);
                break;
            case R.id.icon_collapse_list_cars:
                findViewById(R.id.rl_list_cars).setVisibility(View.INVISIBLE);
                findViewById(R.id.icon_collapse_list_cars).setVisibility(View.GONE);
                break;

            case R.id.btn_myLocation_from_car:
                showMyLocation();
                break;
            case R.id.btn_backTo_car:
                b.layoutDropoff.getRoot().startAnimation(AnimUtils.slideOutRight(this));
                b.layoutDropoff.getRoot().setVisibility(View.INVISIBLE);
                b.layoutCar.getRoot().setVisibility(View.VISIBLE);
                b.layoutCar.getRoot().startAnimation(AnimUtils.slideInRight(this));
                pickupModeEnable = true;
                dropoffModeEnable = false;
                b.layoutDropoff.textDropoffAddress.setText("Updating Location");
                b.customMarkerText.setText("Select Pickup Address");
                setDropOff(false);
                break;
            case R.id.btn_back_details:
                b.layoutDetails.getRoot().startAnimation(AnimUtils.slideOutRight(this));
                b.layoutDetails.getRoot().setVisibility(View.INVISIBLE);
                b.layoutDropoff.getRoot().setVisibility(View.VISIBLE);
                b.layoutDropoff.getRoot().startAnimation(AnimUtils.slideInRight(this));
                pickupModeEnable = false;
                dropoffModeEnable = true;
                b.layoutDetails.textEtaDetails.setText("No destination selected");
                b.layoutDetails.textAmountDetails.setText("00.00");
                b.customMarkerText.setText("Select Dropoff Address");
                map.clear();
                showDrivers();
                b.setShowMarker(true);
                break;
            case R.id.btn_pickUp_car:
                if (b.layoutCar.txtPickupAddressCar.getText().equals("Updating Location")) {
                    ToastUtils.showErrorToast(context, "Please select pickup address.");
                    return;
                }
                b.layoutCar.getRoot().startAnimation(AnimUtils.slideOutLeft(this));
                b.layoutCar.getRoot().setVisibility(View.INVISIBLE);
                b.layoutDropoff.getRoot().setVisibility(View.VISIBLE);
                b.layoutDropoff.getRoot().startAnimation(AnimUtils.slideInLeft(this));
                b.customMarkerText.setText("Select Dropoff Address");
                pickupModeEnable = false;
                dropoffModeEnable = true;
                setDropOff(true);
                break;
            case R.id.btn_dropoff:
                if (b.layoutDropoff.textDropoffAddress.getText().equals("Updating Location")) {
                    ToastUtils.showErrorToast(context, "Please select dropoff address.");
                    return;
                }
                b.layoutDropoff.getRoot().startAnimation(AnimUtils.slideOutLeft(this));
                b.layoutDropoff.getRoot().setVisibility(View.INVISIBLE);
                b.layoutDetails.getRoot().setVisibility(View.VISIBLE);
                b.layoutDetails.getRoot().startAnimation(AnimUtils.slideInLeft(this));
                if (dropoffModeEnable) {
                    calculateDistanceForDetails();
                }
                pickupModeEnable = false;
                dropoffModeEnable = false;
                b.setShowMarker(false);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == RequestCodeUtils.GPS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!MapUtils.isGPSEnable(this)) {
                    MapUtils.buildAlertMessageNoGps(this);
                    return;
                }
                showMyLocation();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                ToastUtils.showErrorToast(this, "Uffride cannot run without permission granted!!!");
                finish();
            }
        }
    }

    private class GetDistance extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            return MapUtils.downloadUrl(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject obj = new JSONObject(s);
                JSONArray arr = obj.getJSONArray("rows");
                etaTime = arr.getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").get("text").toString();
                etaDistance = arr.getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance").get("text").toString();

                b.layoutDetails.textEtaDetails.setText(etaDistance + ", " + etaTime);
                float amount = FairUtils.getFair(Float.parseFloat(etaDistance.split(" ")[0]), carListItem.get(carSelectedIndex).getCarName());
                etaEarning = new DecimalFormat("#.##").format(amount);
                b.layoutDetails.textAmountDetails.setText(etaEarning);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}