package com.widget.noname.plus.nonameui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

public class NButton extends RelativeLayout {
    private static final int BG_COLOR_BLUR = 0;
    private static final int BG_COLOR_GREED = 1;
    private static final int BG_COLOR_GREY = 2;
    private static final int BG_COLOR_ORANGE = 3;
    private static final int BG_COLOR_PURPLE = 4;
    private static final int BG_COLOR_RED = 5;
    private static final int BG_COLOR_YELLOW = 6;

    private final PathInterpolator alphaInterpolator = new PathInterpolator(0.33f, 0f, 0.67f, 1f);

    private TextView firstText = null;
    private TextView secondText = null;

    private ValueAnimator downAnimator = null;
    private ValueAnimator upAnimator = null;
    private AppCompatImageView captionBg = null;
    private String buttonText = null;
    private int captionColor = 0;

    public NButton(Context context) {
        this(context, null);
    }

    public NButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NButton);

        buttonText = ta.getString(R.styleable.NButton_text);
        captionColor = ta.getInt(R.styleable.NButton_caption_color, 0);
        ta.recycle();

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

        if (null != captionBg) {
            setCaptionBg(captionColor);
        }
    }

    private void setCaptionBg(int captionColor) {
        System.out.println("zyq: " + captionColor);

        switch (captionColor) {
            case BG_COLOR_BLUR: {
                captionBg.setBackgroundResource(R.drawable.button_blue);
                break;
            }
            case BG_COLOR_GREED: {
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
            super.onTouchEvent(event);

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

            return false;
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
