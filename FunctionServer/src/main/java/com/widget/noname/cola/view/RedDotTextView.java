package com.widget.noname.cola.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.widget.noname.cola.library.R;
import com.widget.noname.plus.server.NonameWebSocketServer;

public class RedDotTextView extends AppCompatTextView {
    private static final int[] SERVER_STATE_SET_RUNNING = {R.attr.state_server_running};
    private static final int[] SERVER_STATE_SET_STOP = {R.attr.state_server_stop};
    private static final int[] SERVER_STATE_SET_ERROR = {R.attr.state_server_error};
    private static final int[] SERVER_STATE_SET_CLOSE = {R.attr.state_server_close};

    private int status = NonameWebSocketServer.SERVER_TYPE_STOP;

    public RedDotTextView(Context context) {
        super(context);
    }

    public RedDotTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RedDotTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
        if (drawables != null) {
            Drawable drawableLeft = drawables[0];
            if (drawableLeft != null) {
                float textWidth = getPaint().measureText(getText().toString());
                int drawablePadding = getCompoundDrawablePadding();
                int drawableWidth = 0;
                drawableWidth = drawableLeft.getIntrinsicWidth();
                float bodyWidth = textWidth + drawableWidth + drawablePadding;
                canvas.translate((getWidth() - bodyWidth) / 2, 0);
            }
        }
        super.onDraw(canvas);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        switch (status) {
            case NonameWebSocketServer.SERVER_TYPE_START:
            case NonameWebSocketServer.SERVER_TYPE_RUNNING: {
                mergeDrawableStates(drawableState, SERVER_STATE_SET_RUNNING);
                break;
            }
            case NonameWebSocketServer.SERVER_TYPE_CLOSE: {
                mergeDrawableStates(drawableState, SERVER_STATE_SET_CLOSE);
                break;
            }
            case NonameWebSocketServer.SERVER_TYPE_ERROR: {
                mergeDrawableStates(drawableState, SERVER_STATE_SET_ERROR);
                break;
            }
            case NonameWebSocketServer.SERVER_TYPE_STOP: {
                mergeDrawableStates(drawableState, SERVER_STATE_SET_STOP);
                break;
            }
        }

        return drawableState;
    }

    public void setStatus(int status) {
        this.status = status;
        refreshDrawableState();
    }
}
