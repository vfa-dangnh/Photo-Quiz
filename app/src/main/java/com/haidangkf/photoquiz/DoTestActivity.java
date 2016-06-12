package com.haidangkf.photoquiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class DoTestActivity extends AppCompatActivity {

    final String TAG = "my_log";
    ArrayList<String> selectedItems = new ArrayList<>();
    ArrayList<Question> allQuestions = new ArrayList<>();
    ArrayList<Question> myQuestions = new ArrayList<>();
    int numberOfQuestion;
    private String photoPath;
    private String audioPath;
    int count = 0;
    int score = 0;

    ImageView imgPhoto;
    ImageButton btnPlay, btnRight, btnWrong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_test);

        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnRight = (ImageButton) findViewById(R.id.btnRight);
        btnWrong = (ImageButton) findViewById(R.id.btnWrong);

        selectedItems = getIntent().getStringArrayListExtra("categoryList");
        numberOfQuestion = getIntent().getIntExtra("numberOfQuestion", 0);
        Log.i(TAG, "selectedItems = " + selectedItems.toString());
        Log.i(TAG, "numberOfQuestion = " + numberOfQuestion);

        allQuestions = MyApplication.db.getQuestionList();
        for (String category : selectedItems) {
            for (Question question : allQuestions) {
                if (question.getCategory().equalsIgnoreCase(category)) {
                    myQuestions.add(question);
                }
            }
        }

        Log.i(TAG, myQuestions.size() + " questions matches.");
        if (numberOfQuestion > myQuestions.size()) {
            showInformDialog();
        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(audioPath);
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score++;
                loadQuestion(++count);
            }
        });

        btnWrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadQuestion(++count);
            }
        });

        loadQuestion(++count); // load the first question

    }

    public void loadQuestion(int id) {
        if (id > numberOfQuestion) {
            Toast.makeText(DoTestActivity.this, "You have finished the test", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(DoTestActivity.this, ResultActivity.class);
            i.putExtra("numberOfQuestion",numberOfQuestion);
            i.putExtra("score",score);
            startActivity(i);
        }

        Random rand = new Random();
        int n = rand.nextInt(numberOfQuestion);
        Question question = myQuestions.get(n);
        photoPath = question.getPhotoPath();
        audioPath = question.getAudioPath();
        loadPhotoToView(photoPath);

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
            final int REQUIRED_SIZE=70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
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
        Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
    }

    public void showInformDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                numberOfQuestion = myQuestions.size();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        String msg = "Not enough question!" + "\n";
        msg += "The maximum number of question matches your selection is " + myQuestions.size() + "\n";
        msg += "Press OK to continue or Cancel to exit !";
        builder.setMessage(msg);
        builder.create().show();
    }

}
