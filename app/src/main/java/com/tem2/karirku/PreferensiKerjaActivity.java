package com.tem2.karirku;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;

public class PreferensiKerjaActivity extends AppCompatActivity {

    // UI
    TextView inputJenisPekerjaan;
    TextView inputLokasiKerja;
    TextView inputJenisKontrak;

    FlexboxLayout tagJobContainer;
    FlexboxLayout tagLokasiContainer;
    FlexboxLayout tagKontrakContainer;

    Spinner spinnerGaji;

    CheckBox cbRemote, cbHybrid, cbOnsite;
    Button btnSave;
    ImageView btnBack;
    TextView btnEdit;

    // Edit Mode State
    boolean isEditMode = false;

    // Storage
    SharedPreferences prefs;

    // Data list
    String[] listJenisPekerjaan = {
            "Software Engineer", "Admin", "Customer Service",
            "Desainer UI/UX", "Barista", "Marketing", "Kasir",
            "Teknisi", "Operasional", "Video Editor"
    };

    String[] listLokasi = {
            "Jakarta", "Surabaya", "Bandung", "Jogja", "Malang",
            "Jember", "Denpasar", "Semarang", "Medan"
    };

    String[] listKontrak = {
            "Full-time", "Part-time", "Magang", "Freelance", "Harian"
    };

