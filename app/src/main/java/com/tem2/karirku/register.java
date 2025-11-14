package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {

    private EditText edtNama, edtEmail, edtPassword, edtNotlp;
    private ImageView imgTogglePassword;
    private boolean isPasswordVisible = false;

    private final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView masukText = findViewById(R.id.textView5);
        edtNama = findViewById(R.id.edt_nama);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_Password);
        edtNotlp = findViewById(R.id.edt_notlp);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);

        masukText.setOnClickListener(v -> {
            startActivity(new Intent(register.this, MainActivity.class));
            finish();
        });

        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imgTogglePassword.setImageResource(R.drawable.ic_eyeoff);
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imgTogglePassword.setImageResource(R.drawable.ic_eyeon);
        }
        edtPassword.setSelection(edtPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void registerUser() {
        String nama = edtNama.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String noTlp = edtNotlp.getText().toString().trim();

        if (nama.isEmpty() || email.isEmpty() || password.isEmpty() || noTlp.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show();
            return;
        }

        sendSignupToSupabase(email, password, nama, noTlp);
    }

    private void sendSignupToSupabase(String email, String password, String nama, String noTlp) {
        String url = SUPABASE_URL + "/auth/v1/signup";

        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException e) {
            Log.e("REGISTER_ERROR", "JSON error: " + e.getMessage());
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest signupRequest = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Log.i("REGISTER_SUCCESS", "Signup success: " + response);

                    // Informasi ke user
                    Toast.makeText(this, "Registrasi berhasil! Cek email kamu untuk verifikasi.", Toast.LENGTH_LONG).show();

                    // Simpan data tambahan ke tabel pengguna (tanpa password)
                    saveUserToDatabase(nama, email, noTlp);

                    startActivity(new Intent(register.this, MainActivity.class));
                    finish();
                },
                error -> {
                    Log.e("REGISTER_ERROR", "Error: " + error.toString());
                    Toast.makeText(this, "Registrasi gagal! Periksa koneksi atau email sudah digunakan.", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(signupRequest);
    }

    private void saveUserToDatabase(String nama, String email, String noTlp) {
        String url = SUPABASE_URL + "/rest/v1/pengguna";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("nama_lengkap", nama);
            jsonBody.put("email", email);
            jsonBody.put("no_hp", noTlp);
            jsonBody.put("role", "pencaker");
        } catch (JSONException e) {
            Log.e("REGISTER_DB_ERROR", "JSON error: " + e.getMessage());
            return;
        }

        JsonObjectRequest dbRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> Log.i("REGISTER_DB", "User data saved: " + response),
                error -> Log.e("REGISTER_DB_ERROR", "Error saving to DB: " + error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(dbRequest);
    }
}