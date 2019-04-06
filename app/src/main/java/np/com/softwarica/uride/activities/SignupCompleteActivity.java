package np.com.softwarica.uride.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.databinding.ActivitySignupCompleteBinding;
import np.com.softwarica.uride.utils.SharedPref;

public class SignupCompleteActivity extends AppCompatActivity {

    private ActivitySignupCompleteBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_signup_complete);

        b.toolbar.setTitle("Signup Complete");
    }

    public void goBack(View view) {
        super.onBackPressed();
    }

    public void onDonePressed(View view) {
        if (SharedPref.getString(this, "isDriver").equals("true")) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
