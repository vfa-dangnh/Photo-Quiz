package com.haidangkf.photoquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AddQuestionActivity extends AppCompatActivity {

    ImageView btnPhotoTaking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        LinearLayout parentView = (LinearLayout) findViewById(R.id.parentView);
        parentView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Log.i("focuschange", "hasFocus = " + hasFocus);
                if (hasFocus) {
                    hideSoftKeyboard(AddQuestionActivity.this);
                }
            }
        });

        btnPhotoTaking = (ImageView) findViewById(R.id.btnPhotoTaking);
        btnPhotoTaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddQuestionActivity.this, TakePhotoActivity.class));
            }
        });

    }

    public static void hideSoftKeyboard (Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
