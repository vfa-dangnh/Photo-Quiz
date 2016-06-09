package com.haidangkf.photoquiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void addQuestion(View v){
        startActivity(new Intent(MainActivity.this, AddQuestionActivity.class));
    }

    public void createTest(View v){
        startActivity(new Intent(MainActivity.this, CreateTestActivity.class));
    }

    public void doTest(View v){
        startActivity(new Intent(MainActivity.this, DoTestActivity.class));
    }

    public void seeResult(View v){
        startActivity(new Intent(MainActivity.this, ResultActivity.class));
    }
}
