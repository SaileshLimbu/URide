package np.com.softwarica.uride.utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import np.com.softwarica.uride.R;

public class AnimUtils {

    public static Animation slideInLeft(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
    }

    public static Animation slideOutLeft(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.slide_out_left);
    }

    public static Animation slideInRight(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
    }

    public static Animation slideOutRight(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.slide_out_right);
    }

}
