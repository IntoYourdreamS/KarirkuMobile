package com.tem2.karirku;

import java.io.Serializable;

public class Job implements Serializable {
    private String id; // TAMBAH INI
    private String companyName;
    private String location;
    private String jobTitle;
    private String postedTime;
    private String applicants;
    private String tag1;
    private String tag2;
    private String tag3;

    // Constructor TAMBAH PARAMETER id
    public Job(String id, String companyName, String location, String jobTitle,
               String postedTime, String applicants,
               String tag1, String tag2, String tag3) {
        this.id = id;
        this.companyName = companyName;
        this.location = location;
        this.jobTitle = jobTitle;
        this.postedTime = postedTime;
        this.applicants = applicants;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
    }

    // GETTER untuk id
    public String getId() { return id; }
    public String getCompanyName() { return companyName; }
    public String getLocation() { return location; }
    public String getJobTitle() { return jobTitle; }
    public String getPostedTime() { return postedTime; }
    public String getApplicants() { return applicants; }
    public String getTag1() { return tag1; }
    public String getTag2() { return tag2; }
    public String getTag3() { return tag3; }
}