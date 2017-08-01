package com.kikyo.note.EditText;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * Created by 婷 on 2017/8/1.
 */

public class CustomEditText extends android.support.v7.widget.AppCompatEditText {


    private Rect mRect;
    private Paint mPaint;

    private final int padding = 10;

    private int lineHeight;
    private int viewHeight,viewWidth;

    public CustomEditText(Context context) {
        this(context, null);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.LTGRAY);
        mPaint.setAntiAlias(true);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getLineCount();
        Rect r = mRect;
        Paint paint = mPaint;
        int lineHeight = 0;
        int i = 0;
        while (i < count) {
            lineHeight = getLineBounds(i, r);
            canvas.drawLine(r.left, lineHeight + padding, r.right, lineHeight + padding,
                    paint);
            i++;
        }
        int maxLines = 15;
        int avgHeight = lineHeight / count;
        int currentLineHeight = lineHeight;

        while(i < maxLines){
            currentLineHeight = currentLineHeight + avgHeight + padding;
            canvas.drawLine(r.left, currentLineHeight, r.right, currentLineHeight, paint);
            i++;
        }
        super.onDraw(canvas);
    }
}
