package ru.hse.dascherbakov_1.facialExpressionRecognition;

import android.graphics.RectF;
import android.util.Size;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FacialExpressionViewModel extends ViewModel {
    /**
     * Надпись с классом, который наиболее вероятен
     */
    private final MutableLiveData<String> label = new MutableLiveData<>("");
    /**
     * Прямоугольник, обозначающий границы лица на изображении
     */
    private final MutableLiveData<RectF> faceRect = new MutableLiveData<>();
    /**
     * Размер обрабатываемого изображения
     */
    private final MutableLiveData<Size> imageSize = new MutableLiveData<>();

    private final MutableLiveData<float[]> output = new MutableLiveData<>();

    /**
     * Записать прямоугольник границ лица
     */
    public void setFaceRect(RectF faceRect) {
        this.faceRect.setValue(faceRect);
    }

    /**
     * Получить прямоугольник границ лица
     */
    public MutableLiveData<RectF> getFaceRect() {
        return faceRect;
    }

    /**
     * Записать название эмоции
     */
    public void setLabel(String label) {
        this.label.setValue(label);
    }

    /**
     * Получить название эмоции
     */
    public LiveData<String> getLabel() {
        return label;
    }

    /**
     * Записать размер изображения
     */
    public void setImageSize(Size imageSize) {
        this.imageSize.setValue(imageSize);
    }

    /**
     * Получить размер изображения
     */
    public LiveData<Size> getImageSize() {
        return imageSize;
    }

    /**
     * Записать вектор с вероятностями эмоций
     */
    public void setOutput(float[] output) {
        this.output.setValue(output);
    }

    /**
     * Получить вектор с вероятностями эмоций
     */
    public LiveData<float[]> getOutput() {
        return output;
    }
}
