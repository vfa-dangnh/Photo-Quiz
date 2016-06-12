package com.haidangkf.photoquiz;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePhotoActivity extends Activity {

    final String TAG = "my_log";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private ImageView mImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_question);

        mImageView = (ImageView) findViewById(R.id.btnPhotoTaking);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.i(TAG, "Photo saved into the Gallery");
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG, "Error: " + ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

        finish(); // finish this activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                // set the taking photo to ImageView
                mImageView.setImageBitmap(mImageBitmap);
//                if (saveImageToStorage(mImageBitmap)) {
//                    Log.i(TAG, "Photo saved into the Gallery");
//                } else {
//                    Log.i(TAG, "Unable to store the photo");
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "photo_" + timeStamp + "_";
        String dirPath = Environment.getExternalStorageDirectory().toString() + "/Photo_Quiz/Photos";
//        File storageDir = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);
        File storageDir = new File(dirPath);
        storageDir.mkdirs(); // make this as a directory
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "" + image.getAbsolutePath();
        AddQuestionActivity.photoPath = mCurrentPhotoPath;
//        Log.i(TAG, "mCurrentPhotoPath = " + mCurrentPhotoPath);

        // send Broadcast to notify this photo and be able to see it in Gallery
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(image);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);

        return image;
    }

    // Hàm lưu file ảnh Bitmap vào bộ nhớ máy dưới dạng JPG
    private boolean saveImageToStorage(Bitmap finalBitmap) {
        boolean result = false;

        String storageDir = Environment.getExternalStorageDirectory().toString() + "/Photo_Quiz/Photos";
        Log.i(TAG, "storageDir = " + storageDir);
        File myDir = new File(storageDir);
        myDir.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "photo_" + timeStamp + ".jpg";
        File file = new File(storageDir, fileName);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            // send Broadcast to notify this photo and be able to see it in Gallery
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);

            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}