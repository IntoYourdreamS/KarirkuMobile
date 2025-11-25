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
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.HashMap;
import java.util.Map;

public class profilFragment extends Fragment {

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    private TextView tvName, tvEmail;
    private CircleImageView imgProfile;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        sessionManager = new SessionManager(requireContext());

        tvName = view.findViewById(R.id.tvNama);
        tvEmail = view.findViewById(R.id.tvSelengkapnya);
        imgProfile = view.findViewById(R.id.imgProfile);

        loadUserData();

        setupClickListeners(view);

        return view;
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        Log.d("PROFILE", "Load profile for user ID: " + userId);

        String url = SUPABASE_URL + "/rest/v1/pengguna?id_pengguna=eq." + userId + "&select=nama_lengkap,email,foto_url";

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject user = response.getJSONObject(0);

                            String name = user.optString("nama_lengkap", "-");
                            String email = user.optString("email", "-");
                            String fotoUrl = user.optString("foto_url", "");

                            tvName.setText(name);
                            tvEmail.setText(email);

                            // Jika ada foto di Supabase, load
                            if (!fotoUrl.isEmpty()) {
                                String fullImageUrl =
                                        SUPABASE_URL + "/storage/v1/object/public/" + fotoUrl;

                                Log.d("PROFILE", "Load image from: " + fullImageUrl);

                                Glide.with(requireContext())
                                        .load(fullImageUrl)
                                        .into(imgProfile);
                            }
                        }

                    } catch (Exception e) {
                        Log.e("PROFILE", "Parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("PROFILE", "Volley error: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                return headers;
            }
        };

        queue.add(request);
    }


    private void setupClickListeners(View view) {

        // Dilamar
        view.findViewById(R.id.dilamarContainer).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), DilamarActivity.class)));

        // Disimpan
        view.findViewById(R.id.disimpanContainer).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), DisimpanActivity.class)));

        // CV
        view.findViewById(R.id.itemCV).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CvActivity.class)));

        //pendataan user
        view.findViewById(R.id.itemPreferensiKerja).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), UserDataActivity.class)));

        // Setting
        view.findViewById(R.id.btnSetting).setOnClickListener(v ->
                Toast.makeText(getActivity(), "Fitur settings", Toast.LENGTH_SHORT).show());

        // Lengkapi Profil
        view.findViewById(R.id.btnLengkapiProfil).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), LengkapiProfilActivity.class)));

        // LOGOUT
        view.findViewById(R.id.btnKeluar).setOnClickListener(v -> {
            sessionManager.clearSession();
            startActivity(new Intent(getActivity(), MainActivity.class));
            requireActivity().finish();
        });
    }
}
