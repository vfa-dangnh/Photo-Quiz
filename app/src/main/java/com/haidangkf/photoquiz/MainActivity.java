package com.haidangkf.photoquiz;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageButton imgBtnLang;
    Locale myLocale;
    String currentLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentLang = getString(R.string.lang);
        // set font for the title of the app
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/comic.ttf");
        tvTitle.setTypeface(face);

        imgBtnLang = (ImageButton) findViewById(R.id.imgBtnLang);

        if (currentLang.equalsIgnoreCase("English")) {
            imgBtnLang.setBackgroundResource(R.drawable.flag_vi);
        } else {
            imgBtnLang.setBackgroundResource(R.drawable.flag_en);
        }

        imgBtnLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("lang", "" + currentLang);

                if (currentLang.equalsIgnoreCase("English")) {
                    setLocale("vi");
                    Toast.makeText(MainActivity.this, "Tiếng Việt", Toast.LENGTH_SHORT).show();
                } else {
                    setLocale("en");
                    Toast.makeText(MainActivity.this, "English", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void addQuestion(View v){
        startActivity(new Intent(MainActivity.this, AddQuestionActivity.class));
    }

    public void createTest(View v){
        startActivity(new Intent(MainActivity.this, CreateTestActivity.class));
    }

    public void viewQuestion(View v){
        startActivity(new Intent(MainActivity.this, ViewQuestionActivity.class));
    }

    // method to change Language
    public void setLocale(String lang) {
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
        finish();
    }

}
