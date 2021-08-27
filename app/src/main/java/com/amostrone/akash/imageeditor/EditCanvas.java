package com.amostrone.akash.imageeditor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class EditCanvas extends View {
    Paint paint;
    private float startX;
    private float startY;
    public static String text="";

    public EditCanvas(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(30);
        paint.setTextSize(50);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(text, startX,startY,paint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                startX = event.getX();
                startY = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                startX = event.getX();
                startY = event.getY();
                invalidate();
                break;
        }
        return true;
    }
}

