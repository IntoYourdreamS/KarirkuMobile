package com.tem2.karirku;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        rvNotifications = view.findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        // Data dummy
        notificationList.add(new NotificationItem("1", "Lamaran Diterima!", "Selamat! Lamaran Anda diterima", "2 jam lalu", "application", false, R.drawable.notification));
        notificationList.add(new NotificationItem("2", "Interview", "Jadwal interview diundur", "5 jam lalu", "interview", false, R.drawable.notification));
        notificationList.add(new NotificationItem("3", "Pekerjaan Baru", "Posisi baru sesuai profil Anda", "1 hari lalu", "job", true, R.drawable.notification));

        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        rvNotifications.setAdapter(notificationAdapter);

        TextView tvMarkAllRead = view.findViewById(R.id.tvMarkAllRead);
        tvMarkAllRead.setOnClickListener(v -> {
            notificationAdapter.markAllAsRead();
        });

        return view;
    }
}