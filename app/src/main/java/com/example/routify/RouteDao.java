package com.example.routify;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface RouteDao {
    @Insert
    void insert(Route route);
    @Query("SELECT SUM(distance) FROM routes WHERE timestamp >= :startTime")
    LiveData<Double> getSumDistanceSince(long startTime);
    @Query("SELECT SUM(fuelUsed) FROM routes WHERE timestamp >= :startTime")
    LiveData<Double> getSumFuelSince(long startTime);

    @Query("SELECT * FROM routes ORDER BY timestamp DESC")
    LiveData<List<Route>> getAllRoutes();

    @Query("UPDATE routes SET title = :newTitle WHERE id = :routeId")
    void updateTitle(int routeId, String newTitle);
    @Query("UPDATE routes SET vehicleId = :vId, vehicleName = :vName, fuelUsed = :fuel WHERE id = :rId")
    void updateRouteVehicle(int rId, int vId, String vName, double fuel);
}