package ru.hse.d_shcherbakov1.facialExpressionRecognition;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;
import ru.hse.d_shcherbakov1.torchvisioncameralib.R;

import java.util.concurrent.ExecutionException;

/**
 * Фрагмент для предпросмотра изображения с камеры и анализа эмоций по изображению лица в реальном времени
 */
public class CameraFeedView extends Fragment {
    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis faceAnalyzer;
    private PreviewView previewView;
    private OverlayView overLayView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {

        }
    }

    /**
     * Инициализация визуальных виджетов фрагмента
     */
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.camera_feed, viewGroup, false);
        previewView = view.findViewById(R.id.previewView);
        overLayView = view.findViewById(R.id.overlayView);
        return view;
    }

    /**
     * Завершение создания фрагмента
     */
    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
        requestPermissions();
        initCamera();
    }

    /**
     * Инициализация камеры
     */
    private void initCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFeature = ProcessCameraProvider.getInstance(getActivity());
        cameraProviderFeature.addListener(() -> {
            if (!checkPermissions()) {
                return;
            }
            try {
                cameraProvider = cameraProviderFeature.get();
                if (cameraProvider == null) {
                    return;
                }
                bindUseCases();
            } catch (ExecutionException | InterruptedException ignored) { }
        }, this.getContext().getMainExecutor());
    }

    /**
     * Проверка разрешений на использование камеры
     */
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Запрос разрешений на использование камеры
     */
    private void requestPermissions() {
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.CAMERA }, 1);
        }
    }

    /**
     * Подключение анализатора и предпросмотра к камере
     */
    @SuppressLint("UnsafeOptInUsageError")
    private void bindUseCases() {
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        faceAnalyzer = CameraFeedAnalyzer.buildAnalysis(this);

        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(faceAnalyzer)
                .build();

        cameraProvider.bindToLifecycle(ProcessLifecycleOwner.get(), cameraSelector, useCaseGroup);
    }

    /**
     * Задать размер изображения с камеры
     * @param imageSize размер изображения
     */
    public void setImageSize(Size imageSize) {
        overLayView.setImageSize(imageSize);
    }

    /**
     * Задать прямоугольник с рамкой вокруг лица
     * @param rect прямоугольник
     */
    public void setRect(RectF rect) {
        overLayView.setRect(rect);
    }

    /**
     * Задать надпись
     * @param label надпись
     */
    public void setLabel(String label) {
        overLayView.setLabel(label);
    }

    /**
     * Дебаг-метод для задания обрезанного черно-белого изображения лица
     * Это изображение далее передается в нейросеть
     */
    public void setFaceImage(int[] data) {
        overLayView.setFaceImage(data);
    }

    /**
     * Получить название текущей эмоции
     */
    public String getLabel() {
        return overLayView.getLabel();
    }
}