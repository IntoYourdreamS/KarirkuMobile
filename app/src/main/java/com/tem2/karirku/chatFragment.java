package com.tem2.karirku;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class chatFragment extends Fragment {

    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvChatTitle;

    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private String currentUserId = "user1";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout chat detail langsung
        View view = inflater.inflate(R.layout.activity_chat_detail, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadBotWelcomeMessage();

        // AUTO SHOW KEYBOARD SAAT FRAGMENT DIBUKA
        showKeyboard();

        return view;
    }

    private void initViews(View view) {
        rvChatMessages = view.findViewById(R.id.rvChatMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        tvChatTitle = view.findViewById(R.id.tvChatTitle);

        // Set judul chatbot
        tvChatTitle.setText("Karirku Assistant");
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatMessages.setAdapter(chatAdapter);

        // AUTO SCROLL KE BAWAH SAAT KEYBOARD MUNCUL
        rvChatMessages.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    rvChatMessages.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (messageList.size() > 0) {
                                rvChatMessages.scrollToPosition(messageList.size() - 1);
                            }
                        }
                    }, 100);
                }
            }
        });
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void showKeyboard() {
        etMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                etMessage.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 200);
    }

    private void loadBotWelcomeMessage() {
        // Pesan welcome dari bot
        Message welcomeMessage = new Message(
                "1",
                "bot",
                "Karirku Assistant",
                "Halo! Saya asisten Karirku. Ada yang bisa saya bantu terkait lowongan kerja atau karier Anda?",
                Calendar.getInstance(),
                false
        );

        messageList.add(welcomeMessage);
        chatAdapter.notifyDataSetChanged();

        // Scroll ke bottom
        if (messageList.size() > 0) {
            rvChatMessages.scrollToPosition(messageList.size() - 1);
        }
    }

    private void sendMessage() {
        String messageContent = etMessage.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            // Create new message dari user
            Message newMessage = new Message(
                    String.valueOf(System.currentTimeMillis()),
                    currentUserId,
                    "Anda",
                    messageContent,
                    Calendar.getInstance(),
                    true
            );

            // Add to list dan update adapter
            messageList.add(newMessage);
            chatAdapter.notifyItemInserted(messageList.size() - 1);

            // Clear input field
            etMessage.setText("");

            // Scroll to bottom
            rvChatMessages.scrollToPosition(messageList.size() - 1);

            // JAGA KEYBOARD TETAP TERBUKA SETELAH KIRIM PESAN
            etMessage.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
            }

            // Simulasikan balasan bot (nanti bisa diganti dengan AI chatbot)
            simulateBotResponse(messageContent);
        }
    }

    private void simulateBotResponse(String userMessage) {
        // Simulasi response bot sederhana
        String botResponse = generateBotResponse(userMessage);

        // Delay sedikit untuk simulasi typing
        btnSend.postDelayed(new Runnable() {
            @Override
            public void run() {
                Message botMessage = new Message(
                        String.valueOf(System.currentTimeMillis() + 1),
                        "bot",
                        "Karirku Assistant",
                        botResponse,
                        Calendar.getInstance(),
                        false
                );

                messageList.add(botMessage);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                rvChatMessages.scrollToPosition(messageList.size() - 1);
            }
        }, 1000);
    }

    private String generateBotResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("hallo") || lowerMessage.contains("hai") || lowerMessage.contains("hi")) {
            return "Halo! Ada yang bisa saya bantu hari ini?";
        } else if (lowerMessage.contains("lowongan") || lowerMessage.contains("kerja") || lowerMessage.contains("job")) {
            return "Saya bisa membantu mencari lowongan kerja yang sesuai dengan keahlian Anda. Coba gunakan fitur Scan CV untuk rekomendasi yang lebih personal!";
        } else if (lowerMessage.contains("cv") || lowerMessage.contains("scan")) {
            return "Fitur Scan CV ada di menu bawah. Upload CV Anda untuk mendapatkan rekomendasi lowongan yang cocok!";
        } else if (lowerMessage.contains("terima kasih") || lowerMessage.contains("thanks")) {
            return "Sama-sama! Semoga sukses dengan perjalanan karier Anda 🚀";
        } else {
            return "Terima kasih atas pesannya! Untuk bantuan lebih spesifik mengenai lowongan kerja, silakan coba fitur Scan CV atau jelaskan kebutuhan Anda.";
        }
    }
}