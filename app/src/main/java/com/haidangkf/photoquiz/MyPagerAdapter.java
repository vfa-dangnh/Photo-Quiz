package com.haidangkf.photoquiz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MyPagerAdapter extends PagerAdapter {

    final String TAG = "my_log";
    ArrayList<String> selectedItems = new ArrayList<>();
    ArrayList<Question> allQuestions = new ArrayList<>();
    ArrayList<Question> myQuestions = new ArrayList<>();
    int numberOfQuestion, numberOfQuestionCopy;
    private String photoPath;
    private String audioPath;
    int index = 0;
    int correctAnswers = 0;

    ImageView imgPhoto;
    ImageButton btnPlay, btnRight, btnWrong;

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) container.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int layout_id = R.layout.layout_activity_do_test;
        View view = layoutInflater.inflate(layout_id, null);

        // ****************************************
        view.setTag(position);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=(Integer) v.getTag();
                Log.i(TAG, "You clicked "+ position);
            }
        });

        imgPhoto = (ImageView) view.findViewById(R.id.imgPhoto);
        btnPlay = (ImageButton) view.findViewById(R.id.btnPlay);
        btnRight = (ImageButton) view.findViewById(R.id.btnRight);
        btnWrong = (ImageButton) view.findViewById(R.id.btnWrong);

        selectedItems = DoTestActivity.selectedItems;
        numberOfQuestion = DoTestActivity.numberOfQuestion;
        Log.i(TAG, "selectedItems = " + selectedItems.toString());
        Log.i(TAG, "numberOfQuestion = " + numberOfQuestion);
        Log.i(TAG, "getItemPosition = " + getItemPosition(this));


        allQuestions = MyApplication.db.getQuestionList();
        for (String category : selectedItems) {
            for (Question question : allQuestions) {
                if (question.getCategory().equalsIgnoreCase(category)) {
                    myQuestions.add(question);
                }
            }
        }

        numberOfQuestionCopy = numberOfQuestion;
//        loadQuestion(++index); // load the first question
        loadQuestion(getItemPosition(this));

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(audioPath);
                Log.i(TAG, "play audio");
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correctAnswers++;
//                loadQuestion(++index);
                Log.i(TAG, "right button");
            }
        });

        btnWrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                loadQuestion(++index);
                Log.i(TAG, "wrong button");
            }
        });

        // ****************************************

        ((ViewPager) container).addView(view, 0);
        return view;
    }

    public void loadQuestion(int index) {
        if (index > numberOfQuestionCopy) {
            Log.i(TAG, "Xong bai test roi.");
//            Toast.makeText(DoTestActivity.this, getString(R.string.msg_finish_test), Toast.LENGTH_SHORT).show();

//            Intent i = new Intent(DoTestActivity.this, ResultActivity.class);
//            i.putExtra("numberOfQuestion", numberOfQuestionCopy);
//            i.putExtra("correctAnswers", correctAnswers);
//            startActivity(i);
//            finish();
        } else {
            Random rand = new Random();
            int n = rand.nextInt(numberOfQuestion);
            Question question = myQuestions.get(n);

            photoPath = question.getPhotoPath();
            audioPath = question.getAudioPath();
            loadPhotoToView(photoPath);

            removeQuestion(n);
        }
    }

    public void removeQuestion(int n){
        myQuestions.remove(n);
        numberOfQuestion = myQuestions.size();
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
//        Toast.makeText(getApplicationContext(), getString(R.string.msg_playing_audio), Toast.LENGTH_LONG).show();
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public int getItemPosition (Object object)
    {
        return DoTestActivity._position;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}