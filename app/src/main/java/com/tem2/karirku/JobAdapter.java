package com.tem2.karirku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;

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

        // Gambar perusahaan (kalau punya aset default)
        holder.imgCompany.setImageResource(R.drawable.iconloker);

        // 🔹 Tombol Simpan (klik ubah ikon)
        holder.btnSave.setOnClickListener(v -> {
            holder.btnSave.setImageResource(R.drawable.icsimpan);
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    // 🔹 Method untuk update data saat ganti tab
    public void setData(List<Job> newList) {
        this.jobList = newList;
        notifyDataSetChanged();
    }

    // 🔹 ViewHolder
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
