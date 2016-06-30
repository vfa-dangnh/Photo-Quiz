package com.haidangkf.photoquiz;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
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
import java.util.ArrayList;
import java.util.Date;

public class DownloadDatabaseActivity extends AppCompatActivity {

    private static final String TAG = "my_log";
    ArrayList<Question> questionList = new ArrayList<Question>();
    private ProgressDialog mProgressDialog;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_database);

        String url = "http://studiocitync.org/wp-content/uploads/2011/11/stop-dog-barking1.jpg";
        String storageDir = Environment.getExternalStorageDirectory().toString() + "/Photo_TEST";
//        Picasso.with(MainActivity.this).load("file:///android_asset/cat.jpg").into(img);
//        Picasso.with(this).load("http://studiocitync.org/wp-content/uploads/2011/11/stop-dog-barking1.jpg").into(img);
        Picasso.with(this).load(url).into(getTargetToSaveImage(storageDir));

        new DownloadMP3().execute();

        btnBack = (Button) findViewById(R.id.btnBack);
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
                String photoPath = columns.getJSONObject(3).getString("v");
                String audioPath = columns.getJSONObject(4).getString("v");
                Question question = new Question(category, comment, photoPath, audioPath);
                Log.d(TAG, question.toString());
                questionList.add(question);
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
                        Log.i(TAG, "storageDir = " + storageDir);
                        File myDir = new File(storageDir);
                        myDir.mkdirs();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String fileName = "photo_" + timeStamp + ".jpg";
                        File file = new File(storageDir, fileName);
                        Log.d(TAG, "downloaded image = " + file.getAbsolutePath()); // get file path here
                        if (file.exists()) file.delete();

                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();

                            notifyNewMediaFile(file);
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
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.d(TAG, "Scan Completed");
                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

// --------------------------------------------------------------------------

    private class DownloadMP3 extends AsyncTask<String, Integer, String> { //params, progress, result
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(DownloadDatabaseActivity.this,
                    "ProgressDialog",
                    "Wait for downloading audio...");
        }

        @Override
        protected String doInBackground(String... arg0) {
            int count;
            try {
                URL url = new URL("https://s3.amazonaws.com/freesoundeffects/mp3/mp3_1338.mp3");
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show on UI 0-100% progress bar
                int lengthOfFile = connection.getContentLength();

                //////////////////////////////
                File dirPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Audio_TEST");
                dirPath.mkdirs(); // make this as directory

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String audioFileName = "download_" + timeStamp + ".3gp";
                String audioPath = dirPath + "/" + audioFileName;
                Log.i(TAG, "audioPath = " + audioPath);
                File file = new File(audioPath);
                if (file.exists()) file.delete();
                //////////////////////////////

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(audioPath);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int) (total * 100 / lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                notifyNewMediaFile(file);
                return "succeeded";
            } catch (Exception e) {
                e.printStackTrace();
                return "failed";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Audio download result = " + result);
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
        }

    }
}