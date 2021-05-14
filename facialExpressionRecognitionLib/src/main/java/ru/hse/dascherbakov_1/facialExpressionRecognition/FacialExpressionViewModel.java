package ru.hse.dascherbakov_1.facialExpressionRecognition;

import android.graphics.RectF;
import android.util.Size;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FacialExpressionViewModel extends ViewModel {
    private final MutableLiveData<String> label = new MutableLiveData<>("");
    private final MutableLiveData<RectF> faceRect = new MutableLiveData<>();
    private final MutableLiveData<Size> imageSize = new MutableLiveData<>();

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
}
