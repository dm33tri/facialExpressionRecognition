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

/**
 * Класс анализатора изображения
 */
public class CameraFeedAnalyzer implements Analyzer {
    public final static String[] classes = { "Angry", "Disgust", "Fear", "Happy", "Sad", "Surprised", "Neutral" };

    /**
     * Визуальный виджет для передачи информации
     */
    private final CameraFeedView cameraFeedView;
    /**
     * Определитель лица
     */
    private final FaceDetector detector;
    /**
     * Загруженная TorchScript модель
     */
    private final Module module;

    public CameraFeedAnalyzer(CameraFeedView cameraFeedView) {
        this.cameraFeedView = cameraFeedView;
        FaceDetectorOptions options = new FaceDetectorOptions.Builder().build();
        this.detector = FaceDetection.getClient(options);
        this.module = Module.load(unpackModel());
    }

    /**
     * Основной метод для анализа
     */
    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(@NonNull ImageProxy imageProxy) {
        Rect cropRect = imageProxy.getCropRect();
        Image mediaImage = imageProxy.getImage();
        FacialExpressionViewModel viewModel = cameraFeedView.getViewModel();

        if (mediaImage != null) {
            viewModel.setImageSize(new Size(imageProxy.getHeight(), imageProxy.getWidth()));
            mediaImage.setCropRect(cropRect);
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            detector.process(image)
                .addOnSuccessListener((faces) -> {
                    if (faces.size() > 0) {
                        Rect bounds = getFaceRect(faces.get(0), image.getWidth(), image.getHeight());

                        Tensor inputTensor = getTensor(mediaImage, bounds);
                        Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
                        float[] output = getResult(outputTensor);

                        viewModel.setFaceRect(new RectF(bounds));
                        viewModel.setOutput(output);
                        viewModel.setLabel(getStringResult(output));
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

    /**
     * Получение прямоугольника лица
     */
    private Rect getFaceRect(Face face, int width, int height) {
        Rect bounds = face.getBoundingBox();
        bounds.left = Math.max(0, bounds.left - 10);
        bounds.right = Math.min(width, bounds.right + 10);
        bounds.top = Math.max(0, bounds.top - 10);
        bounds.bottom = Math.min(height, bounds.bottom + 10);
        return bounds;
    }

    /**
     * Получение наиболее вероятного класса
     */
    private String getStringResult(float[] output) {
        int maxIndex = 0;
        float max = output[maxIndex];
        for (int i = 1; i < 7; ++i) {
            if (output[i] >= max) {
                max = output[i];
                maxIndex = i;
            }
        }

        return classes[maxIndex];
    }

    /**
     * Получение вероятностей классов
     */
    private float[] getResult(Tensor outputTensor) {
        return outputTensor.getDataAsFloatArray();
    }

    /**
     * Перевод картинки в тензор
     */
    private Tensor getTensor(Image image, Rect face) {
        int[] buffer = getImage(image, face);
        float[] floatBuffer = new float[buffer.length];
        for (int i = 0; i < buffer.length; ++i) {
            floatBuffer[i] = Color.valueOf(buffer[i]).luminance();
        }
        return Tensor.fromBlob(floatBuffer, new long[] { 1, 1, 44, 44 });
    }

    /**
     * Перевод YUV_420_888 в набор пикселей RGBA_888
     */
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

    /**
     * Извлечение файла модели
     */
    private String unpackModel() {
        File file = new File(cameraFeedView.getContext().getFilesDir(), "model.pt");

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

    /**
     * Получить анализатор для подключения к камере
     */
    public static ImageAnalysis buildAnalysis(CameraFeedView cameraFeedView) {
        int width = cameraFeedView.getView().getWidth();
        int height = cameraFeedView.getView().getHeight();
        ImageAnalysis analysis = new ImageAnalysis.Builder().setTargetResolution(new Size(width, height)).build();
        CameraFeedAnalyzer faceAnalyzer = new CameraFeedAnalyzer(cameraFeedView);
        analysis.setAnalyzer(cameraFeedView.getContext().getMainExecutor(), faceAnalyzer);
        return analysis;
    }
}
