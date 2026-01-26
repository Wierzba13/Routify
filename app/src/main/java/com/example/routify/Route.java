package com.example.routify;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routes")
public class Route {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double distance;
    private long timestamp;
    private String title;
    private int vehicleId;
    private String vehicleName;

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public double getFuelUsed() {
        return fuelUsed;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public String getTitle() {
        return title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getDistance() {
        return distance;
    }

    public int getId() {
        return id;
    }

    public double fuelUsed;

    public Route(double distance, long timestamp, String title, int vehicleId, String vehicleName, double fuelUsed) {
        this.distance = distance;
        this.timestamp = timestamp;
        this.title = title;
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.fuelUsed = fuelUsed;
    }
}