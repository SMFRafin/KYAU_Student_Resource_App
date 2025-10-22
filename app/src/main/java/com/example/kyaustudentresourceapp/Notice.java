package com.example.kyaustudentresourceapp;

public class Notice {
    private String title;
    private String body;
    private String date;
    private String category;

    public Notice(String title, String body, String date, String category) {
        this.title = title;
        this.body = body;
        this.date = date;
        this.category = category;
    }

    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
}
