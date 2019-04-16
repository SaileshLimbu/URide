package np.com.softwarica.uride.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.activities.passengers.RegisterPassengerActivity;
import np.com.softwarica.uride.databinding.ActivityLoginBinding;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.SharedPref;
import np.com.softwarica.uride.utils.StoreUtils;
import np.com.softwarica.uride.utils.ToastUtils;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding b;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private DatabaseReference database;
    private String countryDialCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
        database = FirebaseDatabase.getInstance().getReference();
        //countryDialCode = SharedPref.getString(this, "phoneCode");
        countryDialCode = "+977";

        b.txtCountryCode.setText(countryDialCode);
    }

    public void sendVerification(View view) {
        if (!validate()) {
            return;
        }

        final String phoneNumber = countryDialCode + b.txtPhone.getText().toString().trim();

        dialog.setMessage("Sending Verification Code...");
        dialog.setCancelable(false);
        dialog.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        dialog.setMessage("Verifying...");
                        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                final FirebaseUser user = task.getResult().getUser();
                                //Check if FirebaseUser has an account under USER section
                                database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(user.getUid())) {
                                            openActivity(MainActivity.class);
                                            finish();
                                        } else {
                                            //Check if FirebaseUser has an account under DRIVER section
                                            database.child("drivers").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.hasChild(user.getUid())) {
                                                        SharedPref.setString(LoginActivity.this, "isDriver", "true");
                                                        openActivity(MainActivity.class);
                                                        dialog.dismiss();
                                                        finish();
                                                    } else {
                                                        //If no account found prompt them to register
                                                        dialog.dismiss();
                                                        Intent intent = new Intent(LoginActivity.this, RegisterPassengerActivity.class);
                                                        intent.putExtra("phoneNumber", b.txtPhone.getText().toString().trim());
                                                        startActivity(intent);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    dialog.dismiss();
                                                    Toast.makeText(LoginActivity.this, "You have cancel the process.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        dialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "You have cancel the process.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                dialog.dismiss();
                                ToastUtils.showErrorToast(LoginActivity.this, task.getException().getMessage());
                            }
                        });
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.d("error", e.getMessage());
                        ToastUtils.showErrorToast(LoginActivity.this, e.getMessage());
                        dialog.dismiss();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        dialog.dismiss();
                        StoreUtils.token = forceResendingToken;
                        Intent intent = new Intent(LoginActivity.this, VerificationActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phoneNumber", phoneNumber);
                        startActivity(intent);
                        finish();
                    }
                });

    }

    private boolean validate() {
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return false;
        }

        if (!b.chkTos.isChecked()) {
            ToastUtils.showErrorToast(this, "Please accept our terms and conditions.");
            return false;
        }

        if (b.txtPhone.getText().toString().isEmpty()) {
            b.txtPhone.setError("Please enter phone number.");
            b.txtPhone.setFocusable(true);
            return false;
        }
        return true;
    }

    public void openActivity(Class<?> cls) {
        startActivity(new Intent(new Intent(LoginActivity.this, cls)));
    }
}
