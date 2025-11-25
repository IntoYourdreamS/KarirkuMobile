package com.tem2.karirku;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;
    private List<Integer> savedJobIds = new ArrayList<>();
    private SessionManager sessionManager;
    private int currentUserId;
    private boolean isSavedMode; // Flag untuk mode disimpan

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    // Konstruktor dengan mode
    public JobAdapter(Context context, List<Job> jobList, boolean isSavedMode) {
        this.context = context;
        this.jobList = jobList;
        this.isSavedMode = isSavedMode;
        this.sessionManager = new SessionManager(context);
        this.currentUserId = sessionManager.getUserId();

        if (!isSavedMode) {
            loadSavedJobs(); // Hanya load saved jobs jika bukan mode disimpan
        }
    }

    // Konstruktor lama untuk kompatibilitas
    public JobAdapter(Context context, List<Job> jobList) {
        this(context, jobList, false);
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        Log.d("JOB_ADAPTER", "🎯 Binding job - " +
                "Position: " + position + ", " +
                "Title: " + job.getJobTitle() + ", " +
                "Company: " + job.getCompanyName() + ", " +
                "ID: " + job.getIdLowongan() + ", " +
                "SavedMode: " + isSavedMode);

        holder.tvCompanyName.setText(job.getCompanyName());
        holder.tvLocation.setText(job.getLocation());
        holder.tvJobTitle.setText(job.getJobTitle());
        holder.tvPostedTime.setText(job.getPostedTime());
        holder.tvApplicants.setText(job.getApplicants());

        // Set tags
        holder.tvTag1.setText(job.getTag1());
        holder.tvTag2.setText(job.getTag2());
        holder.tvTag3.setText(job.getTag3());

        // Sembunyikan tag yang kosong
        holder.tvTag1.setVisibility(job.getTag1().isEmpty() ? View.GONE : View.VISIBLE);
        holder.tvTag2.setVisibility(job.getTag2().isEmpty() ? View.GONE : View.VISIBLE);
        holder.tvTag3.setVisibility(job.getTag3().isEmpty() ? View.GONE : View.VISIBLE);

        holder.imgCompany.setImageResource(R.drawable.iconloker);

        // Tombol save - di mode disimpan selalu aktif
        if (isSavedMode || savedJobIds.contains(job.getIdLowongan())) {
            holder.btnSave.setImageResource(R.drawable.icsimpan_active);
        } else {
            holder.btnSave.setImageResource(R.drawable.ic_simpan);
        }

        holder.btnSave.setOnClickListener(v -> {
            Log.d("FAVORITE", "🎯 Tombol save diklik - Job ID: " + job.getIdLowongan() +
                    ", User ID: " + currentUserId + ", SavedMode: " + isSavedMode);

            if (currentUserId == 0) {
                Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isSavedMode) {
                // Di mode disimpan, hapus dari favorit
                removeFromFavorites(job.getIdLowongan(), holder, position);
            } else {
                // Di mode biasa, toggle favorit
                if (savedJobIds.contains(job.getIdLowongan())) {
                    removeFromFavorites(job.getIdLowongan(), holder, position);
                } else {
                    addToFavorites(job, holder);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailActivity.class);
            intent.putExtra("JOB_DATA", job);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void setData(List<Job> newList) {
        this.jobList = newList;
        notifyDataSetChanged();
    }

    private void addToFavorites(Job job, JobViewHolder holder) {
        int jobId = job.getIdLowongan();

        new Thread(() -> {
            try {
                String url = SUPABASE_URL + "/rest/v1/favorit_lowongan";

                JSONObject bodyJson = new JSONObject();
                bodyJson.put("id_pencaker", currentUserId);
                bodyJson.put("id_lowongan", jobId);

                Log.d("FAVORITE", "📤 Menyimpan favorit: " + bodyJson.toString());

                okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");
                okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, bodyJson.toString());

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .header("apikey", SUPABASE_API_KEY)
                        .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                        .header("Prefer", "return=minimal")
                        .post(body)
                        .build();

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Response response = client.newCall(request).execute();

                Log.d("FAVORITE", "📨 Response Code: " + response.code());

                if (response.isSuccessful() || response.code() == 201) {
                    Log.d("FAVORITE", "✅ Berhasil menyimpan ke favorit");
                    savedJobIds.add(jobId);

                    ((android.app.Activity) context).runOnUiThread(() -> {
                        holder.btnSave.setImageResource(R.drawable.icsimpan_active);
                        Toast.makeText(context, "✅ Lowongan disimpan", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e("FAVORITE", "❌ Gagal menyimpan favorit: " + errorBody);

                    // Handle error berdasarkan response code
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        if (response.code() == 409 || errorBody.contains("23505")) {
                            // Duplicate entry - sudah disimpan sebelumnya
                            Toast.makeText(context, "✅ Lowongan sudah disimpan", Toast.LENGTH_SHORT).show();
                            holder.btnSave.setImageResource(R.drawable.icsimpan_active);
                            savedJobIds.add(jobId);
                        } else {
                            Toast.makeText(context, "❌ Gagal menyimpan lowongan", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (Exception e) {
                Log.e("FAVORITE", "❌ Exception: " + e.getMessage(), e);
                ((android.app.Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void removeFromFavorites(int jobId, JobViewHolder holder, int position) {
        new Thread(() -> {
            try {
                String url = SUPABASE_URL + "/rest/v1/favorit_lowongan" +
                        "?id_pencaker=eq." + currentUserId +
                        "&id_lowongan=eq." + jobId;

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .header("apikey", SUPABASE_API_KEY)
                        .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                        .delete()
                        .build();

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Response response = client.newCall(request).execute();

                Log.d("FAVORITE", "🗑️ Response Code (delete): " + response.code());

                if (response.isSuccessful()) {
                    savedJobIds.remove((Integer) jobId);

                    ((android.app.Activity) context).runOnUiThread(() -> {
                        if (isSavedMode) {
                            // Di mode disimpan, hapus item dari list
                            jobList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, jobList.size());
                            Toast.makeText(context, "❌ Lowongan dihapus dari disimpan", Toast.LENGTH_SHORT).show();
                        } else {
                            // Di mode biasa, hanya ubah icon
                            holder.btnSave.setImageResource(R.drawable.ic_simpan);
                            Toast.makeText(context, "❌ Lowongan dihapus dari disimpan", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("FAVORITE", "❌ Gagal menghapus favorit");
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "❌ Gagal menghapus lowongan", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e("FAVORITE", "❌ Error remove: " + e.getMessage());
                ((android.app.Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadSavedJobs() {
        if (currentUserId == 0) return;

        String url = SUPABASE_URL + "/rest/v1/favorit_lowongan" +
                "?id_pencaker=eq." + currentUserId +
                "&select=id_lowongan";

        RequestQueue queue = Volley.newRequestQueue(context);
        com.android.volley.toolbox.JsonArrayRequest request = new com.android.volley.toolbox.JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        savedJobIds.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            int jobId = obj.getInt("id_lowongan");
                            savedJobIds.add(jobId);
                        }
                        Log.d("FAVORITE", "✅ Loaded " + savedJobIds.size() + " saved jobs");
                        notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("FAVORITE", "❌ Error parsing saved jobs: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("FAVORITE", "❌ Gagal load saved jobs: " + error.toString());
                }
        ) {
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

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCompany, btnSave;
        TextView tvCompanyName, tvLocation, tvJobTitle, tvPostedTime, tvApplicants;
        TextView tvTag1, tvTag2, tvTag3;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCompany = itemView.findViewById(R.id.imgCompany);
            btnSave = itemView.findViewById(R.id.btnSave);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvPostedTime = itemView.findViewById(R.id.tvPostedTime);
            tvApplicants = itemView.findViewById(R.id.tvApplicants);
            tvTag1 = itemView.findViewById(R.id.tvTag1);
            tvTag2 = itemView.findViewById(R.id.tvTag2);
            tvTag3 = itemView.findViewById(R.id.tvTag3);
        }
    }
}