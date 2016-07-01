package com.haidangkf.photoquiz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadDatabaseTask extends AsyncTask<String, Void, String> {
    private Context context;
    AsyncResult callback;
    private static final String TAG = "my_log";
//    private String photoPath = "";
//    private String audioPath = "";
//    private String audioUrl = "";

    // constructor
    public DownloadDatabaseTask(Context context, AsyncResult callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... urls) {

        // params comes from the execute() call: params[0] is the url.
        try {
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            return "Unable to download the requested page.";
        }
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        Log.d("my_log", result);

        // remove the unnecessary parts from the response and construct a JSON
        int start = result.indexOf("{", result.indexOf("{") + 1);
        int end = result.lastIndexOf("}");
        String jsonResponse = result.substring(start, end);
        Log.d("my_log", "jsonResponse = " + jsonResponse);

        try {
            // we have got the JSONObject here
            JSONObject object = new JSONObject(jsonResponse);
            processJson(object);

            callback.onFinishProcess(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String downloadUrl(String urlString) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int responseCode = conn.getResponseCode();
            is = conn.getInputStream();

            String contentAsString = convertStreamToString(is);
            return contentAsString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
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
                String audioUrl = columns.getJSONObject(4).getString("v");

                File file = createPhotoFile();
                String photoPath = file.getAbsolutePath();
                Picasso.with(context).load(photoUrl).into(getTargetToSaveImage(file)); // download image

                file = createAudioFile();
                String audioPath = file.getAbsolutePath();
                downloadAudioFile(audioUrl, file); // download audio

                Question question = new Question(category.trim(), comment.trim(), photoPath, audioPath);
                MyApplication.db.addQuestion(question);
                Log.d(TAG, question.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Target getTargetToSaveImage(final File file) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(TAG, "Bitmap loaded successful");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
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

    private File createPhotoFile(){
        String storageDir = Environment.getExternalStorageDirectory().toString() + "/Photo_Quiz/Photos";
        File myDir = new File(storageDir);
        myDir.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String fileName = "photo_" + timeStamp + ".jpg";
        File file = new File(storageDir, fileName);
        if (file.exists()) file.delete();

        return file;
    }

    private File createAudioFile(){
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Photo_Quiz/Audio/");
        storageDir.mkdirs(); // make this as directory

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String audioFileName = "audio_" + timeStamp + ".3gp";
        String audioPath = storageDir + "/" + audioFileName;
        File file = new File(audioPath);
        if (file.exists()) file.delete();

        return file;
    }

    private void notifyNewMediaFile(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    private String downloadAudioFile(String audioUrl, File file){
        try {
            URL url = new URL(audioUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bufferLength;
            while ((bufferLength = input.read(buffer)) > 0) {
                output.write(buffer, 0, bufferLength);
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

}
