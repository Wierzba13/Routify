package com.example.routify;

public class Vehicle {
    private String title;
    private String consumption;
    private String actionText;
    private int imageResId;

    public Vehicle(String title, String consumption, String actionText, int imageResId) {
        this.title = title;
        this.consumption = consumption;
        this.actionText = actionText;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title; }
    public String getConsumption() { return consumption; }
    public String getActionText() { return actionText; }
    public int getImageResId() { return imageResId; }
}
