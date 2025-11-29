package com.tem2.karirku;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UpdateAkunActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword, edtWhatsapp;
    Button btnSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_akun);

        // ==== Inisialisasi Komponen ====
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtWhatsapp = findViewById(R.id.edtWhatsapp);
        btnSimpan = findViewById(R.id.btnSimpan);

        // ==== Ambil data lama (jika ada) ====
        if (getIntent() != null) {
            String email = getIntent().getStringExtra("email");
            String whatsapp = getIntent().getStringExtra("whatsapp");

            if (email != null) edtEmail.setText(email);
            if (whatsapp != null) edtWhatsapp.setText(whatsapp);
        }

        // ==== Tombol Simpan ====
        btnSimpan.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String whatsapp = edtWhatsapp.getText().toString().trim();

            // Validasi
            if (email.isEmpty()) {
                edtEmail.setError("Email tidak boleh kosong");
                return;
            }
            if (whatsapp.isEmpty()) {
                edtWhatsapp.setError("Whatsapp tidak boleh kosong");
                return;
            }

            // Beri opsi password tidak wajib diubah
            if (!password.isEmpty() && password.length() < 6) {
                edtPassword.setError("Password minimal 6 karakter");
                return;
            }

            // ==== TODO: Kirim ke Supabase (nanti aku bantu kalau kamu mau) ====
            // supabase.from("users").update(...)

            Toast.makeText(this, "Akun berhasil diperbarui", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
