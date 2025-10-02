package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
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

        // Ambil view
        TextView daftarText = findViewById(R.id.textView5); // "Daftar"
        edtPassword = findViewById(R.id.edt_Password);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);

        // Klik "Daftar" → buka RegisterActivity
        daftarText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, register.class);
            startActivity(intent);
        });

        // Toggle password
        imgTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgTogglePassword.setImageResource(R.drawable.eyeoff);
                isPasswordVisible = false;
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imgTogglePassword.setImageResource(R.drawable.eyeon);
                isPasswordVisible = true;
            }
            edtPassword.setSelection(edtPassword.getText().length());
        });
    }
}
