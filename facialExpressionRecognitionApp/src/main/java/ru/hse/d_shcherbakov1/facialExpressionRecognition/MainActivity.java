package ru.hse.d_shcherbakov1.facialExpressionRecognition;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public CameraFeedView getCameraFeedView() {
        return cameraFeedView;
    }

    public void setCameraFeedView(CameraFeedView cameraFeedView) {
        this.cameraFeedView = cameraFeedView;
    }

    CameraFeedView cameraFeedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}