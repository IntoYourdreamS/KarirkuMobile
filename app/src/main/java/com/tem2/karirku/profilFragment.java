package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class profilFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);
        setupClickListeners(view);
        return view;
    }

    private void setupClickListeners(View view) {

        // 1. Dilamar
        view.findViewById(R.id.dilamarContainer).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), DilamarActivity.class));
        });

        // 2. Disimpan
        view.findViewById(R.id.disimpanContainer).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), DisimpanActivity.class));
        });

        // 3. CV
        view.findViewById(R.id.itemCV).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CvActivity.class));
        });

        // 4. Setting
        view.findViewById(R.id.btnSetting).setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Fitur settings", Toast.LENGTH_SHORT).show();
        });

        // 5. Keluar
        view.findViewById(R.id.btnKeluar).setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Logout belum diatur", Toast.LENGTH_SHORT).show();
        });

        // 6. ⭐ FIX PALING PENTING → BUKA LENGKAPI PROFIL
        view.findViewById(R.id.btnLengkapiProfil).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LengkapiProfilActivity.class);
            startActivity(intent);
        });
    }
}
