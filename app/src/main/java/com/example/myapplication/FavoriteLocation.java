package com.example.myapplication;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_locations")
public class FavoriteLocation {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double latitude;
    public double longitude;
}
