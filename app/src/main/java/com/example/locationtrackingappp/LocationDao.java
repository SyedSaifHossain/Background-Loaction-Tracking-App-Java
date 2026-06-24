package com.example.locationtrackingappp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationDao {
    @Insert
    void insert(LocationEntity location);

    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    List<LocationEntity> getAllLocations();

    @Query("DELETE FROM locations")
    void deleteAll();
}
