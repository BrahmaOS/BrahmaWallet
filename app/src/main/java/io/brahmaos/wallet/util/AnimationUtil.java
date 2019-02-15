package io.brahmaos.wallet.util;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimationUtil {
    public static TranslateAnimation makeInAnimation() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(250);
        return mHiddenAction;
    }

    public static TranslateAnimation makeOutAnimation() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(250);
        return mHiddenAction;
    }
}
