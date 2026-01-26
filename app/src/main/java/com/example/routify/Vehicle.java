package com.example.routify;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehicles")
public class Vehicle {
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String title;
    private double consumption;
    private String actionText;
    private int imageResId;

    public Vehicle(String title, double consumption, String actionText, int imageResId) {

        this.title = title;
        this.consumption = consumption;
        this.actionText = actionText;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title; }
    public double getConsumption() { return consumption; }
    public String getActionText() { return actionText; }
    public int getImageResId() { return imageResId; }
}
