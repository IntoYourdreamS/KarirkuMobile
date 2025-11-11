package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText edtPassword;
    private ImageView imgTogglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setup splash screen dulu
        setupSplashScreen();
    }

    private void setupSplashScreen() {
        // Sembunyikan SEMUA element login form dulu
        hideAllLoginElements();

        // Tampilkan splash screen selama 3 detik
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Sembunyikan splash, tampilkan login form
                showLoginForm();
            }
        }, 1000);
    }

    private void hideAllLoginElements() {
        // Sembunyikan login form
        RelativeLayout loginForm = findViewById(R.id.loginForm);
        if (loginForm != null) {
            loginForm.setVisibility(View.GONE);
        }

        // Tampilkan splash screen (harusnya sudah visible dari XML)
        ImageView splashScreen = findViewById(R.id.splashScreen);
        if (splashScreen != null) {
            splashScreen.setVisibility(View.VISIBLE);
        }
    }

    private void showLoginForm() {
        // Sembunyikan splash screen
        ImageView splashScreen = findViewById(R.id.splashScreen);
        if (splashScreen != null) {
            splashScreen.setVisibility(View.GONE);
        }

        // Tampilkan login form
        RelativeLayout loginForm = findViewById(R.id.loginForm);
        if (loginForm != null) {
            loginForm.setVisibility(View.VISIBLE);
        }

        // Setup login logic
        setupLoginLogic();
    }

    private void setupLoginLogic() {
        try {
            // Ambil view
            TextView daftarText = findViewById(R.id.textView5);
            edtPassword = findViewById(R.id.edt_Password);
            imgTogglePassword = findViewById(R.id.imgTogglePassword);

            // Klik "Daftar" → buka RegisterActivity
            daftarText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, register.class);
                    startActivity(intent);
                }
            });

            // Toggle password visibility
            imgTogglePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPasswordVisible) {
                        // Hide password
                        edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        imgTogglePassword.setImageResource(R.drawable.eyeoff);
                        isPasswordVisible = false;
                    } else {
                        // Show password
                        edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        imgTogglePassword.setImageResource(R.drawable.eyeon);
                        isPasswordVisible = true;
                    }
                    edtPassword.setSelection(edtPassword.getText().length());
                }
            });

            // Login button click
            findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Untuk testing, langsung pindah ke beranda
                    Intent intent = new Intent(MainActivity.this, beranda.class);
                    startActivity(intent);
                    finish();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}