package com.haidangkf.photoquiz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AddQuestionActivity extends AppCompatActivity {

    final String TAG = "my_log";
    ImageView btnPhotoTaking;
    Spinner spinnerCategory;
    EditText etComment;
    Button btnRecord, btnDelete, btnAdd, btnCancel, btnAddNewCategory;

    public static String photoPath = "";
    public static String audioPath = "";
    File dirPath;
    ArrayList<String> sampleCategories = new ArrayList<>();
    ArrayAdapter<String> adapterSpinner;
    String category = "";

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
                if (hasFocus) {
                    hideSoftKeyboard(AddQuestionActivity.this);
                }
            }
        });

        getControls();
        sampleCategories = readSharedPreferences(); // read from Preferences

        adapterSpinner = new ArrayAdapter<String>(AddQuestionActivity.this, android.R.layout.simple_spinner_item, sampleCategories);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterSpinner);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = sampleCategories.get(position);
//                Toast.makeText(AddQuestionActivity.this, getString(R.string.msg_category_is) + " " + category, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        dirPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Photo_Quiz/Audio/");
        dirPath.mkdirs(); // make this as directory

        btnDelete.setEnabled(false); // disable Delete button

        btnAddNewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddQuestionActivity.this, AddMoreCategoryActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        btnPhotoTaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nếu đã tồn tại file ảnh rồi thì xoá nó
                File file = new File(photoPath);
                if (file.exists()) {
                    file.delete();
                }
                photoPath = "";
                startActivity(new Intent(AddQuestionActivity.this, TakePhotoActivity.class));
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isRecording) {
                    // Nếu đã tồn tại file ghi âm rồi thì xoá nó
                    File file = new File(audioPath);
                    if (file.exists()) {
                        file.delete();
                    }

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String audioFileName = "audio_" + timeStamp + ".3gp";
                    audioPath = dirPath + "/" + audioFileName;
                    Log.i(TAG, "audioPath = " + audioPath);

                    isRecording = true;
                    btnRecord.setBackgroundResource(R.drawable.custom_btn_sandrift);
                    btnRecord.setText(getString(R.string.stop));
                    recordAudio(); // call this method to record
                } else {
                    stopRecordAudio();
                    isRecording = false;
                    btnRecord.setBackgroundResource(R.drawable.custom_btn_3);
                    btnRecord.setText(getString(R.string.record));
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
                    Toast.makeText(AddQuestionActivity.this, getString(R.string.msg_record_deleted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddQuestionActivity.this, getString(R.string.msg_file_not_exists), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = etComment.getText().toString();

                if (category.isEmpty() || comment.isEmpty()) {
                    Toast.makeText(AddQuestionActivity.this, getString(R.string.msg_category_question_not_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (photoPath.isEmpty()) {
                    Toast.makeText(AddQuestionActivity.this, getString(R.string.msg_take_photo), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (audioPath.isEmpty()) {
                    Toast.makeText(AddQuestionActivity.this, getString(R.string.msg_record_voice), Toast.LENGTH_SHORT).show();
                    return;
                }

                Question question = new Question(category.trim(), comment.trim(), photoPath, audioPath);
                if (MyApplication.db.addQuestion(question) > 0) {
                    Toast.makeText(AddQuestionActivity.this, getString(R.string.msg_ques_add_success), Toast.LENGTH_SHORT).show();
                    btnDelete.setEnabled(false); // disable Delete button
                    btnPhotoTaking.setBackgroundResource(R.drawable.takingphoto);
                    Bitmap transparentBitmap = BitmapFactory.decodeResource(AddQuestionActivity.this.getResources(),
                            R.drawable.transparent_background);
                    btnPhotoTaking.setImageBitmap(transparentBitmap);
                    etComment.setText("");
                    photoPath = "";
                    audioPath = "";
                } else {
                    Toast.makeText(AddQuestionActivity.this, getString(R.string.msg_ques_add_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(audioPath);
                if (file.exists()) {
                    file.delete();
                }

                file = new File(photoPath);
                if (file.exists()) {
                    file.delete();
                }

                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Nếu gói dữ liệu có mã nhận là 2 (add more category)
        if (resultCode == 2) {
            Bundle bundle = data.getBundleExtra("DATA_CATEGORY");
            sampleCategories.add(bundle.getString("newCategory"));
            saveSharedPreferences(sampleCategories);
            adapterSpinner.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume AddQuestionActivity");

        reloadPhotoToView(photoPath);
        Log.i(TAG, "photoPath = " + photoPath);
    }

//    public void AddSampleCategories() {
//        sampleCategories.add("Object");
//        sampleCategories.add("Human");
//        sampleCategories.add("Animal");
//        sampleCategories.add("Scenery");
//    }

    public void reloadPhotoToView(String path) {
        File imgFile = new File(path);
        if (imgFile.exists()) {
            Bitmap myBitmap = decodeFile(imgFile);
            btnPhotoTaking.setBackgroundResource(R.drawable.transparent_background);
            btnPhotoTaking.setImageBitmap(myBitmap);
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

    public void recordAudio() {
        try {
            myAudioRecorder = new MediaRecorder();
            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            myAudioRecorder.setOutputFile(audioPath);

            myAudioRecorder.prepare();
            myAudioRecorder.start();
            Log.i(TAG, "is recording...");
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        btnDelete.setEnabled(false);

        Toast.makeText(getApplicationContext(), getString(R.string.msg_is_recording), Toast.LENGTH_LONG).show();
    }

    public void stopRecordAudio() {
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder = null;

        btnDelete.setEnabled(true);

        Toast.makeText(getApplicationContext(), getString(R.string.msg_record_success), Toast.LENGTH_LONG).show();
    }

    public void saveSharedPreferences(ArrayList<String> categoriesList) {
        SharedPreferences pre = getSharedPreferences("my_categories", MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        Set<String> set = new HashSet<>();
        for (String s : categoriesList) {
            set.add(s);
        }
        // lưu Set<String> vào key=categories
        editor.putStringSet("categories", set);
        editor.commit();
    }

    public ArrayList<String> readSharedPreferences() {
        SharedPreferences pre = getSharedPreferences("my_categories", MODE_PRIVATE);

        Set<String> newSet = new HashSet<>();
        newSet.add("Object");
        newSet.add("Animal");
        newSet.add("Human");
        newSet.add("Scenery");

// lấy Set lưu trong key=categories, không có thì gán giá trị mặc định là newSet
        Set<String> set = pre.getStringSet("categories", newSet);
        ArrayList<String> list = new ArrayList<>(set);
        return list;
    }

    public void getControls() {
        btnPhotoTaking = (ImageView) findViewById(R.id.btnPhotoTaking);
        spinnerCategory = (Spinner) findViewById(R.id.spinnerCategory);
        etComment = (EditText) findViewById(R.id.etComment);
        btnAddNewCategory = (Button) findViewById(R.id.btnAddNewCategory);
        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnCancel = (Button) findViewById(R.id.btnCancel);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
