package com.tem2.karirku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisimpanActivity extends AppCompatActivity {

    private RecyclerView recyclerJobs;
    private JobAdapter jobAdapter;
    private List<Job> savedJobList = new ArrayList<>();
    private SessionManager sessionManager;
    private int currentUserId;

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disimpan);

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        if (!sessionManager.isLoggedIn() || currentUserId == 0) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadSavedJobs();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerJobs = findViewById(R.id.recyclerJobs);
        recyclerJobs.setLayoutManager(new LinearLayoutManager(this));

        jobAdapter = new JobAdapter(this, savedJobList, true); // true untuk mode disimpan
        recyclerJobs.setAdapter(jobAdapter);
    }

    private void loadSavedJobs() {
        // Query yang lebih sederhana dan efektif
        String url = SUPABASE_URL + "/rest/v1/favorit_lowongan" +
                "?id_pencaker=eq." + currentUserId +
                "&select=id_lowongan,lowongan(id_lowongan,judul,lokasi,kategori,tipe_pekerjaan,gaji_range,deskripsi,kualifikasi,mode_kerja,benefit,no_telp,nama_perusahaan,dibuat_pada)";

        Log.d("SAVED_JOBS", "🔍 Loading saved jobs from: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("SAVED_JOBS", "✅ Response received: " + response.length() + " items");
                    savedJobList.clear();
                    parseSavedJobs(response);
                    jobAdapter.setData(savedJobList);

                    if (savedJobList.isEmpty()) {
                        Toast.makeText(DisimpanActivity.this, "Belum ada lowongan yang disimpan", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DisimpanActivity.this, "Ditemukan " + savedJobList.size() + " lowongan disimpan", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("SAVED_JOBS", "❌ Gagal load saved jobs: " + error.toString());
                    Toast.makeText(DisimpanActivity.this, "Gagal memuat lowongan disimpan", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    private void parseSavedJobs(JSONArray response) {
        try {
            int validJobs = 0;
            int invalidJobs = 0;

            for (int i = 0; i < response.length(); i++) {
                JSONObject favoritObj = response.getJSONObject(i);
                JSONObject lowongan = favoritObj.getJSONObject("lowongan");

                int idLowongan = lowongan.optInt("id_lowongan", 0);

                // Validasi: Skip jika ID lowongan tidak valid
                if (idLowongan <= 0) {
                    invalidJobs++;
                    Log.w("SAVED_JOBS", "⚠️ Skip job dengan ID tidak valid: " + idLowongan);
                    continue;
                }

                // Ambil data lowongan
                String judul = lowongan.optString("judul", "-");
                String lokasi = lowongan.optString("lokasi", "-");
                String kategori = lowongan.optString("kategori", "-");
                String tipe = lowongan.optString("tipe_pekerjaan", "-");
                String gaji = lowongan.optString("gaji_range", "-");
                String deskripsi = lowongan.optString("deskripsi", "");
                String kualifikasi = lowongan.optString("kualifikasi", "");
                String modeKerja = lowongan.optString("mode_kerja", "On-site");
                String benefit = lowongan.optString("benefit", "");
                String noTelp = lowongan.optString("no_telp", "");
                String namaPerusahaan = lowongan.optString("nama_perusahaan", "Perusahaan");

                // Format waktu posting
                String postedTime = "Baru saja";
                String dibuatPada = lowongan.optString("dibuat_pada", "");
                if (!dibuatPada.isEmpty()) {
                    postedTime = formatTimeAgo(dibuatPada);
                }

                // Format jumlah pendaftar
                String applicants = "0 Pendaftar";

                // Buat objek Job
                Job job = new Job(
                        idLowongan,
                        namaPerusahaan,
                        lokasi,
                        judul,
                        postedTime,
                        applicants,
                        kategori,
                        tipe,
                        gaji,
                        modeKerja,
                        deskripsi,
                        kualifikasi,
                        benefit,
                        noTelp
                );

                savedJobList.add(job);
                validJobs++;

                Log.d("SAVED_JOBS", "✅ Loaded saved job: " + judul +
                        " (ID: " + idLowongan + ", Phone: " + noTelp + ")");
            }

            Log.d("SAVED_JOBS", "📊 Summary - Valid: " + validJobs + ", Invalid: " + invalidJobs);

        } catch (JSONException e) {
            Log.e("SAVED_JOBS", "❌ Error parsing saved jobs: " + e.getMessage());
        }
    }

    private String formatTimeAgo(String dateTime) {
        try {
            // Parse ISO 8601 datetime dari Supabase
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date past = sdf.parse(dateTime.substring(0, 19));
            java.util.Date now = new java.util.Date();

            long diff = now.getTime() - past.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + " Hari lalu";
            } else if (hours > 0) {
                return hours + " Jam lalu";
            } else if (minutes > 0) {
                return minutes + " Menit lalu";
            } else {
                return "Baru saja";
            }
        } catch (Exception e) {
            Log.e("SAVED_JOBS", "Error parsing time: " + e.getMessage());
            return "Baru saja";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data ketika activity di-resume
        loadSavedJobs();
    }
}