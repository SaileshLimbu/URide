package np.com.softwarica.uride.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.widget.TextView;

import java.util.HashMap;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.activities.passengers.CancelOptionActivity;


public class CancelBooking {
    public static void showDialog(final Activity activity, final HashMap<String, Object> obj) {
        final Dialog dialog = new Dialog(activity, R.style.AlertDialog);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_confirm_cancel);

        TextView txtNo = dialog.findViewById(R.id.txtNo);
        TextView txtYes = dialog.findViewById(R.id.txtYes);

        txtNo.setOnClickListener(view -> dialog.dismiss());

        txtYes.setOnClickListener(view -> {
            dialog.dismiss();
            Intent intent = new Intent(activity, CancelOptionActivity.class);
            intent.putExtra("requestData", obj);
            activity.startActivity(intent);
        });
        dialog.show();
    }
}
