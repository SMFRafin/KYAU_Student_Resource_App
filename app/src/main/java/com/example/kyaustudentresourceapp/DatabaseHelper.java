package com.example.kyaustudentresourceapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "student_db";
    private static final int DATABASE_VERSION = 2; // Increment version for schema change

    private static final String TABLE_NAME = "students";
    private static final String COL_ID = "student_id";
    private static final String COL_NAME = "student_name";
    private static final String COL_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " TEXT PRIMARY KEY, " +
                COL_NAME + " TEXT, " +
                COL_PASSWORD + " TEXT)";
        db.execSQL(createTable);

        // Insert some pre-existing IDs with names and passwords
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + COL_ID + "," + COL_NAME + "," + COL_PASSWORD + ") VALUES ('0622210105101024','Fayaz','123456')");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + COL_ID + "," + COL_NAME + "," + COL_PASSWORD + ") VALUES ('0622210105101025','Ali','password')");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + COL_ID + "," + COL_NAME + "," + COL_PASSWORD + ") VALUES ('0622210105101033','Riana','riana123')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Updated method to validate both student ID and password
    public boolean isValidStudent(String studentId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                        " WHERE " + COL_ID + "=? AND " + COL_PASSWORD + "=?",
                new String[]{studentId, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Keep the old method for backward compatibility (optional)
    public boolean isValidStudentId(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL_ID + "=?", new String[]{studentId});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public String getStudentName(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_NAME + " FROM " + TABLE_NAME +
                " WHERE " + COL_ID + "=?", new String[]{studentId});

        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return name;
    }

    // Method to register a new student
    public boolean registerStudent(String studentId, String name, String password) {
        // First check if student ID already exists
        if (isValidStudentId(studentId)) {
            return false; // Student ID already exists
        }

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO " + TABLE_NAME + " (" + COL_ID + "," + COL_NAME + "," + COL_PASSWORD + ") VALUES (?,?,?)",
                    new String[]{studentId, name, password});
            db.close();
            return true; // Registration successful
        } catch (Exception e) {
            db.close();
            return false; // Registration failed
        }
    }
}