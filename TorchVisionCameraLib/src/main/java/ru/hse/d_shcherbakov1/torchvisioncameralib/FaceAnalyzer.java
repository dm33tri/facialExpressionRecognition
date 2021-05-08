package ru.hse.d_shcherbakov1.torchvisioncameralib;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.concurrent.Executor;

public class FaceAnalyzer implements Analyzer {
    private Analyzer baseAnalyzer;
    private OverlayView overlayView;
    private FaceDetectorOptions options;
    private FaceDetector detector;

    @SuppressLint("RestrictedApi")
    public FaceAnalyzer(Analyzer analyzer, OverlayView overlayView) {
        this.baseAnalyzer = analyzer;
        this.overlayView = overlayView;
        this.options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build();
        detector = FaceDetection.getClient(options);
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(@NonNull ImageProxy imageProxy) {
        Rect cropRect = imageProxy.getCropRect();
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            mediaImage.setCropRect(cropRect);
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            overlayView.setImageSourceInfo(imageProxy.getWidth(), imageProxy.getHeight());
            detector.process(image)
                .addOnSuccessListener((faces) -> {
                    if (faces.size() > 0) {
                        Rect bounds = faces.get(0).getBoundingBox();
                        overlayView.setRect(new RectF(bounds));
                    }
                })
                .addOnCompleteListener((err) -> {
                    imageProxy.close();
                });
        }
    }

    public static ImageAnalysis buildAnalysis(Executor executor, Analyzer analyzer, OverlayView overlayView) {
        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        FaceAnalyzer faceAnalyzer = new FaceAnalyzer(analyzer, overlayView);
        analysis.setAnalyzer(executor, faceAnalyzer);

        return analysis;
    }
}
