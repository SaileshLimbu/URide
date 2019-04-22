package np.com.softwarica.uride.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.activities.drivers.DriverDashboardActivity;

public class AccountVerifiedDialog {
    public static void showDialog(final Activity activity) {
        final Dialog dialog = new Dialog(activity, R.style.AlertDialog);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_account_verified);

        View ok = dialog.findViewById(R.id.imgOk);
        ok.setOnClickListener(view -> {
            ((DriverDashboardActivity) activity).openFirstPaymentActivity();
            dialog.dismiss();
        });
        dialog.show();
    }
}
