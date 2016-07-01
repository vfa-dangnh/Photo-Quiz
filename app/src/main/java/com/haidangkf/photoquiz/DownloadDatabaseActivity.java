package com.haidangkf.photoquiz;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

public class DownloadDatabaseActivity extends AppCompatActivity {

    private static final String TAG = "my_log";
    private ProgressDialog mProgressDialog;

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
        new DownloadDatabaseTask(this, new AsyncResult() {
            @Override
            public void onFinishProcess(JSONObject object) {
                hideProgressDialog();
            }
        }).execute("https://spreadsheets.google.com/tq?key=1ogeXtGLE3vb0mM74ZSLIuQJKNgtJij6ZrGqbUdz9kgQ");
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}