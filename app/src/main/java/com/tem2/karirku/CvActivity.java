package com.tem2.karirku;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class CvActivity extends AppCompatActivity {

    ImageView btnBack;
    RecyclerView recyclerRiwayatCV;
    ArrayList<CvModel> list = new ArrayList<>();
    CvAdapter adapter;

    final int PICK_CV = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cv);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerRiwayatCV = findViewById(R.id.recyclerRiwayatCV);
        recyclerRiwayatCV.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CvAdapter(this, list);
        recyclerRiwayatCV.setAdapter(adapter);

        findViewById(R.id.btnUploadCV).setOnClickListener(v -> pilihFile());

        loadRiwayatCV();
    }

    private void pilihFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(intent, PICK_CV);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CV && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            String fileName = getFileName(uri);
            String savedPath = saveFileToInternal(uri, fileName);

            saveToPrefs(fileName, savedPath);

            list.add(new CvModel(fileName, savedPath));
            adapter.notifyDataSetChanged();
        }
    }

    private String getFileName(Uri uri) {
        String result = "";
        var cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            result = cursor.getString(nameIndex);
            cursor.close();
        }
        return result;
    }

    private String saveFileToInternal(Uri uri, String name) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), name);
            FileOutputStream out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;

            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);

            in.close();
            out.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void saveToPrefs(String name, String path) {
        var prefs = getSharedPreferences("cvdata", MODE_PRIVATE);
        String old = prefs.getString("list", "");
        prefs.edit().putString("list", old + name + "|" + path + ";;").apply();
    }

    private void loadRiwayatCV() {
        var prefs = getSharedPreferences("cvdata", MODE_PRIVATE);
        String data = prefs.getString("list", "");

        if (data.isEmpty()) return;

        String[] rows = data.split(";;");

        for (String row : rows) {
            if (row.trim().isEmpty()) continue;

            String[] parts = row.split("\\|");
            list.add(new CvModel(parts[0], parts[1]));
        }

        adapter.notifyDataSetChanged();
    }
}
