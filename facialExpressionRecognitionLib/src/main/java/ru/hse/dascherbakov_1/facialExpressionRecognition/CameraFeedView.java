package ru.hse.dascherbakov_1.facialExpressionRecognition;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;
import ru.hse.dascherbakov_1.torchvisioncameralib.R;

import java.util.concurrent.ExecutionException;

/**
 * Фрагмент для предпросмотра изображения с камеры и анализа эмоций по изображению лица в реальном времени
 */
public class CameraFeedView extends Fragment {
    /**
     * Объект для работы с камерой телефона
     */
    private ProcessCameraProvider cameraProvider;
    /**
     * Анализатор изображения
     */
    private ImageAnalysis faceAnalyzer;
    /**
     * Виджет для отображения картинки с камеры
     */
    private PreviewView previewView;
    /**
     * Виджет для помещения дополнительной информации поверх картинки с камеры
     */
    private OverlayView overlayView;
    /**
     * ViewModel для чтения данных, полученных из анализатора
     */
    private FacialExpressionViewModel viewModel;

    /**
     * Получение класса ViewModel
     */
    public FacialExpressionViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Метод, выполняемый после инициализации визуальных виджетов фрагмента
     */
    @Override
    public void onViewCreated(@NotNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        viewModel = new ViewModelProvider(requireActivity()).get(FacialExpressionViewModel.class);
        viewModel.getFaceRect().observe(getViewLifecycleOwner(), overlayView::setRect);
        viewModel.getLabel().observe(getViewLifecycleOwner(), overlayView::setLabel);
        viewModel.getImageSize().observe(getViewLifecycleOwner(), overlayView::setImageSize);
    }

    /**
     * Инициализация визуальных виджетов фрагмента
     */
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.camera_feed, viewGroup, false);
        previewView = view.findViewById(R.id.previewView);
        overlayView = view.findViewById(R.id.overlayView);
        return view;
    }

    /**
     * Завершение создания фрагмента
     */
    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
        initCamera();
    }

    /**
     * Инициализация камеры
     */
    private void initCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFeature = ProcessCameraProvider.getInstance(getActivity());
        cameraProviderFeature.addListener(() -> {
            if (!checkPermissions()) {
                requestPermissions();
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

    public void setOnClickListener(View.OnClickListener listener) {
        overlayView.setOnClickListener(listener);
    }
}