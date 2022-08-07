package com.example.myaugmentedrealityapp.CarGame;


import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {HighScore.class}, version = 2)
public abstract class ScoreDatabase extends RoomDatabase {

    public abstract HighScoreDao highScoreDao();
}