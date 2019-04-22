package np.com.softwarica.uride.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import np.com.softwarica.uride.R;


public class SuccessDialog {
    public static void showDialog(final Activity activity) {
        final Dialog dialog = new Dialog(activity, R.style.AlertDialog);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_booking_successful);

        View done = dialog.findViewById(R.id.viewDone);

        done.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }
}
