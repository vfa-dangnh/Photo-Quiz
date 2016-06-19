package com.haidangkf.photoquiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
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

    FrameLayout frameDone;
    ImageView imgPhoto;
    Button btnPlay, btnDone;
    ImageButton btnRight, btnWrong;
    // -------------------------

    // constructor
    public MyPagerAdapter(Activity activity, ArrayList<Question> myTestQuestions) {
        this.activity = activity;
        this.myTestQuestions = myTestQuestions;
        Log.i(TAG, "enter Constructor");
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
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int thisPosition = (int) v.getTag();
//                Log.i(TAG, "You clicked on page " + thisPosition);
//            }
//        });

        // ****************************************
        findViewById(view);

        loadQuestion(position); // load the question at position to screen

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Playing audio at " + position);
                playAudio(myTestQuestions.get(position).getAudioPath());
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Position = " + position + " , Clicked Right");
            }
        });

        btnWrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Position = " + position + " , Clicked Wrong");
            }
        });

        // ****************************************

        container.addView(view);
        return view;
    }

    @Override
    public int getCount() { // number of pages can view
        return myTestQuestions.size() + 1;
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
        frameDone = (FrameLayout) view.findViewById(R.id.frameDone);
        btnDone = (Button) view.findViewById(R.id.btnDone);
        imgPhoto = (ImageView) view.findViewById(R.id.imgPhoto);
        btnPlay = (Button) view.findViewById(R.id.btnPlay);
        btnRight = (ImageButton) view.findViewById(R.id.btnRight);
        btnWrong = (ImageButton) view.findViewById(R.id.btnWrong);
    }

    public void loadQuestion(int position) {
        if (position == getCount() - 1) { // đang ở trang cuối cùng (đã làm xong bài test)
            imgPhoto.setVisibility(View.GONE);
            btnPlay.setVisibility(View.GONE);
            btnRight.setVisibility(View.GONE);
            btnWrong.setVisibility(View.GONE);

            frameDone.setVisibility(View.VISIBLE);
            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, ResultActivity.class);
                    activity.startActivity(i);
                    activity.finish();
                }
            });
            return;
        }

        Question question = myTestQuestions.get(position);
        photoPath = question.getPhotoPath();
        loadPhotoToView(photoPath);
        Log.i(TAG, "comment = " + question.getComment());
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
        Toast.makeText(activity, activity.getString(R.string.msg_playing_audio), Toast.LENGTH_SHORT).show();
    }

}