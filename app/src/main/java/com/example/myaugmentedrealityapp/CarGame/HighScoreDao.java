package com.example.myaugmentedrealityapp.CarGame;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;


@Dao
public interface HighScoreDao{
    @Delete
    public void delete(HighScore highScore);

    @Insert
    void insert(HighScore highScore);

    @Query("SELECT * FROM HighScore")
    LiveData<List<HighScore>> getHighScore();
}