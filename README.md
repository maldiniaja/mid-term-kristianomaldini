MainActivity.java
package com.example.studentapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText etName, etStudentId;
    Spinner spMajor;
    Button btnAdd, btnEdit, btnDelete, btnView;
    ListView lvStudents;
    DatabaseHelper db;

    String[] majors = {"Informatics", "Information Systems"};
    ArrayList<String[]> data = new ArrayList<>();   // {id, name, major}
    ArrayList<String> display = new ArrayList<>();
    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etStudentId = findViewById(R.id.etStudentId);
        spMajor = findViewById(R.id.spMajor);
        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnView = findViewById(R.id.btnView);
        lvStudents = findViewById(R.id.lvStudents);
        db = new DatabaseHelper(this);

        spMajor.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, majors));

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, display);
        lvStudents.setAdapter(listAdapter);

        // Klik item list => isi form (untuk Edit / Delete)
        lvStudents.setOnItemClickListener((parent, view, position, id) -> {
            String[] s = data.get(position);
            etStudentId.setText(s[0]);
            etName.setText(s[1]);
            spMajor.setSelection(s[2].equals(majors[0]) ? 0 : 1);
            etStudentId.setEnabled(false); // ID tidak boleh diubah saat edit
        });

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String sid = etStudentId.getText().toString().trim();
            String major = spMajor.getSelectedItem().toString();

            if (name.isEmpty() || sid.isEmpty()) {
                toast("Name dan Student ID wajib diisi");
                return;
            }
            if (db.addStudent(sid, name, major)) {
                toast("Data berhasil ditambahkan");
                clearForm();
                loadData();
            } else {
                toast("Student ID sudah ada! ID harus unik");
            }
        });

        btnEdit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String sid = etStudentId.getText().toString().trim();
            String major = spMajor.getSelectedItem().toString();

            if (name.isEmpty() || sid.isEmpty()) {
                toast("Pilih data dari list dulu, lalu ubah");
                return;
            }
            if (db.updateStudent(sid, name, major)) {
                toast("Data berhasil diubah");
                clearForm();
                loadData();
            } else {
                toast("Student ID tidak ditemukan");
            }
        });

        btnDelete.setOnClickListener(v -> {
            String sid = etStudentId.getText().toString().trim();
            if (sid.isEmpty()) {
                toast("Pilih data dari list dulu");
                return;
            }
            if (db.deleteStudent(sid)) {
                toast("Data berhasil dihapus");
                clearForm();
                loadData();
            } else {
                toast("Student ID tidak ditemukan");
            }
        });

        btnView.setOnClickListener(v -> loadData());

        loadData();
    }

    private void loadData() {
        data.clear();
        display.clear();
        Cursor c = db.getAllStudents();
        while (c.moveToNext()) {
            String id = c.getString(0);
            String name = c.getString(1);
            String major = c.getString(2);
            data.add(new String[]{id, name, major});
            display.add(id + "  |  " + name + "  |  " + major);
        }
        c.close();
        listAdapter.notifyDataSetChanged();
    }

    private void clearForm() {
        etName.setText("");
        etStudentId.setText("");
        etStudentId.setEnabled(true);
        spMajor.setSelection(0);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

DatabaseHelper.java
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

activity_main.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <EditText
        android:id="@+id/etName"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Enter Student Name"
        android:inputType="textPersonName" />

    <EditText
        android:id="@+id/etStudentId"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:hint="Enter Student ID"
        android:inputType="number" />

    <Spinner
        android:id="@+id/spMajor"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:layout_marginTop="8dp" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:orientation="horizontal">

        <Button
            android:id="@+id/btnAdd"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Add" />

        <Button
            android:id="@+id/btnEdit"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Edit" />

        <Button
            android:id="@+id/btnDelete"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Delete" />

        <Button
            android:id="@+id/btnView"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="View" />
    </LinearLayout>

    <ListView
        android:id="@+id/lvStudents"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        android:layout_weight="1" />
</LinearLayout>
