package np.com.softwarica.uride.activities.passengers;

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
import np.com.softwarica.uride.activities.drivers.RegisterDriverActivity;
import np.com.softwarica.uride.databinding.ActivityRegisterPassengerBinding;
import np.com.softwarica.uride.utils.FirebaseUtils;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.SharedPref;
import np.com.softwarica.uride.utils.ToastUtils;

public class RegisterPassengerActivity extends AppCompatActivity {

    private ActivityRegisterPassengerBinding b;
    private ProgressDialog dialog;
    private DatabaseReference database;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_register_passenger);

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
        if (!validate()) {
            return;
        }

        dialog.setMessage("Creating Profile...");
        dialog.setCancelable(false);
        dialog.show();

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("fullName", getString(b.etFullName));
        userData.put("email", getString(b.etEmail));
        userData.put("phoneNumber", phoneNumber);
        userData.put("joinDate", System.currentTimeMillis());
        userData.put("profilePic", "");
        userData.put("online", true);
        userData.put("isDriver", false);

        database.child("users").child(FirebaseUtils.userId).setValue(userData).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                startActivity(new Intent(RegisterPassengerActivity.this, UserDashboardActivity.class));
                SharedPref.setString(RegisterPassengerActivity.this, "isDriver", "false");
                finish();
            } else {
                ToastUtils.showErrorToast(RegisterPassengerActivity.this, task.getException().getMessage());
            }
        });
    }

    private boolean validate() {
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return false;
        }
        if (getString(b.etFullName).isEmpty()) {
            b.etFullName.setError("Please enter your full name.");
            b.etFullName.setFocusable(true);
            return false;
        }
        if (getString(b.etEmail).isEmpty()) {
            b.etEmail.setError("Please enter email address.");
            b.etEmail.setFocusable(true);
            return false;
        }
        return true;
    }

    private String getString(EditText editText) {
        return editText.getText().toString().trim();
    }

    public void openDriverRegisterActivity(View view) {
        Intent intent = new Intent(this, RegisterDriverActivity.class);
        intent.putExtra("phoneNumber", phoneNumber);
        startActivity(intent);
    }
}
