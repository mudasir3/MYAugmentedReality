package com.example.myaugmentedrealityapp.CarGame;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HighScore{
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int highScore;

    public HighScore(@NonNull int highScore, int id){
        this.highScore=highScore;
        this.id = id;
    }

    public void setHighScore(int highScore){
        this.highScore = highScore;
    }
    public int getHighScore(){
        return highScore;
    }

    public int getId(){
        return id;
    }
}
