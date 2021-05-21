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
    /**
     * Вектор с вероятностями эмоций
     */
    private final MutableLiveData<float[]> output = new MutableLiveData<>();

    public void setFaceRect(RectF faceRect) {
        this.faceRect.setValue(faceRect);
    }

    public MutableLiveData<RectF> getFaceRect() {
        return faceRect;
    }

    public void setLabel(String label) {
        this.label.setValue(label);
    }

    public LiveData<String> getLabel() {
        return label;
    }

    public void setImageSize(Size imageSize) {
        this.imageSize.setValue(imageSize);
    }

    public LiveData<Size> getImageSize() {
        return imageSize;
    }

    public void setOutput(float[] output) {
        this.output.setValue(output);
    }

    public LiveData<float[]> getOutput() {
        return output;
    }
}
