package com.example.myapplication_1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //화면 설정 (가로/세로)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        //콘텐츠 뷰 설정
        setContentView(R.layout.activity_main);
        //와이파이, 배터리 등 정보 보이지 않게하기
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams);
        //서포트 액션바 숨기기
        getSupportActionBar().hide();
    }
}