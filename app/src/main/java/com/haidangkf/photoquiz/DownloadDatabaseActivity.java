package com.haidangkf.photoquiz;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadDatabaseActivity extends AppCompatActivity {

    private static final String TAG = "my_log";
    private ProgressDialog mProgressDialog;
    private String photoPath = "";
    private String audioPath = "";
    private String audioUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_database);

        Button btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void downloadEvent(View view) {
        if (!isNetworkAvailable()) {
            Toast.makeText(DownloadDatabaseActivity.this, "No Internet connection !", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog();
        new DownloadWebpageTask(new AsyncResult() {
            @Override
            public void onResult(JSONObject object) {
                processJson(object);
                hideProgressDialog();
            }
        }).execute("https://spreadsheets.google.com/tq?key=1ogeXtGLE3vb0mM74ZSLIuQJKNgtJij6ZrGqbUdz9kgQ");
    }

    private void processJson(JSONObject object) {
        try {
            JSONArray rows = object.getJSONArray("rows");
            Log.d(TAG, "rows.length() = " + rows.length());

            for (int r = 0; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");

                String category = columns.getJSONObject(1).getString("v");
                String comment = columns.getJSONObject(2).getString("v");
                String photoUrl = columns.getJSONObject(3).getString("v");
                audioUrl = columns.getJSONObject(4).getString("v");

                String storageDir = Environment.getExternalStorageDirectory().toString() + "/Photo_Quiz/Photos";
                Picasso.with(this).load(photoUrl).into(getTargetToSaveImage(storageDir)); // download image
                if (photoPath.isEmpty()) { // failed to download image from Internet
//                    continue; // go to next row
                }

                new DownloadAudioFile().execute(); // download audio
                if (audioPath.isEmpty()) { // failed to download audio from Internet
//                    continue;
                }

                Question question = new Question(category.trim(), comment.trim(), photoPath, audioPath);
                MyApplication.db.addQuestion(question);
                Log.d(TAG, question.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Target getTargetToSaveImage(final String storageDir) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(TAG, "Bitmap loaded successful");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File myDir = new File(storageDir);
                        myDir.mkdirs();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String fileName = "photo_" + timeStamp + ".jpg";
                        File file = new File(storageDir, fileName);
                        if (file.exists()) file.delete();

                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();

                            notifyNewMediaFile(file);
                            photoPath = file.getAbsolutePath(); // get file path here
                            Log.d(TAG, "Image saved to storage !");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Failed to save image to storage !");
                        }
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d(TAG, "Failed to download image");
                photoPath = "";
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.d(TAG, "onPrepareLoad");
            }
        };

        return target;
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("");
            mProgressDialog.setMessage("Loading…");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void notifyNewMediaFile(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

// --------------------------------------------------------------------------
// for downloading audio

    private class DownloadAudioFile extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                URL url = new URL(audioUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Photo_Quiz/Audio/");
                storageDir.mkdirs(); // make this as directory

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String audioFileName = "audio_" + timeStamp + ".3gp";
                audioPath = storageDir + "/" + audioFileName;
                File file = new File(audioPath);
                if (file.exists()) file.delete();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(audioPath);
                byte data[] = new byte[1024];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                notifyNewMediaFile(file);
                return "succeeded";
            } catch (Exception e) {
                e.printStackTrace();
                audioPath = "";
                return "failed";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Audio download result = " + result);
        }
    }

}