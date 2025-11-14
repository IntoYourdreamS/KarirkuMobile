package com.tem2.karirku;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private Handler pollingHandler;
    private Runnable pollingRunnable;

    private static final String SUPABASE_URL = "https://tkjnbelcgfwpbhppsnrl.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRram5iZWxjZ2Z3cGJocHBzbnJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE3NDA3NjIsImV4cCI6MjA3NzMxNjc2Mn0.wOjK4X2qJV6LzOG4yXxnfeTezDX5_3Sb3wezhCuQAko";

    private int currentUserId = 2;
    private static final long POLLING_INTERVAL = 10000; // 10 detik
    private String lastPollingTime = "2024-01-01T00:00:00";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        rvNotifications = view.findViewById(R.id.rvNotifications);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        rvNotifications.setAdapter(notificationAdapter);

        TextView tvMarkAllRead = view.findViewById(R.id.tvMarkAllRead);
        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());

        swipeRefresh.setOnRefreshListener(this::loadNotifications);

        loadNotifications();
        startPolling();

        return view;
    }

    private void loadNotifications() {
        String url = SUPABASE_URL + "/rest/v1/notifikasi" +
                "?id_pengguna=eq." + currentUserId +
                "&select=*" +
                "&order=dibuat_pada.desc";

        Log.d("NOTIFICATION", "Loading from: " + url);

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    notificationList.clear();
                    parseNotifications(response);
                    notificationAdapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);

                    // Update last polling time setelah load berhasil
                    if (!notificationList.isEmpty()) {
                        updateLastPollingTime();
                    }

                    Log.d("NOTIFICATION", "✅ Loaded " + notificationList.size() + " notifications");
                },
                error -> {
                    Log.e("NOTIFICATION", "❌ Error: " + error.toString());
                    Toast.makeText(getContext(), "Gagal memuat notifikasi", Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                }) {
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

    private void parseNotifications(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                String id = obj.optString("id_notifikasi");
                String message = obj.optString("pesan", "");
                String type = obj.optString("tipe", "general");
                boolean isRead = obj.optBoolean("sudah_dibaca", false);
                String createdAt = obj.optString("dibuat_pada", "");

                String timeAgo = formatTimeAgo(createdAt);
                int iconRes = getIconForType(type);

                NotificationItem item = new NotificationItem(
                        id,
                        type.toUpperCase(Locale.ROOT),
                        message,
                        timeAgo,
                        type,
                        isRead,
                        iconRes
                );

                notificationList.add(item);
            }

            Log.d("NOTIFICATION", "Parsed " + notificationList.size() + " notifikasi");
        } catch (Exception e) {
            Log.e("NOTIFICATION", "Parse error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatTimeAgo(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(timestamp);

            if (date == null) return "Baru saja";

            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) return "Baru saja";
            else if (minutes < 60) return minutes + " menit lalu";
            else if (hours < 24) return hours + " jam lalu";
            else if (days < 7) return days + " hari lalu";
            else {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
                return displayFormat.format(date);
            }
        } catch (Exception e) {
            return "Baru saja";
        }
    }

    private int getIconForType(String type) {
        switch (type) {
            case "lowongan_baru":
            case "lowongan_match":
                return R.drawable.iconloker;
            case "application":
                return R.drawable.ic_success;
            case "interview":
                return R.drawable.ic_calendar;
            case "job_recommendation":
                return R.drawable.ic_job;
            case "reminder":
                return R.drawable.ic_reminder;
            default:
                return R.drawable.notification;
        }
    }

    private void markAllAsRead() {
        String url = SUPABASE_URL + "/rest/v1/notifikasi?id_pengguna=eq." + currentUserId;

        JSONObject updateBody = new JSONObject();
        try {
            updateBody.put("sudah_dibaca", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                Request.Method.PATCH,
                url,
                updateBody,
                response -> {
                    Log.d("NOTIFICATION", "✅ Semua notifikasi ditandai dibaca");
                    notificationAdapter.markAllAsRead();
                    Toast.makeText(getContext(), "Semua notifikasi telah dibaca", Toast.LENGTH_SHORT).show();
                },
                error -> Log.e("NOTIFICATION", "❌ Error mark all read: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_API_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_API_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                return headers;
            }
        };

        queue.add(request);
    }

    // 🔄 POLLING SYSTEM
    private void startPolling() {
        pollingHandler = new Handler();
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                checkNewNotifications();
                pollingHandler.postDelayed(this, POLLING_INTERVAL);
            }
        };
        pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
        Log.d("POLLING", "🔄 Polling started every " + POLLING_INTERVAL + "ms");
    }

    private void checkNewNotifications() {
        String url = SUPABASE_URL + "/rest/v1/notifikasi" +
                "?id_pengguna=eq." + currentUserId +
                "&dibuat_pada=gt." + lastPollingTime +
                "&select=*" +
                "&order=dibuat_pada.desc";

        Log.d("POLLING", "Checking new notifications since: " + lastPollingTime);

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            Log.d("POLLING", "🎯 Found " + response.length() + " new notifications");
                            parseNewNotifications(response);

                            // Update last polling time ke waktu sekarang
                            updateLastPollingTimeToNow();
                        }
                    } catch (Exception e) {
                        Log.e("POLLING", "Error checking new notifications: " + e.getMessage());
                    }
                },
                error -> {
                    // Silent error untuk polling
                }) {
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

    private void parseNewNotifications(JSONArray newNotifications) {
        try {
            for (int i = 0; i < newNotifications.length(); i++) {
                JSONObject obj = newNotifications.getJSONObject(i);

                String id = obj.optString("id_notifikasi");
                String message = obj.optString("pesan", "");
                String type = obj.optString("tipe", "general");
                String createdAt = obj.optString("dibuat_pada", "");

                // Cek duplikat
                boolean isDuplicate = false;
                for (NotificationItem item : notificationList) {
                    if (item.getId().equals(id)) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (!isDuplicate) {
                    NotificationItem newItem = new NotificationItem(
                            id,
                            type.toUpperCase(Locale.ROOT),
                            message,
                            "Baru saja",
                            type,
                            false,
                            getIconForType(type)
                    );

                    // Tambah ke list dan update UI
                    notificationList.add(0, newItem);
                    notificationAdapter.notifyItemInserted(0);
                    rvNotifications.scrollToPosition(0);

                    // Show toast
                    Toast.makeText(getContext(), "🔔 " + message, Toast.LENGTH_SHORT).show();
                    Log.d("POLLING", "✅ New notification: " + message);
                }
            }
        } catch (Exception e) {
            Log.e("POLLING", "Error parsing new notifications: " + e.getMessage());
        }
    }

    private void updateLastPollingTime() {
        // Update ke waktu notifikasi terbaru
        if (!notificationList.isEmpty()) {
            // Ambil timestamp dari notifikasi terbaru (index 0)
            lastPollingTime = getCurrentTimestamp();
        }
    }

    private void updateLastPollingTimeToNow() {
        lastPollingTime = getCurrentTimestamp();
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPolling();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pollingHandler == null) {
            startPolling();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPolling();
    }

    private void stopPolling() {
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
            pollingHandler = null;
            Log.d("POLLING", "🛑 Polling stopped");
        }
    }
}