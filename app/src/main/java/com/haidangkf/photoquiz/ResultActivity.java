package com.haidangkf.photoquiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class ResultActivity extends AppCompatActivity {

    final String TAG = "my_log";
    TextView tvYourScore;
    Button btnMainScreen;

    HashMap<Integer, Integer> answerMap;
    int numberOfQuestion;
    int correctAnswers=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvYourScore = (TextView) findViewById(R.id.tvYourScore);
        btnMainScreen = (Button) findViewById(R.id.btnMainScreen);

        Intent intent = getIntent();
        numberOfQuestion = intent.getIntExtra("numberOfQuestion",0);
        answerMap = (HashMap<Integer, Integer>) intent.getSerializableExtra("answerMap");
        Log.i(TAG, "numberOfQuestion = "+numberOfQuestion);
        Log.i(TAG, "answerMap Size = "+answerMap.size());

        for (int i=0; i<answerMap.size(); i++){
            if (answerMap.get(i)==1){
                correctAnswers++;
            }
        }

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
