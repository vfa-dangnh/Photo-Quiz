package com.haidangkf.photoquiz;

        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Environment;
        import android.provider.MediaStore;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;

        import java.io.File;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.Date;

public class TakePhotoActivity extends AppCompatActivity {

    final String TAG = "my_log";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_question);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d(TAG, "photoFile = " + photoFile.getAbsolutePath());
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

        finish(); // finish this activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult of TakePhotoActivity");

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                // get the bitmap image after capturing
                Bitmap mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() {
        File storageDir = new File(Environment.getExternalStorageDirectory().toString() + "/Photo_Quiz/Photos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String fileName = "photo_" + timeStamp + ".jpg";
        File image = new File(storageDir, fileName);

        mCurrentPhotoPath = image.getAbsolutePath();
        AddQuestionActivity.photoPath = mCurrentPhotoPath;
        Log.d(TAG, "mCurrentPhotoPath = " + mCurrentPhotoPath);

        // send Broadcast to notify this photo and be able to see it in Gallery
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(image);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);

        return image;
    }

}