package com.tem2.karirku;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList = new ArrayList<>();
    private List<Job> filteredJobList = new ArrayList<>();
    private EditText searchEditText;

    private TextView tabSemua, tabTerbaru, tabTerlama;
    private String currentSearchQuery = "";

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co/rest/v1/lowongan?select=*";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerJobs = view.findViewById(R.id.recyclerJobs);
        recyclerJobs.setLayoutManager(new LinearLayoutManager(getContext()));

        searchEditText = view.findViewById(R.id.searchEditText);
        tabSemua = view.findViewById(R.id.tab_semua);
        tabTerbaru = view.findViewById(R.id.tab_terbaru);
        tabTerlama = view.findViewById(R.id.tab_terlama);

        jobAdapter = new JobAdapter(getContext(), filteredJobList);
        recyclerJobs.setAdapter(jobAdapter);

        loadLowonganFromAPI();
        setupSearchListener();

        View.OnClickListener tabClickListener = v -> {
            resetTabs();
            ((TextView) v).setBackgroundResource(R.drawable.selected);
            ((TextView) v).setTextColor(getResources().getColor(android.R.color.white));

            List<Job> dataToShow = getCurrentFilteredList();

            if (v.getId() == R.id.tab_semua) {
                jobAdapter.setData(dataToShow);
            } else if (v.getId() == R.id.tab_terbaru) {
                List<Job> terbaru = new ArrayList<>(dataToShow);
                Collections.reverse(terbaru);
                jobAdapter.setData(terbaru);
            } else if (v.getId() == R.id.tab_terlama) {
                jobAdapter.setData(dataToShow);
            }
        };

        tabSemua.setOnClickListener(tabClickListener);
        tabTerbaru.setOnClickListener(tabClickListener);
        tabTerlama.setOnClickListener(tabClickListener);

        return view;
    }

    // 🔍 Setup Real-time Search
    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                performSearch(currentSearchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    // 🔍 Perform Search dengan keyword
    private void performSearch(String query) {
        filteredJobList.clear();

        if (query.isEmpty()) {
            // Jika search kosong, gunakan filter CV atau tampilkan semua
            filterJobsByKeywords();
            return;
        }

        Log.d("SEARCH_DEBUG", "🔍 Searching for: " + query);

        String queryLower = query.toLowerCase(Locale.ROOT);

        for (Job job : jobList) {
            // Cek di judul, kategori, perusahaan, dan lokasi
            boolean matchJudul = job.getJobTitle().toLowerCase(Locale.ROOT).contains(queryLower);
            boolean matchKategori = job.getTag1().toLowerCase(Locale.ROOT).contains(queryLower);
            boolean matchPerusahaan = job.getCompanyName().toLowerCase(Locale.ROOT).contains(queryLower);
            boolean matchLokasi = job.getLocation().toLowerCase(Locale.ROOT).contains(queryLower);

            // Smart match via KeywordMapper
            boolean smartMatchKategori = KeywordMapper.isRelated(query, job.getTag1());
            boolean smartMatchJudul = KeywordMapper.isRelated(query, job.getJobTitle());

            if (matchJudul || matchKategori || matchPerusahaan || matchLokasi ||
                    smartMatchKategori || smartMatchJudul) {
                filteredJobList.add(job);

                Log.d("SEARCH_DEBUG", "✅ Found: " + job.getJobTitle() +
                        " (" + job.getTag1() + ")");
            }
        }

        Log.d("SEARCH_DEBUG", "📊 Found " + filteredJobList.size() + " results for '" + query + "'");

        if (filteredJobList.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada hasil untuk \"" + query + "\"", Toast.LENGTH_SHORT).show();
        }

        jobAdapter.setData(filteredJobList);
    }

    private void resetTabs() {
        tabSemua.setBackgroundResource(R.drawable.shape_stroke);
        tabSemua.setTextColor(getResources().getColor(R.color.gray));
        tabTerbaru.setBackgroundResource(R.drawable.shape_stroke);
        tabTerbaru.setTextColor(getResources().getColor(R.color.gray));
        tabTerlama.setBackgroundResource(R.drawable.shape_stroke);
        tabTerlama.setTextColor(getResources().getColor(R.color.gray));
    }

    private void loadLowonganFromAPI() {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, SUPABASE_URL, null,
                response -> {
                    jobList.clear();
                    parseResponse(response);

                    // 🔍 Cek apakah ada keyword dari CV scan ATAU search query
                    if (currentSearchQuery.isEmpty()) {
                        filterJobsByKeywords();
                    } else {
                        performSearch(currentSearchQuery);
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Volley error: " + error.toString());
                    Toast.makeText(getContext(), "Gagal memuat data: " + error.toString(), Toast.LENGTH_LONG).show();
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

    private void parseResponse(JSONArray response) {
        Log.d("SUPABASE_DATA", "📦 Total data dari Supabase: " + response.length());

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);

                String judul = obj.optString("judul", "-");
                String lokasi = obj.optString("lokasi", "-");
                String kategori = obj.optString("kategori", "-");
                String tipe = obj.optString("tipe_pekerjaan", "-");
                String gaji = obj.optString("gaji_range", "-");
                String deskripsi = obj.optString("deskripsi", "-");
                String requirements = obj.optString("requirements", "-");

                Job job = new Job(
                        obj.optString("nama_perusahaan", "Perusahaan"),
                        lokasi,
                        judul,
                        "Baru saja",
                        gaji + " Pendaftar",
                        kategori,
                        tipe,
                        "On-site"
                );

                jobList.add(job);

                // 📝 LOG untuk debug - lihat data yang masuk
                Log.d("JOB_DATA", "Job #" + (i+1) +
                        "\n  Judul: " + judul +
                        "\n  Kategori: " + kategori +
                        "\n  Tipe: " + tipe +
                        "\n  Perusahaan: " + job.getCompanyName());

            } catch (JSONException e) {
                Log.e("JSON_ERROR", "Parsing gagal: " + e.getMessage());
            }
        }

        Log.d("SUPABASE_DATA", "✅ Berhasil parsing " + jobList.size() + " lowongan");
    }

    // 🔍 Filter lowongan berdasarkan keyword dari CV (digunakan saat search kosong)
    private void filterJobsByKeywords() {
        CVKeywordManager keywordManager = CVKeywordManager.getInstance();

        if (keywordManager.hasScannedCV() && !keywordManager.getKeywords().isEmpty()) {
            List<String> keywords = keywordManager.getKeywords();
            filteredJobList.clear();

            Log.d("FILTER_DEBUG", "Keywords dari CV: " + keywords.toString());

            for (Job job : jobList) {
                boolean isMatch = false;

                for (String keyword : keywords) {
                    // 🎯 Cek di JUDUL, KATEGORI, TAG1, TAG2, TAG3
                    if (matchesKeyword(job, keyword)) {
                        isMatch = true;
                        Log.d("FILTER_DEBUG", "✅ Match: " + job.getJobTitle() + " dengan keyword: " + keyword);
                        break;
                    }
                }

                if (isMatch) {
                    filteredJobList.add(job);
                }
            }

            // 📊 Hasil filtering
            Log.d("FILTER_DEBUG", "Total lowongan: " + jobList.size() + ", Filtered: " + filteredJobList.size());

            if (filteredJobList.isEmpty()) {
                Toast.makeText(getContext(), "❌ Tidak ada lowongan yang cocok dengan keyword CV.\nMenampilkan semua lowongan.", Toast.LENGTH_LONG).show();
                filteredJobList.addAll(jobList); // Tampilkan semua jika tidak ada yang cocok
            } else {
                String matchInfo = "✅ Ditemukan " + filteredJobList.size() + " lowongan cocok dari " + jobList.size() + " total";
                Toast.makeText(getContext(), matchInfo, Toast.LENGTH_SHORT).show();
                Log.d("FILTER_DEBUG", matchInfo);
            }
        } else {
            // Jika belum scan CV, tampilkan semua
            Log.d("FILTER_DEBUG", "Tidak ada keyword CV, tampilkan semua lowongan");
            filteredJobList.addAll(jobList);
        }

        jobAdapter.setData(filteredJobList);
    }



    // 🔍 Cek apakah job cocok dengan keyword (untuk CV filter)
    private boolean matchesKeyword(Job job, String keyword) {
        String keywordLower = keyword.toLowerCase(Locale.ROOT).trim();

        // 🎯 Yang paling penting: Tag1 adalah KATEGORI dari Supabase
        // Tag2 = tipe_pekerjaan, Tag3 = mode kerja
        String kategoriLower = job.getTag1().toLowerCase(Locale.ROOT).trim();
        String judulLower = job.getJobTitle().toLowerCase(Locale.ROOT).trim();

        // 1. Exact match di judul atau kategori
        boolean exactMatchJudul = judulLower.contains(keywordLower);
        boolean exactMatchKategori = kategoriLower.contains(keywordLower);

        // 2. Smart match via KeywordMapper - CEK KATEGORI (Tag1) dulu!
        boolean smartMatchKategori = KeywordMapper.isRelated(keyword, job.getTag1());
        boolean smartMatchJudul = KeywordMapper.isRelated(keyword, job.getJobTitle());

        boolean anyMatch = exactMatchJudul || exactMatchKategori || smartMatchKategori || smartMatchJudul;

        // 📝 LOG untuk debug per job yang dicek
        Log.d("MATCH_CHECK", "Checking Job: " + job.getJobTitle() +
                " | Kategori: " + job.getTag1() +
                " | Keyword: " + keyword +
                " | Match: " + anyMatch);

        if (anyMatch) {
            Log.d("MATCH_DETAIL", "✅ MATCH FOUND!" +
                    "\n  Job: " + job.getJobTitle() +
                    "\n  Kategori: " + job.getTag1() +
                    "\n  Keyword CV: " + keyword +
                    "\n  Exact - Judul:" + exactMatchJudul + ", Kategori:" + exactMatchKategori +
                    "\n  Smart - Judul:" + smartMatchJudul + ", Kategori:" + smartMatchKategori);
        }

        return anyMatch;
    }

    // 🔄 Ambil list yang sudah difilter untuk sorting tab
    private List<Job> getCurrentFilteredList() {
        return new ArrayList<>(filteredJobList);
    }
}