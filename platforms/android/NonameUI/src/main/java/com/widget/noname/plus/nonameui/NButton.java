package com.widget.noname.plus.nonameui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

public class NButton extends RelativeLayout {
    private static final String BG_COLOR_BLUR = "blue";
    private static final String BG_COLOR_GREEN = "green";
    private static final String BG_COLOR_GREY = "grey";
    private static final String BG_COLOR_ORANGE = "orange";
    private static final String BG_COLOR_PURPLE = "purple";
    private static final String BG_COLOR_RED = "red";
    private static final String BG_COLOR_YELLOW = "yellow";

    private final PathInterpolator alphaInterpolator = new PathInterpolator(0.33f, 0f, 0.67f, 1f);

    private TextView firstText = null;
    private TextView secondText = null;

    private ValueAnimator downAnimator = null;
    private ValueAnimator upAnimator = null;
    private AppCompatImageView captionBg = null;
    private String buttonText = null;

    public NButton(Context context) {
        this(context, null);
    }

    public NButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setButtonText(String text) {
        buttonText = text;

        if ((null != text) && text.length() > 1) {
            int mid = text.length() / 2;
            String first = text.substring(0, mid);
            String second = text.substring(mid);
            firstText.setText(first);
            secondText.setText(second);
        }
    }

    public String getButtonText() {
        return buttonText;
    }

    private void init() {
        setClipChildren(false);
        setClipToPadding(false);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.nbutton_layout, this);
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "font/xingkai.ttf");

        firstText = view.findViewById(R.id.text_lt);
        secondText = view.findViewById(R.id.text_rb);

        if (null != typeface) {
            firstText.setTypeface(typeface);
            secondText.setTypeface(typeface);
        }

        setButtonText(buttonText);
        captionBg = view.findViewById(R.id.caption_bg);
        setCaptionColor(BG_COLOR_ORANGE);
    }

    public void setCaptionColor(String captionColor) {
        System.out.println("zyq: " + captionColor);

        if ((null == captionBg) || TextUtils.isEmpty(captionColor)) {
            return;
        }

        switch (captionColor) {
            case BG_COLOR_BLUR: {
                captionBg.setBackgroundResource(R.drawable.button_blue);
                break;
            }
            case BG_COLOR_GREEN: {
                captionBg.setBackgroundResource(R.drawable.button_green);
                break;
            }
            case BG_COLOR_GREY: {
                captionBg.setBackgroundResource(R.drawable.button_grey);
                break;
            }
            case BG_COLOR_ORANGE: {
                captionBg.setBackgroundResource(R.drawable.button_orange);
                break;
            }
            case BG_COLOR_PURPLE: {
                captionBg.setBackgroundResource(R.drawable.button_purple);
                break;
            }
            case BG_COLOR_RED: {
                captionBg.setBackgroundResource(R.drawable.button_red);
                break;
            }
            case BG_COLOR_YELLOW: {
                captionBg.setBackgroundResource(R.drawable.button_yellow);
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (null != captionBg && isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    executeDownAnimation();
                    return true;
                }
                case MotionEvent.ACTION_CANCEL:
                    executeUpAnimation();
                    break;
                case MotionEvent.ACTION_UP: {
                    executeUpAnimation();

                    performClick();
                    return true;
                }
            }

            return super.onTouchEvent(event);
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void executeDownAnimation() {
        if (null != upAnimator && upAnimator.isStarted()) {
            upAnimator.cancel();
        }

        if (null == downAnimator) {
            downAnimator = ValueAnimator.ofFloat(1f, 0.5f);
            downAnimator.addUpdateListener(animation -> captionBg.setAlpha((float) animation.getAnimatedValue()));
            downAnimator.setInterpolator(alphaInterpolator);
            downAnimator.setDuration(150);
        }

        downAnimator.start();
    }

    private void executeUpAnimation() {
        if (null != downAnimator && downAnimator.isStarted()) {
            downAnimator.cancel();
        }

        if (null == upAnimator) {
            upAnimator = ValueAnimator.ofFloat(0.5f, 1f);
            upAnimator.addUpdateListener(animation -> captionBg.setAlpha((float) animation.getAnimatedValue()));
            upAnimator.setInterpolator(alphaInterpolator);
            upAnimator.setDuration(150);
        }

        upAnimator.start();
    }
}
