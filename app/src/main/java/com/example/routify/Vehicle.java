package com.example.routify;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehicles")
public class Vehicle {
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String title;
    private double consumption;
    private String imageUri;

    public Vehicle(String title, double consumption, String imageUri) {
        this.title = title;
        this.consumption = consumption;
        this.imageUri = imageUri;
    }

    public String getTitle() { return title; }
    public double getConsumption() { return consumption; }
    public String getImageUri() { return imageUri; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
