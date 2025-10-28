package com.tem2.karirku;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList;

    private TextView tabSemua, tabTerbaru, tabTerlama;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 🔹 Inisialisasi RecyclerView
        recyclerJobs = view.findViewById(R.id.recyclerJobs);
        recyclerJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        // 🔹 Inisialisasi tab
        tabSemua = view.findViewById(R.id.tab_semua);
        tabTerbaru = view.findViewById(R.id.tab_terbaru);
        tabTerlama = view.findViewById(R.id.tab_terlama);

        // 🔹 Data dummy job
        jobList = new ArrayList<>();
        jobList.add(new Job("Mekar", "Bendebesah City", "Nagih hutang ke nasabah",
                "2 Hari lalu", "130 Pendaftar", "Bengalan", "Fulltime", "Santai"));
        jobList.add(new Job("PT. Sejahtera Abadi", "Jember", "Customer Service Perbankan",
                "1 Hari lalu", "245 Pendaftar", "Perbankan", "Kontrak", "Remote"));
        jobList.add(new Job("CV. Maju Bersama", "Surabaya", "Admin Data Keuangan",
                "5 Hari lalu", "80 Pendaftar", "Keuangan", "Part-time", "On-site"));
        jobList.add(new Job("PT. Mitra Digital", "Malang", "Frontend Developer",
                "3 Hari lalu", "56 Pendaftar", "Teknologi", "Fulltime", "Hybrid"));
        jobList.add(new Job("PT. Cahaya Abadi", "Sidoarjo", "Quality Control Pabrik",
                "1 Minggu lalu", "110 Pendaftar", "Produksi", "Kontrak", "Shift"));
        jobList.add(new Job("PT. Arta Mandiri", "Jakarta", "UI/UX Designer",
                "1 Hari lalu", "95 Pendaftar", "Desain", "Fulltime", "Remote"));
        jobList.add(new Job("CV. Data Insight", "Bandung", "Data Analyst",
                "2 Hari lalu", "120 Pendaftar", "Teknologi", "Fulltime", "Hybrid"));
        jobList.add(new Job("PT. Hijau Lestari", "Yogyakarta", "Ahli Agronomi",
                "3 Hari lalu", "64 Pendaftar", "Pertanian", "Kontrak", "Lapangan"));
        jobList.add(new Job("CV. Sumber Energi", "Semarang", "Teknisi Panel Surya",
                "5 Hari lalu", "45 Pendaftar", "Teknik", "Fulltime", "On-site"));
        jobList.add(new Job("PT. Global Edukasi", "Surakarta", "Instruktur Komputer",
                "6 Hari lalu", "72 Pendaftar", "Pendidikan", "Part-time", "On-site"));

        // 🔹 Set adapter
        jobAdapter = new JobAdapter(getContext(), jobList);
        recyclerJobs.setAdapter(jobAdapter);

        // 🔹 Logika klik tab
        View.OnClickListener tabClickListener = v -> {
            // Reset semua tab ke nonaktif
            tabSemua.setBackgroundResource(R.drawable.shape_stroke);
            tabSemua.setTextColor(getResources().getColor(R.color.gray));
            tabTerbaru.setBackgroundResource(R.drawable.shape_stroke);
            tabTerbaru.setTextColor(getResources().getColor(R.color.gray));
            tabTerlama.setBackgroundResource(R.drawable.shape_stroke);
            tabTerlama.setTextColor(getResources().getColor(R.color.gray));

            // Tab yang diklik jadi aktif (biru)
            ((TextView) v).setBackgroundResource(R.drawable.selected);
            ((TextView) v).setTextColor(getResources().getColor(android.R.color.white));

            // 🔹 Filter/sort data sesuai tab
            if (v.getId() == R.id.tab_semua) {
                jobAdapter.setData(jobList);
            } else if (v.getId() == R.id.tab_terbaru) {
                List<Job> terbaru = new ArrayList<>(jobList);
                Collections.reverse(terbaru); // urutan dibalik biar terbaru dulu
                jobAdapter.setData(terbaru);
            } else if (v.getId() == R.id.tab_terlama) {
                jobAdapter.setData(jobList); // urutan lama
            }
        };

        // 🔹 Pasang listener ke semua tab
        tabSemua.setOnClickListener(tabClickListener);
        tabTerbaru.setOnClickListener(tabClickListener);
        tabTerlama.setOnClickListener(tabClickListener);

        return view;
    }
}
