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
    private int viewHeight, viewWidth;

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
        //把行高变成2倍
        setLineSpacing(36, 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //保存画布当前状态
        canvas.save();
        //把画布坐标系下移getLineHeight() / 3的高度。这样的话下面画的横线就会比原先下移一点了。
        canvas.translate(0, 18);
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
        int avgHeight = getLineHeight();
        int currentLineHeight = lineHeight;

        while (i < maxLines) {
            currentLineHeight = currentLineHeight + avgHeight + padding;
            canvas.drawLine(r.left, currentLineHeight, r.right, currentLineHeight, paint);
            i++;
        }
        //恢复画布状态（也就是撤销坐标系变换），使super.onDraw能正常绘制。
        canvas.restore();
        super.onDraw(canvas);
    }
}
