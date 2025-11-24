package com.tem2.karirku;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class JobDetailActivity extends AppCompatActivity {

    private static final String TAG = "JOB_DETAIL";
    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    private ImageView btnBack;
    private TextView tvJobTitle, tvCompanyName, tvLocation, tvPostedTime, tvApplicants;
    private TextView tvTag1, tvTag2, tvTag3, tvJobDescription, tvRequirements, tvAdditionalInfo;
    private MaterialButton btnWhatsApp;
    private Button btnApply;

    private Job currentJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        initViews();
        loadJobData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvLocation = findViewById(R.id.tvLocation);
        tvPostedTime = findViewById(R.id.tvPostedTime);
        tvApplicants = findViewById(R.id.tvApplicants);
        tvTag1 = findViewById(R.id.tvTag1);
        tvTag2 = findViewById(R.id.tvTag2);
        tvTag3 = findViewById(R.id.tvTag3);
        tvJobDescription = findViewById(R.id.tvJobDescription);
        tvRequirements = findViewById(R.id.tvRequirements);
        tvAdditionalInfo = findViewById(R.id.tvAdditionalInfo);
        btnWhatsApp = findViewById(R.id.btnWhatsApp);
        btnApply = findViewById(R.id.btnApplywa);
    }

    private void loadJobData() {
        Intent intent = getIntent();
        if (intent != null) {
            currentJob = (Job) intent.getSerializableExtra("JOB_DATA");

            if (currentJob != null) {
                Log.d(TAG, "========================================");
                Log.d(TAG, "Loading job data: " + currentJob.toString());
                Log.d(TAG, "========================================");

                // Set data ke UI
                tvJobTitle.setText(currentJob.getJobTitle());
                tvCompanyName.setText(currentJob.getCompanyName());
                tvLocation.setText(currentJob.getLocation());
                tvPostedTime.setText(currentJob.getPostedTime());
                tvApplicants.setText("• " + currentJob.getApplicants());
                tvTag1.setText(currentJob.getTag1());
                tvTag2.setText(currentJob.getTag2());
                tvTag3.setText(currentJob.getTag3());

                // Set deskripsi
                String description = currentJob.getDeskripsi();
                if (description != null && !description.isEmpty()) {
                    tvJobDescription.setText(description);
                } else {
                    tvJobDescription.setText("Lowongan " + currentJob.getJobTitle() +
                            " di " + currentJob.getCompanyName() +
                            " membuka kesempatan bagi profesional yang berpengalaman di bidang " +
                            currentJob.getTag1() + ".");
                }

                // Set kualifikasi
                String kualifikasi = currentJob.getKualifikasi();
                if (kualifikasi != null && !kualifikasi.isEmpty()) {
                    tvRequirements.setText(kualifikasi);
                } else {
                    tvRequirements.setText("• Pengalaman di bidang " + currentJob.getTag1() +
                            "\n• Memahami " + currentJob.getTag2() +
                            "\n• Dapat bekerja " + currentJob.getTag3() +
                            "\n• Berdomisili di " + currentJob.getLocation());
                }

                // Additional info
                StringBuilder additionalInfo = new StringBuilder();
                if (currentJob.getTipePekerjaan() != null && !currentJob.getTipePekerjaan().isEmpty()) {
                    additionalInfo.append("• Jenis: ").append(currentJob.getTipePekerjaan()).append("\n");
                }
                if (currentJob.getGajiRange() != null && !currentJob.getGajiRange().isEmpty()) {
                    additionalInfo.append("• Gaji: ").append(currentJob.getGajiRange()).append("\n");
                }
                if (currentJob.getModeKerja() != null && !currentJob.getModeKerja().isEmpty()) {
                    additionalInfo.append("• Mode Kerja: ").append(currentJob.getModeKerja()).append("\n");
                }
                additionalInfo.append("• Lokasi: ").append(currentJob.getLocation()).append("\n");
                additionalInfo.append("• Waktu: ").append(currentJob.getPostedTime()).append("\n");
                additionalInfo.append("• Pendaftar: ").append(currentJob.getApplicants());
                tvAdditionalInfo.setText(additionalInfo.toString());

                // Jika noTelp kosong, fetch dari database
                if (currentJob.getNoTelp() == null || currentJob.getNoTelp().isEmpty()) {
                    fetchNoTelpFromDatabase();
                } else {
                    setupWhatsAppButton();
                }
            }
        }
    }

    private void fetchNoTelpFromDatabase() {
        new Thread(() -> {
            try {
                // Query langsung ke tabel lowongan untuk ambil no_telp
                String url = SUPABASE_URL + "/rest/v1/lowongan?select=no_telp&id_lowongan=eq." + currentJob.getIdLowongan();

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("apikey", SUPABASE_API_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");

                InputStream responseStream = connection.getInputStream();
                Scanner scanner = new Scanner(responseStream).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";

                Log.d(TAG, "API Response for no_telp: " + response);

                JSONArray jsonArray = new JSONArray(response);
                if (jsonArray.length() > 0) {
                    JSONObject jobData = jsonArray.getJSONObject(0);
                    String phoneNumber = jobData.getString("no_telp");

                    runOnUiThread(() -> {
                        currentJob.setNoTelp(phoneNumber);
                        Log.d(TAG, "✅ Fetched no_telp from database: " + phoneNumber);
                        setupWhatsAppButton();
                    });
                } else {
                    runOnUiThread(() -> {
                        Log.e(TAG, "❌ No data found for job ID: " + currentJob.getIdLowongan());
                        setupWhatsAppButton();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error fetching no_telp: " + e.getMessage());
                runOnUiThread(() -> {
                    setupWhatsAppButton();
                });
            }
        }).start();
    }

    private void setupWhatsAppButton() {
        String phoneNumber = currentJob.getNoTelp();

        Log.d(TAG, "========================================");
        Log.d(TAG, "🔍 WHATSAPP SETUP");
        Log.d(TAG, "Company: " + currentJob.getCompanyName());
        Log.d(TAG, "Job: " + currentJob.getJobTitle());
        Log.d(TAG, "Phone: '" + phoneNumber + "'");
        Log.d(TAG, "========================================");

        btnWhatsApp.setVisibility(View.VISIBLE);

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            btnWhatsApp.setEnabled(true);
            btnWhatsApp.setAlpha(1.0f);
            btnWhatsApp.setText("💬 Hubungi via WhatsApp");
            Log.d(TAG, "✅ WhatsApp button ENABLED");
        } else {
            btnWhatsApp.setEnabled(false);
            btnWhatsApp.setAlpha(0.5f);
            btnWhatsApp.setText("WhatsApp (Tidak Tersedia)");
            Log.d(TAG, "❌ WhatsApp button DISABLED - no phone number");
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnWhatsApp.setOnClickListener(v -> openWhatsApp());

        btnApply.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur lamar sedang dikembangkan", Toast.LENGTH_SHORT).show();
        });
    }

    private void openWhatsApp() {
        if (currentJob == null) {
            Toast.makeText(this, "❌ Data lowongan tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = currentJob.getNoTelp();

        Log.d(TAG, "🎯 OPENING WHATSAPP WITH: " + phoneNumber);

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(this, "❌ Nomor WhatsApp tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bersihkan nomor - hapus semua non-digit
        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");

        // Format nomor untuk WhatsApp
        if (cleanNumber.startsWith("0")) {
            cleanNumber = "62" + cleanNumber.substring(1);
        } else if (!cleanNumber.startsWith("62")) {
            cleanNumber = "62" + cleanNumber;
        }

        String message = "Halo, saya tertarik dengan lowongan *" + currentJob.getJobTitle() +
                "* di *" + currentJob.getCompanyName() + "*. " +
                "Bisakah saya mendapatkan informasi lebih lanjut?";

        String url = "https://wa.me/" + cleanNumber + "?text=" + Uri.encode(message);

        Log.d(TAG, "🔗 WhatsApp URL: " + url);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "❌ Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
}