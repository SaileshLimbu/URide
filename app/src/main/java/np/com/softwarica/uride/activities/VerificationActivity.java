package np.com.softwarica.uride.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

import java.util.concurrent.TimeUnit;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.activities.passengers.RegisterPassengerActivity;
import np.com.softwarica.uride.activities.passengers.UserDashboardActivity;
import np.com.softwarica.uride.databinding.ActivityVerificationBinding;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.SharedPref;
import np.com.softwarica.uride.utils.StoreUtils;
import np.com.softwarica.uride.utils.ToastUtils;

public class VerificationActivity extends AppCompatActivity {

    private ActivityVerificationBinding binding;
    private String verificationId;
    private String phoneNumber;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private SmsVerifyCatcher smsVerifyCatcher;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verification);

        binding.toolbar.setTitle("Verify Mobile Number");
        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                binding.edittextCode1.setText(message.substring(0, 1));
                binding.edittextCode2.setText(message.substring(1, 2));
                binding.edittextCode3.setText(message.substring(2, 3));
                binding.edittextCode4.setText(message.substring(3, 4));
                binding.edittextCode5.setText(message.substring(4, 5));
                binding.edittextCode6.setText(message.substring(5, 6));
                verifyUser();
            }
        });

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void goBack(View view) {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    protected void onStart() {
        super.onStart();
        smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        smsVerifyCatcher.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void submit(View view) {
        verifyUser();
    }

    public void resendVerificationCode(View view) {
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }
        dialog = new ProgressDialog(this);
        dialog.setMessage("Re-sending verification code.");
        dialog.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                VerificationActivity.this,
                new VerificationCallBack(),
                StoreUtils.token);
    }

    private void verifyUser() {
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }
        String code1 = binding.edittextCode1.getText().toString();
        String code2 = binding.edittextCode2.getText().toString();
        String code3 = binding.edittextCode3.getText().toString();
        String code4 = binding.edittextCode4.getText().toString();
        String code5 = binding.edittextCode5.getText().toString();
        String code6 = binding.edittextCode6.getText().toString();

        if (code1.isEmpty() || code2.isEmpty() || code3.isEmpty() || code4.isEmpty() || code5.isEmpty() || code6.isEmpty()) {
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Verifying...");
        dialog.setCancelable(false);
        dialog.show();

        String verificationCode = code1 + code2 + code3 + code4 + code5 + code6;
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, verificationCode);

        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                final FirebaseUser user = task.getResult().getUser();
                //Layer1
                database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user.getUid())) {
                            //openActivity(UserDashboardActivity.class);
                            openActivity(UserDashboardActivity.class);
                            finish();
                        } else {
                            //Layer2
                            database.child("drivers").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user.getUid())) {
                                        SharedPref.setString(VerificationActivity.this, "isDriver", "true");
                                        //openActivity(DriverDashboardActivity.class);
                                        openActivity(UserDashboardActivity.class);
                                        dialog.dismiss();
                                        finish();
                                    } else {
                                        dialog.dismiss();
                                        Intent intent = new Intent(VerificationActivity.this, RegisterPassengerActivity.class);
                                        intent.putExtra("phoneNumber", phoneNumber);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    dialog.dismiss();
                                    Toast.makeText(VerificationActivity.this, "You have cancel the process.", Toast.LENGTH_SHORT);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        dialog.dismiss();
                        Toast.makeText(VerificationActivity.this, "You have cancel the process.", Toast.LENGTH_SHORT);
                    }
                });
            } else {
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    dialog.dismiss();
                    Log.d("ERROR", task.getException().getMessage());
                }
            }
        });
    }

    public void openActivity(Class<?> cls) {
        startActivity(new Intent(new Intent(VerificationActivity.this, cls)));
    }

    private class VerificationCallBack extends PhoneAuthProvider.OnVerificationStateChangedCallbacks {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            dialog.dismiss();
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            dialog.dismiss();
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(verificationId, forceResendingToken);
            dialog.dismiss();
        }
    }
}