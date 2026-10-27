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
