package com.haidangkf.photoquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MyDatabase extends SQLiteOpenHelper {

    // Ten co so du lieu cua minh
    public static final String databaseName = "Photo_Quiz_DB";

    // Duong dan co so du lieu
    // com.example.ten_project doi lai ten package cua minh
    public static String DB_PATH = "/data/data/com.haidangkf.photoquiz/databases/";

    // Ten file co so du lieu - viet lai cho giong voi minh
    private static String DB_NAME = "Photo_Quiz_DB.sqlite";
    private SQLiteDatabase database;
    private final Context mContext;

    public MyDatabase(Context context) {
        super(context, DB_NAME, null, 1);
        this.mContext = context;
    }


    public boolean isCreatedDatabase() throws IOException {
        // Default là đã có DB
        boolean result = true;
        // Nếu chưa tồn tại DB thì copy từ Assets vào Data
        if (!checkExistDatabase()) {
            this.getReadableDatabase();
            try {
                copyDatabase();
                result = false;
            } catch (Exception e) {
                throw new Error("Error copying database");
            }
        }

        return result;
    }


    private boolean checkExistDatabase() {

        try {
            String myPath = DB_PATH + DB_NAME;
            File fileDB = new File(myPath);

            if (fileDB.exists()) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            return false;
        }
    }


    private void copyDatabase() throws IOException {
        InputStream myInput = mContext.getAssets().open(DB_NAME);
        OutputStream myOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }


    public boolean deleteDatabase() {
        File file = new File(DB_PATH + DB_NAME);
        return file.delete();
    }


    public void openDatabase() throws SQLException {
        database = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
                SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (database != null)
            database.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // do nothing
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }

    public int deleteDataFromTable(String tableName) {

        int result = 0;
        try {
            openDatabase();
            database.beginTransaction();
            result = database.delete(tableName, null, null);
            if (result >= 0) {
                database.setTransactionSuccessful();
            }
        } catch (Exception e) {
            database.endTransaction();
            close();
        } finally {
            database.endTransaction();
            close();
        }

        return result;
    }

    // ================================================================

    public ArrayList<Question> getQuestionList() {

        ArrayList<Question> questionList = new ArrayList<>();
        String query = "SELECT * FROM Question";
        Cursor cursor = database.rawQuery(query, null);
        // Các ColumeIndex phải ghi giống trong table database
        int ColumeId = cursor.getColumnIndex("Id");
        int ColumeCategory = cursor.getColumnIndex("Category");
        int ColumeComment = cursor.getColumnIndex("Comment");
        int ColumePhotoPath = cursor.getColumnIndex("PhotoPath");
        int ColumeAudioPath = cursor.getColumnIndex("AudioPath");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(ColumeId);
            String category = cursor.getString(ColumeCategory);
            String comment = cursor.getString(ColumeComment);
            String photoPath = cursor.getString(ColumePhotoPath);
            String audioPath = cursor.getString(ColumeAudioPath);

            Question question = new Question(id, category, comment, photoPath, audioPath);
            questionList.add(question);
        }

        cursor.close();
        return questionList;
    }

    public long addQuestion(Question question) {
        ContentValues v = new ContentValues();
        v.put("Category", question.getCategory());
        v.put("Comment", question.getComment());
        v.put("PhotoPath", question.getPhotoPath());
        v.put("AudioPath", question.getAudioPath());

        long result = database.insert("Question", null, v);
        if (result > 0) {
            Log.d("InsertDB", "Insert to DB successful");
        } else {
            Log.d("InsertDB", "Insert to DB failed");
        }

        return result;
    }

    public int getCountOfQuestion() {
        String query = "SELECT * FROM Question";
        Cursor cursor = database.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void deleteQuestion(String id) {
        database.delete("Question", "Id = ?", new String[]{id});
    }

    public int deleteAllDB(){
        int count = database.delete("Question", "1", null);
        return count; // count of deleted rows
    }

}
