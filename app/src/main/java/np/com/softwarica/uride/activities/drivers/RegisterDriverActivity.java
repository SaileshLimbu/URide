package np.com.softwarica.uride.activities.drivers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.activities.passengers.UserDashboardActivity;
import np.com.softwarica.uride.adapters.SpinnerCarAdapter;
import np.com.softwarica.uride.databinding.ActivityRegisterDriverBinding;
import np.com.softwarica.uride.models.CarItem;
import np.com.softwarica.uride.utils.FirebaseUtils;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.ToastUtils;

public class RegisterDriverActivity extends AppCompatActivity {

    private ActivityRegisterDriverBinding b;
    private ProgressDialog dialog;
    private DatabaseReference database;
    private String phoneNumber;
    private ArrayList<CarItem> carListItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_register_driver);

        dialog = new ProgressDialog(this);
        database = FirebaseDatabase.getInstance().getReference();
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        b.toolbar.setTitle("Sign Up");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        initSpinner();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void goBack(View view) {
        this.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void registerAsDriver(View view) {
        if (!validate()) {
            return;
        }
        dialog.setMessage("Creating Profile...");
        dialog.setCancelable(false);
        dialog.show();

        HashMap<String, Object> tripData = new HashMap<>();
        tripData.put("totalTrip", 0);
        tripData.put("totalEarning", 00.00);
        tripData.put("totalDistance", 0);

        HashMap<String, Object> driverData = new HashMap<>();
        driverData.put("fullName", getString(b.etFullName));
        driverData.put("email", getString(b.etEmail));
        driverData.put("phoneNumber", phoneNumber);
        driverData.put("vehicleColor", b.etVehicleColor);
        driverData.put("vehicleNumber", b.etVehicleNumber);
        driverData.put("vehicleType", b.spnVehicleType);
        driverData.put("isDriver", true);
        driverData.put("isOnTrip", false);
        driverData.put("isCardAdded", false);
        driverData.put("isPickupModeEnabled", false);
        driverData.put("joinDate", System.currentTimeMillis());
        driverData.put("tripData", tripData);
        driverData.put("profilePic", "");
        driverData.put("verified", false);

        database.child("drivers").child(FirebaseUtils.userId).setValue(driverData).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                startActivity(new Intent(RegisterDriverActivity.this, UserDashboardActivity.class));
                finish();
            } else {
                ToastUtils.showErrorToast(RegisterDriverActivity.this, task.getException().getMessage());
            }
        });
    }

    private boolean validate() {
        if (TextUtils.isEmpty(getString(b.etEmail))) {
            b.etEmail.setError("Please enter email address.");
            b.etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(getString(b.etFullName))) {
            b.etFullName.setError("Please enter your full name.");
            b.etFullName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(getString(b.etVehicleNumber))) {
            b.etVehicleNumber.setError("Please enter vehicle number.");
            b.etVehicleNumber.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(getString(b.etVehicleColor))) {
            b.etVehicleColor.setError("Please enter vehicle color.");
            b.etVehicleColor.requestFocus();
            return false;
        }
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return false;
        }
        return true;
    }

    private String getString(EditText editText) {
        return editText.getText().toString().trim();
    }

    public void initSpinner() {
        carListItem = new ArrayList<>();
        carListItem.add(new CarItem(R.drawable.vehicle_1, "Sedan", "3 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_2, "Plus", "7 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_3, "Regular", "3 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_4, "SUV", "6 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_5, "Luxury", "4 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_6, "Motor Bike", "2 seats"));

        SpinnerCarAdapter adapter = new SpinnerCarAdapter(this, carListItem);
        b.spnVehicleType.setAdapter(adapter);
    }
}