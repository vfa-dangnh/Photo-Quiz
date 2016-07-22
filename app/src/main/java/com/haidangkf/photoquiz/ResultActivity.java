package com.haidangkf.photoquiz;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private static final String TAG = "my_log";
    TextView tvYourScore;
    LinearLayout btnShare;
    Button btnMainScreen;

    HashMap<Integer, Integer> answerMap;
    int numberOfQuestion;
    int correctAnswers = 0;
    String text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvYourScore = (TextView) findViewById(R.id.tvYourScore);
        btnShare = (LinearLayout) findViewById(R.id.btnShare);
        btnMainScreen = (Button) findViewById(R.id.btnMainScreen);

        Intent intent = getIntent();
        numberOfQuestion = intent.getIntExtra("numberOfQuestion", 0);
        answerMap = (HashMap<Integer, Integer>) intent.getSerializableExtra("answerMap");
        Log.d(TAG, "numberOfQuestion = " + numberOfQuestion);
        Log.d(TAG, "answerMap.size() = " + answerMap.size());

        for (int i = 0; i < numberOfQuestion; i++) {
            if (answerMap.get(i) == 1) {
                correctAnswers++;
            }
        }

        if (numberOfQuestion > 0) {
            String str = String.format("%.0f", correctAnswers * 1.0 / numberOfQuestion * 100);
            text = "Correct answers: " + correctAnswers + "/" + numberOfQuestion +
                    "\nScore in percentage = " + str + "%";
            tvYourScore.setText(text);
        } else {
            text = getString(R.string.msg_something_went_wrong);
            tvYourScore.setText(text);
            Log.d(TAG, "Error: numberOfQuestion = " + numberOfQuestion);
        }

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIntentSpecificApps(text);
            }
        });

        btnMainScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // finish this Activity will Resume MainActivity
            }
        });
    }

    // Share to some specific apps
    // Facebook, Messenger, Twitter, Google Plus, Gmail
    public void shareIntentSpecificApps(String content) {
        List<Intent> intentShareList = new ArrayList<Intent>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(shareIntent, 0);

        for (ResolveInfo resInfo : resolveInfoList) {
            String packageName = resInfo.activityInfo.packageName;
            String name = resInfo.activityInfo.name;

            if (packageName.contains("com.facebook") ||
                    packageName.contains("com.twitter.android") ||
                    packageName.contains("com.google.android.apps.plus") ||
                    packageName.contains("com.google.android.gm")) {

                if (name.contains("com.twitter.android.DMActivity")) {
                    continue;
                }

                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "My score in Photo-Quiz");
                intent.putExtra(Intent.EXTRA_TEXT, content);
                intentShareList.add(intent);
            }
        }

        if (intentShareList.isEmpty()) {
            Toast.makeText(ResultActivity.this, "No apps to share !", Toast.LENGTH_SHORT).show();
        } else {
            Intent chooserIntent = Intent.createChooser(intentShareList.remove(0), "Share via");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentShareList.toArray(new Parcelable[]{}));
            startActivity(chooserIntent);
        }
    }

}
