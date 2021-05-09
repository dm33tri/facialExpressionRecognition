package ru.hse.d_shcherbakov1.TorchVisionCamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import ru.hse.d_shcherbakov1.torchvisioncameralib.FaceAnalyzer;
import ru.hse.d_shcherbakov1.torchvisioncameralib.OverlayView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    PreviewView previewView;
    CameraSelector cameraSelector;
    ProcessCameraProvider cameraProvider;
    Preview preview;
    ImageAnalysis faceAnalyzer;
    OverlayView overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.preview_view);
        overlayView = findViewById(R.id.overlay_view);

        requestPermissions();
        initCamera();
    }

    protected void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, 1);
        }
    }

    protected void initCamera() {
        cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        ListenableFuture<ProcessCameraProvider> cameraProviderFeature = ProcessCameraProvider.getInstance(this);
        cameraProviderFeature.addListener(() -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            try {
                cameraProvider = cameraProviderFeature.get();

                if (cameraProvider == null) {
                    return;
                }

                bindUseCases();
            } catch (ExecutionException | InterruptedException ignored) { }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    protected void bindUseCases() {
        preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        faceAnalyzer = FaceAnalyzer.buildAnalysis(this, overlayView);

        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(faceAnalyzer)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup);
    }
}