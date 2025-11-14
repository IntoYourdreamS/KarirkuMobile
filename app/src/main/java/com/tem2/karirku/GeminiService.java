package com.tem2.karirku;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiService {
    private static final String GEMINI_API_KEY = "AIzaSyCeGPTMO3_qxoZ4e-KiMyYooQ9l_rxrh5c";
    private GenerativeModelFutures model;
    private final Executor mainExecutor;

    public GeminiService(Context context) {
        // Gunakan model gemini-pro yang gratis
        GenerativeModel generativeModel = new GenerativeModel(
                "gemini-pro",
                GEMINI_API_KEY
        );
        this.model = GenerativeModelFutures.from(generativeModel);

        // Buat executor untuk main thread
        this.mainExecutor = new MainThreadExecutor();
    }

    public void getHRDResponse(String userMessage, GeminiCallback callback) {
        // System prompt untuk membatasi percakapan hanya ke topik HRD dan karier
        String systemPrompt = "Anda adalah ASISTEN HRD PROFESIONAL dari platform Karirku. " +
                "IDENTITAS: Spesialis HRD dengan expertise di rekrutmen, perkembangan karier, dan dunia kerja.\n\n" +
                "LINGKUP BANTUAN YANG DIIZINKAN:\n" +
                "1. Lowongan kerja dan proses rekrutmen\n" +
                "2. Tips wawancara kerja dan persiapan\n" +
                "3. Pembuatan CV, resume, dan portofolio\n" +
                "4. Pengembangan karier dan skill\n" +
                "5. Negosiasi gaji dan benefit\n" +
                "6. Masalah di tempat kerja\n" +
                "7. Rekomendasi jalur karier\n" +
                "8. Tips melamar pekerjaan\n\n" +
                "ATURAN KETAT:\n" +
                "- HANYA jawab pertanyaan dalam lingkup di atas\n" +
                "- Jika pertanyaan di luar topik, tolong jawab dengan SOPAN dan PROFESIONAL:\n" +
                "  \"Maaf, sebagai asisten HRD, saya hanya dapat membantu pertanyaan seputar karier, lowongan kerja, dan topik HRD lainnya.\"\n" +
                "- Tetap ramah dan helpful dalam batas yang diizinkan\n" +
                "- Berikan jawaban yang praktis dan actionable\n\n" +
                "Pertanyaan user: " + userMessage;

        try {
            Content content = new Content.Builder()
                    .addText(systemPrompt)
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String text = result.getText();
                    if (text != null && !text.isEmpty()) {
                        // Bersihkan respons dari markdown yang tidak perlu
                        String cleanResponse = text.replace("*", "").replace("**", "");
                        callback.onSuccess(cleanResponse);
                    } else {
                        callback.onError("Tidak ada respons dari AI");
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    callback.onError("Error koneksi: " + t.getMessage());
                }
            }, mainExecutor); // GUNAKAN mainExecutor YANG SUDAH DIBUAT

        } catch (Exception e) {
            callback.onError("Error: " + e.getMessage());
        }
    }

    // Custom Executor untuk main thread di Java
    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }

    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}