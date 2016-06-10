package com.haidangkf.photoquiz;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddQuestionActivity extends AppCompatActivity {

    final String TAG = "my_log";
    ImageView btnPhotoTaking;
    EditText etCategory;
    EditText etComment;
    ImageButton btnRecord;
    ImageButton btnDelete;
    Button btnAdd;
    Button btnCancel;

    public static String photoPath = "";
    public static String audioPath = "";

    private MediaRecorder myAudioRecorder;
    private boolean isRecording = false;


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


        getControls();
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        btnDelete.setEnabled(false);

        btnPhotoTaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddQuestionActivity.this, TakePhotoActivity.class));
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isRecording) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String audioFileName = "audio_" + timeStamp + ".3gp";
                    File dirPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Photo_Quiz/Audio/");
                    dirPath.mkdirs(); // make this as directory
                    audioPath = dirPath + "/" + audioFileName;
                    Log.i(TAG, "audioPath = " + audioPath);


                    myAudioRecorder.setOutputFile(audioPath);

                    isRecording = true;
                    btnRecord.setBackgroundResource(R.drawable.record_stop);
                    recordAudio();
                } else {
                    stopRecordAudio();
                    isRecording = false;
                    btnRecord.setBackgroundResource(R.drawable.record);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File audioFile = new File(audioPath);
                if (audioFile.exists()) {
                    audioFile.delete();
                    audioPath = "";
                    Toast.makeText(AddQuestionActivity.this, "The record file has been deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddQuestionActivity.this, "File does not exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = etCategory.getText().toString();
                String comment = etComment.getText().toString();

                if (category.isEmpty() || comment.isEmpty()) {
                    Toast.makeText(AddQuestionActivity.this, "Category and Comment cannot empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (photoPath.isEmpty()) {
                    Toast.makeText(AddQuestionActivity.this, "Please taking a photo", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (audioPath.isEmpty()) {
                    Toast.makeText(AddQuestionActivity.this, "Please record your voice", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume AddQuestion");

        reloadPhotoToView(photoPath);
        Log.i(TAG, "photoPath = " + photoPath);
    }

    public void reloadPhotoToView(String path) {
        File imgFile = new File(path);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            btnPhotoTaking.setBackgroundResource(R.drawable.transparent_background);
            btnPhotoTaking.setImageBitmap(myBitmap);
        }
    }

    public void recordAudio() {
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
            Log.i(TAG, "dang ghi day");
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        btnDelete.setEnabled(false);

        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
    }

    public void stopRecordAudio() {
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder = null;

        btnDelete.setEnabled(true);

        Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
    }

    public void getControls() {
        btnPhotoTaking = (ImageView) findViewById(R.id.btnPhotoTaking);
        etCategory = (EditText) findViewById(R.id.etCategory);
        etComment = (EditText) findViewById(R.id.etComment);
        btnRecord = (ImageButton) findViewById(R.id.btnRecord);
        btnDelete = (ImageButton) findViewById(R.id.btnDelete);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnCancel = (Button) findViewById(R.id.btnCancel);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
