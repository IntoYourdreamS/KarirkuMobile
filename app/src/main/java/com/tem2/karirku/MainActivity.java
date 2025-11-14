package com.tem2.karirku;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private ImageView icGoogle;
    private ImageView splashScreen;
    private RelativeLayout loginForm;

    // ✅ Ganti dengan project Supabase kamu
    private final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi view untuk splash screen
        splashScreen = findViewById(R.id.splashScreen);
        loginForm = findViewById(R.id.loginForm);

        setupSplashScreen();
    }

    private void setupSplashScreen() {
        loginForm.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Sembunyikan splash screen
                splashScreen.setVisibility(View.GONE);

                // Tampilkan login form
                loginForm.setVisibility(View.VISIBLE);

                // Setup login logic setelah splash selesai
                setupLoginLogic();
            }
        }, 1000); // 1 DETIK
    }

    private void setupLoginLogic() {
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_Password);
        icGoogle = findViewById(R.id.ic_google);

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi semua field!", Toast.LENGTH_SHORT).show();
            } else {
                loginManual(email, password);
            }
        });

        icGoogle.setOnClickListener(v -> loginWithGoogle());

        // Tambahkan click listener untuk daftar text
        findViewById(R.id.textView5).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, register.class);
            startActivity(intent);
        });

        // Toggle password visibility
        ImageView imgTogglePassword = findViewById(R.id.imgTogglePassword);
        imgTogglePassword.setOnClickListener(v -> {
            if (edtPassword.getInputType() == 129) { // Password hidden
                edtPassword.setInputType(1); // Text visible
                imgTogglePassword.setImageResource(R.drawable.eyeon);
            } else {
                edtPassword.setInputType(129); // Password hidden
                imgTogglePassword.setImageResource(R.drawable.eyeoff);
            }
            edtPassword.setSelection(edtPassword.getText().length());
        });
    }

    // 🔹 LOGIN MANUAL VIA REST API
    private void loginManual(String email, String password) {
        String url = SUPABASE_URL + "/rest/v1/pengguna?email=eq." + email + "&select=*";

        Log.d("LOGIN_DEBUG", "Request URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("LOGIN_DEBUG", "Response: " + response.toString());

                    if (response.length() > 0) {
                        try {
                            JSONObject user = response.getJSONObject(0);
                            String pass = user.optString("password", "");
                            boolean emailVerified = user.optBoolean("email_verified", false);

                            if (pass.equals(password)) {
                                if (emailVerified) {
                                    Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, beranda.class));
                                    finish();
                                } else {
                                    Toast.makeText(this,
                                            "Email belum diverifikasi! Silakan cek email Anda.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, "Password salah", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "Parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("LOGIN_ERROR", "JSON error: ", e);
                        }
                    } else {
                        Toast.makeText(this, "Email tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("LOGIN_ERROR", "Volley error: " + error.toString());
                    Toast.makeText(this, "Gagal koneksi ke server: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    // 🔹 LOGIN VIA GOOGLE SUPABASE
    private void loginWithGoogle() {
        String redirectUrl = SUPABASE_URL + "/auth/v1/authorize?provider=google"
                + "&redirect_to=" + Uri.encode("karirku://auth-callback"); // callback custom scheme

        Log.d("LOGIN_GOOGLE", "Redirect ke: " + redirectUrl);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
        startActivity(browserIntent);
    }

    // 🔹 Tangkap callback dari Supabase (Google OAuth)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if (data != null) {
            String fullUri = data.toString();
            Log.d("SUPABASE_CALLBACK", "Data: " + fullUri);

            if (fullUri.contains("access_token")) {
                String token = fullUri.substring(fullUri.indexOf("access_token=") + 13);
                if (token.contains("&")) token = token.substring(0, token.indexOf("&"));
                Toast.makeText(this, "Login Google sukses!", Toast.LENGTH_SHORT).show();
                Log.d("SUPABASE_TOKEN", token);
                startActivity(new Intent(this, beranda.class));
                finish();
            } else {
                Toast.makeText(this, "Gagal ambil token Google!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}