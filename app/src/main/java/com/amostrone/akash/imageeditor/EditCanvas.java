package com.amostrone.akash.imageeditor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class EditCanvas extends View {
    Paint paint;
    private final Paint mPaint;
    private float doo_startX;
    private float doo_startY;
    private float doo_endX;
    private float doo_endY;
    float text_currX;
    float text_currY;
    public static String text="";

    public EditCanvas(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(30);
        paint.setTextSize(60);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(30);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(text, text_currX,text_currY,paint);
        canvas.drawLine(doo_startX, doo_startY, doo_endX, doo_endY, mPaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(!MainActivity.isAddTextchosen) {
                    doo_startX = event.getX();
                    doo_startY = event.getY();
                }
                if(MainActivity.isAddTextchosen){
                    text_currX=event.getX();
                    text_currY=event.getY();
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if(!MainActivity.isAddTextchosen) {
                    doo_endX = event.getX();
                    doo_endY = event.getY();
                }
                if(MainActivity.isAddTextchosen){
                    text_currX=event.getX();
                    text_currY=event.getY();
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(!MainActivity.isAddTextchosen) {
                    doo_endX = event.getX();
                    doo_endY = event.getY();
                }
                invalidate();
                break;

        }
        return true;
    }
}

