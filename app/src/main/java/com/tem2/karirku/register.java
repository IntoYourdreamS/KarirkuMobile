package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class register extends AppCompatActivity {

    private EditText edtPassword;
    private ImageView imgTogglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ambil view
        TextView masukText = findViewById(R.id.textView5); // "Masuk"
        edtPassword = findViewById(R.id.edt_Password);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);

        // Klik "Masuk" → balik ke MainActivity
        masukText.setOnClickListener(v -> {
            Intent intent = new Intent(register.this, MainActivity.class);
            startActivity(intent);
            finish(); // biar RegisterActivity ditutup
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
