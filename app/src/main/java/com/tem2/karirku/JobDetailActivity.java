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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * JobDetailActivity - menampilkan detail lowongan.
 * Ditambahkan: BottomSheet untuk Kirim Lamaran (catatan) dan fungsi uploadLamaran().
 */
public class JobDetailActivity extends AppCompatActivity {

    private static final String TAG = "JOB_DETAIL";
    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    private ImageView btnBack;
    private TextView tvJobTitle, tvCompanyName, tvLocation, tvJobDescription, tvRequirements;
    private TextView tvTag1, tvTag2, tvTag3, tvJobTypeValue, tvSalary, tvWorkingHours, tvExpertise;
    private MaterialButton btnWhatsApp;
    private Button btnApply;

    private Job currentJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        try {
            initViews();
            loadJobData();
            setupClickListeners();
        } catch (Exception e) {
            Log.e(TAG, "❌ ERROR in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Terjadi kesalahan saat memuat detail lowongan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvLocation = findViewById(R.id.tvLocation);
        tvJobDescription = findViewById(R.id.tvJobDescription);
        tvRequirements = findViewById(R.id.tvRequirements);
        tvTag1 = findViewById(R.id.tvTag1);
        tvTag2 = findViewById(R.id.tvTag2);
        tvTag3 = findViewById(R.id.tvTag3);
        tvJobTypeValue = findViewById(R.id.tvJobTypeValue);
        tvSalary = findViewById(R.id.tvSalary);
        tvWorkingHours = findViewById(R.id.tvWorkingHours);
        tvExpertise = findViewById(R.id.tvExpertise);
        btnWhatsApp = findViewById(R.id.btnWhatsApp);
        btnApply = findViewById(R.id.btnApplywa);
    }

    private void loadJobData() {
        try {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("JOB_DATA")) {
                currentJob = (Job) intent.getSerializableExtra("JOB_DATA");

                if (currentJob != null) {
                    Log.d(TAG, "========================================");
                    Log.d(TAG, "Loading job data: " + currentJob.toString());
                    Log.d(TAG, "========================================");

                    // Set basic job info
                    tvJobTitle.setText(currentJob.getJobTitle() != null ? currentJob.getJobTitle() : "Judul tidak tersedia");
                    tvCompanyName.setText(currentJob.getCompanyName() != null ? currentJob.getCompanyName() : "Perusahaan tidak tersedia");
                    tvLocation.setText(currentJob.getLocation() != null ? currentJob.getLocation() : "Lokasi tidak tersedia");

                    // Set tags dengan null safety
                    tvTag1.setText(currentJob.getTag1() != null ? currentJob.getTag1() : "");
                    tvTag2.setText(currentJob.getTag2() != null ? currentJob.getTag2() : "");
                    tvTag3.setText(currentJob.getTag3() != null ? currentJob.getTag3() : "");

                    // Sembunyikan tag yang kosong
                    tvTag1.setVisibility(currentJob.getTag1() != null && !currentJob.getTag1().isEmpty() ? View.VISIBLE : View.GONE);
                    tvTag2.setVisibility(currentJob.getTag2() != null && !currentJob.getTag2().isEmpty() ? View.VISIBLE : View.GONE);
                    tvTag3.setVisibility(currentJob.getTag3() != null && !currentJob.getTag3().isEmpty() ? View.VISIBLE : View.GONE);

                    // Set job details grid
                    tvJobTypeValue.setText(currentJob.getTipePekerjaan() != null ? currentJob.getTipePekerjaan() : "Full Time");
                    tvSalary.setText(currentJob.getGajiRange() != null ? currentJob.getGajiRange() : "Dirahasiakan");
                    tvWorkingHours.setText(currentJob.getModeKerja() != null ? currentJob.getModeKerja() : "Fleksibel");
                    tvExpertise.setText(currentJob.getTag1() != null ? currentJob.getTag1() : "Menyesuaikan");

                    // Set deskripsi
                    String description = currentJob.getDeskripsi();
                    if (description != null && !description.isEmpty()) {
                        tvJobDescription.setText(description);
                    } else {
                        tvJobDescription.setText("Lowongan " + currentJob.getJobTitle() +
                                " di " + currentJob.getCompanyName() +
                                " membuka kesempatan bagi profesional yang berpengalaman di bidang " +
                                (currentJob.getTag1() != null ? currentJob.getTag1() : "terkait") + ".\n\n" +
                                "Bergabunglah dengan tim kami yang dinamis dan berkembang pesat.");
                    }

                    // Set kualifikasi
                    String kualifikasi = currentJob.getKualifikasi();
                    if (kualifikasi != null && !kualifikasi.isEmpty()) {
                        tvRequirements.setText(kualifikasi);
                    } else {
                        StringBuilder defaultRequirements = new StringBuilder();
                        if (currentJob.getTag1() != null && !currentJob.getTag1().isEmpty()) {
                            defaultRequirements.append("• Pengalaman di bidang ").append(currentJob.getTag1()).append("\n");
                        }
                        if (currentJob.getTag2() != null && !currentJob.getTag2().isEmpty()) {
                            defaultRequirements.append("• Memahami ").append(currentJob.getTag2()).append("\n");
                        }
                        if (currentJob.getTag3() != null && !currentJob.getTag3().isEmpty()) {
                            defaultRequirements.append("• Dapat bekerja ").append(currentJob.getTag3()).append("\n");
                        }
                        if (currentJob.getLocation() != null && !currentJob.getLocation().isEmpty()) {
                            defaultRequirements.append("• Berdomisili di ").append(currentJob.getLocation()).append("\n");
                        }

                        if (defaultRequirements.length() == 0) {
                            defaultRequirements.append("• Pengalaman di bidang terkait\n");
                            defaultRequirements.append("• Kemampuan komunikasi yang baik\n");
                            defaultRequirements.append("• Dapat bekerja dalam tim\n");
                            defaultRequirements.append("• Memiliki motivasi tinggi");
                        }

                        tvRequirements.setText(defaultRequirements.toString());
                    }

                    // Jika noTelp kosong, fetch dari database
                    if (currentJob.getNoTelp() == null || currentJob.getNoTelp().isEmpty()) {
                        fetchNoTelpFromDatabase();
                    } else {
                        setupWhatsAppButton();
                    }
                } else {
                    Toast.makeText(this, "Data lowongan tidak tersedia", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Terjadi kesalahan saat membuka detail lowongan", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in loadJobData: " + e.getMessage(), e);
            Toast.makeText(this, "Gagal memuat data lowongan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchNoTelpFromDatabase() {
        if (currentJob == null) {
            Log.e(TAG, "❌ currentJob is null, cannot fetch telephone");
            return;
        }

        if (currentJob.getIdLowongan() == 0) {
            Log.e(TAG, "❌ Cannot fetch no_telp: Invalid job ID (0)");
            runOnUiThread(() -> {
                currentJob.setNoTelp("");
                setupWhatsAppButton();
            });
            return;
        }

        new Thread(() -> {
            try {
                String url = SUPABASE_URL + "/rest/v1/lowongan?select=no_telp&id_lowongan=eq." + currentJob.getIdLowongan();

                Log.d(TAG, "🔍 Fetching no_telp from URL: " + url);

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("apikey", SUPABASE_API_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "🔍 Fetch no_telp - Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream responseStream = connection.getInputStream();
                    Scanner scanner = new Scanner(responseStream).useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";

                    Log.d(TAG, "📨 API Response for no_telp: " + response);

                    if (response == null || response.trim().isEmpty() || response.equals("[]")) {
                        Log.e(TAG, "❌ Empty response from API for job ID: " + currentJob.getIdLowongan());
                        runOnUiThread(() -> {
                            currentJob.setNoTelp("");
                            setupWhatsAppButton();
                        });
                        return;
                    }

                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray.length() > 0) {
                        JSONObject jobData = jsonArray.getJSONObject(0);
                        String phoneNumber = jobData.optString("no_telp", "");

                        runOnUiThread(() -> {
                            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                                currentJob.setNoTelp(phoneNumber);
                                Log.d(TAG, "✅ Fetched no_telp from database: " + phoneNumber);
                            } else {
                                Log.w(TAG, "⚠️ No telephone number found for job ID: " + currentJob.getIdLowongan());
                                currentJob.setNoTelp("");
                            }
                            setupWhatsAppButton();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Log.e(TAG, "❌ No data found for job ID: " + currentJob.getIdLowongan());
                            currentJob.setNoTelp("");
                            setupWhatsAppButton();
                        });
                    }
                } else {
                    Log.e(TAG, "❌ HTTP Error fetching no_telp: " + responseCode);
                    runOnUiThread(() -> {
                        currentJob.setNoTelp("");
                        setupWhatsAppButton();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error fetching no_telp: " + e.getMessage());
                runOnUiThread(() -> {
                    currentJob.setNoTelp("");
                    setupWhatsAppButton();
                });
            }
        }).start();
    }

    private void setupWhatsAppButton() {
        if (currentJob == null) return;

        String phoneNumber = currentJob.getNoTelp();

        Log.d(TAG, "========================================");
        Log.d(TAG, "🔍 WHATSAPP SETUP DEBUG");
        Log.d(TAG, "Job ID: " + currentJob.getIdLowongan());
        Log.d(TAG, "Company: " + currentJob.getCompanyName());
        Log.d(TAG, "Job Title: " + currentJob.getJobTitle());
        Log.d(TAG, "Raw Phone: '" + phoneNumber + "'");
        Log.d(TAG, "Phone Length: " + (phoneNumber != null ? phoneNumber.length() : "null"));
        Log.d(TAG, "Phone Empty: " + (phoneNumber == null || phoneNumber.trim().isEmpty()));
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

        btnWhatsApp.setOnClickListener(v -> {
            if (btnWhatsApp.isEnabled()) {
                openWhatsApp();
            } else {
                Toast.makeText(this, "Fitur WhatsApp tidak tersedia untuk lowongan ini", Toast.LENGTH_SHORT).show();
            }
        });

        // Ganti: saat tekan "Lamar Pekerjaan Ini" -> munculkan BottomSheet untuk catatan
        btnApply.setOnClickListener(v -> showLamaranBottomSheet());
    }

    /**
     * Menampilkan BottomSheet dialog untuk input catatan sebelum mengirim lamaran.
     */
    private void showLamaranBottomSheet() {
        if (currentJob == null) {
            Toast.makeText(this, "Data lowongan tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottomsheet_lamaran, null);
        dialog.setContentView(view);

        TextInputEditText edtCatatan = view.findViewById(R.id.edtCatatanHRD);
        MaterialButton btnKirim = view.findViewById(R.id.btnKirimLamaran);
        MaterialButton btnBatal = view.findViewById(R.id.btnBatalLamaran);

        // Prefill (opsional) contoh: nama posisi singkat
        edtCatatan.setHint("Contoh: Saya memiliki 3 tahun pengalaman..., saya tersedia Senin-Jumat");

        btnKirim.setOnClickListener(v -> {
            String catatan = edtCatatan.getText() != null ? edtCatatan.getText().toString().trim() : "";
            // Validasi opsional (jika mau wajib isi, uncomment)
            // if (catatan.isEmpty()) { edtCatatan.setError("Harap isi catatan atau tetap kosong jika tidak ada"); return; }

            // Panggil upload (akan dijalankan di thread background)
            uploadLamaran(catatan);

            dialog.dismiss();
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Upload lamaran ke Supabase (tabel 'lamaran').
     * Mengirim: id_lowongan, catatan, tanggal (unix millis).
     */
    private void uploadLamaran(String catatan) {
        if (currentJob == null) {
            Toast.makeText(this, "Data lowongan tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("id_lowongan", currentJob.getIdLowongan());
            body.put("catatan", catatan);
            body.put("tanggal", System.currentTimeMillis());

            // Jalankan di thread agar tidak blocking UI
            new Thread(() -> {
                // BUAT VARIABLE FINAL DI LUAR TRY-CATCH
                final int[] responseCode = {-1};

                try {
                    URL url = new URL(SUPABASE_URL + "/rest/v1/lamaran");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("apikey", SUPABASE_API_KEY);
                    connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    byte[] out = body.toString().getBytes("UTF-8");
                    connection.getOutputStream().write(out);

                    responseCode[0] = connection.getResponseCode();
                    Log.d(TAG, "Upload lamaran response code: " + responseCode[0]);

                    // Baca response (opsional)
                    InputStream responseStream = connection.getInputStream();
                    Scanner scanner = new Scanner(responseStream).useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    Log.d(TAG, "Upload lamaran response body: " + response);

                    runOnUiThread(() -> {
                        if (responseCode[0] == 201 || responseCode[0] == 200) {
                            Toast.makeText(JobDetailActivity.this, "Lamaran berhasil dikirim!", Toast.LENGTH_LONG).show();
                            // TODO: bisa tampilkan snackbar atau navigasi
                        } else {
                            Toast.makeText(JobDetailActivity.this, "Gagal mengirim lamaran (code " + responseCode[0] + ")", Toast.LENGTH_LONG).show();
                        }
                    });

                    connection.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error uploadLamaran: " + e.getMessage(), e);
                    runOnUiThread(() -> Toast.makeText(JobDetailActivity.this, "Error mengirim lamaran: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "❌ Preparing lamaran error: " + e.getMessage(), e);
            Toast.makeText(this, "Error menyiapkan data lamaran", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWhatsApp() {
        if (currentJob == null) {
            Toast.makeText(this, "❌ Data lowongan tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = currentJob.getNoTelp();

        Log.d(TAG, "🎯 OPENING WHATSAPP");
        Log.d(TAG, "Company: " + currentJob.getCompanyName());
        Log.d(TAG, "Job: " + currentJob.getJobTitle());
        Log.d(TAG, "Original Phone: '" + phoneNumber + "'");

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(this, "❌ Nomor WhatsApp tidak tersedia untuk lowongan ini", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bersihkan nomor - hapus semua non-digit
        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");
        Log.d(TAG, "Cleaned Phone (numeric only): " + cleanNumber);

        if (cleanNumber.isEmpty()) {
            Toast.makeText(this, "❌ Format nomor tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handle berbagai format nomor
        if (cleanNumber.startsWith("62")) {
            // Nomor sudah dalam format internasional Indonesia, langsung gunakan
            Log.d(TAG, "✅ Phone already in international format (62)");
        } else if (cleanNumber.startsWith("0")) {
            // Ganti 0 dengan 62
            cleanNumber = "62" + cleanNumber.substring(1);
            Log.d(TAG, "🔄 Converted 0 to 62 format: " + cleanNumber);
        } else {
            // Tambahkan 62 jika tidak ada kode negara
            cleanNumber = "62" + cleanNumber;
            Log.d(TAG, "➕ Added 62 prefix: " + cleanNumber);
        }

        // Pastikan tidak ada karakter tambahan
        cleanNumber = cleanNumber.replaceAll("[^0-9]", "");

        // Validasi panjang nomor (minimal 10 digit setelah 62)
        if (cleanNumber.length() < 12) {
            Toast.makeText(this, "❌ Nomor telepon terlalu pendek", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Phone number too short: " + cleanNumber);
            return;
        }

        String message = "Halo, saya tertarik dengan lowongan *" + currentJob.getJobTitle() +
                "* di *" + currentJob.getCompanyName() + "*. " +
                "Bisakah saya mendapatkan informasi lebih lanjut?";

        String url = "https://wa.me/" + cleanNumber + "?text=" + Uri.encode(message);

        Log.d(TAG, "🔗 Final WhatsApp URL: " + url);
        Log.d(TAG, "📞 Final Phone Number: " + cleanNumber);
        Log.d(TAG, "💬 Message: " + message);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            Log.d(TAG, "✅ WhatsApp opened successfully");
        } catch (Exception e) {
            Toast.makeText(this, "❌ Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Error opening WhatsApp: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "JobDetailActivity destroyed");
    }
}
