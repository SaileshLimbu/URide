package np.com.softwarica.uride.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.databinding.ActivityRegisterBinding;
import np.com.softwarica.uride.utils.FirebaseUtils;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.SharedPref;
import np.com.softwarica.uride.utils.ToastUtils;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding b;
    private ProgressDialog dialog;
    private DatabaseReference database;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_register);

        dialog = new ProgressDialog(this);
        database = FirebaseDatabase.getInstance().getReference();
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        b.toolbar.setTitle("Sign Up");
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

    public void registerAsUser(View view) {
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }
        if (getString(b.textFullName).isEmpty()) {
            b.textFullName.setError("Full Name is required!!!");
            b.textFullName.setFocusable(true);
            return;
        }
        if (getString(b.textEmail).isEmpty()) {
            b.textEmail.setError("Email is required!!!");
            b.textEmail.setFocusable(true);
            return;
        }

        dialog.setMessage("Creating Profile...");
        dialog.setCancelable(false);
        dialog.show();

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("fullName", getString(b.textFullName));
        userData.put("email", getString(b.textEmail));
        userData.put("phoneNumber", phoneNumber);
        userData.put("joinDate", System.currentTimeMillis());
        userData.put("profilePic", "");
        userData.put("online", true);
        userData.put("isDriver", false);

        database.child("users").child(FirebaseUtils.userId).setValue(userData).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                SharedPref.setString(RegisterActivity.this, "isDriver", "false");
                finish();
            } else {
                ToastUtils.showErrorToast(RegisterActivity.this, task.getException().getMessage());
            }
        });
    }

    public void registerAsDriver(View view) {
        if (getString(b.textFullName).isEmpty()) {
            b.textFullName.setError("Full Name is required!!!");
            b.textFullName.setFocusable(true);
            return;
        }
        if (getString(b.textEmail).isEmpty()) {
            b.textEmail.setError("Email is required!!!");
            b.textEmail.setFocusable(true);
            return;
        }
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }

        dialog.setMessage("Creating Profile...");
        dialog.show();

        HashMap<String, Object> tripData = new HashMap<>();
        tripData.put("totalTrip", 0);
        tripData.put("totalEarning", 00.00);
        tripData.put("totalDistance", 0);

        HashMap<String, Object> driverData = new HashMap<>();
        driverData.put("fullName", getString(b.textFullName));
        driverData.put("email", getString(b.textEmail));
        driverData.put("phoneNumber", phoneNumber);
        driverData.put("isDriver", true);
        driverData.put("isOnTrip", false);
        driverData.put("isCardAdded", false);
        driverData.put("isPickupModeEnabled", false);
        driverData.put("documentUploadLevel", 0);
        driverData.put("joinDate", System.currentTimeMillis());
        driverData.put("tripData", tripData);
        driverData.put("profilePic", "");
        driverData.put("verified", false);

        database.child("drivers").child(FirebaseUtils.userId).setValue(driverData).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                startActivity(new Intent(RegisterActivity.this, AddVehicleDetailsActivity.class));
                finish();
            } else {
                ToastUtils.showErrorToast(RegisterActivity.this, task.getException().getMessage());
            }
        });
    }

    private String getString(EditText editText) {
        return editText.getText().toString().trim();
    }
}
