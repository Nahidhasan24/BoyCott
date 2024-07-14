package com.nahidsoft.boycott;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BarcodeOverlayView extends View {
    private Paint framePaint;
    private Paint backgroundPaint;
    private Rect frameRect;

    public BarcodeOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        framePaint = new Paint();
        framePaint.setColor(getResources().getColor(R.color.primary)); // Use your primary color resource
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(5);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#80000000")); // 50% transparent black
        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        // Define the size and position of the frame
        int frameWidth = (int) (width * 3 / 3.1);
        int frameHeight = (int) (height / 4.1);

        int left = (width - frameWidth) / 2;
        int top = (height - frameHeight) / 2;
        int right = left + frameWidth;
        int bottom = top + frameHeight;

        frameRect = new Rect(left, top, right, bottom);

        // Draw the background outside the frame
        canvas.drawRect(0, 0, width, top, backgroundPaint);
        canvas.drawRect(0, top, left, bottom, backgroundPaint);
        canvas.drawRect(right, top, width, bottom, backgroundPaint);
        canvas.drawRect(0, bottom, width, height, backgroundPaint);

        // Draw the frame
        canvas.drawRect(frameRect, framePaint);
    }

    public Rect getFrameRect() {
        return frameRect;
    }
}
