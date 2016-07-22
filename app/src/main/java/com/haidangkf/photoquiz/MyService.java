package com.haidangkf.photoquiz;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
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

public class MyService extends Service {

    private static final String TAG = "my_log";
    public static Thread myThread;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate Service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy Service");

        Thread.currentThread().interrupt(); // làm gián đoạn Thread hiện tại khi Service bị huỷ
        myThread = null; // set this to make the condition in while loop (of method ThreadRemind) become false --> just single Thread run at a time
    }

    // onStartCommand() is called EVERY TIME a client starts the service using startService(Intent intent)
    // Dù ứng dụng bị tắt nó vẫn tự chạy lại được
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand Service");

        threadDownloadDatabase();

        return START_STICKY; // when service is killed by system, it tells the OS to recreate the service after it has enough memory
    }

    public void threadDownloadDatabase() {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (DownloadDatabaseActivity.isCallDownload && myThread == Thread.currentThread()) {
                    try {
                        String token = downloadTask(DownloadDatabaseActivity.accountName,
                                DownloadDatabaseActivity.spreadsheetName, DownloadDatabaseActivity.worksheetName);
                        DownloadDatabaseActivity.isCallDownload = false;
                        DownloadDatabaseActivity.hideProgressDialog();

                        Log.d(TAG, "Token Value: " + token);
                        if (DownloadDatabaseActivity.isDownloadSuccess) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MyService.this, "Download completed !", Toast.LENGTH_LONG).show();
                                }
                            });

                            DownloadDatabaseActivity.stopService(MyService.this);
                            sendBroadcast(new Intent("finish_download")); // finish DownloadDatabaseActivity
                        } else {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MyService.this, "Download failed ! Please try again !", Toast.LENGTH_LONG).show();
                                }
                            });

                            DownloadDatabaseActivity.stopService(MyService.this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        myThread.start();
    }

    //--------------------------------------------------------------------------

    public String downloadTask(String accountName, String spreadsheetName, String worksheetName) {
        String mEmail = accountName; // your google account name
        Log.d(TAG, "Email = " + mEmail);
        String mType = "com.google";
        Account account = new Account(mEmail, mType);
        String scopes = "oauth2:https://docs.google.com/feeds/ "
                + "https://docs.googleusercontent.com/ "
                + "https://spreadsheets.google.com/feeds/";
        String token = null;
        try {
            // request the OAuth token from a Service
            // if an error occurs, it creates an appropriate notification to user
            token = GoogleAuthUtil.getTokenWithNotification(MyService.this, account, scopes, null);
            // Note: using GoogleAuthUtil.getToken() if request the OAuth token from an Activity
        } catch (UserRecoverableAuthException e) {
            Log.d(TAG, "ERROR 1: " + e.toString());
        } catch (IOException e) {
            Log.d(TAG, "ERROR 2: " + e.toString());
        } catch (GoogleAuthException e) {
            Log.d(TAG, "ERROR 3: " + e.toString());

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyService.this, "Access Requested !", Toast.LENGTH_SHORT).show();
                }
            });
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
                if (spreadsheet.getTitle().getPlainText().equalsIgnoreCase(spreadsheetName)) { // your spreadsheet name
                    List<WorksheetEntry> worksheets = spreadsheet.getWorksheets();

                    for (WorksheetEntry worksheet : worksheets) { // inner for loop
                        String title = worksheet.getTitle().getPlainText();
                        if (title.equalsIgnoreCase(worksheetName)) { // your worksheet name
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
                                            DownloadDatabaseActivity.isDownloadSuccess = true;
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