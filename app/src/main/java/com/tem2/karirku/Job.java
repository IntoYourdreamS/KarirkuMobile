package com.tem2.karirku;

import java.io.Serializable;

public class Job implements Serializable {
    private String companyName;
    private String location;
    private String jobTitle;
    private String postedTime;
    private String applicants;
    private String tag1;
    private String tag2;
    private String tag3;
    private int idLowongan;
    private String deskripsi;
    private String kualifikasi;
    private String tipePekerjaan;
    private String gajiRange;
    private String modeKerja;
    private String benefit;
    private String noTelp;

    // Konstruktor utama
    public Job(String companyName, String location, String jobTitle,
               String postedTime, String applicants,
               String tag1, String tag2, String tag3) {
        this.companyName = companyName;
        this.location = location;
        this.jobTitle = jobTitle;
        this.postedTime = postedTime;
        this.applicants = applicants;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.noTelp = "";
    }

    // Konstruktor lengkap untuk data dari API
    public Job(int idLowongan, String companyName, String location, String jobTitle,
               String postedTime, String applicants, String kategori, String tipePekerjaan,
               String gajiRange, String modeKerja, String deskripsi, String kualifikasi,
               String benefit, String noTelp) {
        this.idLowongan = idLowongan;
        this.companyName = companyName;
        this.location = location;
        this.jobTitle = jobTitle;
        this.postedTime = postedTime;
        this.applicants = applicants;
        this.tag1 = kategori;
        this.tag2 = tipePekerjaan;
        this.tag3 = modeKerja;
        this.deskripsi = deskripsi;
        this.kualifikasi = kualifikasi;
        this.tipePekerjaan = tipePekerjaan;
        this.gajiRange = gajiRange;
        this.modeKerja = modeKerja;
        this.benefit = benefit;
        this.noTelp = noTelp != null ? noTelp : "";
    }

    // Getter dan Setter
    public String getCompanyName() { return companyName; }
    public String getLocation() { return location; }
    public String getJobTitle() { return jobTitle; }
    public String getPostedTime() { return postedTime; }
    public String getApplicants() { return applicants; }
    public String getTag1() { return tag1; }
    public String getTag2() { return tag2; }
    public String getTag3() { return tag3; }
    public int getIdLowongan() { return idLowongan; }
    public void setIdLowongan(int idLowongan) { this.idLowongan = idLowongan; }
    public String getDeskripsi() { return deskripsi; }
    public String getKualifikasi() { return kualifikasi; }
    public String getTipePekerjaan() { return tipePekerjaan; }
    public String getGajiRange() { return gajiRange; }
    public String getModeKerja() { return modeKerja; }
    public String getBenefit() { return benefit; }

    // Getter & Setter untuk noTelp
    public String getNoTelp() {
        return noTelp != null ? noTelp : "";
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp != null ? noTelp : "";
    }

    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public void setKualifikasi(String kualifikasi) { this.kualifikasi = kualifikasi; }
    public void setTipePekerjaan(String tipePekerjaan) { this.tipePekerjaan = tipePekerjaan; }
    public void setGajiRange(String gajiRange) { this.gajiRange = gajiRange; }
    public void setModeKerja(String modeKerja) { this.modeKerja = modeKerja; }
    public void setBenefit(String benefit) { this.benefit = benefit; }

    @Override
    public String toString() {
        return "Job{" +
                "idLowongan=" + idLowongan +
                ", companyName='" + companyName + '\'' +
                ", location='" + location + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", postedTime='" + postedTime + '\'' +
                ", applicants='" + applicants + '\'' +
                ", tag1='" + tag1 + '\'' +
                ", tag2='" + tag2 + '\'' +
                ", tag3='" + tag3 + '\'' +
                ", noTelp='" + noTelp + '\'' +
                '}';
    }
}