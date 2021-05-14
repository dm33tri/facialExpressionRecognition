package ru.hse.dascherbakov_1.facialExpressionRecognition;

import android.os.Bundle;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView);
        FacialExpressionViewModel model = new ViewModelProvider(this).get(FacialExpressionViewModel.class);
        model.getLabel().observe(this, textView::setText);
    }
}