    boolean[] checkedJenisPekerjaan;
    boolean[] checkedLokasi;
    boolean[] checkedKontrak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferensi_kerja);

        prefs = getSharedPreferences("preferensi", MODE_PRIVATE);

        initViews();
        setupSpinnerGaji();
        loadData();
        setupActions();
        setEditMode(false);
    }

    // ---------------- INIT VIEW ---------------- //
    private void initViews() {
        inputJenisPekerjaan = findViewById(R.id.inputJenisPekerjaan);
        inputLokasiKerja = findViewById(R.id.inputLokasiKerja);
        inputJenisKontrak = findViewById(R.id.inputJenisKontrak);

        tagJobContainer = findViewById(R.id.tagJobContainer);
        tagLokasiContainer = findViewById(R.id.tagLokasiContainer);
        tagKontrakContainer = findViewById(R.id.tagKontrakContainer);

        spinnerGaji = findViewById(R.id.spinnerGaji);

        cbRemote = findViewById(R.id.checkRemote);
        cbHybrid = findViewById(R.id.checkHybrid);
        cbOnsite = findViewById(R.id.checkOnsite);

        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);

        checkedJenisPekerjaan = new boolean[listJenisPekerjaan.length];
        checkedLokasi = new boolean[listLokasi.length];
        checkedKontrak = new boolean[listKontrak.length];
    }

    // ---------------- SPINNER GAJI ---------------- //
    private void setupSpinnerGaji() {

        String[] gajiList = {
                "Rp 1.000.000 - Rp 2.000.000",
                "Rp 2.000.000 - Rp 3.000.000",
                "Rp 3.000.000 - Rp 5.000.000",
                "Rp 5.000.000 - Rp 8.000.000",
                "Rp 8.000.000 - Rp 12.000.000",
                "Rp 12.000.000+"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                gajiList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGaji.setAdapter(adapter);
    }

    // ---------------- EDIT MODE HANDLER ---------------- //
    private void setEditMode(boolean enable) {
        isEditMode = enable;

        btnEdit.setText(enable ? "Selesai" : "Edit");

        inputJenisPekerjaan.setEnabled(enable);
        inputLokasiKerja.setEnabled(enable);
        inputJenisKontrak.setEnabled(enable);

        spinnerGaji.setEnabled(enable);

        cbRemote.setEnabled(enable);
        cbHybrid.setEnabled(enable);
        cbOnsite.setEnabled(enable);

        btnSave.setEnabled(enable);
    }

    // ---------------- SETUP ACTION ---------------- //
    private void setupActions() {

        // Toggle edit mode
        btnEdit.setOnClickListener(v -> setEditMode(!isEditMode));

        // Jenis Pekerjaan
        inputJenisPekerjaan.setOnClickListener(v -> {
            if (!isEditMode) return;

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Pilih Jenis Pekerjaan");
            dialog.setMultiChoiceItems(listJenisPekerjaan, checkedJenisPekerjaan,
                    (d, which, isChecked) -> checkedJenisPekerjaan[which] = isChecked);

            dialog.setPositiveButton("OK", (d, w) -> {
                inputJenisPekerjaan.setText(getSelected(listJenisPekerjaan, checkedJenisPekerjaan));
                loadTag(tagJobContainer, listJenisPekerjaan, checkedJenisPekerjaan);
            });

            dialog.show();
        });

        // Lokasi
        inputLokasiKerja.setOnClickListener(v -> {
            if (!isEditMode) return;

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Pilih Lokasi Kerja");
            dialog.setMultiChoiceItems(listLokasi, checkedLokasi,
                    (d, which, isChecked) -> checkedLokasi[which] = isChecked);

            dialog.setPositiveButton("OK", (d, w) ->
                    loadTag(tagLokasiContainer, listLokasi, checkedLokasi));

            dialog.show();
        });

        // Kontrak
        inputJenisKontrak.setOnClickListener(v -> {
            if (!isEditMode) return;

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Pilih Jenis Kontrak");
            dialog.setMultiChoiceItems(listKontrak, checkedKontrak,
                    (d, which, isChecked) -> checkedKontrak[which] = isChecked);

            dialog.setPositiveButton("OK", (d, w) -> {
                inputJenisKontrak.setText(getSelected(listKontrak, checkedKontrak));
                loadTag(tagKontrakContainer, listKontrak, checkedKontrak);
            });

            dialog.show();
        });

        // Simpan data
        btnSave.setOnClickListener(v -> saveData());

        // Tombol back
        btnBack.setOnClickListener(v -> finish());
    }

    // ---------------- TAG GENERATOR ---------------- //
    private void loadTag(FlexboxLayout container, String[] list, boolean[] checked) {
        container.removeAllViews();

        for (int i = 0; i < list.length; i++) {
            if (checked[i]) {
                TextView tag = new TextView(this);
                tag.setText(list[i]);
                tag.setTextSize(14);
                tag.setPadding(20, 12, 20, 12);
                tag.setBackgroundResource(R.drawable.shape_tag);
                tag.setTextColor(0xFF000000);

                FlexboxLayout.LayoutParams params =
                        new FlexboxLayout.LayoutParams(
                                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                FlexboxLayout.LayoutParams.WRAP_CONTENT);

                params.setMargins(10, 10, 10, 10);
                tag.setLayoutParams(params);

                container.addView(tag);
            }
        }
    }

    private String getSelected(String[] list, boolean[] checked) {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < list.length; i++)
            if (checked[i]) result.add(list[i]);

        return result.isEmpty() ? "Belum dipilih" : String.join(", ", result);
    }

    // ---------------- SAVE & LOAD ---------------- //
    private void saveData() {
        SharedPreferences.Editor edit = prefs.edit();

        edit.putString("jenis_pekerjaan", inputJenisPekerjaan.getText().toString());
        edit.putString("lokasi", inputLokasiKerja.getText().toString());
        edit.putString("gaji", spinnerGaji.getSelectedItem().toString());
        edit.putString("kontrak", inputJenisKontrak.getText().toString());

        edit.putBoolean("remote", cbRemote.isChecked());
        edit.putBoolean("hybrid", cbHybrid.isChecked());
        edit.putBoolean("onsite", cbOnsite.isChecked());

        edit.apply();

        Toast.makeText(this, "Preferensi berhasil disimpan!", Toast.LENGTH_SHORT).show();
        setEditMode(false);
    }

    private void loadData() {

        inputJenisPekerjaan.setText(prefs.getString("jenis_pekerjaan", "Belum dipilih"));
        inputLokasiKerja.setText(prefs.getString("lokasi", "Belum dipilih"));
        inputJenisKontrak.setText(prefs.getString("kontrak", "Belum dipilih"));

        cbRemote.setChecked(prefs.getBoolean("remote", false));
        cbHybrid.setChecked(prefs.getBoolean("hybrid", false));
        cbOnsite.setChecked(prefs.getBoolean("onsite", false));
    }
}
