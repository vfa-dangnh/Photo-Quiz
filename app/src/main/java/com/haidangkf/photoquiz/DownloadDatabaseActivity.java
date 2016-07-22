package com.haidangkf.photoquiz;

import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;

public class DownloadDatabaseActivity extends AppCompatActivity {

    private static final String TAG = "my_log";
    private static final int REQUEST_ACCOUNT_PICKER = 1;
    public static ProgressDialog mProgressDialog;
    public static boolean isCallDownload = false;
    public static boolean isDownloadSuccess;
    public static String accountName;
    public static String spreadsheetName;
    public static String worksheetName;

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

        registerReceiver(finishActivityReceiver, new IntentFilter("finish_download"));

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
                        new String[]{"com.google"}, true, null, null, null, null);
                startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            isCallDownload = true;
            showProgressDialog(this);
            if (!isMyServiceRunning(this, MyService.class)) {
                startService(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(finishActivityReceiver);
        stopService(this);
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

    public static void startService(Context context) {
        Log.d(TAG, "start Service");
        context.startService(new Intent(context, MyService.class));
    }

    public static void stopService(Context context) {
        Log.d(TAG, "stop Service");
        context.stopService(new Intent(context, MyService.class));
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private final BroadcastReceiver finishActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

}