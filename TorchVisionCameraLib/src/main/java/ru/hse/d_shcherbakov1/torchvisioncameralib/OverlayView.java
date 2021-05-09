package ru.hse.d_shcherbakov1.torchvisioncameralib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;

public class OverlayView extends View {
    private final Object lock = new Object();
    private final Paint paint;
    private final Paint textPaint;
    private Size imageSize;
    private RectF rect;
    private float xOffset;
    private float scale;
    private String label;
    private int[] croppedFace;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(48);
    }

    public void setImageSize(Size imageSize) {
        synchronized (lock) {
            this.imageSize = imageSize;
        }
        postInvalidate();
    }

    public void setRect(RectF rect) {
        synchronized (lock) {
            this.rect = rect;
        }
        postInvalidate();
    }

    public void setLabel(String label) {
        synchronized (lock) {
            this.label = label;
        }
        postInvalidate();
    }

    public void setCroppedFace(int[] data) {
        synchronized (lock) {
            this.croppedFace = data;
        }
        postInvalidate();
    }

    private void updateTransform() {
        if (imageSize == null) {
            return;
        }
        int width = imageSize.getWidth();
        int height = imageSize.getHeight();
        float imageAspectRatio = (float) width / height;
        scale = (float) getHeight() / height;
        xOffset = ((float) getHeight() * imageAspectRatio - getWidth()) / 2;
    }

    private float translateX(float x) {
        return getWidth() - (scale * x - xOffset);
    }

    private float translateY(float y) {
        return scale * y;
    }

    private void drawRect(Canvas canvas) {
        if (this.rect != null) {
            float left = translateX(rect.left);
            float right = translateX(rect.right);
            float top = translateY(rect.top);
            float bottom = translateY(rect.bottom);
            canvas.drawRoundRect(left, top, right, bottom, 16, 16, paint);
        }
    }

    private void drawLabel(Canvas canvas) {
        if (this.label != null && this.rect != null) {
            canvas.drawText(this.label, 50, 50, textPaint);
        }
    }

    private void drawFace(Canvas canvas) {
        if (this.croppedFace != null) {
            Bitmap bitmap = Bitmap.createBitmap(this.croppedFace, 44, 44, Bitmap.Config.ARGB_8888);
            canvas.drawBitmap(bitmap, null, new RectF(getWidth() - 500, 100, getWidth() - 100, 500), paint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (lock) {
            updateTransform();
            drawRect(canvas);
            drawLabel(canvas);
            drawFace(canvas);
        }
    }
}