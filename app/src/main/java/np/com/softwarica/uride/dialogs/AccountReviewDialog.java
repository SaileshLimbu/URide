package np.com.softwarica.uride.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import np.com.softwarica.uride.R;


public class AccountReviewDialog {
    public static void showDialog(final Activity activity) {
        final Dialog dialog = new Dialog(activity, R.style.AlertDialog);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_notify_ac_verification);

        View ok = dialog.findViewById(R.id.imgOk);

        ok.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }
}
