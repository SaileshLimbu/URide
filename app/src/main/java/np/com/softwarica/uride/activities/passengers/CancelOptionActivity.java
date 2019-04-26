package np.com.softwarica.uride.activities.passengers;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.callbacks.UValueEventListener;
import np.com.softwarica.uride.databinding.ActivityCancelOptionBinding;
import np.com.softwarica.uride.utils.FirebaseUtils;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.ToastUtils;

public class CancelOptionActivity extends AppCompatActivity {

    private ActivityCancelOptionBinding binding;
    private HashMap<String, Object> requestData;
    private String reasonCancellation = "Others";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cancel_option);
        binding.toolbar.setTitle("Reason for Cancellation");
        binding.setIndex(10);

        requestData = (HashMap<String, Object>) getIntent().getSerializableExtra("requestData");

        binding.txtDone.setOnClickListener(view -> cancelTrip());
        binding.txtCancel.setOnClickListener(view -> this.onBackPressed());

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

    void cancelTrip() {
        if(!NetworkUtils.isConnected(this)){
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }
        FirebaseUtils.database.child("trips/" + FirebaseUtils.userId + "/isTripAccepted").addListenerForSingleValueEvent(new UValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((boolean) dataSnapshot.getValue()) {
                    ToastUtils.showErrorToast(CancelOptionActivity.this, "Trip already started!!!");
                } else {
                    requestData.put("status", false);
                    requestData.put("reason", reasonCancellation);
                    FirebaseUtils.database.child("users/" + FirebaseUtils.userId + "/history").push().setValue(requestData);
                    FirebaseUtils.database.child("trips/" + FirebaseUtils.userId).removeValue();
                    ToastUtils.showSuccessToast(CancelOptionActivity.this, "Trip cancelled successfully!!!");
                    startActivity(new Intent(CancelOptionActivity.this, UserDashboardActivity.class));
                    finishAffinity();
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                }
            }
        });
    }

    public void selectOption(View view) {
        switch (view.getId()) {
            case R.id.option1:
                binding.setIndex(1);
                reasonCancellation = "Lost Items";
                break;
            case R.id.option2:
                binding.setIndex(2);
                reasonCancellation = "Fare Issues";
                break;
            case R.id.option3:
                binding.setIndex(3);
                reasonCancellation = "Route Feedback";
                break;
            case R.id.option4:
                binding.setIndex(4);
                reasonCancellation = "Driver Feedback";
                break;
            case R.id.option5:
                binding.setIndex(5);
                reasonCancellation = "Vehicle Feedback";
                break;
            case R.id.option6:
                binding.setIndex(6);
                reasonCancellation = "Receipts and Payments";
                break;
            case R.id.option7:
                binding.setIndex(7);
                reasonCancellation = "Promotions";
                break;
            case R.id.option8:
                binding.setIndex(8);
                reasonCancellation = "I was involved in an accident";
                break;
            case R.id.option9:
                binding.setIndex(9);
                reasonCancellation = "Cash Trips";
                break;
            case R.id.option10:
                binding.setIndex(10);
                reasonCancellation = "Others";
                break;
        }
    }
}
