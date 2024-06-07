package com.widget.noname.plus.common.animation;

import android.animation.ValueAnimator;
import android.view.animation.PathInterpolator;

public class AnimatorUtil {
    private static final int DURATION_ALPHA = 300;
    private static final int DURATION_SCALE = 300;
    private final static PathInterpolator ALPHA_INTERPOLATOR = new PathInterpolator(0.33f, 0f, 0.67f, 1f);
    private final static PathInterpolator SCALE_INTERPOLATOR = new PathInterpolator(0.33f, 0f, 0.67f, 1f);
    private final static PathInterpolator TRANS_INTERPOLATOR = new PathInterpolator(0.3f, 0f, 0.1f, 1f);

    public static ValueAnimator ofAlpha(float from, float to, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(ALPHA_INTERPOLATOR);
        return valueAnimator;
    }

    public static ValueAnimator ofAlpha(float from, float to) {
        return ofAlpha(from, to, DURATION_ALPHA);
    }

    public static ValueAnimator ofScale(float from, float to) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
        valueAnimator.setDuration(DURATION_SCALE);
        valueAnimator.setInterpolator(SCALE_INTERPOLATOR);
        return valueAnimator;
    }
}
