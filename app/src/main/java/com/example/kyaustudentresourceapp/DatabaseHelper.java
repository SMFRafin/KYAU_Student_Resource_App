package com.example.kyaustudentresourceapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "student_db";
    private static final int DATABASE_VERSION = 6; // incremented for Firestore student sync

    private static final String TABLE_STUDENTS = "students";
    private static final String COL_ID = "student_id";
    private static final String COL_NAME = "student_name";
    private static final String COL_PASSWORD = "password";
    private static final String COL_BATCH = "batch";
    private static final String COL_DEPARTMENT = "department";

    private static final String TABLE_NOTICES = "notices";
    private static final String COL_NOTICE_ID = "id";
    private static final String COL_NOTICE_TITLE = "title";
    private static final String COL_NOTICE_BODY = "body";
    private static final String COL_NOTICE_DATE = "date";
    private static final String COL_NOTICE_CATEGORY = "category";

    private final FirebaseFirestore firestore;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create students table
        String createStudentsTable = "CREATE TABLE " + TABLE_STUDENTS + " (" +
                COL_ID + " TEXT PRIMARY KEY, " +
                COL_NAME + " TEXT, " +
                COL_PASSWORD + " TEXT, " +
                COL_BATCH + " TEXT, " +
                COL_DEPARTMENT + " TEXT)";
        db.execSQL(createStudentsTable);

        // Sample data
        db.execSQL("INSERT INTO " + TABLE_STUDENTS + " VALUES ('0622210105101024','Fayaz','123456','13','CSE')");
        db.execSQL("INSERT INTO " + TABLE_STUDENTS + " VALUES ('0622210105101025','Ali','password','12','EEE')");
        db.execSQL("INSERT INTO " + TABLE_STUDENTS + " VALUES ('0622210105101033','Riana','riana123','14','BBA')");

        // Create notices table
        String createNoticesTable = "CREATE TABLE " + TABLE_NOTICES + " (" +
                COL_NOTICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NOTICE_TITLE + " TEXT NOT NULL, " +
                COL_NOTICE_BODY + " TEXT NOT NULL, " +
                COL_NOTICE_DATE + " TEXT NOT NULL, " +
                COL_NOTICE_CATEGORY + " TEXT NOT NULL)";
        db.execSQL(createNoticesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTICES);
        onCreate(db);
    }

    // ==========================================
    // 🔐 Student-related methods + Firestore sync
    // ==========================================

    public boolean isValidStudent(String studentId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STUDENTS +
                " WHERE " + COL_ID + "=? AND " + COL_PASSWORD + "=?", new String[]{studentId, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean isValidStudentId(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STUDENTS +
                " WHERE " + COL_ID + "=?", new String[]{studentId});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public String getStudentName(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_NAME + " FROM " + TABLE_STUDENTS +
                " WHERE " + COL_ID + "=?", new String[]{studentId});
        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return name;
    }

    public boolean registerStudent(String studentId, String name, String password, String batch, String department) {
        if (isValidStudentId(studentId)) {
            return false; // already exists
        }

        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO " + TABLE_STUDENTS + " (" +
                            COL_ID + "," + COL_NAME + "," + COL_PASSWORD + "," + COL_BATCH + "," + COL_DEPARTMENT +
                            ") VALUES (?,?,?,?,?)",
                    new String[]{studentId, name, password, batch, department});
            db.close();
        } catch (Exception e) {
            db.close();
            return false;
        }

        // Also add to Firestore
        Map<String, Object> student = new HashMap<>();
        student.put("student_id", studentId);
        student.put("name", name);
        student.put("password", password);
        student.put("batch", batch);
        student.put("department", department);

        firestore.collection("students").document(studentId)
                .set(student)
                .addOnSuccessListener(a -> Log.d(TAG, "Student added to Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error adding student", e));

        return true;
    }

    // Sync Firestore → Local SQLite
    public void syncStudentsFromFirebase() {
        firestore.collection("students").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Student sync failed.", e);
                    return;
                }

                if (snapshots != null) {
                    SQLiteDatabase db = getWritableDatabase();
                    db.execSQL("DELETE FROM " + TABLE_STUDENTS); // clear old
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String id = doc.getString("student_id");
                        String name = doc.getString("name");
                        String password = doc.getString("password");
                        String batch = doc.getString("batch");
                        String department = doc.getString("department");

                        db.execSQL("INSERT INTO " + TABLE_STUDENTS +
                                        " (" + COL_ID + "," + COL_NAME + "," + COL_PASSWORD + "," + COL_BATCH + "," + COL_DEPARTMENT + ") VALUES (?,?,?,?,?)",
                                new String[]{id, name, password, batch, department});
                    }
                    db.close();
                    Log.d(TAG, "Local students synced from Firebase (" + snapshots.size() + ")");
                }
            }
        });
    }

    // ==========================================
    // 📢 Notice-related methods (unchanged)
    // ==========================================

    public boolean addNotice(String title, String body, String date, String category) {
        boolean success = false;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO " + TABLE_NOTICES + " (" +
                            COL_NOTICE_TITLE + "," + COL_NOTICE_BODY + "," + COL_NOTICE_DATE + "," + COL_NOTICE_CATEGORY +
                            ") VALUES (?,?,?,?)",
                    new String[]{title, body, date, category});
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "SQLite insert error", e);
        } finally {
            db.close();
        }

        Map<String, Object> notice = new HashMap<>();
        notice.put("title", title);
        notice.put("body", body);
        notice.put("date", date);
        notice.put("category", category);

        firestore.collection("notices").add(notice)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Notice added to Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Firestore error", e));

        return success;
    }
    public Cursor getAllStudents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_STUDENTS, null);
    }

    public Cursor getAllNotices() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NOTICES + " ORDER BY " + COL_NOTICE_ID + " DESC", null);
    }

    public void syncNoticesFromFirebase() {
        firestore.collection("notices").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Notice sync failed.", e);
                return;
            }

            if (snapshots != null) {
                SQLiteDatabase db = getWritableDatabase();
                db.execSQL("DELETE FROM " + TABLE_NOTICES);
                for (QueryDocumentSnapshot doc : snapshots) {
                    String title = doc.getString("title");
                    String body = doc.getString("body");
                    String date = doc.getString("date");
                    String category = doc.getString("category");

                    db.execSQL("INSERT INTO " + TABLE_NOTICES +
                                    " (" + COL_NOTICE_TITLE + "," + COL_NOTICE_BODY + "," + COL_NOTICE_DATE + "," + COL_NOTICE_CATEGORY + ") VALUES (?,?,?,?)",
                            new String[]{title, body, date, category});
                }
                db.close();
                Log.d(TAG, "Local notices synced from Firebase (" + snapshots.size() + ")");
            }
        });
    }


    public boolean addResult(String studentId, String courseCode, String courseName,
                             String semester, int marks, String grade, int creditHours) {

        Map<String, Object> result = new HashMap<>();
        result.put("student_id", studentId);
        result.put("course_code", courseCode);
        result.put("course_name", courseName);
        result.put("semester", semester);
        result.put("marks", marks);
        result.put("grade", grade);
        result.put("credit_hours", creditHours);

        firestore.collection("results")
                .add(result)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Result added to Firestore for student: " + studentId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error adding result to Firestore", e));

        return true;
    }

    public void syncResultsFromFirebase(String studentId) {
        firestore.collection("results")
                .whereEqualTo("student_id", studentId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Results sync failed.", e);
                        return;
                    }
                    if (snapshots != null) {
                        Log.d(TAG, "Results updated from Firebase (" + snapshots.size() + " records)");
                        // You can update local SQLite here if needed
                    }
                });
    }

    public boolean addCourse(String courseCode, String courseName, String department,
                             String batch, int credits, String semester, String instructor, String description) {

        Map<String, Object> course = new HashMap<>();
        course.put("course_code", courseCode);
        course.put("course_name", courseName);
        course.put("department", department);
        course.put("batch", batch);
        course.put("credits", credits);
        course.put("semester", semester);
        course.put("instructor", instructor);
        course.put("description", description);

        firestore.collection("courses")
                .add(course)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Course added to Firestore: " + courseCode))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error adding course to Firestore", e));

        return true;
    }

    // Method to get student's department and batch
    public Map<String, String> getStudentDepartmentBatch(String studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, String> studentInfo = new HashMap<>();

        Cursor cursor = db.rawQuery("SELECT " + COL_DEPARTMENT + ", " + COL_BATCH +
                " FROM " + TABLE_STUDENTS +
                " WHERE " + COL_ID + "=?", new String[]{studentId});

        if (cursor.moveToFirst()) {
            studentInfo.put("department", cursor.getString(0));
            studentInfo.put("batch", cursor.getString(1));
        }
        cursor.close();
        db.close();
        return studentInfo;
    }
}
