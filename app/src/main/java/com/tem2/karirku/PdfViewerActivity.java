package com.tem2.karirku;

import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class PdfViewerActivity extends AppCompatActivity {

    ImageView pdfImage;
    PdfRenderer renderer;
    PdfRenderer.Page page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        pdfImage = findViewById(R.id.pdfImage);

        String path = getIntent().getStringExtra("path");

        try {
            File file = new File(path);
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(fd);

            page = renderer.openPage(0);
            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(
                    page.getWidth(), page.getHeight(), android.graphics.Bitmap.Config.ARGB_8888
            );
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pdfImage.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
