package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FavoriteLocationDao {
    @Query("SELECT * FROM favorite_locations")
    List<FavoriteLocation> getAll();


    @Insert
    void insert(FavoriteLocation favoriteLocation);

    @Update
    void update(FavoriteLocation favoriteLocation);

    @Delete
    void delete(FavoriteLocation favoriteLocation);
}