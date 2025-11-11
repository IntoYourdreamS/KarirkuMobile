package com.tem2.karirku;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class JobDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvJobTitle, tvCompanyName, tvLocation, tvPostedTime, tvApplicants;
    private TextView tvTag1, tvTag2, tvTag3, tvJobDescription, tvRequirements, tvAdditionalInfo;
    private Button btnApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        initViews();
        loadJobData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);

        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvLocation = findViewById(R.id.tvLocation);
        tvPostedTime = findViewById(R.id.tvPostedTime);
        tvApplicants = findViewById(R.id.tvApplicants);

        tvTag1 = findViewById(R.id.tvTag1);
        tvTag2 = findViewById(R.id.tvTag2);
        tvTag3 = findViewById(R.id.tvTag3);

        // TAMBAH INI - TextView untuk detail lengkap
        tvJobDescription = findViewById(R.id.tvJobDescription);
        tvRequirements = findViewById(R.id.tvRequirements);
        tvAdditionalInfo = findViewById(R.id.tvAdditionalInfo);

        btnApply = findViewById(R.id.btnApply);
    }

    private void loadJobData() {
        // Ambil data job dari intent
        Intent intent = getIntent();
        if (intent != null) {
            Job job = (Job) intent.getSerializableExtra("JOB_DATA");

            if (job != null) {
                // Set data dasar
                tvJobTitle.setText(job.getJobTitle());
                tvCompanyName.setText(job.getCompanyName());
                tvLocation.setText(job.getLocation());
                tvPostedTime.setText(job.getPostedTime());
                tvApplicants.setText("• " + job.getApplicants());

                // Set tags
                tvTag1.setText(job.getTag1());
                tvTag2.setText(job.getTag2());
                tvTag3.setText(job.getTag3());

                // TAMBAH INI - Set data detail lengkap
                tvJobDescription.setText("Lowongan " + job.getJobTitle() + " di " + job.getCompanyName() + " membuka kesempatan bagi profesional yang berpengamaan di bidang " + job.getTag1() + ".");
                tvRequirements.setText("• Pengalaman di bidang " + job.getTag1() + "\n• Memahami " + job.getTag2() + "\n• Dapat bekerja " + job.getTag3() + "\n• Berdomisili di " + job.getLocation());
                tvAdditionalInfo.setText("• Jenis: " + job.getTag2() + "\n• Lokasi: " + job.getLocation() + "\n• Waktu: " + job.getPostedTime() + "\n• Pendaftar: " + job.getApplicants());
            }
        }
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Apply button
        btnApply.setOnClickListener(v -> {
            // Handle apply job
            // Bisa tambah Toast atau logic apply di sini
        });
    }
}