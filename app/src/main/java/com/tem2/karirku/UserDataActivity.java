package com.tem2.karirku;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserDataActivity extends AppCompatActivity {

    private ImageView imgProfile, btnEditPhoto, btnBack;
    private EditText inputNama, inputEmail, inputNoHp, inputAlamat, inputPengalaman;
    private TextView inputTanggalLahir, btnEdit;
    private Spinner spinnerGender;
    private Button btnSave;

    private Uri photoUri;
    private Bitmap photoBitmap;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    private SessionManager sessionManager;
    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;

    private int idPencaker = 0;
    private int idPengguna = 0;
    private boolean isEditMode = false;

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferensi_kerja);

        sessionManager = new SessionManager(this);
        requestQueue = Volley.newRequestQueue(this);
        idPengguna = sessionManager.getUserId();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        initViews();
        initSpinner();
        initGalleryPicker();
        initListeners();
        loadUserData();
    }

    private void initViews() {
        imgProfile = findViewById(R.id.imgProfile);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        inputNama = findViewById(R.id.inputNama);
        inputEmail = findViewById(R.id.inputEmail);
        inputNoHp = findViewById(R.id.inputNoHp);
        inputAlamat = findViewById(R.id.inputAlamat);
        inputPengalaman = findViewById(R.id.inputPengalaman);
        inputTanggalLahir = findViewById(R.id.inputTanggalLahir);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);

        // Set semua input disabled dulu
        setEditMode(false);
    }

    private void initSpinner() {
        String[] genderItems = {"Pilih Gender", "male", "female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                genderItems
        );
        spinnerGender.setAdapter(adapter);
    }

    private void initGalleryPicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        photoUri = result.getData().getData();
                        try {
                            photoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                            imgProfile.setImageBitmap(photoBitmap);
                        } catch (IOException e) {
                            Log.e("USER_DATA", "Error loading image: " + e.getMessage());
                        }
                    }
                }
        );
    }

    private void initListeners() {
        btnEditPhoto.setOnClickListener(v -> {
            if (isEditMode) {
                openGallery();
            } else {
                Toast.makeText(this, "Klik Edit untuk mengubah foto", Toast.LENGTH_SHORT).show();
            }
        });

        inputTanggalLahir.setOnClickListener(v -> {
            if (isEditMode) {
                showDatePicker();
            }
        });

        btnSave.setOnClickListener(v -> saveUserData());
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            setEditMode(isEditMode);
            btnEdit.setText(isEditMode ? "Batal" : "Edit");
        });
    }

    private void setEditMode(boolean enabled) {
        inputNama.setEnabled(enabled);
        inputEmail.setEnabled(enabled);
        inputNoHp.setEnabled(enabled);
        inputAlamat.setEnabled(enabled);
        inputPengalaman.setEnabled(enabled);
        inputTanggalLahir.setEnabled(enabled);
        spinnerGender.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnEditPhoto.setEnabled(enabled);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String date = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", dayOfMonth);
                    inputTanggalLahir.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadUserData() {
        progressDialog.show();

        String url = SUPABASE_URL + "/rest/v1/pencaker?id_pengguna=eq." + idPengguna + "&select=*";

        Log.d("USER_DATA", "Loading data for user ID: " + idPengguna);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressDialog.dismiss();
                    Log.d("USER_DATA", "Load response length: " + response.length());
                    Log.d("USER_DATA", "Load response: " + response.toString());

                    if (response.length() > 0) {
                        try {
                            JSONObject data = response.getJSONObject(0);
                            populateFields(data);
                            Log.d("USER_DATA", "✅ Data loaded - id_pencaker: " + idPencaker);
                        } catch (JSONException e) {
                            Log.e("USER_DATA", "Error parsing data: " + e.getMessage());
                        }
                    } else {
                        Log.d("USER_DATA", "❌ No data found after save!");
                        // Reset idPencaker jika tidak ada data
                        idPencaker = 0;
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("USER_DATA", "Error loading data: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("USER_DATA", "Load error response: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void populateFields(JSONObject data) {
        try {
            idPencaker = data.optInt("id_pencaker", 0);

            inputNama.setText(data.optString("nama_lengkap", ""));
            inputEmail.setText(data.optString("email_pencaker", ""));
            inputNoHp.setText(data.optString("no_hp", ""));
            inputTanggalLahir.setText(data.optString("tanggal_lahir", ""));
            inputAlamat.setText(data.optString("alamat", ""));
            inputPengalaman.setText(data.optString("pengalaman_tahun", ""));

            String gender = data.optString("gender", "");
            if (gender.equals("male")) {
                spinnerGender.setSelection(1);
            } else if (gender.equals("female")) {
                spinnerGender.setSelection(2);
            }

            String fotoUrl = data.optString("foto_profil_url", "");
            if (!fotoUrl.isEmpty()) {
                Glide.with(this)
                        .load(fotoUrl)
                        .circleCrop()
                        .into(imgProfile);
            }

            Log.d("USER_DATA", "Data loaded successfully. ID Pencaker: " + idPencaker);
        } catch (Exception e) {
            Log.e("USER_DATA", "Error populating fields: " + e.getMessage());
        }
    }

    private void saveUserData() {
        String nama = inputNama.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String noHp = inputNoHp.getText().toString().trim();
        String tglLahir = inputTanggalLahir.getText().toString().trim();
        String alamat = inputAlamat.getText().toString().trim();
        String pengalaman = inputPengalaman.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (nama.isEmpty() || email.isEmpty() || noHp.isEmpty() || tglLahir.isEmpty() ||
                alamat.isEmpty() || gender.equals("Pilih Gender")) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Menyimpan data...");
        progressDialog.show();

        if (photoBitmap != null) {
            uploadPhotoThenSaveData(nama, email, noHp, tglLahir, gender, alamat, pengalaman);
        } else {
            saveToDatabase(nama, email, noHp, tglLahir, gender, alamat, pengalaman, null, null);
        }
    }

    private void uploadPhotoThenSaveData(String nama, String email, String noHp, String tglLahir,
                                         String gender, String alamat, String pengalaman) {

        long timestamp = System.currentTimeMillis();
        String fileName = "profile_" + idPengguna + "_" + timestamp + ".jpg";
        String storagePath = idPengguna + "/" + fileName;
        String uploadUrl = SUPABASE_URL + "/storage/v1/object/profile-pictures/" + storagePath;

        Log.d("USER_DATA", "Uploading photo to: " + uploadUrl);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                uploadUrl,
                response -> {
                    String fotoUrl = SUPABASE_URL + "/storage/v1/object/public/profile-pictures/" + storagePath;
                    Log.d("USER_DATA", "✅ Photo uploaded: " + fotoUrl);
                    saveToDatabase(nama, email, noHp, tglLahir, gender, alamat, pengalaman, fotoUrl, storagePath);
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("USER_DATA", "❌ Upload failed: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("USER_DATA", "Status: " + error.networkResponse.statusCode);
                        Log.e("USER_DATA", "Response: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Gagal upload foto", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "image/jpeg");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return imageBytes;
            }
        };

        requestQueue.add(multipartRequest);
    }

    private void saveToDatabase(String nama, String email, String noHp, String tglLahir,
                                String gender, String alamat, String pengalaman,
                                String fotoUrl, String fotoPath) {

        // Selalu gunakan INSERT seperti web, biarkan database yang generate id_pencaker
        String url = SUPABASE_URL + "/rest/v1/pencaker";

        JSONObject body = new JSONObject();
        try {
            // SAMA PERSIS dengan data yang dikirim web PHP
            body.put("id_pengguna", idPengguna);
            body.put("nama_lengkap", nama);
            body.put("email_pencaker", email);
            body.put("no_hp", noHp);
            body.put("alamat", alamat);
            body.put("tanggal_lahir", tglLahir);
            body.put("gender", gender);
            body.put("pengalaman_tahun", pengalaman);
            body.put("dibuat_pada", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

            if (fotoUrl != null) {
                body.put("foto_profil_url", fotoUrl);
                body.put("foto_profil_path", fotoPath);
            }

        } catch (JSONException e) {
            Log.e("USER_DATA", "Error creating JSON: " + e.getMessage());
            progressDialog.dismiss();
            return;
        }

        Log.d("USER_DATA", "INSERT data for id_pengguna: " + idPengguna);
        Log.d("USER_DATA", "Request body: " + body.toString());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Log.d("USER_DATA", "✅ INSERT successful! Response: " + response);

                    Toast.makeText(this, "✅ Data berhasil disimpan!", Toast.LENGTH_SHORT).show();
                    setEditMode(false);
                    isEditMode = false;
                    btnEdit.setText("Edit");
                    sessionManager.updateUserProfile(nama, noHp);

                    // Load data ulang untuk mendapatkan id_pencaker yang baru
                    new android.os.Handler().postDelayed(() -> {
                        loadUserData();
                    }, 1500);
                },
                error -> {
                    Log.e("USER_DATA", "❌ INSERT failed: " + error.toString());

                    if (error.networkResponse != null) {
                        String errorResponse = new String(error.networkResponse.data);
                        Log.e("USER_DATA", "Error status: " + error.networkResponse.statusCode);
                        Log.e("USER_DATA", "Error response: " + errorResponse);

                        // Jika INSERT gagal karena data sudah ada, coba UPDATE
                        if (error.networkResponse.statusCode == 409 || errorResponse.contains("duplicate") || errorResponse.contains("already exists")) {
                            Log.d("USER_DATA", "Data already exists, trying UPDATE...");
                            updateExistingData(nama, email, noHp, tglLahir, gender, alamat, pengalaman, fotoUrl, fotoPath);
                            return;
                        }
                    }

                    progressDialog.dismiss();
                    Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                // Sama seperti web - minimal preference
                headers.put("Prefer", "return=minimal");
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    return body.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }
        };

        requestQueue.add(request);
    }

    private void updateExistingData(String nama, String email, String noHp, String tglLahir,
                                    String gender, String alamat, String pengalaman,
                                    String fotoUrl, String fotoPath) {

        // Jika INSERT gagal karena data sudah ada, update berdasarkan id_pengguna
        String url = SUPABASE_URL + "/rest/v1/pencaker?id_pengguna=eq." + idPengguna;

        JSONObject body = new JSONObject();
        try {
            body.put("nama_lengkap", nama);
            body.put("email_pencaker", email);
            body.put("no_hp", noHp);
            body.put("alamat", alamat);
            body.put("tanggal_lahir", tglLahir);
            body.put("gender", gender);
            body.put("pengalaman_tahun", pengalaman);

            if (fotoUrl != null) {
                body.put("foto_profil_url", fotoUrl);
                body.put("foto_profil_path", fotoPath);
            }
        } catch (JSONException e) {
            Log.e("USER_DATA", "Error creating update JSON: " + e.getMessage());
            progressDialog.dismiss();
            return;
        }

        StringRequest updateRequest = new StringRequest(Request.Method.PATCH, url,
                response -> {
                    progressDialog.dismiss();
                    Log.d("USER_DATA", "✅ UPDATE successful! Response: " + response);
                    Toast.makeText(this, "✅ Data berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    setEditMode(false);
                    isEditMode = false;
                    btnEdit.setText("Edit");
                    sessionManager.updateUserProfile(nama, noHp);

                    // Load data ulang
                    new android.os.Handler().postDelayed(() -> {
                        loadUserData();
                    }, 1000);
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("USER_DATA", "❌ UPDATE failed: " + error.toString());
                    Toast.makeText(this, "Gagal memperbarui data", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    return body.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }
        };

        requestQueue.add(updateRequest);
    }

    private void tryInsertAsNew(String nama, String email, String noHp, String tglLahir,
                                String gender, String alamat, String pengalaman,
                                String fotoUrl, String fotoPath) {
        Log.d("USER_DATA", "Trying INSERT as new data...");

        String url = SUPABASE_URL + "/rest/v1/pencaker";

        JSONObject body = new JSONObject();
        try {
            body.put("id_pengguna", idPengguna);
            body.put("nama_lengkap", nama);
            body.put("email_pencaker", email);
            body.put("no_hp", noHp);
            body.put("alamat", alamat);
            body.put("tanggal_lahir", tglLahir);
            body.put("gender", gender);
            body.put("pengalaman_tahun", pengalaman);

            if (fotoUrl != null) {
                body.put("foto_profil_url", fotoUrl);
                body.put("foto_profil_path", fotoPath);
            }
        } catch (JSONException e) {
            Log.e("USER_DATA", "Error creating JSON: " + e.getMessage());
            return;
        }

        StringRequest insertRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Log.d("USER_DATA", "✅ INSERT as new successful!");
                    Toast.makeText(this, "✅ Data berhasil disimpan!", Toast.LENGTH_SHORT).show();
                    setEditMode(false);
                    isEditMode = false;
                    btnEdit.setText("Edit");
                    sessionManager.updateUserProfile(nama, noHp);

                    // Load data untuk mendapatkan id_pencaker
                    new android.os.Handler().postDelayed(() -> {
                        loadUserData();
                    }, 1000);
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("USER_DATA", "❌ INSERT as new failed: " + error.toString());
                    Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    return body.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }
        };

        requestQueue.add(insertRequest);
    }

    private void tryUpdateExisting(String nama, String email, String noHp, String tglLahir,
                                   String gender, String alamat, String pengalaman,
                                   String fotoUrl, String fotoPath) {
        Log.d("USER_DATA", "Trying UPDATE existing data...");

        String url = SUPABASE_URL + "/rest/v1/pencaker?id_pengguna=eq." + idPengguna;

        JSONObject body = new JSONObject();
        try {
            body.put("nama_lengkap", nama);
            body.put("email_pencaker", email);
            body.put("no_hp", noHp);
            body.put("alamat", alamat);
            body.put("tanggal_lahir", tglLahir);
            body.put("gender", gender);
            body.put("pengalaman_tahun", pengalaman);

            if (fotoUrl != null) {
                body.put("foto_profil_url", fotoUrl);
                body.put("foto_profil_path", fotoPath);
            }
        } catch (JSONException e) {
            Log.e("USER_DATA", "Error creating JSON: " + e.getMessage());
            return;
        }

        StringRequest updateRequest = new StringRequest(Request.Method.PATCH, url,
                response -> {
                    Log.d("USER_DATA", "✅ Data updated successfully!");
                    Log.d("USER_DATA", "Update response: " + response);

                    // Tunggu sebentar lalu load data untuk konfirmasi
                    new android.os.Handler().postDelayed(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "✅ Data berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                        setEditMode(false);
                        isEditMode = false;
                        btnEdit.setText("Edit");
                        sessionManager.updateUserProfile(nama, noHp);

                        // Load data dengan delay untuk pastikan update sudah propagate
                        new android.os.Handler().postDelayed(() -> {
                            loadUserData();
                        }, 1000);
                    }, 500);
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("USER_DATA", "❌ Update failed: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("USER_DATA", "Update error: " + new String(error.networkResponse.data));
                    }

                    tryLastResort(nama, email, noHp, tglLahir, gender, alamat, pengalaman, fotoUrl, fotoPath);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=representation"); // Ubah ke representation untuk dapat response
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    return body.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }
        };

        requestQueue.add(updateRequest);
    }
    private void tryLastResort(String nama, String email, String noHp, String tglLahir,
                               String gender, String alamat, String pengalaman,
                               String fotoUrl, String fotoPath) {
        Log.d("USER_DATA", "Trying last resort approach...");

        // Cek dulu apakah data sudah ada
        String checkUrl = SUPABASE_URL + "/rest/v1/pencaker?id_pengguna=eq." + idPengguna + "&select=id_pencaker";

        JsonArrayRequest checkRequest = new JsonArrayRequest(Request.Method.GET, checkUrl, null,
                response -> {
                    if (response.length() > 0) {
                        // Data sudah ada, gunakan PUT untuk replace
                        tryPutReplace(nama, email, noHp, tglLahir, gender, alamat, pengalaman, fotoUrl, fotoPath);
                    } else {
                        // Data belum ada, coba INSERT dengan approach berbeda
                        trySimpleInsert(nama, email, noHp, tglLahir, gender, alamat, pengalaman, fotoUrl, fotoPath);
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("USER_DATA", "❌ Check existing failed: " + error.toString());
                    Toast.makeText(this, "Gagal memeriksa data existing", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                return headers;
            }
        };

        requestQueue.add(checkRequest);
    }

    private void tryPutReplace(String nama, String email, String noHp, String tglLahir,
                               String gender, String alamat, String pengalaman,
                               String fotoUrl, String fotoPath) {
        Log.d("USER_DATA", "Trying PUT to replace existing data...");

        String url = SUPABASE_URL + "/rest/v1/pencaker?id_pengguna=eq." + idPengguna;

        JSONObject body = new JSONObject();
        try {
            body.put("id_pengguna", idPengguna);
            body.put("nama_lengkap", nama);
            body.put("email_pencaker", email);
            body.put("no_hp", noHp);
            body.put("alamat", alamat);
            body.put("tanggal_lahir", tglLahir);
            body.put("gender", gender);
            body.put("pengalaman_tahun", pengalaman);
            body.put("dibuat_pada", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

            if (fotoUrl != null) {
                body.put("foto_profil_url", fotoUrl);
                body.put("foto_profil_path", fotoPath);
            }
        } catch (JSONException e) {
            Log.e("USER_DATA", "Error creating JSON: " + e.getMessage());
            return;
        }

        StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                response -> {
                    progressDialog.dismiss();
                    Log.d("USER_DATA", "✅ Data replaced via PUT!");
                    Toast.makeText(this, "✅ Data berhasil disimpan!", Toast.LENGTH_SHORT).show();
                    setEditMode(false);
                    isEditMode = false;
                    btnEdit.setText("Edit");
                    sessionManager.updateUserProfile(nama, noHp);
                    loadUserData();
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("USER_DATA", "❌ PUT failed: " + error.toString());
                    Toast.makeText(this, "Gagal menyimpan data setelah beberapa percobaan", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    return body.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }
        };

        requestQueue.add(putRequest);
    }

    private void trySimpleInsert(String nama, String email, String noHp, String tglLahir,
                                 String gender, String alamat, String pengalaman,
                                 String fotoUrl, String fotoPath) {
        Log.d("USER_DATA", "Trying simple INSERT without optional fields...");

        String url = SUPABASE_URL + "/rest/v1/pencaker";

        JSONObject simpleBody = new JSONObject();
        try {
            // Hanya field yang paling essential
            simpleBody.put("id_pengguna", idPengguna);
            simpleBody.put("nama_lengkap", nama);
            simpleBody.put("email_pencaker", email);
            simpleBody.put("no_hp", noHp);
            // Skip field optional dulu
        } catch (JSONException e) {
            Log.e("USER_DATA", "Error creating simple JSON: " + e.getMessage());
            return;
        }

        StringRequest simpleRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Log.d("USER_DATA", "✅ Simple data saved!");
                    Toast.makeText(this, "Data dasar tersimpan, silakan lengkapi kemudian", Toast.LENGTH_SHORT).show();
                    setEditMode(false);
                    isEditMode = false;
                    btnEdit.setText("Edit");
                    sessionManager.updateUserProfile(nama, noHp);
                    loadUserData();
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("USER_DATA", "❌ Simple insert failed: " + error.toString());
                    Toast.makeText(this, "Gagal menyimpan data ke server", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    return simpleBody.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }
        };

        requestQueue.add(simpleRequest);
    }

    private void tryAlternativeSave(String nama, String email, String noHp, String tglLahir,
                                    String gender, String alamat, String pengalaman,
                                    String fotoUrl, String fotoPath) {
        Log.d("USER_DATA", "Trying alternative save approach...");

        // Coba dengan UPSERT menggunakan on_conflict
        String url = SUPABASE_URL + "/rest/v1/pencaker?on_conflict=id_pengguna";

        JSONObject body = new JSONObject();
        try {
            // Untuk UPSERT, kita hanya perlu id_pengguna sebagai conflict target
            body.put("id_pengguna", idPengguna);
            body.put("nama_lengkap", nama);
            body.put("email_pencaker", email);
            body.put("no_hp", noHp);
            body.put("tanggal_lahir", tglLahir);
            body.put("gender", gender);
            body.put("alamat", alamat);
            body.put("pengalaman_tahun", pengalaman);

            if (fotoUrl != null) {
                body.put("foto_profil_url", fotoUrl);
                body.put("foto_profil_path", fotoPath);
            }
        } catch (JSONException e) {
            Log.e("USER_DATA", "Error creating JSON: " + e.getMessage());
            return;
        }

        StringRequest upsertRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Log.d("USER_DATA", "✅ Data saved via UPSERT!");
                    Log.d("USER_DATA", "Response: " + response);

                    // Parse response untuk mendapatkan id_pencaker
                    if (response != null && !response.trim().isEmpty() && !response.equals("[]")) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() > 0) {
                                JSONObject firstItem = jsonArray.getJSONObject(0);
                                idPencaker = firstItem.getInt("id_pencaker");
                                Log.d("USER_DATA", "UPSERT id_pencaker: " + idPencaker);
                            }
                        } catch (JSONException e) {
                            Log.e("USER_DATA", "Error parsing UPSERT response: " + e.getMessage());
                        }
                    }

                    Toast.makeText(this, "✅ Data berhasil disimpan!", Toast.LENGTH_SHORT).show();
                    setEditMode(false);
                    isEditMode = false;
                    btnEdit.setText("Edit");
                    sessionManager.updateUserProfile(nama, noHp);
                    loadUserData();
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("USER_DATA", "❌ UPSERT failed: " + error.toString());
                    Toast.makeText(this, "Gagal menyimpan data setelah beberapa percobaan", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=representation");
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    return body.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }
        };

        requestQueue.add(upsertRequest);
    }
}