package ru.hse.d_shcherbakov1.facialExpressionRecognition;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;

/**
 * Виджет для отображения рамки вокруг лица и надписи с названием текущей эмоции
 */
class OverlayView extends View {
    /**
     * Объект синхронизации
     */
    private final Object lock = new Object();
    /**
     * Стиль для рамки
     */
    private final Paint paint;
    /**
     * Стиль для текста
     */
    private final Paint textPaint;
    /**
     * Размер изображения
     */
    private Size imageSize;
    /**
     * Прямоугольник лица
     */
    private RectF rect;
    /**
     * Смещение по оси X для картинки, вытянутой по вертикали
     */
    private float xOffset;
    /**
     * Коэффициент масштабирования изображения
     */
    private float scale;
    /**
     * Надпись с названием эмоции
     */
    private String label;

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

    /**
     * Задать размер изображения с камеры
     * @param imageSize размер изображения
     */
    public void setImageSize(Size imageSize) {
        synchronized (lock) {
            this.imageSize = imageSize;
            updateTransform();
        }
        postInvalidate();
    }

    /**
     * Задать прямоугольник с рамкой вокруг лица
     * @param rect прямоугольник
     */
    public void setRect(RectF rect) {
        synchronized (lock) {
            this.rect = rect != null ? new RectF(
                    translateX(rect.left),
                    translateY(rect.top),
                    translateX(rect.right),
                    translateY(rect.bottom)
            ) : null;
        }
        postInvalidate();
    }

    /**
     * Задать надпись
     * @param label надпись
     */
    public void setLabel(String label) {
        synchronized (lock) {
            this.label = label;
        }
        postInvalidate();
    }

    /**
     * Получить текущую надпись
     */
    public String getLabel() {
        return label;
    }

    /**
     * Рассчет смещения и растяжения для перевода координат из
     * пространства изображения камеры в пространство виджета предпросмотра
     */
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

    /**
     * Перевод координаты X из пространства изображения камеры в пространство виджета предпросмотра
     */
    private float translateX(float x) {
        return getWidth() - (scale * x - xOffset);
    }

    /**
     * Перевод координаты Y из пространства изображения камеры в пространство виджета предпросмотра
     */
    private float translateY(float y) {
        return scale * y;
    }

    /**
     * Метод рисования прямоугольника
     */
    private void drawRect(Canvas canvas) {
        if (this.rect != null) {
            canvas.drawRoundRect(this.rect, 16, 16, paint);
        }
    }

    /**
     * Метод рисования надписи
     */
    private void drawLabel(Canvas canvas) {
        if (this.label != null && this.rect != null) {
            canvas.drawText(this.label, this.rect.right, this.rect.top - 20, textPaint);
        }
    }

    /**
     * Метод жизненного цикла для рисования виджета
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (lock) {
            drawRect(canvas);
            drawLabel(canvas);
            drawFaceImage(canvas);
        }
    }

    private int[] faceImage;

    public void setFaceImage(int[] data) {
        synchronized (lock) {
            this.faceImage = data;
        }
        postInvalidate();
    }

    private void drawFaceImage(Canvas canvas) {
        if (this.faceImage != null) {
            Bitmap bitmap = Bitmap.createBitmap(this.faceImage, 44, 44, Bitmap.Config.ARGB_8888);
            canvas.drawBitmap(bitmap, null, new RectF(getWidth() - 500, 100, getWidth() - 100, 500), paint);
        }
    }
}