package np.com.softwarica.uride.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import np.com.softwarica.uride.R;

public class ToastUtils {
    public static void showSuccessToast(Context context, String message) {
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_success_toast, null);
        ((TextView) view.findViewById(R.id.txtMessage)).setText(message);
        toast.setView(view);
        toast.show();
    }

    public static void showErrorToast(Context context, String message) {
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_error_toast, null);
        ((TextView) view.findViewById(R.id.txtMessage)).setText(message);
        toast.setView(view);
        toast.show();
    }
}
