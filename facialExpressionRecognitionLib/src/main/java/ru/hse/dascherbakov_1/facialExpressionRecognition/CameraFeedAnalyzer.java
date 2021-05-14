package ru.hse.dascherbakov_1.facialExpressionRecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

public class CameraFeedAnalyzer implements Analyzer {
    private final CameraFeedView cameraFeedView;
    private final FaceDetector detector;
    private final Module module;

    public CameraFeedAnalyzer(CameraFeedView cameraFeedView) {
        this.cameraFeedView = cameraFeedView;
        FaceDetectorOptions options = new FaceDetectorOptions.Builder().build();
        this.detector = FaceDetection.getClient(options);
        this.module = Module.load(unpackModel());
    }

    private final static String[] classes = { "Angry", "Disgust", "Fear", "Happy", "Sad", "Surprised", "Neutral" };

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(@NonNull ImageProxy imageProxy) {
        Rect cropRect = imageProxy.getCropRect();
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            FacialExpressionViewModel viewModel = cameraFeedView.getViewModel();
            mediaImage.setCropRect(cropRect);
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            detector.process(image)
                .addOnSuccessListener((faces) -> {
                    if (faces.size() > 0) {
                        Rect bounds = getFaceRect(faces.get(0), image.getWidth(), image.getHeight());

                        Tensor inputTensor = getTensor(mediaImage, bounds);
                        Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

                        viewModel.setImageSize(new Size(imageProxy.getHeight(), imageProxy.getWidth()));
                        viewModel.setFaceRect(new RectF(bounds));
                        viewModel.setLabel(getResult(outputTensor));
                    } else {
                        viewModel.setFaceRect(null);
                        viewModel.setLabel(null);
                    }
                })
                .addOnCompleteListener((err) -> {
                    imageProxy.close();
                });
        }
    }

    private Rect getFaceRect(Face face, int width, int height) {
        Rect bounds = face.getBoundingBox();
        bounds.left = Math.max(0, bounds.left - 20);
        bounds.right = Math.min(width, bounds.right + 20);
        bounds.top = Math.max(0, bounds.top - 20);
        bounds.bottom = Math.min(height, bounds.bottom + 20);
        return bounds;
    }

    private String getResult(Tensor outputTensor) {
        float[] arr = outputTensor.getDataAsFloatArray();
        int maxIndex = 0;
        float max = arr[maxIndex];
        for (int i = 1; i < 7; ++i) {
            if (arr[i] > max) {
                maxIndex = i;
                max = arr[i];
            }
        }

        return classes[maxIndex];
    }

    private Tensor getTensor(Image image, Rect face) {
        int[] buffer = getImage(image, face);
        float[] floatBuffer = new float[buffer.length];
        for (int i = 0; i < buffer.length; ++i) {
            floatBuffer[i] = Color.valueOf(buffer[i]).luminance();
        }
        return Tensor.fromBlob(floatBuffer, new long[] { 1, 1, 44, 44 });
    }

    private int[] getImage(Image image, Rect face) {
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        int[] buffer = new int[44 * 44];
        int width = face.width();
        int height = face.height();
        int xStep = width / 44;
        int yStep = height / 44;
        int rowStride = image.getPlanes()[0].getRowStride();
        int colStride = image.getPlanes()[0].getPixelStride();
        for (int x = face.left, targetX = 0; targetX < 44; x += xStep, targetX++) {
            for (int y = face.top, targetY = 0; targetY < 44; y += yStep, targetY++) {
                if (x >= image.getHeight() || y >= image.getHeight()) {
                    buffer[targetY * 44 + targetX] = 0xffffffff;
                } else {
                    int lum = byteBuffer.get(x * rowStride + (image.getWidth() - y) * colStride) & 0xFF;
                    buffer[targetY * 44 + targetX] = 0xff000000 | lum << 16 | lum << 8 | lum;
                }
            }
        }
        return buffer;
    }

    private String unpackModel() {
        File file = new File(cameraFeedView.getContext().getFilesDir(), "model.pt");
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = cameraFeedView.getContext().getAssets().open("model.pt")) {
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

    public static ImageAnalysis buildAnalysis(CameraFeedView cameraFeedView) {
        ImageAnalysis analysis = new ImageAnalysis.Builder().build();
        CameraFeedAnalyzer faceAnalyzer = new CameraFeedAnalyzer(cameraFeedView);
        analysis.setAnalyzer(cameraFeedView.getContext().getMainExecutor(), faceAnalyzer);
        return analysis;
    }
}
