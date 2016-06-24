package com.haidangkf.photoquiz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        File imgFile = new File(path);
        if (imgFile.exists()) {
            Bitmap myBitmap = decodeFile(imgFile);
            imgPhoto.setImageBitmap(myBitmap);
        }
    }

    // Decodes image and scales it to reduce memory consumption
    public Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
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
