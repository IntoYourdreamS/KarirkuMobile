package com.tem2.karirku;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;
    private Set<Integer> savedJobs = new HashSet<>(); // Simpan status UI doang

    public JobAdapter(Context context, List<Job> jobList) {
        this.context = context;
        this.jobList = jobList;
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

        holder.tvCompanyName.setText(job.getCompanyName());
        holder.tvLocation.setText(job.getLocation());
        holder.tvJobTitle.setText(job.getJobTitle());
        holder.tvPostedTime.setText(job.getPostedTime());
        holder.tvApplicants.setText(job.getApplicants());
        holder.tvTag1.setText(job.getTag1());
        holder.tvTag2.setText(job.getTag2());
        holder.tvTag3.setText(job.getTag3());

        // Gambar perusahaan
        holder.imgCompany.setImageResource(R.drawable.iconloker);

        // 🎯 Toggle Save Button - Cek status UI doang
        if (savedJobs.contains(position)) {
            holder.btnSave.setImageResource(R.drawable.icsimpan_active); // Icon aktif
        } else {
            holder.btnSave.setImageResource(R.drawable.ic_simpan); // Icon normal
        }

        // 🎯 Tombol Simpan - UI toggle doang
        holder.btnSave.setOnClickListener(v -> {
            if (savedJobs.contains(position)) {
                // Unsaved job
                savedJobs.remove(position);
                holder.btnSave.setImageResource(R.drawable.ic_simpan);
                Toast.makeText(context, "Job dihapus dari simpan", Toast.LENGTH_SHORT).show();
            } else {
                // Save job
                savedJobs.add(position);
                holder.btnSave.setImageResource(R.drawable.icsimpan_active);
                Toast.makeText(context, "Job disimpan", Toast.LENGTH_SHORT).show();
            }
        });

        // 🎯 Click item untuk buka detail
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

    // 🎯 Method untuk update data saat ganti tab
    public void setData(List<Job> newList) {
        this.jobList = newList;
        savedJobs.clear(); // Reset status saved saat data berubah
        notifyDataSetChanged();
    }

    // 🎯 ViewHolder
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

            // Tag kategori
            tvTag1 = itemView.findViewById(R.id.tvTag1);
            tvTag2 = itemView.findViewById(R.id.tvTag2);
            tvTag3 = itemView.findViewById(R.id.tvTag3);
        }
    }
}