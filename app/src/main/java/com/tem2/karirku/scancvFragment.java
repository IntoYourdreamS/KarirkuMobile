package com.tem2.karirku;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class scancvFragment extends Fragment {

    private static final int PICK_PDF_REQUEST = 1001;
    private static final int CAMERA_REQUEST = 1002;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private ImageView btnUploadPDF;
    private Button btnCamera;
    private TextRecognizer textRecognizer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scancv, container, false);

        PDFBoxResourceLoader.init(requireContext());

        // Initialize ML Kit Text Recognition
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Log.d("CAMERA_OCR", "✅ TextRecognizer initialized");

        btnUploadPDF = view.findViewById(R.id.btnUploadPDF);
        btnCamera = view.findViewById(R.id.btnCamera);

        btnUploadPDF.setOnClickListener(v -> openFileChooser());
        btnCamera.setOnClickListener(v -> openCamera());

        return view;
    }

    // 📷 Buka kamera untuk scan CV
    private void openCamera() {
        Log.d("CAMERA_OCR", "🎬 openCamera() called");

        // Check camera permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("CAMERA_OCR", "⚠️ Camera permission not granted, requesting...");
            // Request permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            Log.d("CAMERA_OCR", "✅ Camera permission granted");
            // Permission granted, open camera
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                Log.d("CAMERA_OCR", "📸 Launching camera...");
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Log.e("CAMERA_OCR", "❌ Camera app not available");
                Toast.makeText(getContext(), "Kamera tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("CAMERA_OCR", "📋 onRequestPermissionsResult: requestCode=" + requestCode);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("CAMERA_OCR", "✅ Permission granted by user");
                openCamera();
            } else {
                Log.w("CAMERA_OCR", "❌ Permission denied by user");
                Toast.makeText(getContext(), "Izin kamera diperlukan untuk scan CV", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 📄 Buka file picker untuk PDF
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Pilih CV (PDF)"), PICK_PDF_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("CAMERA_OCR", "📬 onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == PICK_PDF_REQUEST && data.getData() != null) {
                // Handle PDF
                Log.d("CAMERA_OCR", "📄 PDF selected");
                Uri pdfUri = data.getData();
                handlePdfFile(pdfUri);
            } else if (requestCode == CAMERA_REQUEST) {
                // Handle Camera Image
                Log.d("CAMERA_OCR", "📸 Camera image received");
                Bundle extras = data.getExtras();

                if (extras != null) {
                    Log.d("CAMERA_OCR", "📦 Extras found, keys: " + extras.keySet());

                    if (extras.get("data") != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        Log.d("CAMERA_OCR", "🖼️ Bitmap received: " + imageBitmap.getWidth() + "x" + imageBitmap.getHeight());
                        processCameraImage(imageBitmap);
                    } else {
                        Log.e("CAMERA_OCR", "❌ No 'data' key in extras");
                        Toast.makeText(getContext(), "❌ Gagal mengambil gambar", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("CAMERA_OCR", "❌ Extras is null");
                    Toast.makeText(getContext(), "❌ Gagal mengambil gambar", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.w("CAMERA_OCR", "⚠️ Result not OK or data is null");
        }
    }

    // 📷 Process gambar dari kamera dengan OCR
    private void processCameraImage(Bitmap bitmap) {
        Log.d("CAMERA_OCR", "🔄 Starting OCR processing...");
        Toast.makeText(getContext(), "🔍 Memproses gambar CV...", Toast.LENGTH_SHORT).show();

        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            Log.d("CAMERA_OCR", "✅ InputImage created successfully");

            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String recognizedText = visionText.getText();

                        Log.d("CAMERA_OCR", "✅ OCR SUCCESS!");
                        Log.d("CAMERA_OCR", "📝 Text length: " + recognizedText.length());
                        Log.d("CAMERA_OCR", "📝 Text preview (first 200 chars): " +
                                (recognizedText.length() > 200 ? recognizedText.substring(0, 200) : recognizedText));
                        Log.d("CAMERA_OCR", "📝 Full text: " + recognizedText);

                        if (recognizedText.isEmpty()) {
                            Log.w("CAMERA_OCR", "⚠️ OCR returned empty text");
                            Toast.makeText(getContext(),
                                    "❌ Tidak ada text yang terdeteksi.\n\n💡 Tips:\n• Pastikan foto jelas\n• Coba foto lebih dekat\n• Gunakan pencahayaan baik\n\nAtau gunakan Upload PDF",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Extract keywords dari text OCR
                        Toast.makeText(getContext(), "✅ Text terdeteksi! Mencari keyword...", Toast.LENGTH_SHORT).show();
                        extractKeywordsFromText(recognizedText, "gambar");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CAMERA_OCR", "❌ OCR FAILED: " + e.getClass().getSimpleName());
                        Log.e("CAMERA_OCR", "❌ Error message: " + e.getMessage());
                        e.printStackTrace();

                        Toast.makeText(getContext(),
                                "❌ Gagal memproses gambar\n\nError: " + e.getMessage() +
                                        "\n\nCoba gunakan Upload PDF",
                                Toast.LENGTH_LONG).show();
                    })
                    .addOnCompleteListener(task -> {
                        Log.d("CAMERA_OCR", "🏁 OCR task completed. Success: " + task.isSuccessful());
                    });

        } catch (Exception e) {
            Log.e("CAMERA_OCR", "❌ Exception during image processing: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // 📄 Process PDF file
    private void handlePdfFile(Uri pdfUri) {
        try {
            String fileName = getFileName(pdfUri);
            Toast.makeText(getContext(), "📄 Memproses: " + fileName, Toast.LENGTH_SHORT).show();

            InputStream inputStream = requireContext().getContentResolver().openInputStream(pdfUri);
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            document.close();

            Log.d("PDF_PARSE", "📝 Text dari PDF (length): " + text.length());
            Log.d("PDF_PARSE", "📝 Text preview: " + (text.length() > 200 ? text.substring(0, 200) : text));

            // Extract keywords dari PDF text
            extractKeywordsFromText(text, "PDF");

        } catch (Exception e) {
            Log.e("PDF_PARSE", "❌ Error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(getContext(), "❌ Gagal membaca PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // 🔍 Extract keywords dari text (PDF atau OCR)
    private void extractKeywordsFromText(String text, String source) {
        Log.d("KEYWORD_EXTRACT", "🔍 Starting keyword extraction from " + source);
        Log.d("KEYWORD_EXTRACT", "📝 Text length: " + text.length());

        // Kata kunci yang relevan (disesuaikan dengan kategori Supabase)
        String[] keywords = {
                // Teknologi
                "teknologi", "technology", "software", "developer", "programmer", "IT", "coding",
                "engineer", "java", "python", "web", "mobile", "android", "ios", "react", "angular",
                // Desain
                "desain", "design", "UI", "UX", "graphic", "photoshop", "figma", "illustrator", "corel",
                // Keuangan
                "keuangan", "finance", "accounting", "akuntan", "akuntansi", "financial", "auditor",
                // Perbankan
                "perbankan", "bank", "banking", "teller", "credit", "kredit",
                // Produksi
                "produksi", "production", "hardware", "manufaktur", "operator", "pabrik", "quality control",
                // Administrasi
                "administrasi", "admin", "sekretaris", "office", "data entry", "staff",
                // Teknik
                "teknik", "engineering", "mekanik", "elektro", "sipil", "mechanical", "electrical",
                // Pertanian
                "pertanian", "agriculture", "agronomi", "farming", "perkebunan",
                // Pendidikan
                "pendidikan", "education", "guru", "teacher", "dosen", "pengajar", "training"
        };

        List<String> matchedKeywords = new ArrayList<>();
        String textLower = text.toLowerCase(Locale.ROOT);

        for (String keyword : keywords) {
            if (textLower.contains(keyword.toLowerCase(Locale.ROOT))) {
                // Hindari duplikat
                if (!matchedKeywords.contains(keyword)) {
                    matchedKeywords.add(keyword);
                    Log.d("KEYWORD_EXTRACT", "✅ Match found: " + keyword);
                }
            }
        }

        Log.d("KEYWORD_EXTRACT", "📊 Total keywords matched: " + matchedKeywords.size());

        // Hasil matching
        if (matchedKeywords.isEmpty()) {
            Log.w("KEYWORD_EXTRACT", "⚠️ No keywords matched");
            Toast.makeText(getContext(),
                    "❌ Tidak ada kata kunci cocok dari " + source + ".\n\n" +
                            "💡 Kata kunci yang dicari:\n" +
                            "• Teknologi (IT, software, developer)\n" +
                            "• Desain (UI/UX, graphic)\n" +
                            "• Keuangan, Perbankan\n" +
                            "• Produksi, Administrasi\n" +
                            "• Teknik, Pertanian, Pendidikan\n\n" +
                            "Cek halaman Home untuk semua lowongan.",
                    Toast.LENGTH_LONG).show();
            CVKeywordManager.getInstance().clearKeywords();
        } else {
            // ✅ Simpan keyword ke Singleton
            CVKeywordManager.getInstance().setKeywords(matchedKeywords);

            String keywordText = String.join(", ", matchedKeywords);
            Log.d("KEYWORD_EXTRACT", "✅ Keywords saved: " + keywordText);

            Toast.makeText(getContext(),
                    "✅ CV berhasil dipindai dari " + source + "!\n\n" +
                            "🎯 Kata kunci: " + keywordText + "\n\n" +
                            "👉 Klik tab 'Home' untuk melihat lowongan yang cocok",
                    Toast.LENGTH_LONG).show();
        }
    }

    // 📝 Get file name dari URI
    private String getFileName(Uri uri) {
        String result = null;
        try (android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                result = cursor.getString(nameIndex);
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textRecognizer != null) {
            textRecognizer.close();
            Log.d("CAMERA_OCR", "🔚 TextRecognizer closed");
        }
    }
}