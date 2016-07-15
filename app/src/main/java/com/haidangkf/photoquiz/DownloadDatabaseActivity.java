package com.haidangkf.photoquiz;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DownloadDatabaseActivity extends AppCompatActivity {

    private static final String TAG = "my_log";
    public static ProgressDialog mProgressDialog;
    public static boolean isDownloadSuccess;
    String spreadsheetName;
    String worksheetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_database);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        Log.d(TAG, "SDK_INT = " + SDK_INT);
//        if (SDK_INT > 8) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
//                    .permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }

        Button btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnDownload = (Button) findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etSpreadsheetName = (EditText) findViewById(R.id.etSpreadsheetName);
                EditText etWorksheetName = (EditText) findViewById(R.id.etWorksheetName);
                spreadsheetName = etSpreadsheetName.getText().toString();
                worksheetName = etWorksheetName.getText().toString();

                if (spreadsheetName.isEmpty() || worksheetName.isEmpty()) {
                    Toast.makeText(DownloadDatabaseActivity.this, "Please enter Spreadsheet name and Worksheet name !", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isNetworkAvailable()) {
                    Toast.makeText(DownloadDatabaseActivity.this, "No Internet connection !", Toast.LENGTH_SHORT).show();
                    return;
                }

                isDownloadSuccess = false;
                Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                        new String[]{"com.google"}, false, null, null, null, null);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.d(TAG, "--> enter first if");

            final String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            new DownloadTask(this).execute(accountName, spreadsheetName, worksheetName);

        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            Log.d(TAG, "--> enter second if");
        }
    }

    public static void showProgressDialog(Context context) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("");
        mProgressDialog.setMessage("downloading…");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mProgressDialog.show();
    }

    public static void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    ////////////////////////////////////////////////////////////////////////

    private class DownloadTask extends AsyncTask<String, Void, String> {
        private Context context;

        // constructor
        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(context);
        }

        @Override
        protected String doInBackground(String... params) {
            String mEmail = params[0]; // your google account name
            String mType = "com.google";
            Account account = new Account(mEmail, mType);
            String scopes = "oauth2:https://spreadsheets.google.com/feeds "
                    + "https://www.googleapis.com/auth/plus.login "
                    + "https://www.googleapis.com/auth/drive";
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                Intent recoveryIntent = e.getIntent();
                startActivityForResult(recoveryIntent, 2);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }

            try {
                SpreadsheetService service = new SpreadsheetService("MySpreadsheetIntegration-v1");
                service.setAuthSubToken(token);
                URL SPREADSHEET_FEED_URL = new URL(
                        "https://spreadsheets.google.com/feeds/spreadsheets/private/full");
                SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
                List<SpreadsheetEntry> spreadsheets = feed.getEntries();

                Log.d(TAG, "spreadsheets.size() = " + spreadsheets.size());

                for (SpreadsheetEntry spreadsheet : spreadsheets) { // outer for loop
                    if (spreadsheet.getTitle().getPlainText().equalsIgnoreCase(params[1])) { // your spreadsheet name
                        List<WorksheetEntry> worksheets = spreadsheet.getWorksheets();

                        for (WorksheetEntry worksheet : worksheets) { // inner for loop
                            String title = worksheet.getTitle().getPlainText();
                            if (title.equalsIgnoreCase(params[2])) { // your worksheet name
                                Log.d(TAG, "Worksheet name: " + title);
                                URL listFeedUrl = worksheet.getListFeedUrl();
                                ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
                                Log.d(TAG, "Number of row = " + listFeed.getTotalResults());

                                // scan through each row in worksheet
                                for (ListEntry row : listFeed.getEntries()) {
                                    String rowTitle = row.getTitle().getPlainText();
                                    ArrayList<String> currentRow = new ArrayList<>();

                                    // get elements in current row stored in ArrayList
                                    for (String element : row.getCustomElements().getTags()) {
                                        if (!row.getCustomElements().getValue(element).equals(rowTitle))
                                            currentRow.add(row.getCustomElements().getValue(element));
                                    }
//                                    Log.d(TAG, "currentRow.size() = "+currentRow.size());
                                    if (currentRow.size() == 4) {
                                        String category = currentRow.get(0).trim();
                                        String comment = currentRow.get(1).trim();
                                        String photoUrl = currentRow.get(2).trim();
                                        String audioUrl = currentRow.get(3).trim();

                                        Log.d(TAG, category + "\n" + comment + "\n" + photoUrl + "\n" + audioUrl);

                                        if (category.isEmpty() || comment.isEmpty()
                                                || photoUrl.isEmpty() || audioUrl.isEmpty()) {
                                            continue;
                                        } else {
                                            String photoPath = downloadImageFromUrl(photoUrl);
                                            String audioPath = downloadAudioFromUrl(audioUrl);

                                            Question question = new Question(category, comment, photoPath, audioPath);
                                            if (MyApplication.db.addQuestion(question) > 0) {
                                                Log.d(TAG, getString(R.string.msg_ques_add_success));
                                                isDownloadSuccess = true;
                                            } else {
                                                Log.d(TAG, getString(R.string.msg_ques_add_fail));
                                            }
                                        }
                                    }
                                }
                                break; // break inner
                            }
                        }
                        break; // break outer
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return token;
        }

        @Override
        protected void onPostExecute(String token) {
            Log.d(TAG, "Token Value: " + token);
            hideProgressDialog();
            if (isDownloadSuccess) {
                Toast.makeText(context, "Download completed !", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(context, "Download failed ! Please try again !", Toast.LENGTH_LONG).show();
            }
        }

        //-------------------------------------------------------------------

        public String downloadAudioFromUrl(String url) {
            int count;
            File file = null;
            try {
                URL urls = new URL(url);
                URLConnection connection = urls.openConnection();
                connection.connect();
                // this will be useful to show the percentage 0-100% in progress bar
                int lengthOfFile = connection.getContentLength();

                File storageDir = new File(Environment.getExternalStorageDirectory().toString() + "/Photo_Quiz/Audio");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
                String filename = "audio_" + timeStamp + ".3gp";
                file = new File(storageDir, filename);

                InputStream input = new BufferedInputStream(urls.openStream());
                OutputStream output = new FileOutputStream(file.getAbsolutePath());
                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress...
//                    publishProgress((int) (total * 100 / lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                notifyNewMediaFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return file.getAbsolutePath();
        }

        public String downloadImageFromUrl(String url) {
            String filePath = null;
            try {
                URL _url = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) _url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                File storageDir = new File(Environment.getExternalStorageDirectory().toString() + "/Photo_Quiz/Photos");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
                String filename = "photo_" + timeStamp + ".jpg";
                File file = new File(storageDir, filename);

                FileOutputStream output = new FileOutputStream(file);
                InputStream input = urlConnection.getInputStream();
                int totalSize = urlConnection.getContentLength();
                int downloadedSize = 0;
                byte[] buffer = new byte[1024];
                int bufferLength = 0;
                while ((bufferLength = input.read(buffer)) > 0) {
                    output.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                }

                output.close();
                if (downloadedSize == totalSize) {
                    filePath = file.getAbsolutePath();
                    notifyNewMediaFile(file);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return filePath;
        }

        private void notifyNewMediaFile(File file) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        }

    }

}