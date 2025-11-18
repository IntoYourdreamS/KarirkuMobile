package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class profilFragment extends Fragment {

    private ImageView imgProfile;
    private TextView tvNama, tvKeluar;

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        session = new SessionManager(requireContext());

        imgProfile = view.findViewById(R.id.imgProfile);
        tvNama = view.findViewById(R.id.tvNama);
        tvKeluar = view.findViewById(R.id.tvKeluar);

        loadUserData();
        setupLogout();

        return view;
    }

    // 🔹 Ambil data pengguna dari Supabase berdasarkan session
    private void loadUserData() {
        int userId = session.getUserId();

        if (userId == 0) {
            Toast.makeText(getContext(), "Gagal mengambil ID pengguna", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = SUPABASE_URL + "/rest/v1/pengguna?id_pengguna=eq." + userId + "&select=*";

        Log.d("PROFILE", "Fetching user data: " + url);

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() == 0) {
                            Log.d("PROFILE", "No user data found");
                            return;
                        }

                        JSONObject user = response.getJSONObject(0);

                        String nama = user.optString("nama_lengkap", "-");
                        String foto = user.optString("foto_url", "");

                        Log.d("PROFILE", "Nama: " + nama);
                        Log.d("PROFILE", "Foto URL from DB: " + foto);

                        tvNama.setText(nama);
                        loadProfileImage(foto);

                    } catch (Exception e) {
                        Log.e("PROFILE", "Error parsing user data: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("PROFILE", "❌ Volley Error: " + error.getMessage());
                    Toast.makeText(getContext(), "Gagal memuat data profil", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(request);
    }

    // 🔹 Load foto profil dari Supabase Storage
    // 🔹 Load foto profil dari Supabase Storage
    // 🔹 Load foto profil dari Supabase Storage
    private void loadProfileImage(String fotoPath) {
        // SELALU gunakan file IMG_1462.JPG dari storage, abaikan database
        String fullUrl = SUPABASE_URL + "/storage/v1/object/public/profile/IMG_1462.JPG";
        Log.d("PROFILE", "Loading image from: " + fullUrl);

        Glide.with(this)
                .load(fullUrl)
                .circleCrop()  // BUAT BUNDAR
                .into(imgProfile);
    }

    // 🔹 Fungsi logout
    private void setupLogout() {
        tvKeluar.setOnClickListener(v -> {
            session.clearSession();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }

            Toast.makeText(getContext(), "Berhasil logout", Toast.LENGTH_SHORT).show();
        });
    }
}