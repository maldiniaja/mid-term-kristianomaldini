package com.example.studentapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "student.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "students";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // student_id PRIMARY KEY => otomatis UNIQUE
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                "student_id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "major TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public boolean idExists(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE + " WHERE student_id = ?", new String[]{id});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public boolean addStudent(String id, String name, String major) {
        if (idExists(id)) return false; // ID harus unik
        ContentValues cv = new ContentValues();
        cv.put("student_id", id);
        cv.put("name", name);
        cv.put("major", major);
        return getWritableDatabase().insert(TABLE, null, cv) != -1;
    }

    public boolean updateStudent(String id, String name, String major) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("major", major);
        return getWritableDatabase().update(TABLE, cv, "student_id = ?", new String[]{id}) > 0;
    }

    public boolean deleteStudent(String id) {
        return getWritableDatabase().delete(TABLE, "student_id = ?", new String[]{id}) > 0;
    }

    public Cursor getAllStudents() {
        return getReadableDatabase().rawQuery(
                "SELECT student_id, name, major FROM " + TABLE + " ORDER BY name", null);
    }
}
