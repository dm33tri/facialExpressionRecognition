package ru.hse.d_shcherbakov1.torchvisioncameralib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;


public class FaceAnalyzer implements Analyzer {
    private Context context;
    private OverlayView overlayView;
    private FaceDetectorOptions options;
    private FaceDetector detector;
    private Module module;
    private Tensor inputTensor;

    public FaceAnalyzer(Context context, OverlayView overlayView) {
        this.context = context;
        this.overlayView = overlayView;
        this.options = new FaceDetectorOptions.Builder().build();
        this.detector = FaceDetection.getClient(options);
        this.module = Module.load(unpackModel());
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(@NonNull ImageProxy imageProxy) {
        Rect cropRect = imageProxy.getCropRect();
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            mediaImage.setCropRect(cropRect);
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            detector.process(image)
                .addOnSuccessListener((faces) -> {
                    if (faces.size() > 0) {
                        Rect bounds = faces.get(0).getBoundingBox();
                        overlayView.setImageSize(new Size(imageProxy.getHeight(), imageProxy.getWidth()));
                        overlayView.setRect(new RectF(bounds));

                        inputTensor = getTensor(mediaImage, bounds);

                        Tensor output = module.forward(IValue.from(inputTensor)).toTensor();
                        Log.d("res", output.toString());
                    } else {
                        overlayView.setRect(null);
                    }
                })
                .addOnCompleteListener((err) -> {
                    imageProxy.close();
                });
        }
    }

    private Bitmap cropImage(InputImage image, Rect bounds) {
        int width = bounds.width();
        int height = bounds.height();
        Matrix transform = new Matrix();
        transform.setScale(44f / width, 44f / height);
        return Bitmap.createBitmap(image.getBitmapInternal(), bounds.left, bounds.top, width, height, transform, true);
    }

    private Tensor getTensor(Image image, Rect face) {

    }

    private String unpackModel() {
        File file = new File(context.getFilesDir(), "model2.pt");
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open("model2.pt")) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException ignored) { }

        return null;
    }

    public static ImageAnalysis buildAnalysis(Context context, OverlayView overlayView) {
        ImageAnalysis analysis = new ImageAnalysis.Builder().build();
        FaceAnalyzer faceAnalyzer = new FaceAnalyzer(context, overlayView);
        analysis.setAnalyzer(context.getMainExecutor(), faceAnalyzer);
        return analysis;
    }
}
