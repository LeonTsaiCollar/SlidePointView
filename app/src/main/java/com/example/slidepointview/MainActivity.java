package com.example.slidepointview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.leon.slidepointview.SlidePointView;

public class MainActivity extends AppCompatActivity {

    SlidePointView slidePointView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slidePointView=findViewById(R.id.slidePoint);
        slidePointView.setOnAngleListener(new SlidePointView.OnAngleListener() {
            @Override
            public void onAngle(int angle) {
                Log.d("cyl","angle:"+angle);
            }
        });
    }
}