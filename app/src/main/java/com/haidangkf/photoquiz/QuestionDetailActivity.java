package com.haidangkf.photoquiz;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class QuestionDetailActivity extends AppCompatActivity {

    ImageButton btnBack;
    ImageView imgPhoto;
    Button btnPlay;
    TextView tvCategory, tvComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        findViewById();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent i = getIntent();
        Question question = (Question) i.getSerializableExtra("questionObject");
        String photoPath = question.getPhotoPath();
        final String audioPath = question.getAudioPath();
        String category = question.getCategory();
        String comment = question.getComment();
        loadPhotoToView(photoPath);
        tvCategory.setText(getString(R.string.category) + " : " + category);
        tvComment.setText(getString(R.string.question_name) + " : " + comment);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(audioPath);
            }
        });
    }

    public void loadPhotoToView(String path) {
        if (path.startsWith("/storage")) { // file in storage
            File imgFile = new File(path);
            Picasso.with(this).load(imgFile).into(imgPhoto);
        } else { // file in url from Internet
            Picasso.with(this).load(path).into(imgPhoto);
        }
    }

    public void playAudio(String audioPath) {
        MediaPlayer m = new MediaPlayer();

        try {
            m.setDataSource(audioPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            m.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        m.start();
        Toast.makeText(QuestionDetailActivity.this, getString(R.string.msg_playing_audio), Toast.LENGTH_SHORT).show();
    }

    private void findViewById() {
        btnBack = (ImageButton) findViewById(R.id.imgBtnBack);
        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvComment = (TextView) findViewById(R.id.tvComment);
    }
}
