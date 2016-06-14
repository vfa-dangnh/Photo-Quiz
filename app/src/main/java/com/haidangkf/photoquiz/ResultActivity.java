package com.haidangkf.photoquiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    TextView tvYourScore;
    Button btnMainScreen;

    int numberOfQuestion;
    int correctAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvYourScore = (TextView) findViewById(R.id.tvYourScore);
        btnMainScreen = (Button) findViewById(R.id.btnMainScreen);

        numberOfQuestion = getIntent().getIntExtra("numberOfQuestion", 0);
        correctAnswers = getIntent().getIntExtra("correctAnswers", 0);

        if (numberOfQuestion > 0) {
            String str = String.format("%.0f", correctAnswers * 1.0 / numberOfQuestion * 100);
            tvYourScore.setText("Correct answers: " + correctAnswers + "/" + numberOfQuestion +
                    "\nScore in percentage = " + str + "%");
        }

        btnMainScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResultActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
