package com.tem2.karirku;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class UserDataActivity extends AppCompatActivity {

    private ImageView imgProfile, btnEditPhoto;
    private EditText inputNama, inputEmail, inputNoHp, inputAlamat, inputPengalaman;
    private TextView inputTanggalLahir;
    private Spinner spinnerGender;
    private Button btnSave;

    private Uri photoUri;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferensi_kerja); // layout yang kamu pakai

        initViews();
        initSpinner();
        initGalleryPicker();
        initListeners();
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
    }

    private void initSpinner() {
        String[] genderItems = {"Pilih Gender", "Laki-laki", "Perempuan"};

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
                        imgProfile.setImageURI(photoUri);
                    }
                }
        );
    }

    private void initListeners() {

        // Edit foto profil
        btnEditPhoto.setOnClickListener(v -> openGallery());

        // DatePicker
        inputTanggalLahir.setOnClickListener(v -> showDatePicker());

        // Tombol Simpan
        btnSave.setOnClickListener(v -> saveUserData());

        // Tombol Back
        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());
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
                    String date = dayOfMonth + "-" + (month + 1) + "-" + year;
                    inputTanggalLahir.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void saveUserData() {

        String nama = inputNama.getText().toString();
        String email = inputEmail.getText().toString();
        String nohp = inputNoHp.getText().toString();
        String tglLahir = inputTanggalLahir.getText().toString();
        String alamat = inputAlamat.getText().toString();
        String pengalaman = inputPengalaman.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();

        // TODO:
        // - Simpan ke Firebase
        // - Simpan ke SQLite
        // - Simpan ke API server
        // Kamu bebas pilih.

        // Tes output di Logcat
        System.out.println("Nama: " + nama);
        System.out.println("Email: " + email);
        System.out.println("No HP: " + nohp);
        System.out.println("Tanggal Lahir: " + tglLahir);
        System.out.println("Gender: " + gender);
        System.out.println("Alamat: " + alamat);
        System.out.println("Pengalaman: " + pengalaman);
        System.out.println("Foto URI: " + photoUri);

    }
}
