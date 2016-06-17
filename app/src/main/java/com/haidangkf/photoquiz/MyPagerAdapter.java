package com.haidangkf.photoquiz;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MyPagerAdapter extends PagerAdapter {

    final String TAG = "my_log";
    private Activity activity;
    private LayoutInflater layoutInflater;
    ArrayList<Question> myTestQuestions;
    // -------------------------

    private String photoPath = "";
    private String audioPath = "";

    ImageView imgPhoto;
    Button btnPlay;
    ImageButton btnRight, btnWrong;
    // -------------------------

    // constructor
    public MyPagerAdapter(Activity activity, ArrayList<Question> myTestQuestions) {
        this.activity = activity;
        this.myTestQuestions = myTestQuestions;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        Log.i(TAG, "instantiate Item " + position);
//        LayoutInflater layoutInflater = (LayoutInflater) container.getContext()
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int layout_id = R.layout.layout_activity_do_test;
        final View view = layoutInflater.inflate(layout_id, null);

        view.setTag(position);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int thisPosition = (int) v.getTag();
                Log.i(TAG, "You clicked on page " + thisPosition);
                loadQuestion(view, position);
            }
        });

        // ****************************************
        findViewById(view);

        loadQuestion(view, position); // load the question at position to screen

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(audioPath);
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "Clicked Right", Toast.LENGTH_SHORT).show();
            }
        });

        btnWrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "Clicked Wrong", Toast.LENGTH_SHORT).show();
            }
        });

        // ****************************************

        container.addView(view);
        return view;
    }

    @Override
    public int getItemPosition(Object object){
        return POSITION_NONE;
    }

    @Override
    public int getCount() { // number of pages can view
        return myTestQuestions.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.i(TAG, "destroy Item " + position);
        container.removeView((View) object);
    }

    public void findViewById(View view) {
        btnPlay = (Button) view.findViewById(R.id.btnPlay);
        btnRight = (ImageButton) view.findViewById(R.id.btnRight);
        btnWrong = (ImageButton) view.findViewById(R.id.btnWrong);
    }

    public void loadQuestion(View view, int position) {
        Question question = myTestQuestions.get(position);
        photoPath = question.getPhotoPath();
        audioPath = question.getAudioPath();
        loadPhotoToView(view, photoPath);
//        playAudio(audioPath); // thử cái này xem sao
    }

    public void loadPhotoToView(View view, String path) {
        imgPhoto = (ImageView) view.findViewById(R.id.imgPhoto);
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
        Toast.makeText(activity, activity.getString(R.string.msg_playing_audio), Toast.LENGTH_SHORT).show();
    }

}