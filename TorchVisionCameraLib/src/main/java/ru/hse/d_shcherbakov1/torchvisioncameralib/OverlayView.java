package ru.hse.d_shcherbakov1.torchvisioncameralib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;

public class OverlayView extends View {
    private final Object lock = new Object();
    private final Matrix transformationMatrix = new Matrix();
    private final Paint paint;
    private Size imageSize;
    private RectF rect;
    private boolean needUpdateTransformation = true;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        addOnLayoutChangeListener(
                (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                        needUpdateTransformation = true);
    }

    public void setImageSourceInfo(int imageWidth, int imageHeight) {
        synchronized (lock) {
            this.imageSize = new Size(imageWidth, imageHeight);
            needUpdateTransformation = true;
        }
        postInvalidate();
    }

    public void setRect(RectF rect) {
        synchronized (lock) {
            this.rect = rect;
            needUpdateTransformation = true;
        }
        postInvalidate();
    }

    private void updateTransformationIfNeeded() {
        int width = 0;
        int height = 0;
        if (imageSize != null) {
            width = imageSize.getWidth();
            height = imageSize.getHeight();
        }
        if (!needUpdateTransformation || width <= 0 || height <= 0) {
            return;
        }
        float viewAspectRatio = (float) getWidth() / getHeight();
        float imageAspectRatio = (float) width / height;
        float postScaleWidthOffset = 0;
        float postScaleHeightOffset = 0;
        float scaleFactor;
        if (viewAspectRatio > imageAspectRatio) {
            scaleFactor = (float) getWidth() / width;
            postScaleHeightOffset = ((float) getWidth() / imageAspectRatio - getHeight()) / 2;
        } else {
            scaleFactor = (float) getHeight() / height;
            postScaleWidthOffset = ((float) getHeight() * imageAspectRatio - getWidth()) / 2;
        }
        transformationMatrix.reset();
        transformationMatrix.setScale(scaleFactor, scaleFactor);
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset);
        if (this.rect != null) {
            transformationMatrix.mapRect(this.rect);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (lock) {
            updateTransformationIfNeeded();
            if (this.rect != null) {
                canvas.drawRect(this.rect, paint);
            }
        }
    }
}