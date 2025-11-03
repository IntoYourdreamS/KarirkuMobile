package com.tem2.karirku;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class chatFragment extends Fragment {
    private RecyclerView rvChatList;
    private List<ChatListItem> chatList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        rvChatList = view.findViewById(R.id.rvChatList);
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Data dummy HRD
        chatList.add(new ChatListItem("Sultan", "Halo apakah sudah mengisi form?", "12:30", R.drawable.imageprofile));
        chatList.add(new ChatListItem("HRD PT Maju Jaya", "Selamat siang, boleh kirim CV-nya?", "11:15", R.drawable.imageprofile));
        chatList.add(new ChatListItem("Admin Edusync", "Terima kasih sudah melamar!", "Kemarin", R.drawable.imageprofile));

        ChatListAdapter adapter = new ChatListAdapter(getContext(), chatList);
        rvChatList.setAdapter(adapter);

        return view;
    }
}
